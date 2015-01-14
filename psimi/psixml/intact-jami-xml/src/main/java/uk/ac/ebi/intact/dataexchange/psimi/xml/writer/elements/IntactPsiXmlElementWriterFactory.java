package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.xml.PsiXmlType;
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.*;

import javax.xml.stream.XMLStreamWriter;
import java.util.Set;

/**
 * Intact factory for instantiating element writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19/11/14</pre>
 */

public class IntactPsiXmlElementWriterFactory {
    private static final IntactPsiXmlElementWriterFactory instance = new IntactPsiXmlElementWriterFactory();

    private IntactPsiXmlElementWriterFactory(){
        // nothing to do here
    }

    public static IntactPsiXmlElementWriterFactory getInstance() {
        return instance;
    }

    public static PsiXmlInteractionWriter[] createInteractionWritersFor(XMLStreamWriter streamWriter, PsiXmlObjectCache objectIndex, PsiXmlVersion version,
                                                                        PsiXmlType xmlType, InteractionCategory interactionCategory,
                                                                        ComplexType complexType, boolean extended, boolean named,
                                                                        PsiXmlElementWriter<Alias> aliasWriter,
                                                                        PsiXmlElementWriter<Annotation> attributeWriter,
                                                                        PsiXmlXrefWriter primaryRefWriter,
                                                                        PsiXmlElementWriter[] confidenceWriters,
                                                                        PsiXmlElementWriter<Checksum> checksumWriter,
                                                                        PsiXmlVariableNameWriter<CvTerm> interactionTypeWriter,
                                                                        PsiXmlVariableNameWriter<CvTerm> openCvWriter,
                                                                        PsiXmlExperimentWriter experimentWriter,
                                                                        PsiXmlElementWriter<String> availabilityWriter,
                                                                        PsiXmlElementWriter<Interactor> interactorWriter,
                                                                        PsiXmlPublicationWriter publicationWriter){
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();

        PsiXmlParameterWriter[] parameterWriters = elementWriterFactory.createParameterWriters(streamWriter, extended, objectIndex, version, publicationWriter);
        PsiXmlParticipantWriter[] participantWriters = createParticipantWriter(streamWriter, extended, objectIndex, version, xmlType, interactionCategory,
                aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters[0], interactorWriter, interactionTypeWriter, openCvWriter, parameterWriters[0]);
        PsiXmlElementWriter inferredInteractionWriter = elementWriterFactory.createInferredInteractionWriter(streamWriter, objectIndex);

        if (extended){
            return elementWriterFactory.createExtendedPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory, complexType,
                    aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters, checksumWriter, interactionTypeWriter,
                    experimentWriter, availabilityWriter, parameterWriters, participantWriters, inferredInteractionWriter, publicationWriter,
                    openCvWriter);
        }
        else if (named){
            return elementWriterFactory.createNamedPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory, complexType,
                    aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters, checksumWriter, interactionTypeWriter,
                    experimentWriter, availabilityWriter, parameterWriters, participantWriters, inferredInteractionWriter, publicationWriter,
                    openCvWriter);
        }
        else{
            return createDefaultPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory, complexType,
                    aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters, checksumWriter, interactionTypeWriter,
                    experimentWriter, availabilityWriter, parameterWriters, participantWriters, inferredInteractionWriter, publicationWriter,
                    openCvWriter);
        }
    }

    public static PsiXmlExperimentWriter createExperimentWriter(XMLStreamWriter streamWriter, boolean extended,
                                                                PsiXmlObjectCache objectIndex, PsiXmlVersion version,
                                                                boolean named, PsiXmlElementWriter<Alias> aliasWriter,
                                                                PsiXmlElementWriter<Annotation> attributeWriter,
                                                                PsiXmlXrefWriter primaryRefWriter,
                                                                PsiXmlPublicationWriter publicationWriter,
                                                                PsiXmlElementWriter<Organism> nonExperimentalHostOrganismWriter,
                                                                PsiXmlVariableNameWriter<CvTerm> detectionMethodWriter,
                                                                PsiXmlElementWriter<Confidence> confidenceWriter) {
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();

        if (extended){
            return elementWriterFactory.createExperimentWriter(streamWriter,extended,objectIndex, version,named, aliasWriter,
                    attributeWriter,primaryRefWriter,publicationWriter,nonExperimentalHostOrganismWriter,detectionMethodWriter,
                    confidenceWriter);
        }
        else if (named){
            return elementWriterFactory.createExperimentWriter(streamWriter,extended,objectIndex, version,named, aliasWriter,
                    attributeWriter,primaryRefWriter,publicationWriter,nonExperimentalHostOrganismWriter,detectionMethodWriter,
                    confidenceWriter);
        }
        else{
            switch (version){
                case v3_0_0:
                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactExperimentWriter expWriter =
                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactExperimentWriter(streamWriter, objectIndex);
                    expWriter.setXrefWriter(primaryRefWriter);
                    expWriter.setAttributeWriter(attributeWriter);
                    expWriter.setPublicationWriter(publicationWriter);
                    expWriter.setHostOrganismWriter(nonExperimentalHostOrganismWriter);
                    expWriter.setDetectionMethodWriter(detectionMethodWriter);
                    expWriter.setConfidenceWriter(confidenceWriter);
                    return expWriter;
                default:
                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactExperimentWriter expWriter2 =
                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactExperimentWriter(streamWriter, objectIndex);
                    expWriter2.setXrefWriter(primaryRefWriter);
                    expWriter2.setAttributeWriter(attributeWriter);
                    expWriter2.setPublicationWriter(publicationWriter);
                    expWriter2.setHostOrganismWriter(nonExperimentalHostOrganismWriter);
                    expWriter2.setDetectionMethodWriter(detectionMethodWriter);
                    expWriter2.setConfidenceWriter(confidenceWriter);
                    return expWriter2;
            }
        }
    }

    public static <P extends Participant> PsiXmlParticipantWriter<P>[] createParticipantWriter(XMLStreamWriter streamWriter, boolean extended,
                                                                                               PsiXmlObjectCache objectIndex, PsiXmlVersion version,
                                                                                               PsiXmlType xmlType,
                                                                                               InteractionCategory category, PsiXmlElementWriter<Alias> aliasWriter,
                                                                                               PsiXmlElementWriter<Annotation> attributeWriter,
                                                                                               PsiXmlXrefWriter primaryRefWriter,
                                                                                               PsiXmlElementWriter<Confidence> confidenceWriter,
                                                                                               PsiXmlElementWriter<Interactor> interactorWriter,
                                                                                               PsiXmlVariableNameWriter<CvTerm> cvWriter,
                                                                                               PsiXmlVariableNameWriter<CvTerm> openCvWriter,
                                                                                               PsiXmlParameterWriter parameterWriter){
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();

        if (extended){
            return elementWriterFactory.createParticipantWriter(streamWriter, extended,objectIndex, version,
                    xmlType,category, aliasWriter,attributeWriter,primaryRefWriter,
                    confidenceWriter,interactorWriter,cvWriter,openCvWriter,parameterWriter);
        }
        else{
            PsiXmlElementWriter[] featureWriters = createFeatureWriter(streamWriter, extended, objectIndex, version, category, aliasWriter,
                    attributeWriter, primaryRefWriter, cvWriter, parameterWriter);
            PsiXmlVariableNameWriter<CvTerm> experimentalCvWriter = elementWriterFactory.createExperimentalCvWriter(streamWriter, extended, objectIndex, aliasWriter,
                    primaryRefWriter);
            PsiXmlElementWriter[] candidateWriters = elementWriterFactory.createParticipantCandidateWriter(streamWriter, extended, objectIndex, version, xmlType,
                    category, interactorWriter, (PsiXmlElementWriter<ModelledFeature>) featureWriters[1],
                    (PsiXmlElementWriter<FeatureEvidence>) featureWriters[0]);

            switch (version){
                case v3_0_0:
                    switch (xmlType){
                        case compact:
                            uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactModelledParticipantWriter modelledWriter2 =
                                    new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactModelledParticipantWriter(streamWriter, objectIndex);
                            modelledWriter2.setAliasWriter(aliasWriter);
                            modelledWriter2.setAttributeWriter(attributeWriter);
                            modelledWriter2.setXrefWriter(primaryRefWriter);
                            modelledWriter2.setFeatureWriter((PsiXmlElementWriter<ModelledFeature>) featureWriters[1]);
                            modelledWriter2.setInteractorWriter(interactorWriter);
                            modelledWriter2.setBiologicalRoleWriter(cvWriter);
                            modelledWriter2.setParticipantCandidateWriter(candidateWriters[1]);

                            switch (category){
                                case modelled:
                                    return new PsiXmlParticipantWriter[]{modelledWriter2, modelledWriter2};
                                default:
                                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactParticipantEvidenceWriter writer2 =
                                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactParticipantEvidenceWriter(streamWriter, objectIndex);
                                    writer2.setAliasWriter(aliasWriter);
                                    writer2.setAttributeWriter(attributeWriter);
                                    writer2.setXrefWriter(primaryRefWriter);
                                    writer2.setFeatureWriter((PsiXmlElementWriter<FeatureEvidence>) featureWriters[0]);
                                    writer2.setInteractorWriter(interactorWriter);
                                    writer2.setBiologicalRoleWriter(cvWriter);
                                    writer2.setExperimentalCvWriter(experimentalCvWriter);
                                    writer2.setParameterWriter(parameterWriter);
                                    writer2.setConfidenceWriter(confidenceWriter);
                                    writer2.setHostOrganismWriter(elementWriterFactory.createHostOrganismWriter(streamWriter, extended,
                                            objectIndex, aliasWriter,
                                            attributeWriter, primaryRefWriter, openCvWriter));
                                    writer2.setParticipantCandidateWriter(candidateWriters[0]);

                                    return new PsiXmlParticipantWriter[]{writer2, modelledWriter2};
                            }
                        default:
                            uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactModelledParticipantWriter modelledWriter3 =
                                    new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactModelledParticipantWriter(streamWriter, objectIndex);
                            modelledWriter3.setAliasWriter(aliasWriter);
                            modelledWriter3.setAttributeWriter(attributeWriter);
                            modelledWriter3.setXrefWriter(primaryRefWriter);
                            modelledWriter3.setFeatureWriter((PsiXmlElementWriter<ModelledFeature>) featureWriters[1]);
                            modelledWriter3.setInteractorWriter(interactorWriter);
                            modelledWriter3.setBiologicalRoleWriter(cvWriter);
                            modelledWriter3.setParticipantCandidateWriter(candidateWriters[1]);

                            switch (category){
                                case modelled:
                                    return new PsiXmlParticipantWriter[]{modelledWriter3, modelledWriter3};
                                default:
                                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactParticipantEvidenceWriter writer2 =
                                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactParticipantEvidenceWriter(streamWriter, objectIndex);
                                    writer2.setAliasWriter(aliasWriter);
                                    writer2.setAttributeWriter(attributeWriter);
                                    writer2.setXrefWriter(primaryRefWriter);
                                    writer2.setFeatureWriter((PsiXmlElementWriter<FeatureEvidence>) featureWriters[0]);
                                    writer2.setInteractorWriter(interactorWriter);
                                    writer2.setBiologicalRoleWriter(cvWriter);
                                    writer2.setExperimentalCvWriter(experimentalCvWriter);
                                    writer2.setParameterWriter(parameterWriter);
                                    writer2.setConfidenceWriter(confidenceWriter);
                                    writer2.setHostOrganismWriter(elementWriterFactory.createHostOrganismWriter(streamWriter, extended, objectIndex, aliasWriter,
                                            attributeWriter, primaryRefWriter, openCvWriter));
                                    writer2.setParticipantCandidateWriter(candidateWriters[0]);

                                    return new PsiXmlParticipantWriter[]{writer2, modelledWriter3};
                            }
                    }

                default:

                    switch (xmlType){
                        case compact:
                            uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactModelledParticipantWriter modelledWriter2 =
                                    new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactModelledParticipantWriter(streamWriter, objectIndex);
                            modelledWriter2.setAliasWriter(aliasWriter);
                            modelledWriter2.setAttributeWriter(attributeWriter);
                            modelledWriter2.setXrefWriter(primaryRefWriter);
                            modelledWriter2.setFeatureWriter((PsiXmlElementWriter<ModelledFeature>)featureWriters[1]);
                            modelledWriter2.setInteractorWriter(interactorWriter);
                            modelledWriter2.setBiologicalRoleWriter(cvWriter);

                            switch (category){
                                case modelled:
                                    return new PsiXmlParticipantWriter[]{modelledWriter2, modelledWriter2};
                                case basic:
                                    psidev.psi.mi.jami.xml.io.writer.elements.impl.compact.xml25.XmlParticipantWriter writer3 =
                                            new psidev.psi.mi.jami.xml.io.writer.elements.impl.compact.xml25.XmlParticipantWriter(streamWriter, objectIndex);
                                    writer3.setAliasWriter(aliasWriter);
                                    writer3.setAttributeWriter(attributeWriter);
                                    writer3.setXrefWriter(primaryRefWriter);
                                    writer3.setFeatureWriter((PsiXmlElementWriter<Feature>)featureWriters[0]);
                                    writer3.setInteractorWriter(interactorWriter);
                                    writer3.setBiologicalRoleWriter(cvWriter);

                                    return new PsiXmlParticipantWriter[]{writer3, modelledWriter2};
                                default:
                                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactParticipantEvidenceWriter writer2 =
                                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactParticipantEvidenceWriter(streamWriter, objectIndex);
                                    writer2.setAliasWriter(aliasWriter);
                                    writer2.setAttributeWriter(attributeWriter);
                                    writer2.setXrefWriter(primaryRefWriter);
                                    writer2.setFeatureWriter((PsiXmlElementWriter<FeatureEvidence>) featureWriters[0]);
                                    writer2.setInteractorWriter(interactorWriter);
                                    writer2.setBiologicalRoleWriter(cvWriter);
                                    writer2.setExperimentalCvWriter(experimentalCvWriter);
                                    writer2.setParameterWriter(parameterWriter);
                                    writer2.setConfidenceWriter(confidenceWriter);
                                    writer2.setHostOrganismWriter(elementWriterFactory.createHostOrganismWriter(streamWriter, extended, objectIndex, aliasWriter,
                                            attributeWriter, primaryRefWriter, openCvWriter));

                                    return new PsiXmlParticipantWriter[]{writer2, modelledWriter2};
                            }
                        default:
                            uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml25.XmlIntactModelledParticipantWriter modelledWriter3 =
                                    new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml25.XmlIntactModelledParticipantWriter(streamWriter, objectIndex);
                            modelledWriter3.setAliasWriter(aliasWriter);
                            modelledWriter3.setAttributeWriter(attributeWriter);
                            modelledWriter3.setXrefWriter(primaryRefWriter);
                            modelledWriter3.setFeatureWriter((PsiXmlElementWriter<ModelledFeature>)featureWriters[1]);
                            modelledWriter3.setInteractorWriter(interactorWriter);
                            modelledWriter3.setBiologicalRoleWriter(cvWriter);

                            switch (category){
                                case modelled:
                                    return new PsiXmlParticipantWriter[]{modelledWriter3, modelledWriter3};
                                case basic:
                                    psidev.psi.mi.jami.xml.io.writer.elements.impl.expanded.xml25.XmlParticipantWriter writer3 =
                                            new psidev.psi.mi.jami.xml.io.writer.elements.impl.expanded.xml25.XmlParticipantWriter(streamWriter, objectIndex);
                                    writer3.setAliasWriter(aliasWriter);
                                    writer3.setAttributeWriter(attributeWriter);
                                    writer3.setXrefWriter(primaryRefWriter);
                                    writer3.setFeatureWriter((PsiXmlElementWriter<Feature>)featureWriters[0]);
                                    writer3.setInteractorWriter(interactorWriter);
                                    writer3.setBiologicalRoleWriter(cvWriter);

                                    return new PsiXmlParticipantWriter[]{writer3, modelledWriter3};
                                default:
                                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml25.XmlIntactParticipantEvidenceWriter writer2 =
                                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml25.XmlIntactParticipantEvidenceWriter(streamWriter, objectIndex);
                                    writer2.setAliasWriter(aliasWriter);
                                    writer2.setAttributeWriter(attributeWriter);
                                    writer2.setXrefWriter(primaryRefWriter);
                                    writer2.setFeatureWriter((PsiXmlElementWriter<FeatureEvidence>) featureWriters[0]);
                                    writer2.setInteractorWriter(interactorWriter);
                                    writer2.setBiologicalRoleWriter(cvWriter);
                                    writer2.setExperimentalCvWriter(experimentalCvWriter);
                                    writer2.setParameterWriter(parameterWriter);
                                    writer2.setConfidenceWriter(confidenceWriter);
                                    writer2.setHostOrganismWriter(elementWriterFactory.createHostOrganismWriter(streamWriter, extended, objectIndex, aliasWriter,
                                            attributeWriter, primaryRefWriter, openCvWriter));

                                    return new PsiXmlParticipantWriter[]{writer2, modelledWriter3};
                            }
                    }
            }
        }
    }

    public static PsiXmlVariableNameWriter<CvTerm> createOpenCvWriter(XMLStreamWriter streamWriter, boolean extended,
                                                                      PsiXmlElementWriter<Alias> aliasWriter, PsiXmlElementWriter<Annotation> attributeWriter,
                                                                      PsiXmlXrefWriter primaryRefWriter) {
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();
        if (extended){
            return elementWriterFactory.createOpenCvWriter(streamWriter, extended, aliasWriter, attributeWriter, primaryRefWriter);
        }
        else{
            IntactXmlOpenCvTermWriter cellTypeWriter = new IntactXmlOpenCvTermWriter(streamWriter);
            cellTypeWriter.setAttributeWriter(attributeWriter);
            cellTypeWriter.setAliasWriter(aliasWriter);
            cellTypeWriter.setXrefWriter(primaryRefWriter);
            return cellTypeWriter;
        }
    }

    public static PsiXmlElementWriter<Interactor> createInteractorWriter(XMLStreamWriter streamWriter, boolean extended,
                                                                         PsiXmlObjectCache objectIndex, PsiXmlElementWriter<Alias> aliasWriter,
                                                                         PsiXmlElementWriter<Annotation> attributeWriter,
                                                                         PsiXmlXrefWriter primaryRefWriter,
                                                                         PsiXmlVariableNameWriter<CvTerm> interactorTypeWriter,
                                                                         PsiXmlElementWriter<Organism> organismWriter,
                                                                         PsiXmlElementWriter<Checksum> checksumWriter) {
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();
        if (extended){
            return elementWriterFactory.createInteractorWriter(streamWriter, extended, objectIndex, aliasWriter, attributeWriter, primaryRefWriter,
                    interactorTypeWriter, organismWriter, checksumWriter);
        }
        else{
            IntactXmlInteractorWriter interactorWriter = new IntactXmlInteractorWriter(streamWriter, objectIndex);
            interactorWriter.setAliasWriter(aliasWriter);
            interactorWriter.setAttributeWriter(attributeWriter);
            interactorWriter.setXrefWriter(primaryRefWriter);
            interactorWriter.setInteractorTypeWriter(interactorTypeWriter);
            interactorWriter.setOrganismWriter(organismWriter);
            interactorWriter.setChecksumWriter(checksumWriter);
            return interactorWriter;
        }
    }

    public static PsiXmlSourceWriter createSourceWriter(XMLStreamWriter streamWriter, boolean extended,
                                                        PsiXmlVersion version,
                                                        PsiXmlElementWriter<Alias> aliasWriter, PsiXmlElementWriter<Annotation> attributeWriter,
                                                        PsiXmlXrefWriter primaryRefWriter,
                                                        PsiXmlPublicationWriter publicationWriter) {
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();

        if (extended){
            return elementWriterFactory.createSourceWriter(streamWriter, extended, version, aliasWriter, attributeWriter, primaryRefWriter,
                    publicationWriter);
        }
        else{
            switch (version){
                case v3_0_0:
                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactSourceWriter sourceWriter =
                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactSourceWriter(streamWriter);
                    sourceWriter.setXrefWriter(primaryRefWriter);
                    sourceWriter.setAttributeWriter(attributeWriter);
                    sourceWriter.setAliasWriter(aliasWriter);
                    sourceWriter.setPublicationWriter(publicationWriter);
                    return sourceWriter;
                default:
                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactSourceWriter sourceWriter2 =
                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactSourceWriter(streamWriter);
                    sourceWriter2.setXrefWriter(primaryRefWriter);
                    sourceWriter2.setAttributeWriter(attributeWriter);
                    sourceWriter2.setAliasWriter(aliasWriter);
                    sourceWriter2.setPublicationWriter(publicationWriter);
                    return sourceWriter2;
            }
        }

    }

    public static PsiXmlPublicationWriter createPublicationWriter(XMLStreamWriter streamWriter, boolean extended,
                                                                  PsiXmlElementWriter<Annotation> attributeWriter, PsiXmlXrefWriter primaryRefWriter,
                                                                  PsiXmlVersion version) {
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();

        PsiXmlPublicationWriter publicationWriter;
        if (extended){
            return elementWriterFactory.createPublicationWriter(streamWriter, extended, attributeWriter, primaryRefWriter,
                    version);
        }
        else{
            switch (version){
                case v3_0_0:
                    publicationWriter = new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactPublicationWriter(streamWriter);
                    ((uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactPublicationWriter)publicationWriter)
                            .setAttributeWriter(attributeWriter);
                    ((uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactPublicationWriter)publicationWriter)
                            .setXrefWriter(primaryRefWriter);
                    break;
                default:
                    publicationWriter = new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactPublicationWriter(streamWriter);
                    ((uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactPublicationWriter)publicationWriter)
                            .setAttributeWriter(attributeWriter);
                    ((uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactPublicationWriter)publicationWriter)
                            .setXrefWriter(primaryRefWriter);
                    break;
            }
        }
        return publicationWriter;
    }

    public static <F extends Feature> PsiXmlElementWriter<F>[] createFeatureWriter(XMLStreamWriter streamWriter, boolean extended,
                                                                                   PsiXmlObjectCache objectIndex, PsiXmlVersion version,
                                                                                   InteractionCategory category, PsiXmlElementWriter<Alias> aliasWriter,
                                                                                   PsiXmlElementWriter<Annotation> attributeWriter,
                                                                                   PsiXmlXrefWriter primaryRefWriter,
                                                                                   PsiXmlVariableNameWriter<CvTerm> featureTypeWriter,
                                                                                   PsiXmlParameterWriter parameterWriter){
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();

        PsiXmlElementWriter<Range> rangeWriter = elementWriterFactory.createRangeWriter(streamWriter, extended, objectIndex, version, primaryRefWriter,
                featureTypeWriter);

        if (extended){
            return createFeatureWriter(streamWriter, extended, objectIndex, version, category, aliasWriter, attributeWriter, primaryRefWriter, featureTypeWriter,
                    parameterWriter);
        }
        else{
            switch (version){
                case v3_0_0:
                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactModelledFeatureWriter modelledWriter =
                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactModelledFeatureWriter(streamWriter, objectIndex);
                    modelledWriter.setAliasWriter(aliasWriter);
                    modelledWriter.setAttributeWriter(attributeWriter);
                    modelledWriter.setFeatureTypeWriter(featureTypeWriter);
                    modelledWriter.setRangeWriter(rangeWriter);
                    modelledWriter.setXrefWriter(primaryRefWriter);

                    switch (category){
                        case modelled:
                            return new PsiXmlElementWriter[]{modelledWriter, modelledWriter};
                        default:
                            uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactFeatureEvidenceWriter writer =
                                    new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30.XmlIntactFeatureEvidenceWriter(streamWriter, objectIndex);
                            writer.setAliasWriter(aliasWriter);
                            writer.setAttributeWriter(attributeWriter);
                            writer.setFeatureTypeWriter(featureTypeWriter);
                            writer.setRangeWriter(rangeWriter);
                            writer.setXrefWriter(primaryRefWriter);
                            writer.setParameterWriter(parameterWriter);

                            return new PsiXmlElementWriter[]{writer, modelledWriter};
                    }

                default:
                    uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactModelledFeatureWriter modelledWriter2 =
                            new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactModelledFeatureWriter(streamWriter, objectIndex);
                    modelledWriter2.setAliasWriter(aliasWriter);
                    modelledWriter2.setAttributeWriter(attributeWriter);
                    modelledWriter2.setFeatureTypeWriter(featureTypeWriter);
                    modelledWriter2.setRangeWriter(rangeWriter);
                    modelledWriter2.setXrefWriter(primaryRefWriter);

                    switch (category){
                        case modelled:
                            return new PsiXmlElementWriter[]{modelledWriter2, modelledWriter2};
                        case basic:
                            psidev.psi.mi.jami.xml.io.writer.elements.impl.xml25.XmlFeatureWriter writer3 =
                                    new psidev.psi.mi.jami.xml.io.writer.elements.impl.xml25.XmlFeatureWriter(streamWriter, objectIndex);
                            writer3.setAliasWriter(aliasWriter);
                            writer3.setAttributeWriter(attributeWriter);
                            writer3.setFeatureTypeWriter(featureTypeWriter);
                            writer3.setRangeWriter(rangeWriter);
                            writer3.setXrefWriter(primaryRefWriter);

                            return new PsiXmlElementWriter[]{writer3, modelledWriter2};
                        default:
                            uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactFeatureEvidenceWriter writer2 =
                                    new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25.XmlIntactFeatureEvidenceWriter(streamWriter, objectIndex);
                            writer2.setAliasWriter(aliasWriter);
                            writer2.setAttributeWriter(attributeWriter);
                            writer2.setFeatureTypeWriter(featureTypeWriter);
                            writer2.setRangeWriter(rangeWriter);
                            writer2.setXrefWriter(primaryRefWriter);

                            return new PsiXmlElementWriter[]{writer2, modelledWriter2};
                    }
            }
        }
    }

    public static PsiXmlInteractionWriter[] createDefaultPsiXmlInteractionWriters(XMLStreamWriter streamWriter, PsiXmlObjectCache objectIndex,
                                                                                  PsiXmlVersion version, PsiXmlType xmlType,
                                                                                  InteractionCategory interactionCategory, ComplexType complexType,
                                                                                  PsiXmlElementWriter<Alias> aliasWriter,
                                                                                  PsiXmlElementWriter<Annotation> attributeWriter,
                                                                                  PsiXmlXrefWriter primaryRefWriter,
                                                                                  PsiXmlElementWriter[] confidenceWriters,
                                                                                  PsiXmlElementWriter<Checksum> checksumWriter,
                                                                                  PsiXmlVariableNameWriter<CvTerm> interactionTypeWriter,
                                                                                  PsiXmlExperimentWriter experimentWriter,
                                                                                  PsiXmlElementWriter<String> availabilityWriter,
                                                                                  PsiXmlParameterWriter[] parameterWriters,
                                                                                  PsiXmlParticipantWriter[] participantWriters,
                                                                                  PsiXmlElementWriter inferredInteractionWriter,
                                                                                  PsiXmlPublicationWriter publicationWriter,
                                                                                  PsiXmlVariableNameWriter<CvTerm> openCvWriter) {
        PsiXmlElementWriterFactory elementWriterFactory = PsiXmlElementWriterFactory.getInstance();

        switch (version){
            case v3_0_0:
                switch (xmlType){
                    case compact:
                        switch (complexType){
                            case binary:
                                return elementWriterFactory.createDefaultPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory,
                                        complexType, aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters, checksumWriter,
                                        interactionTypeWriter, experimentWriter, availabilityWriter, parameterWriters, participantWriters,
                                        inferredInteractionWriter, publicationWriter, openCvWriter);
                            default:
                                PsiXmlElementWriter<Set<Feature>> bindingFeaturesWriter = elementWriterFactory.createBindingFeaturesWriter(streamWriter,  objectIndex);
                                PsiXmlCausalRelationshipWriter relationshipWriter = elementWriterFactory.createCausalRelationshipWriter(streamWriter,false,  objectIndex,
                                        openCvWriter);
                                PsiXmlElementWriter<Preassembly> preassemblyWriter = elementWriterFactory.createPreassemblyWriter(streamWriter, false, objectIndex, interactionTypeWriter,
                                        attributeWriter, publicationWriter);
                                PsiXmlElementWriter<Allostery> allosteryWriter = elementWriterFactory.createAllosteryWriter(streamWriter, false, objectIndex, interactionTypeWriter,
                                        attributeWriter, publicationWriter);
                                uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactModelledInteractionWriter modelledWriter2 =
                                        new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactModelledInteractionWriter(streamWriter, objectIndex);

                                modelledWriter2.setAliasWriter(aliasWriter);
                                modelledWriter2.setAttributeWriter(attributeWriter);
                                modelledWriter2.setXrefWriter(primaryRefWriter);
                                modelledWriter2.setInteractionTypeWriter(interactionTypeWriter);
                                modelledWriter2.setConfidenceWriter(confidenceWriters[1]);
                                modelledWriter2.setInferredInteractionWriter(inferredInteractionWriter);
                                modelledWriter2.setParticipantWriter(participantWriters[1]);
                                modelledWriter2.setChecksumWriter(checksumWriter);
                                modelledWriter2.setExperimentWriter(experimentWriter);
                                modelledWriter2.setParameterWriter(parameterWriters[1]);
                                modelledWriter2.setBindingFeaturesWriter(bindingFeaturesWriter);
                                modelledWriter2.setCausalRelationshipWriter(relationshipWriter);
                                modelledWriter2.setAllosteryWriter(allosteryWriter);
                                modelledWriter2.setPreAssemblyWriter(preassemblyWriter);
                                switch (interactionCategory){
                                    case modelled:
                                        return new PsiXmlInteractionWriter[]{modelledWriter2, modelledWriter2};
                                    case complex:
                                        return new PsiXmlInteractionWriter[]{modelledWriter2, modelledWriter2};
                                    default:
                                        uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactInteractionEvidenceWriter writer2 =
                                                new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml30.XmlIntactInteractionEvidenceWriter(streamWriter, objectIndex);
                                        writer2.setAttributeWriter(attributeWriter);
                                        writer2.setXrefWriter(primaryRefWriter);
                                        writer2.setInteractionTypeWriter(interactionTypeWriter);
                                        writer2.setConfidenceWriter(confidenceWriters[0]);
                                        writer2.setInferredInteractionWriter(inferredInteractionWriter);
                                        writer2.setParticipantWriter(participantWriters[0]);
                                        writer2.setChecksumWriter(checksumWriter);
                                        writer2.setExperimentWriter(experimentWriter);
                                        writer2.setParameterWriter(parameterWriters[0]);
                                        writer2.setAvailabilityWriter(availabilityWriter);
                                        writer2.setVariableParameterValueSetWriter(elementWriterFactory.createVariableParameterValueSetWriter(streamWriter, objectIndex));
                                        writer2.setCausalRelationshipWriter(relationshipWriter);

                                        return new PsiXmlInteractionWriter[]{writer2, modelledWriter2};
                                }
                        }

                    default:

                        switch (complexType){
                            case binary:
                                return elementWriterFactory.createDefaultPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory,
                                        complexType, aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters, checksumWriter,
                                        interactionTypeWriter, experimentWriter, availabilityWriter, parameterWriters, participantWriters,
                                        inferredInteractionWriter, publicationWriter, openCvWriter);
                            default:
                                PsiXmlElementWriter<Set<Feature>> bindingFeaturesWriter = elementWriterFactory.createBindingFeaturesWriter(streamWriter,  objectIndex);
                                PsiXmlCausalRelationshipWriter relationshipWriter = elementWriterFactory.createCausalRelationshipWriter(streamWriter,false,  objectIndex,
                                        openCvWriter);
                                PsiXmlElementWriter<Preassembly> preassemblyWriter = elementWriterFactory.createPreassemblyWriter(streamWriter, false, objectIndex, interactionTypeWriter,
                                        attributeWriter, publicationWriter);
                                PsiXmlElementWriter<Allostery> allosteryWriter = elementWriterFactory.createAllosteryWriter(streamWriter, false, objectIndex, interactionTypeWriter,
                                        attributeWriter, publicationWriter);
                                uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactModelledInteractionWriter modelledWriter3 =
                                        new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactModelledInteractionWriter(streamWriter, objectIndex);
                                modelledWriter3.setAliasWriter(aliasWriter);

                                modelledWriter3.setAttributeWriter(attributeWriter);
                                modelledWriter3.setXrefWriter(primaryRefWriter);
                                modelledWriter3.setInteractionTypeWriter(interactionTypeWriter);
                                modelledWriter3.setConfidenceWriter(confidenceWriters[1]);
                                modelledWriter3.setInferredInteractionWriter(inferredInteractionWriter);
                                modelledWriter3.setParticipantWriter(participantWriters[1]);
                                modelledWriter3.setChecksumWriter(checksumWriter);
                                modelledWriter3.setExperimentWriter(experimentWriter);
                                modelledWriter3.setParameterWriter(parameterWriters[1]);
                                modelledWriter3.setBindingFeaturesWriter(bindingFeaturesWriter);
                                modelledWriter3.setCausalRelationshipWriter(relationshipWriter);
                                modelledWriter3.setAllosteryWriter(allosteryWriter);
                                modelledWriter3.setPreAssemblyWriter(preassemblyWriter);
                                switch (interactionCategory){
                                    case modelled:
                                        return new PsiXmlInteractionWriter[]{modelledWriter3, modelledWriter3};
                                    case complex:
                                        return new PsiXmlInteractionWriter[]{modelledWriter3, modelledWriter3};
                                    default:
                                        uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactInteractionEvidenceWriter writer2 =
                                                new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml30.XmlIntactInteractionEvidenceWriter(streamWriter, objectIndex);
                                        writer2.setAttributeWriter(attributeWriter);
                                        writer2.setXrefWriter(primaryRefWriter);
                                        writer2.setInteractionTypeWriter(interactionTypeWriter);
                                        writer2.setConfidenceWriter(confidenceWriters[0]);
                                        writer2.setInferredInteractionWriter(inferredInteractionWriter);
                                        writer2.setParticipantWriter(participantWriters[0]);
                                        writer2.setChecksumWriter(checksumWriter);
                                        writer2.setExperimentWriter(experimentWriter);
                                        writer2.setParameterWriter(parameterWriters[0]);
                                        writer2.setAvailabilityWriter(availabilityWriter);
                                        writer2.setVariableParameterValueSetWriter(elementWriterFactory.createVariableParameterValueSetWriter(streamWriter, objectIndex));
                                        writer2.setCausalRelationshipWriter(relationshipWriter);

                                        return new PsiXmlInteractionWriter[]{writer2, modelledWriter3};
                                }
                        }
                }

            default:

                switch (xmlType){
                    case compact:
                        switch (complexType){
                            case binary:
                                return elementWriterFactory.createDefaultPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory,
                                        complexType, aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters, checksumWriter,
                                        interactionTypeWriter, experimentWriter, availabilityWriter, parameterWriters, participantWriters,
                                        inferredInteractionWriter, publicationWriter, openCvWriter);
                            default:
                                uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactComplexWriter modelledWriter2 =
                                        new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactComplexWriter(streamWriter, objectIndex);
                                modelledWriter2.setAliasWriter(aliasWriter);
                                modelledWriter2.setAttributeWriter(attributeWriter);
                                modelledWriter2.setXrefWriter(primaryRefWriter);
                                modelledWriter2.setInteractionTypeWriter(interactionTypeWriter);
                                modelledWriter2.setConfidenceWriter(confidenceWriters[1]);
                                modelledWriter2.setInferredInteractionWriter(inferredInteractionWriter);
                                modelledWriter2.setParticipantWriter(participantWriters[1]);
                                modelledWriter2.setChecksumWriter(checksumWriter);
                                modelledWriter2.setExperimentWriter(experimentWriter);
                                modelledWriter2.setParameterWriter(parameterWriters[1]);
                                switch (interactionCategory){
                                    case modelled:
                                        return new PsiXmlInteractionWriter[]{modelledWriter2, modelledWriter2};
                                    case complex:
                                        return new PsiXmlInteractionWriter[]{modelledWriter2, modelledWriter2};
                                    case basic:
                                        psidev.psi.mi.jami.xml.io.writer.elements.impl.compact.xml25.XmlBasicInteractionWriter writer3 =
                                                new psidev.psi.mi.jami.xml.io.writer.elements.impl.compact.xml25.XmlBasicInteractionWriter(streamWriter, objectIndex);
                                        writer3.setAttributeWriter(attributeWriter);
                                        writer3.setXrefWriter(primaryRefWriter);
                                        writer3.setInteractionTypeWriter(interactionTypeWriter);
                                        writer3.setInferredInteractionWriter(inferredInteractionWriter);
                                        writer3.setParticipantWriter(participantWriters[0]);
                                        writer3.setChecksumWriter(checksumWriter);
                                        writer3.setExperimentWriter(experimentWriter);

                                        return new PsiXmlInteractionWriter[]{writer3, modelledWriter2};
                                    default:
                                        uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactInteractionEvidenceWriter writer2 =
                                                new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25.XmlIntactInteractionEvidenceWriter(streamWriter, objectIndex);
                                        writer2.setAttributeWriter(attributeWriter);
                                        writer2.setXrefWriter(primaryRefWriter);
                                        writer2.setInteractionTypeWriter(interactionTypeWriter);
                                        writer2.setConfidenceWriter(confidenceWriters[0]);
                                        writer2.setInferredInteractionWriter(inferredInteractionWriter);
                                        writer2.setParticipantWriter(participantWriters[0]);
                                        writer2.setChecksumWriter(checksumWriter);
                                        writer2.setExperimentWriter(experimentWriter);
                                        writer2.setParameterWriter(parameterWriters[0]);
                                        writer2.setAvailabilityWriter(availabilityWriter);

                                        return new PsiXmlInteractionWriter[]{writer2, modelledWriter2};
                                }
                        }

                    default:

                        switch (complexType){
                            case binary:
                                return elementWriterFactory.createDefaultPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory,
                                        complexType, aliasWriter, attributeWriter, primaryRefWriter, confidenceWriters, checksumWriter,
                                        interactionTypeWriter, experimentWriter, availabilityWriter, parameterWriters, participantWriters,
                                        inferredInteractionWriter, publicationWriter, openCvWriter);
                            default:
                                uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml25.XmlIntactComplexWriter modelledWriter3 =
                                        new uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml25.XmlIntactComplexWriter(streamWriter, objectIndex);
                                modelledWriter3.setAliasWriter(aliasWriter);
                                modelledWriter3.setAttributeWriter(attributeWriter);
                                modelledWriter3.setXrefWriter(primaryRefWriter);
                                modelledWriter3.setInteractionTypeWriter(interactionTypeWriter);
                                modelledWriter3.setConfidenceWriter(confidenceWriters[1]);
                                modelledWriter3.setInferredInteractionWriter(inferredInteractionWriter);
                                modelledWriter3.setParticipantWriter(participantWriters[1]);
                                modelledWriter3.setChecksumWriter(checksumWriter);
                                modelledWriter3.setExperimentWriter(experimentWriter);
                                modelledWriter3.setParameterWriter(parameterWriters[1]);
                                switch (interactionCategory){
                                    case modelled:
                                        return new PsiXmlInteractionWriter[]{modelledWriter3, modelledWriter3};
                                    case complex:
                                        return new PsiXmlInteractionWriter[]{modelledWriter3, modelledWriter3};
                                    case basic:
                                        psidev.psi.mi.jami.xml.io.writer.elements.impl.expanded.xml25.XmlBasicInteractionWriter writer3 =
                                                new psidev.psi.mi.jami.xml.io.writer.elements.impl.expanded.xml25.XmlBasicInteractionWriter(streamWriter, objectIndex);
                                        writer3.setAttributeWriter(attributeWriter);
                                        writer3.setXrefWriter(primaryRefWriter);
                                        writer3.setInteractionTypeWriter(interactionTypeWriter);
                                        writer3.setInferredInteractionWriter(inferredInteractionWriter);
                                        writer3.setParticipantWriter(participantWriters[0]);
                                        writer3.setChecksumWriter(checksumWriter);
                                        writer3.setExperimentWriter(experimentWriter);

                                        return new PsiXmlInteractionWriter[]{writer3, modelledWriter3};
                                    default:
                                        psidev.psi.mi.jami.xml.io.writer.elements.impl.expanded.xml25.XmlInteractionEvidenceWriter writer2 =
                                                new psidev.psi.mi.jami.xml.io.writer.elements.impl.expanded.xml25.XmlInteractionEvidenceWriter(streamWriter, objectIndex);
                                        writer2.setAttributeWriter(attributeWriter);
                                        writer2.setXrefWriter(primaryRefWriter);
                                        writer2.setInteractionTypeWriter(interactionTypeWriter);
                                        writer2.setConfidenceWriter(confidenceWriters[0]);
                                        writer2.setInferredInteractionWriter(inferredInteractionWriter);
                                        writer2.setParticipantWriter(participantWriters[0]);
                                        writer2.setChecksumWriter(checksumWriter);
                                        writer2.setExperimentWriter(experimentWriter);
                                        writer2.setParameterWriter(parameterWriters[0]);
                                        writer2.setAvailabilityWriter(availabilityWriter);

                                        return new PsiXmlInteractionWriter[]{writer2, modelledWriter3};
                                }
                        }
                }
        }
    }
}
