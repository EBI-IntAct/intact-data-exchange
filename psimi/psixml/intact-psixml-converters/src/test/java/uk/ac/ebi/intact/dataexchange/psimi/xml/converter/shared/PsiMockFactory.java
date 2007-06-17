/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.model.CvInteractorType;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.IdSequenceGenerator;

import java.util.Random;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiMockFactory {

    private static int MIN_CHILDREN = 2;
    private static int MAX_CHILDREN = 10;

    public static void setMinChildren(int minChildren) {
        MIN_CHILDREN = minChildren;
    }

    public static void setMaxChildren(int maxChildren) {
        MAX_CHILDREN = maxChildren;
    }

    private PsiMockFactory() {
    }

    public static Entry createMockEntry() {
        Entry entry = new Entry();

        for (int i = 0; i < childRandom(); i++) {
            Interaction interaction = createMockInteraction();
            entry.getInteractions().add(interaction);

            for (ExperimentDescription expDesc : interaction.getExperiments()) {
                entry.getExperiments().add(expDesc);
            }

            for (Participant part : interaction.getParticipants()) {
                entry.getInteractors().add(part.getInteractor());
            }
        }

        return entry;
    }

    public static Interaction createMockInteraction() {
        Interaction interaction = new Interaction();
        populate(interaction);
        interaction.getInteractionTypes().add(createCvType(InteractionType.class));

        for (int i = 0; i < childRandom(); i++) {
            interaction.getParticipants().add(createMockParticipant(interaction));
        }

        for (int i = 0; i < childRandom(1, 2); i++) {
            interaction.getExperiments().add(createMockExperiment());
        }

        return interaction;
    }

    public static Participant createMockParticipant(Interaction interaction) {
        Participant participant = new Participant();
        populate(participant);
        participant.setInteraction(interaction);
        participant.setInteractor(createMockInteractor());
        participant.setBiologicalRole(createCvType(BiologicalRole.class));
        participant.getExperimentalRoles().add(createCvType(ExperimentalRole.class));

        return participant;
    }

    public static Interactor createMockInteractor() {
        Interactor interactor = new Interactor();
        populate(interactor);
        interactor.setInteractorType(createInteractorType());

        return interactor;
    }


    public static ExperimentDescription createMockExperiment() {
        Xref bibrefXref = createXref(CvXrefQualifier.PRIMARY_REFERENCE, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF);
        bibrefXref.getPrimaryRef().setId("14681455");
        bibrefXref.getSecondaryRef().clear();
        Bibref bibref = new Bibref(bibrefXref);

        InteractionDetectionMethod idm = createCvType(InteractionDetectionMethod.class);

        ExperimentDescription experiment = new ExperimentDescription(bibref, idm);
        populate(experiment);
        experiment.getHostOrganisms().add(createMockOrganism());

        return experiment;
    }

    public static Organism createMockOrganism() {
        Organism organism = new Organism();
        populate(organism);
        organism.setNcbiTaxId(nextInt());

        return organism;
    }

    private static void populate(Object object) {
        if (object instanceof HasId) {
            populateId((HasId) object);
        }
        if (object instanceof NamesContainer) {
            populateNames((NamesContainer) object);
        }
        if (object instanceof XrefContainer) {
            populateXref((XrefContainer) object);
        }
    }

    private static void populateId(HasId hasId) {
        hasId.setId(nextId());
    }

    private static void populateNames(NamesContainer namesContainer) {
        namesContainer.setNames(createNames());
    }

    private static void populateXref(XrefContainer xrefContainer) {
        xrefContainer.setXref(createXref());
    }

    public static Names createNames() {
        Names names = new Names();
        names.setShortLabel(nextString());
        names.setFullName(nextString());
        return names;
    }

    public static Xref createXref() {
        return createXref(CvXrefQualifier.IDENTITY, CvXrefQualifier.IDENTITY_MI_REF);
    }

    public static Xref createXref(String primaryRefType, String primaryRefTypeAc) {
        Xref xref = new Xref();
        xref.setPrimaryRef(createDbReference(primaryRefType, primaryRefTypeAc));

        for (int i = 0; i < childRandom(0, 4); i++) {
            xref.getSecondaryRef().add(createDbReference());
        }

        return xref;
    }

    private static <C extends CvType> C createCvType(Class<C> cvTypeClass) {
        C cv = null;

        try {
            cv = cvTypeClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        populate(cv);

        return cv;
    }

    private static InteractorType createInteractorType() {
        InteractorType intType = createCvType(InteractorType.class);
        intType.getNames().setShortLabel(CvInteractorType.PROTEIN);

        return intType;
    }

    public static DbReference createDbReference() {
        return createDbReference(nextString("reftype"), nextString("ac"));
    }

    public static DbReference createDbReference(String refType, String refTypeAc) {
        DbReference dbRef = new DbReference(nextString("id"), nextString("db"));
        dbRef.setDbAc(nextString("ac"));
        dbRef.setRefType(refType);
        dbRef.setRefTypeAc(refTypeAc);
        dbRef.setSecondary(nextString("secondary"));
        dbRef.setVersion(nextString("version"));

        return dbRef;
    }

    private static String nextString() {
        return nextString("str");
    }

    private static String nextString(String prefix) {
        return prefix + "_" + nextInt();
    }

    private static int nextInt() {
        return new Random().nextInt(10000);
    }

    private static int nextId() {
        return IdSequenceGenerator.getInstance().nextId();
    }

    private static int childRandom() {
        return childRandom(MIN_CHILDREN, MAX_CHILDREN);
    }

    private static int childRandom(int min, int max) {
        if (min == max) return max;

        return new Random().nextInt(max - min) + min;
    }


}