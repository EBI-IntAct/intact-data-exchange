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
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.FeatureEvidence;
import psidev.psi.mi.jami.model.Participant;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Organism;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactGene;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.IntactMolecule;
import uk.ac.ebi.intact.jami.model.extension.IntactNucleicAcid;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;
import uk.ac.ebi.intact.psimitab.converters.enrichers.*;
import uk.ac.ebi.intact.psimitab.converters.util.PsimitabTools;

import java.util.Collection;

/**
 * Contains method to convert the IntactInteractor database data model to Intact psimitab data model
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class InteractorConverter {

    private static final Log log = LogFactory.getLog( InteractorConverter.class );

    protected CrossReferenceConverter<ParticipantEvidenceXref> xRefConverter;
    protected AliasConverter aliasConverter;
    protected AnnotationConverter annotationConverter;
    protected BioSourceConverter organismConverter;
    protected CvObjectConverter cvObjectConverter;
    protected FeatureConverter featureConverter;

    public static final String CRC64 = "crc64";
    public static final String DISPLAY_SHORT = "display_short";
    public static final String DISPLAY_LONG = "display_long";
    public static final String SHORTLABEL = "shortlabel";
    public final static String GENE="MI:0250";

    private ProteinConverter proteinConverter;
    private SmallMoleculeConverter smallMoleculeConverter;
    private GeneConverter geneConverter;
    private NucleicAcidConverter nucleicAcidConverter;
    private DefaultInteractorEnricher defaultEnricher;

    public InteractorConverter(){
        xRefConverter = new CrossReferenceConverter<>();
        aliasConverter = new AliasConverter();
        annotationConverter = new AnnotationConverter();
        organismConverter = new BioSourceConverter();
        cvObjectConverter = new CvObjectConverter();
        featureConverter = new FeatureConverter();

        nucleicAcidConverter = new NucleicAcidConverter(xRefConverter, aliasConverter);
        proteinConverter = new ProteinConverter(xRefConverter, aliasConverter);
        smallMoleculeConverter = new SmallMoleculeConverter(xRefConverter, aliasConverter);
        geneConverter = new GeneConverter(xRefConverter, aliasConverter);
        defaultEnricher = new DefaultInteractorEnricher(xRefConverter, aliasConverter);
    }

    public InteractorConverter(String defaultInstitution){
        xRefConverter = new CrossReferenceConverter<>();
        aliasConverter = new AliasConverter();
        annotationConverter = new AnnotationConverter();
        organismConverter = new BioSourceConverter();
        cvObjectConverter = new CvObjectConverter();
        featureConverter = new FeatureConverter();

        nucleicAcidConverter = new NucleicAcidConverter(xRefConverter, aliasConverter, defaultInstitution);
        proteinConverter = new ProteinConverter(xRefConverter, aliasConverter, defaultInstitution);
        smallMoleculeConverter = new SmallMoleculeConverter(xRefConverter, aliasConverter, defaultInstitution);
        geneConverter = new GeneConverter(xRefConverter, aliasConverter, defaultInstitution);
        defaultEnricher = new DefaultInteractorEnricher(xRefConverter, aliasConverter, defaultInstitution);
    }

    /**
     *  Converts the database IntactInteractor data model to intactpsimitab datamodel
     *
     * @param participant The component to be converted
     * @return ExtendedInteractor with additional fields specific to Intact
     */
    public MitabInteractor intactToMitab(IntactParticipantEvidence participant) {

        if (participant != null){
            psidev.psi.mi.tab.model.Interactor mitabInteractor = new psidev.psi.mi.tab.model.Interactor();
            MitabInteractor convertedInteractorResult = new MitabInteractor(mitabInteractor);

            IntactInteractor interactor = (IntactInteractor) participant.getInteractor();

            // converts interactor details
            if (interactor != null){
                Collection<Annotation> annotations = PsimitabTools.getPublicAnnotations(interactor.getAnnotations());

                CvTerm interactorType = interactor.getInteractorType();

                // enrich proteins following data best practices
                if (interactor instanceof IntactProtein){
                    IntactProtein protein = (IntactProtein) interactor;
                    RigDataModel rigDataModel = proteinConverter.enrichProteinFromIntact(protein, mitabInteractor);
                    convertedInteractorResult.setRigDataModel(rigDataModel);
                }
                // enrich small molecules following data best practices
                else if (interactor instanceof IntactMolecule){
                    IntactMolecule smallMolecule = (IntactMolecule) interactor;
                    smallMoleculeConverter.enrichSmallMoleculeFromIntact(smallMolecule, mitabInteractor);
                }
                // enrich genes following data best practices
                else if (interactorType != null && GENE.equalsIgnoreCase(interactorType.getMIIdentifier())){
                    geneConverter.enrichGeneFromIntact((IntactGene) interactor, mitabInteractor);
                }
                // enrich small molecules following data best practices
                else if (interactor instanceof IntactNucleicAcid){
                    IntactNucleicAcid nucleicAcid = (IntactNucleicAcid) interactor;
                    nucleicAcidConverter.enrichNucleicAcidFromIntact(nucleicAcid, mitabInteractor);
                }
                // default enricher
                else {
                    defaultEnricher.enrichInteractorFromIntact(interactor, mitabInteractor);
                }

                // convert annotations at the level of interactor
                if (!annotations.isEmpty()){
                    for (Annotation annots : annotations){
                        psidev.psi.mi.tab.model.Annotation annotField = annotationConverter.intactToMitab(annots);

                        if (annotField != null){
                            mitabInteractor.getAnnotations().add(annotField);
                        }
                    }
                }

                // convert organism(s)
                if (interactor.getOrganism() != null){
                    Organism bioSourceField = organismConverter.intactToMitab((IntactOrganism) interactor.getOrganism());

                    mitabInteractor.setOrganism(bioSourceField);
                }

                // convert interactor type
                if (interactorType != null){
                    CrossReference type = cvObjectConverter.toCrossReference((IntactCvTerm) interactorType);
                    if (type != null){
                        mitabInteractor.getInteractorTypes().add(type);
                    }
                }
            }

            // convert biological role
            CrossReference bioRole = null;
            if (participant.getBiologicalRole() != null){
                bioRole = cvObjectConverter.toCrossReference((IntactCvTerm) participant.getBiologicalRole());
            }

            if (bioRole == null){
                bioRole = new CrossReferenceImpl(CvTerm.PSI_MI, Participant.UNSPECIFIED_ROLE_MI, Participant.UNSPECIFIED_ROLE);
            }

            mitabInteractor.getBiologicalRoles().add(bioRole);

            // convert experimental roles
            CrossReference expRoleField = null;
            if (participant.getExperimentalRole() != null){
                expRoleField = cvObjectConverter.toCrossReference((IntactCvTerm) participant.getExperimentalRole());
            }

            if (expRoleField == null){
                expRoleField = new CrossReferenceImpl(CvTerm.PSI_MI, Participant.UNSPECIFIED_ROLE_MI, Participant.UNSPECIFIED_ROLE);
            }

            mitabInteractor.getExperimentalRoles().add(expRoleField);

            // convert features
            if (!participant.getFeatures().isEmpty()){
                Collection<FeatureEvidence> features = participant.getFeatures();

                for (FeatureEvidence feature : features){
                    psidev.psi.mi.tab.model.Feature featureField = featureConverter.intactToMitab((IntactFeatureEvidence) feature);
                    if (featureField != null){
                        mitabInteractor.getFeatures().add(featureField);
                    }
                }
            }

            // convert stoichiometry
            if (participant.getStoichiometry() != null) {

                mitabInteractor.getStoichiometry().add(participant.getStoichiometry().getMinValue());
            }

            // convert participant identification methods
            if (!participant.getIdentificationMethods().isEmpty()){
                mitabInteractor.getParticipantIdentificationMethods().clear();

                for (CvTerm detMethod : participant.getIdentificationMethods()){
                    CrossReference methodField = cvObjectConverter.toCrossReference((IntactCvTerm) detMethod);
                    if (methodField != null){
                        mitabInteractor.getParticipantIdentificationMethods().add(methodField);
                    }
                }
            }

            // convert annotations at the level of participant
            Collection<Annotation> annotations = PsimitabTools.getPublicAnnotations(participant.getAnnotations());
            if (!annotations.isEmpty()){

                for (Annotation annots : participant.getAnnotations()){
                    psidev.psi.mi.tab.model.Annotation annotField = annotationConverter.intactToMitab(annots);

                    if (annotField != null){
                        mitabInteractor.getAnnotations().add(annotField);
                    }
                }
            }

            // convert xrefs at the level of participant
            if (!participant.getXrefs().isEmpty()){

                for (Xref xrefs : participant.getXrefs()){
                    CrossReference xrefField = xRefConverter.createCrossReference((ParticipantEvidenceXref) xrefs, true);

                    if (xrefField != null){
                        mitabInteractor.getXrefs().add(xrefField);
                    }
                }
            }

            return convertedInteractorResult;
        }

       return null;
    }

    public ProteinConverter getProteinConverter() {
        return proteinConverter;
    }

    public SmallMoleculeConverter getSmallMoleculeConverter() {
        return smallMoleculeConverter;
    }

    public GeneConverter getGeneConverter() {
        return geneConverter;
    }

    public NucleicAcidConverter getNucleicAcidConverter() {
        return nucleicAcidConverter;
    }

    public DefaultInteractorEnricher getDefaultEnricher() {
        return defaultEnricher;
    }
}
