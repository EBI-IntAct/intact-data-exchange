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
package uk.ac.ebi.intact.psimitab.converters.converters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.model.ParticipantEvidence;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.ChecksumUtils;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.irefindex.seguid.RigidGenerator;
import uk.ac.ebi.intact.irefindex.seguid.SeguidException;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactConfidence;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactParameter;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractionEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.jami.model.extension.InteractionXref;
import uk.ac.ebi.intact.psimitab.converters.util.PsimitabTools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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

    private static final String SMALLMOLECULE_MI_REF = "MI:0328";
    private static final String UNKNOWN_TAXID = "-3";

    private CvObjectConverter cvObjectConverter;

    private CrossReferenceConverter<InteractionXref> xConverter;

    private ConfidenceConverter confidenceConverter;
    private ParameterConverter parameterConverter;
    private AnnotationConverter annotationConverter;
    private ExperimentConverter experimentConverter;
    private InteractorConverter interactorConverter;

    private boolean processExperimentDetails=true;
    private boolean processPublicationDetails = true;

    private String defaultInstitution = "intact";

    public static String CRC = "intact-crc";
    public static String RIGID = "rigid";

    public InteractionConverter(){
        this.confidenceConverter = new ConfidenceConverter();
        cvObjectConverter = new CvObjectConverter();
        xConverter = new CrossReferenceConverter<>();
        parameterConverter = new ParameterConverter();
        annotationConverter = new AnnotationConverter();
        experimentConverter = new ExperimentConverter();
        this.interactorConverter = new InteractorConverter();
    }

    public InteractionConverter(boolean processExperimentDetails, boolean processPublicationsDetails){
        this.confidenceConverter = new ConfidenceConverter();
        cvObjectConverter = new CvObjectConverter();
        xConverter = new CrossReferenceConverter<>();
        parameterConverter = new ParameterConverter();
        annotationConverter = new AnnotationConverter();
        experimentConverter = new ExperimentConverter();

        this.processExperimentDetails = processExperimentDetails;
        this.processPublicationDetails = processPublicationsDetails;
        this.interactorConverter = new InteractorConverter();
    }

    public InteractionConverter(String defaultInstitution){
        this.confidenceConverter = new ConfidenceConverter();
        cvObjectConverter = new CvObjectConverter();
        xConverter = new CrossReferenceConverter<>();
        parameterConverter = new ParameterConverter();
        annotationConverter = new AnnotationConverter();
        experimentConverter = new ExperimentConverter();
        this.interactorConverter = new InteractorConverter(defaultInstitution);

        if (defaultInstitution != null){
            this.defaultInstitution = defaultInstitution;
        }
    }

    public InteractionConverter(boolean processExperimentDetails, boolean processPublicationsDetails, String defaultInstitution){
        this.confidenceConverter = new ConfidenceConverter();
        cvObjectConverter = new CvObjectConverter();
        xConverter = new CrossReferenceConverter<>();
        parameterConverter = new ParameterConverter();
        annotationConverter = new AnnotationConverter();
        experimentConverter = new ExperimentConverter();

        this.processExperimentDetails = processExperimentDetails;
        this.processPublicationDetails = processPublicationsDetails;
        this.interactorConverter = new InteractorConverter(defaultInstitution);

        if (defaultInstitution != null){
            this.defaultInstitution = defaultInstitution;
        }
    }

    ///////////////////////////
    // Getters & Setters

    public BinaryInteraction toBinaryInteraction( IntactInteractionEvidence interaction) {

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        final Collection<ParticipantEvidence> components = interaction.getParticipants();
        if ( components.size() > 2 || components.size() == 0 ) {
            throw new IllegalArgumentException( "We only convert interaction composed of 2 components or only one" );
        }

        Iterator<ParticipantEvidence> iterator = components.iterator();
        ParticipantEvidence componentA = iterator.next();
        ParticipantEvidence componentB = null;
        if (iterator.hasNext()){
            componentB = iterator.next();
        }

        MitabInteractor convertedInteractorA = interactorConverter.intactToMitab((IntactParticipantEvidence) componentA);
        MitabInteractor convertedInteractorB = interactorConverter.intactToMitab((IntactParticipantEvidence) componentB);
        psidev.psi.mi.tab.model.Interactor interactorA = convertedInteractorA != null ? convertedInteractorA.getInteractor() : null;
        psidev.psi.mi.tab.model.Interactor interactorB = convertedInteractorB != null ? convertedInteractorB.getInteractor() : null;

        BinaryInteraction bi = processInteractionDetailsWithoutInteractors(interaction);
        bi.setInteractorA(interactorA);
        bi.setInteractorB(interactorB);

        // process participant detection methods after setting the interactors if not done at the level of interactiors
        if (processExperimentDetails && interactorA != null && interactorA.getParticipantIdentificationMethods().isEmpty()){
            processExperimentParticipantIdentificationMethods(interaction, interactorA);
        }
        if (processExperimentDetails && interactorB != null && interactorB.getParticipantIdentificationMethods().isEmpty()){
            processExperimentParticipantIdentificationMethods(interaction, interactorB);
        }

        if (convertedInteractorA != null && convertedInteractorB != null && convertedInteractorA.getRigDataModel() != null
                && convertedInteractorB.getRigDataModel() != null){
            String rigid = calculateRigidFor(Arrays.asList(convertedInteractorA.getRigDataModel(), convertedInteractorB.getRigDataModel()));

            // add rigid for interaction checksum
            if (rigid != null){
                Checksum checksum = new ChecksumImpl(RIGID, rigid);
                bi.getChecksums().add(checksum);
            }
        }

        // order interactors
        flipInteractorsIfNecessary(bi);

        return bi;
    }

    public void processExperimentParticipantIdentificationMethods(IntactInteractionEvidence interaction, Interactor interactor){

        if (interaction != null && interaction.getExperiment() != null) {
            this.experimentConverter.addParticipantDetectionMethodForInteractor((IntactExperiment) interaction.getExperiment(), interactor);
        }
    }

    public void flipInteractorsIfNecessary(BinaryInteraction bi) {
        PsimitabTools.reorderInteractors(bi, new Comparator<Interactor>() {

            public int compare(Interactor o1, Interactor o2) {

                if (o1 == null){
                   return -1;
                }
                else if (o2 == null){
                   return 1;
                }

                final Collection<CrossReference> type1Coll = o1.getInteractorTypes();
                final Collection<CrossReference> type2Coll = o2.getInteractorTypes();

                CrossReference type1 = (type1Coll != null && !type1Coll.isEmpty()) ? type1Coll.iterator().next() : null;
                CrossReference type2 = (type2Coll != null && !type2Coll.isEmpty()) ? type2Coll.iterator().next() : null;

                if (type1 != null && SMALLMOLECULE_MI_REF.equals(type1.getIdentifier())) {
                    return 1;
                } else if (type2 != null && SMALLMOLECULE_MI_REF.equals(type2.getIdentifier())) {
                    return -1;
                }
                return 0;
            }
        });
    }

    public String calculateRigidFor(Collection<RigDataModel> interactorRigModels){

        if (interactorRigModels == null || interactorRigModels.isEmpty()){
            return null;
        }
        RigidGenerator rigidGenerator = new RigidGenerator();

        for (RigDataModel interactorModel : interactorRigModels){
            rigidGenerator.addSequence(interactorModel.getSequence(), interactorModel.getTaxid());
        }

        String rig = null;
        try {
            rig = rigidGenerator.calculateRigid();

        } catch (SeguidException e) {
            throw new RuntimeException("An error occured while generating RIG identifier for " +
                    "interaction ", e);
        }

        return rig;
    }


    public BinaryInteraction processInteractionDetailsWithoutInteractors(IntactInteractionEvidence interaction){

        if ( interaction == null ) {
            throw new IllegalArgumentException( "Interaction must not be null" );
        }

        BinaryInteraction binary = new BinaryInteractionImpl(null, null);
        // process interaction type
        if (interaction.getInteractionType() != null){
            CrossReference type = cvObjectConverter.toCrossReference((IntactCvTerm) interaction.getInteractionType());

            if (type != null){
                binary.getInteractionTypes().add(type);
            }
        }

        // convert confidences
        if (!interaction.getConfidences().isEmpty()){

            for (psidev.psi.mi.jami.model.Confidence conf : interaction.getConfidences()){
                Confidence confField = confidenceConverter.intactToCalimocho((AbstractIntactConfidence) conf);

                if (confField != null){
                    binary.getConfidenceValues().add(confField);
                }
            }
        }

        // process AC and source
        if (interaction.getAc() != null){
            CrossReference id = new CrossReferenceImpl();

            id.setDatabase(defaultInstitution);
            id.setIdentifier(interaction.getAc());

            binary.getInteractionAcs().add(id);
        }

        // process experiments
        if (processExperimentDetails){
            if (interaction.getExperiment() != null) {
                experimentConverter.intactToMitab((IntactExperiment) interaction.getExperiment(), binary, false, processPublicationDetails);
            }
        }

        //process xrefs
        Collection<Xref> interactionRefs = interaction.getXrefs();

        if (!interactionRefs.isEmpty()){

            // convert xrefs
            for (Xref ref : interactionRefs){

                CrossReference refField = xConverter.createCrossReference((InteractionXref) ref, true);
                if (refField != null && Xref.IMEX.equalsIgnoreCase(refField.getDatabase()) && Xref.IMEX_PRIMARY.equalsIgnoreCase(refField.getText())){
                    refField.setText(null);
                    binary.getInteractionAcs().add(refField);
                }
                // identity
                else if (refField != null && Xref.IDENTITY.equalsIgnoreCase(refField.getText())){
                    refField.setText(null);
                    binary.getInteractionAcs().add(refField);
                }
                else if (refField != null){
                    binary.getXrefs().add(refField);
                }
            }
        }

        //process annotations (could have been processed with experiments)
        Collection<psidev.psi.mi.jami.model.Annotation> annotations = PsimitabTools.getPublicAnnotations(interaction.getAnnotations());
        if (!annotations.isEmpty()){

            for (psidev.psi.mi.jami.model.Annotation annots : annotations){
                psidev.psi.mi.tab.model.Annotation annotField = annotationConverter.intactToMitab(annots);

                if (annotField != null){
                    binary.getAnnotations().add(annotField);
                }
            }
        }

        //process parameters
        if (!interaction.getParameters().isEmpty()){

            for (psidev.psi.mi.jami.model.Parameter param : interaction.getParameters()){
                psidev.psi.mi.tab.model.Parameter paramField = parameterConverter.intactToMitab((AbstractIntactParameter) param);

                if (paramField != null){
                    binary.getParameters().add(paramField);
                }
            }
        }

        //process checksum
        psidev.psi.mi.jami.model.Checksum crc64 = ChecksumUtils.collectFirstChecksumWithMethod(interaction.getChecksums(), null, "crc64");
        if (crc64 != null) {
            Checksum crc = new ChecksumImpl(CRC, crc64.getValue());
            binary.getChecksums().add(crc);
        }

        //process negative
        if (interaction.isNegative()){
            binary.setNegativeInteraction(true);
        }

        // process update date
        if (interaction.getUpdated() != null){
            binary.getUpdateDate().add(interaction.getUpdated());
        }

        return binary;
    }

    public boolean isProcessExperimentDetails() {
        return processExperimentDetails;
    }

    public boolean isProcessPublicationDetails() {
        return processPublicationDetails;
    }
}