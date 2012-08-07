/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Parameter;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.InstitutionUtils;
import uk.ac.ebi.intact.model.util.InteractionUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * Interaction Converter.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class InteractionConverter {

    public static final Log logger = LogFactory.getLog( InteractionConverter.class );

    private static final String TAXID = "taxid";

    private CvObjectConverter cvObjectConverter;

    private CrossReferenceConverter xConverter;

    private ConfidenceConverter confidenceConverter;
    private ParameterConverter parameterConverter;
    private AnnotationConverter annotationConverter;
    private ExperimentConverter experimentConverter;
    private InteractorConverter interactorConverter;

    public static String CRC = "intact-crc";

    private CrossReference defaultSourceDatabase = new CrossReferenceImpl( "psi-mi", "MI:0469", "intact" );

    public InteractionConverter(){
        this.confidenceConverter = new ConfidenceConverter();
        cvObjectConverter = new CvObjectConverter();
        xConverter = new CrossReferenceConverter();
        parameterConverter = new ParameterConverter();
        annotationConverter = new AnnotationConverter();
        experimentConverter = new ExperimentConverter();
        interactorConverter = new InteractorConverter();
    }

    ///////////////////////////
    // Getters & Setters

    public BinaryInteraction toBinaryInteraction( Interaction interaction) {

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        final Collection<Component> components = interaction.getComponents();
        if ( components.size() > 2 || components.size() == 0 ) {
            throw new IllegalArgumentException( "We only convert interaction composed of 2 components or only one" );
        }

        Iterator<Component> iterator = components.iterator();
        Component componentA = iterator.next();
        Component componentB = null;
        if (iterator.hasNext()){
            componentB = iterator.next();
        }

        InteractorConverter interactorConverter = new InteractorConverter();

        psidev.psi.mi.tab.model.Interactor interactorA = interactorConverter.intactToMitab(componentA);
        psidev.psi.mi.tab.model.Interactor interactorB = interactorConverter.intactToMitab(componentB);

        BinaryInteraction bi = processInteractionDetailsWithoutInteractors(interaction);

        bi.setInteractorA(interactorA);
        bi.setInteractorB(interactorB);

        return bi;
    }

    public BinaryInteraction processInteractionDetailsWithoutInteractors(Interaction interaction){

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        BinaryInteraction binary = new BinaryInteractionImpl(null, null);
        // process interaction type
        if (interaction.getCvInteractionType() != null){
            CrossReference type = cvObjectConverter.toCrossReference(interaction.getCvInteractionType());

            if (type != null){
                binary.getInteractionTypes().add(type);
            }
        }

        // convert confidences
        if (!interaction.getConfidences().isEmpty()){

            for (uk.ac.ebi.intact.model.Confidence conf : interaction.getConfidences()){
                Confidence confField = confidenceConverter.intactToCalimocho(conf);

                if (confField != null){
                    binary.getConfidenceValues().add(confField);
                }
            }
        }

        // process AC
        if (interaction.getAc() != null){
            CrossReference id = new CrossReferenceImpl();

            id.setDatabase(CvDatabase.INTACT);
            id.setIdentifier(interaction.getAc());

            if (interaction.getOwner() != null){
                Institution institution = interaction.getOwner();

                CvDatabase database = InstitutionUtils.retrieveCvDatabase(IntactContext.getCurrentInstance(), institution);

                if (database != null && database.getShortLabel() != null){
                    id.setDatabase(database.getShortLabel());
                }
            }

            binary.getInteractionAcs().add(id);
        }

        // process experiments
        for (Experiment exp : interaction.getExperiments()){
            experimentConverter.intactToCalimocho(exp, binary);
        }

        //process xrefs
        Collection<InteractorXref> interactionRefs = interaction.getXrefs();

        if (!interactionRefs.isEmpty()){

            // convert xrefs
            for (InteractorXref ref : interactionRefs){

                CrossReference refField = xConverter.createCrossReference(ref, true);
                if (refField != null){
                    binary.getInteractionXrefs().add(refField);
                }
            }
        }

        //process annotations (could have been processed with experiments)
        Collection<Annotation>  annotations = AnnotatedObjectUtils.getPublicAnnotations(interaction);
        if (!annotations.isEmpty()){

            for (Annotation annots : annotations){
                psidev.psi.mi.tab.model.Annotation annotField = annotationConverter.intactToMitab(annots);

                if (annotField != null){
                    binary.getInteractionAnnotations().add(annotField);
                }
            }
        }

        //process parameters
        if (!interaction.getParameters().isEmpty()){

            for (Parameter param : interaction.getParameters()){
                psidev.psi.mi.tab.model.Parameter paramField = parameterConverter.intactToMitab(param);

                if (paramField != null){
                    binary.getInteractionParameters().add(paramField);
                }
            }
        }

        //process checksum
        if (interaction.getCrc() != null){
            CrossReference crc = new CrossReferenceImpl(CRC, interaction.getCrc());
            binary.getInteractionChecksums().add(crc);
        }

        //process negative
        if (InteractionUtils.isNegative(interaction)){
            binary.setNegativeInteraction(true);
        }

        // process update date
        if (interaction.getUpdated() != null){
            binary.getUpdateDate().add(interaction.getUpdated());
        }

        return binary;
    }
}