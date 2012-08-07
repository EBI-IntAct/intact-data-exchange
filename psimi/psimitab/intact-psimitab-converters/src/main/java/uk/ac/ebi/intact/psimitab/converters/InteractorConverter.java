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
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Annotation;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Interactor;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.InstitutionUtils;
import uk.ac.ebi.intact.model.util.ProteinUtils;

import java.util.*;

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

    private CrossReferenceConverter<InteractorXref> xRefConverter;
    private AliasConverter aliasConverter;
    private AnnotationConverter annotationConverter;
    private BioSourceConverter organismConverter;
    private CvObjectConverter<CvObject> cvObjectConverter;
    private FeatureConverter featureConverter;

    private static final List<String> uniprotKeys;
    public static String CRC64 = "crc64";

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
    }

    /**
     *  Converts the database IntactInteractor data model to intactpsimitab datamodel
     *
     * @param participant The component to be converted
     * @return ExtendedInteractor with additional fields specific to Intact
     */
    public psidev.psi.mi.tab.model.Interactor intactToMitab(uk.ac.ebi.intact.model.Component participant) {

        if (participant != null){
            psidev.psi.mi.tab.model.Interactor mitabInteractor = new psidev.psi.mi.tab.model.Interactor();
            Interactor interactor = participant.getInteractor();

            // converts interactor details
            if (interactor != null){
                Collection<InteractorXref> interactorXrefs = interactor.getXrefs();
                Collection<Annotation>  annotations = AnnotatedObjectUtils.getPublicAnnotations(interactor);
                Collection<InteractorAlias> aliases = interactor.getAliases();

                boolean hasFoundIdentity = false;

                if (!interactorXrefs.isEmpty()){

                    // convert xrefs, and identity
                    for (InteractorXref ref : interactorXrefs){

                        // identity xrefs
                        if (ref.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())){
                            // first identity
                            if (!hasFoundIdentity){

                                CrossReference identity = xRefConverter.createCrossReference(ref, false);
                                if (identity != null){
                                    hasFoundIdentity = true;

                                    mitabInteractor.getIdentifiers().add(identity);
                                }
                            }
                            // other identifiers
                            else {
                                CrossReference identity = xRefConverter.createCrossReference(ref, false);
                                if (identity != null){
                                    hasFoundIdentity = true;

                                    mitabInteractor.getAlternativeIdentifiers().add(identity);
                                }
                            }
                        }
                        // other xrefs
                        else {
                            CrossReference xref = xRefConverter.createCrossReference(ref, true);
                            if (xref != null){
                                hasFoundIdentity = true;

                                mitabInteractor.getXrefs().add(xref);
                            }
                        }
                    }
                }

                // if it is a protein from uniprot, assume the short label is the uniprot ID and add it to the
                // aliases
                if (interactor instanceof Protein) {
                    Protein prot = (Protein)interactor;

                    if (ProteinUtils.isFromUniprot(prot)) {
                        Alias altId = new AliasImpl( CvDatabase.UNIPROT, prot.getShortLabel(),"shortlabel" );
                        mitabInteractor.getAliases().add(altId);
                    }
                }else{
                    //if it is not a instance of protein, add the short label to alternative identifiers with INTACT as database
                    Alias altId = new AliasImpl( CvDatabase.INTACT, interactor.getShortLabel(),"shortlabel" );
                    mitabInteractor.getAliases().add(altId);
                }

                // convert ac as identity or secondary identifier
                if (interactor.getAc() != null){
                    CrossReference acField = new CrossReferenceImpl();

                    String db = CvDatabase.INTACT;

                    acField.setDatabase(db);
                    acField.setIdentifier(interactor.getAc());

                    if (interactor.getOwner() != null){
                        Institution institution = interactor.getOwner();

                        CvDatabase database = InstitutionUtils.retrieveCvDatabase(IntactContext.getCurrentInstance(), institution);

                        if (database != null && database.getShortLabel() != null){
                            acField.setDatabase(database.getShortLabel());
                        }
                    }

                    if (!hasFoundIdentity){

                        mitabInteractor.getIdentifiers().add(acField);
                    }
                    else {
                        mitabInteractor.getAlternativeIdentifiers().add(acField);
                    }
                }

                // convert aliases
                if (!aliases.isEmpty()){

                    for (InteractorAlias alias : aliases){
                        Alias aliasField = aliasConverter.intactToMitab(alias);

                        if (aliasField != null){
                            mitabInteractor.getAliases().add(aliasField);
                        }
                    }
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
                if (interactor.getCvInteractorType() != null){
                    CrossReference type = cvObjectConverter.toCrossReference(interactor.getCvInteractorType());
                    if (type != null){
                        mitabInteractor.getInteractorTypes().add(type);
                    }
                }

                // convert checksum (crc64 : only if sequence available)
                if (interactor instanceof Polymer){
                    Polymer polymer = (Polymer) interactor;
                    if (polymer.getCrc64() != null){
                        Checksum crc64 = new ChecksumImpl(CRC64, polymer.getCrc64());

                        mitabInteractor.getChecksums().add(crc64);
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

            return mitabInteractor;
        }

       return null;
    }

    public Component fromMitab() {
        throw new UnsupportedOperationException();
    }
}
