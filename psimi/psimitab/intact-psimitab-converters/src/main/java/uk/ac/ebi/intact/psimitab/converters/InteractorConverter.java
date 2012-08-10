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
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import psidev.psi.mi.tab.model.Organism;
import uk.ac.ebi.intact.irefindex.seguid.RigDataModel;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.psimitab.converters.enrichers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static uk.ac.ebi.intact.model.CvAliasType.*;

/**
 * Contains method to convert the IntactInteractor database data model to Intact psimitab data model
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class InteractorConverter {

    private static final Log log = LogFactory.getLog( InteractorConverter.class );

    protected CrossReferenceConverter<InteractorXref> xRefConverter;
    protected AliasConverter aliasConverter;
    protected AnnotationConverter annotationConverter;
    protected BioSourceConverter organismConverter;
    protected CvObjectConverter<CvObject> cvObjectConverter;
    protected FeatureConverter featureConverter;

    protected static final List<String> uniprotKeys;
    public static final String CRC64 = "crc64";
    public static final String UNKNOWN_TAXID = "-3";
    public static final String DISPLAY_SHORT = "display_short";
    public static final String DISPLAY_LONG = "display_long";
    public static final String SHORTLABEL = "shortlabel";
    public final static String GENE="MI:0250";

    private ProteinConverter proteinConverter;
    private SmallMoleculeConverter smallMoleculeConverter;
    private GeneConverter geneConverter;
    private NucleicAcidConverter nucleicAcidConverter;
    private DefaultInteractorEnricher defaultEnricher;

    static {
        uniprotKeys = new ArrayList<String>( Arrays.asList( GENE_NAME_MI_REF, GENE_NAME_SYNONYM_MI_REF,
                                                            ISOFORM_SYNONYM_MI_REF, LOCUS_NAME_MI_REF,
                                                            ORF_NAME_MI_REF ) );
    }

    public InteractorConverter(){
        xRefConverter = new CrossReferenceConverter<InteractorXref>();
        aliasConverter = new AliasConverter();
        annotationConverter = new AnnotationConverter();
        organismConverter = new BioSourceConverter();
        cvObjectConverter = new CvObjectConverter<CvObject>();
        featureConverter = new FeatureConverter();

        nucleicAcidConverter = new NucleicAcidConverter(xRefConverter, aliasConverter);
        proteinConverter = new ProteinConverter(xRefConverter, aliasConverter);
        smallMoleculeConverter = new SmallMoleculeConverter(xRefConverter, aliasConverter);
        geneConverter = new GeneConverter(xRefConverter, aliasConverter);
        defaultEnricher = new DefaultInteractorEnricher(xRefConverter, aliasConverter);
    }

    /**
     *  Converts the database IntactInteractor data model to intactpsimitab datamodel
     *
     * @param participant The component to be converted
     * @return ExtendedInteractor with additional fields specific to Intact
     */
    public MitabInteractor intactToMitab(uk.ac.ebi.intact.model.Component participant) {

        if (participant != null){
            psidev.psi.mi.tab.model.Interactor mitabInteractor = new psidev.psi.mi.tab.model.Interactor();
            MitabInteractor convertedInteractorResult = new MitabInteractor(mitabInteractor);

            Interactor interactor = participant.getInteractor();

            // converts interactor details
            if (interactor != null){
                Collection<Annotation>  annotations = AnnotatedObjectUtils.getPublicAnnotations(interactor);

                CvInteractorType interactorType = interactor.getCvInteractorType();

                // enrich proteins following data best practices
                if (interactor instanceof Protein){
                    Protein protein = (Protein) interactor;
                    RigDataModel rigDataModel = proteinConverter.enrichProteinFromIntact(protein, mitabInteractor);
                    convertedInteractorResult.setRigDataModel(rigDataModel);
                }
                // enrich small molecules following data best practices
                else if (interactor instanceof SmallMolecule){
                    SmallMolecule smallMolecule = (SmallMolecule) interactor;
                    smallMoleculeConverter.enrichSmallMoleculeFromIntact(smallMolecule, mitabInteractor);
                }
                // enrich genes following data best practices
                else if (interactorType != null && GENE.equalsIgnoreCase(interactorType.getIdentifier())){
                    geneConverter.enrichGeneFromIntact(interactor, mitabInteractor);
                }
                // enrich small molecules following data best practices
                else if (interactor instanceof NucleicAcid){
                    NucleicAcid nucleicAcid = (NucleicAcid) interactor;
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
                if (interactor.getBioSource() != null){
                    Organism bioSourceField = organismConverter.intactToMitab(interactor.getBioSource());

                    mitabInteractor.setOrganism(bioSourceField);
                }

                // convert interactor type
                if (interactorType != null){
                    CrossReference type = cvObjectConverter.toCrossReference(interactorType);
                    if (type != null){
                        mitabInteractor.getInteractorTypes().add(type);
                    }
                }
            }

            // convert biological role
            CrossReference bioRole = null;
            if (participant.getCvBiologicalRole() != null){
                bioRole = cvObjectConverter.toCrossReference(participant.getCvBiologicalRole());
            }

            if (bioRole == null){
                bioRole = new CrossReferenceImpl(CvDatabase.PSI_MI, CvBiologicalRole.UNSPECIFIED_PSI_REF, CvBiologicalRole.UNSPECIFIED);
            }

            mitabInteractor.getBiologicalRoles().add(bioRole);

            // convert experimental roles
            for (CvExperimentalRole expRole : participant.getExperimentalRoles()){
                CrossReference expRoleField = cvObjectConverter.toCrossReference(expRole);
                if (expRoleField != null){
                    mitabInteractor.getExperimentalRoles().add(expRoleField);
                }
            }

            if (mitabInteractor.getExperimentalRoles().isEmpty()){
                CrossReference expRoleField = new CrossReferenceImpl(CvDatabase.PSI_MI, CvExperimentalRole.UNSPECIFIED_PSI_REF, CvExperimentalRole.UNSPECIFIED);

                mitabInteractor.getExperimentalRoles().add(expRoleField);
            }

            // convert features
            if (!participant.getFeatures().isEmpty()){
                Collection<Feature> features = participant.getFeatures();

                for (Feature feature : features){
                    psidev.psi.mi.tab.model.Feature featureField = featureConverter.intactToMitab(feature);
                    if (featureField != null){
                        mitabInteractor.getFeatures().add(featureField);
                    }
                }
            }

            // convert stoichiometry
            if (participant.hasStoichiometry()){

                mitabInteractor.getStoichiometry().add((int) participant.getStoichiometry());
            }

            // convert participant identification methods
            if (!participant.getParticipantDetectionMethods().isEmpty()){
                mitabInteractor.getParticipantIdentificationMethods().clear();

                for (CvIdentification detMethod : participant.getParticipantDetectionMethods()){
                    CrossReference methodField = cvObjectConverter.toCrossReference(detMethod);
                    if (methodField != null){
                        mitabInteractor.getParticipantIdentificationMethods().add(methodField);
                    }
                }
            }

            // convert annotations at the level of participant
            Collection<Annotation> annotations = AnnotatedObjectUtils.getPublicAnnotations(participant);
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

                for (ComponentXref xrefs : participant.getXrefs()){
                    CrossReference xrefField = xRefConverter.createCrossReference(xrefs, true);

                    if (xrefField != null){
                        mitabInteractor.getXrefs().add(xrefField);
                    }
                }
            }

            return convertedInteractorResult;
        }

       return null;
    }

    public Component fromMitab() {
        throw new UnsupportedOperationException();
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
