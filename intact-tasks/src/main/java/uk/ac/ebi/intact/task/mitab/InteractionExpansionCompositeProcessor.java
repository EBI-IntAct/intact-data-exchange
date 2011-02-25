/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.task.mitab;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.irefindex.seguid.RigidGenerator;
import uk.ac.ebi.intact.irefindex.seguid.RogidGenerator;
import uk.ac.ebi.intact.irefindex.seguid.SeguidException;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.InteractionUtils;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.PsimitabTools;
import uk.ac.ebi.intact.psimitab.converters.InteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;
import uk.ac.ebi.intact.psimitab.converters.expansion.SpokeWithoutBaitExpansion;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

import java.util.*;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionExpansionCompositeProcessor implements ItemProcessor<Interaction, Collection<? extends BinaryInteraction>> {

    private static final Log log = LogFactory.getLog( InteractionExpansionCompositeProcessor.class );

    private static final String SMALLMOLECULE_MI_REF = "MI:0328";
    private static final String UNKNOWN_TAXID = "-3";

    private ExpansionStrategy expansionStategy;

    private List<ItemProcessor<BinaryInteraction, BinaryInteraction>> binaryItemProcessors;

    public InteractionExpansionCompositeProcessor() {
        this.expansionStategy = new SpokeWithoutBaitExpansion();
        this.binaryItemProcessors = new ArrayList<ItemProcessor<BinaryInteraction, BinaryInteraction>>();
    }

    @Transactional(readOnly = true)
    public Collection<? extends BinaryInteraction> process(Interaction item) throws Exception {
        if (!expansionStategy.isExpandable(item)) {
            if (log.isWarnEnabled()) log.warn("Filtered interaction: "+item.getAc()+" (not expandable)");
            return null;
        }

        Collection<Interaction> interactions;

        boolean expanded = false;

        if (InteractionUtils.isBinaryInteraction(item)) {
            interactions = Collections.singleton(item);
        } else {
            try {
                interactions = expansionStategy.expand(item);
            } catch (Throwable e) {
                throw new InteractionExpansionException("Problem expanding interaction: "+item, e);
            }
            expanded = true;
        }

        if (interactions.isEmpty()) {
            if (log.isErrorEnabled()) {
                log.error("Expansion did not generate any interaction for: "+item);
                throw new InteractionExpansionException("Could not expand interaction: "+item);
            }
        }

        Collection<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>(interactions.size());

        InteractionConverter interactionConverter = new InteractionConverter();

        for (Interaction interaction : interactions) {

            IntactBinaryInteraction binaryInteraction = interactionConverter.toBinaryInteraction(interaction);

            //adding the expansion strategy here
            if (expanded) {
                binaryInteraction.getExpansionMethods().add(expansionStategy.getName());
            }

            for (ItemProcessor<BinaryInteraction,BinaryInteraction> delegate : binaryItemProcessors) {
                binaryInteraction = (IntactBinaryInteraction) delegate.process(binaryInteraction);
            }

            flipInteractorsIfNecessary(binaryInteraction);

            Interactor[] pair = findInteractors(interaction, binaryInteraction);

            // Update Interactors' ROGID - first, identify in which order they are stored in MITAB
            RogidGenerator rogidGenerator = new RogidGenerator();
            RigDataModel rigA = buildRigDataModel(pair[0]);
            RigDataModel rigB = buildRigDataModel(pair[1]);
            try {
                final String rogA = rogidGenerator.calculateRogid(rigA.getSequence(), rigA.getTaxid());
                binaryInteraction.getInteractorA().getAlternativeIdentifiers().add(
                        new CrossReferenceImpl("irefindex", rogA, "rogid"));

                final String rogB = rogidGenerator.calculateRogid(rigB.getSequence(), rigB.getTaxid());
                binaryInteraction.getInteractorB().getAlternativeIdentifiers().add(
                        new CrossReferenceImpl("irefindex", rogB, "rogid"));

                // Update Interaction RIGID
                RigidGenerator rigidGenerator = new RigidGenerator();
                rigidGenerator.addSequence(rigA.getSequence(), rigA.getTaxid());
                rigidGenerator.addSequence(rigB.getSequence(), rigB.getTaxid());
                String rig = rigidGenerator.calculateRigid();
                binaryInteraction.getInteractionAcs().add(new CrossReferenceImpl("irefindex", rig, "rigid"));

                binaryInteractions.add(binaryInteraction);

            } catch (SeguidException e) {
                throw new RuntimeException("An error occured while generating RIG/ROG identifier for " +
                        "interaction " + interaction.getAc(), e);
            }
        }

//        BinaryInteraction[] binaryInteractionArr = binaryInteractions.toArray(new BinaryInteraction[binaryInteractions.size()]);
//
//        for (int i=0; i<binaryInteractionArr.length; i++) {
//            for (ItemProcessor<BinaryInteraction,BinaryInteraction> delegate : binaryItemProcessors) {
//                binaryInteractionArr[i] = delegate.process(binaryInteractionArr[i]);
//            }
//        }
//
//        binaryInteractions = Arrays.asList(binaryInteractionArr);

        return binaryInteractions;
    }

    private RigDataModel buildRigDataModel(Interactor interactor) {

        String taxid;

        if (interactor.getBioSource() != null) {
            taxid = interactor.getBioSource().getTaxId();
        } else {
            taxid = UNKNOWN_TAXID;
        }

        String seq = null;
        if (interactor.getClass().isAssignableFrom(Polymer.class)) {
            Polymer polymer = (Polymer) interactor;
            seq = polymer.getSequence();
        }

        if (seq == null) {
            if (interactor instanceof SmallMolecule) {
                // find INCHI key
                final Annotation annotation = AnnotatedObjectUtils.findAnnotationByTopicMiOrLabel(interactor, "MI:2010");// INCHI_MI_REF
                if (annotation != null) {
                    seq = annotation.getAnnotationText();
                }
            }

            if (seq == null) {
                seq = interactor.getAc();
            }
        }

        return new RigDataModel(seq, taxid);
    }

    private Interactor[] findInteractors(Interaction interaction, IntactBinaryInteraction binaryInteraction) {

        Interactor[] pair = new Interactor[2];

        String interactorA = getIntactAc(binaryInteraction.getInteractorA());
        String interactorB = getIntactAc(binaryInteraction.getInteractorB());

        for (uk.ac.ebi.intact.model.Component component : interaction.getComponents()) {

            final String interactorAc = component.getInteractor().getAc();

            if (interactorAc.equals(interactorA) && pair[0] == null) {
                pair[0] = component.getInteractor();
            } else if (interactorAc.equals(interactorB)) {
                pair[1] = component.getInteractor();
            } else {
                throw new IllegalStateException("Interaction AC: " + interaction.getAc() + " with " +
                        interaction.getComponents().size() + " participants" +
                        ", found Interactor '" + interactorAc +
                        "' when expecting '" + interactorA + "' or '" + interactorB + "'");
            }
        }

        if (pair[0] == null) {
            System.out.println(interaction);
            throw new IllegalStateException("Interaction '" + interaction.getAc() + "': Could not identify interactor A: AC='" + interactorA + "' ");
        }

        if (pair[1] == null) {
            System.out.println(interaction);
            throw new IllegalStateException("Interaction '" + interaction.getAc() + "':Could not identify interactor B: AC='" + interactorB + "' ");
        }

        return pair;
    }

    private String getIntactAc(ExtendedInteractor interactor) {
        for (CrossReference reference : interactor.getIdentifiers()) {
            if (reference.getDatabase().equalsIgnoreCase("intact")) {
                return reference.getIdentifier();
            }
        }
        return null;
    }

    /**
     * Flips the interactors if necessary, so the small molecule is always interactor A
     *
     * @param bi
     */
    private void flipInteractorsIfNecessary(IntactBinaryInteraction bi) {
        PsimitabTools.reorderInteractors(bi, new Comparator<ExtendedInteractor>() {

            public int compare(ExtendedInteractor o1, ExtendedInteractor o2) {
                final CrossReference type1 = o1.getInteractorType();
                final CrossReference type2 = o2.getInteractorType();

                if (type1 != null && SMALLMOLECULE_MI_REF.equals(type1.getIdentifier())) {
                    return 1;
                } else if (type2 != null && SMALLMOLECULE_MI_REF.equals(type2.getIdentifier())) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public void setExpansionStategy(ExpansionStrategy expansionStategy) {
        this.expansionStategy = expansionStategy;
    }

    public void setBinaryItemProcessors(List<ItemProcessor<BinaryInteraction, BinaryInteraction>> delegates) {
        this.binaryItemProcessors = delegates;
    }
}
