package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.xml.PsiXmlType;
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.*;

import javax.xml.stream.XMLStreamWriter;

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
                                                                        PsiXmlElementWriter<Confidence> confidenceWriter,
                                                                        PsiXmlElementWriter<Confidence> modelledConfidenceWriter,
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
                aliasWriter, attributeWriter, primaryRefWriter, confidenceWriter, interactorWriter, interactionTypeWriter, openCvWriter, parameterWriters[0]);
        PsiXmlElementWriter inferredInteractionWriter = elementWriterFactory.createInferredInteractionWriter(streamWriter, objectIndex);

        if (extended){
            return elementWriterFactory.createExtendedPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory, complexType,
                    aliasWriter, attributeWriter, primaryRefWriter, modelledConfidenceWriter, checksumWriter, interactionTypeWriter,
                    experimentWriter, availabilityWriter, parameterWriters, participantWriters, inferredInteractionWriter, publicationWriter,
                    openCvWriter);
        }
        else if (named){
            return elementWriterFactory.createNamedPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory, complexType,
                    aliasWriter, attributeWriter, primaryRefWriter, modelledConfidenceWriter, checksumWriter, interactionTypeWriter,
                    experimentWriter, availabilityWriter, parameterWriters, participantWriters, inferredInteractionWriter, publicationWriter,
                    openCvWriter);
        }
        else{
            return elementWriterFactory.createDefaultPsiXmlInteractionWriters(streamWriter, objectIndex, version, xmlType, interactionCategory, complexType,
                    aliasWriter, attributeWriter, primaryRefWriter, modelledConfidenceWriter, checksumWriter, interactionTypeWriter,
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
            PsiXmlElementWriter[] featureWriters = elementWriterFactory.createFeatureWriter(streamWriter, extended, objectIndex, version, category, aliasWriter,
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
}
