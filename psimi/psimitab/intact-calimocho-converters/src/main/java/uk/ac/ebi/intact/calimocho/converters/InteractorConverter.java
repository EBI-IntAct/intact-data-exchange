package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Checksum;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.FeatureEvidence;
import psidev.psi.mi.jami.model.Participant;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.ChecksumUtils;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAlias;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAnnotation;
import uk.ac.ebi.intact.jami.model.extension.IntactCvTerm;
import uk.ac.ebi.intact.jami.model.extension.IntactFeatureEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.IntactOrganism;
import uk.ac.ebi.intact.jami.model.extension.IntactParticipantEvidence;
import uk.ac.ebi.intact.jami.model.extension.IntactPolymer;
import uk.ac.ebi.intact.jami.model.extension.ParticipantEvidenceXref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Converts an interactor in a list of fields
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class InteractorConverter {
    private CrossReferenceConverter<ParticipantEvidenceXref> xrefConverter;
    private AliasConverter aliasConverter;
    private AnnotationConverter annotationConverter;
    private BioSourceConverter bioSourceConverter;
    private CvObjectConverter cvObjectConverter;
    private FeatureConverter featureConverter;
    
    public static String CRC64 = "crc64";

    public InteractorConverter(){
        this.xrefConverter = new CrossReferenceConverter<>();
        this.aliasConverter = new AliasConverter();
        this.annotationConverter = new AnnotationConverter();
        this.bioSourceConverter = new BioSourceConverter();
        this.cvObjectConverter = new CvObjectConverter();
        this.featureConverter = new FeatureConverter();
    }

    /**
     *
     * @param participant : intact participant
     * @param row : the calimocho row to complete with participant details
     * @param isFirst : boolean value to know if the participant is the first participant or not (idA or idB)
     */
    public void intactToCalimocho(IntactParticipantEvidence participant, Row row, boolean isFirst){

        if (participant != null){
            IntactInteractor interactor = (IntactInteractor) participant.getInteractor();

            // converts interactor details
            if (interactor != null){
                Collection<Xref> interactorXrefs = interactor.getXrefs();
                Collection<Annotation>  annotations = interactor.getAnnotations();
                Collection<Alias> aliases = interactor.getAliases();

                boolean hasFoundIdentity = false;
                Collection<Field> otherIdentifiers = Collections.EMPTY_LIST;
                Collection<Field> otherXrefs = Collections.EMPTY_LIST;
                
                if (!interactorXrefs.isEmpty()){
                    otherIdentifiers = new ArrayList<Field>(interactorXrefs.size());
                    otherXrefs = new ArrayList<Field>(interactorXrefs.size());

                    // convert xrefs, and identity
                    for (Xref ref : interactorXrefs){

                        // identity xrefs
                        if (ref.getQualifier() != null && Xref.IDENTITY_MI.equals(ref.getQualifier().getMIIdentifier())){
                            // first identity
                            if (!hasFoundIdentity){

                                Field identity = xrefConverter.intactToCalimocho((ParticipantEvidenceXref) ref, false);
                                if (identity != null){
                                    hasFoundIdentity = true;

                                    if (isFirst){
                                        row.addField(InteractionKeys.KEY_ID_A, identity);
                                    }
                                    else {
                                        row.addField(InteractionKeys.KEY_ID_B, identity);
                                    }
                                }
                            }
                            // other identifiers
                            else {
                                Field identity = xrefConverter.intactToCalimocho((ParticipantEvidenceXref) ref, false);
                                if (identity != null){
                                    otherIdentifiers.add(identity);
                                }
                            }
                        }
                        // other xrefs
                        else {
                            Field refField = xrefConverter.intactToCalimocho((ParticipantEvidenceXref) ref, true);
                            if (refField != null){
                                otherXrefs.add(refField);
                            }
                        }
                    }
                }

                
                // convert ac as identity or secondary identifier
                if (interactor.getAc() != null){
                    Field acField = new DefaultField();

                    acField.set(CalimochoKeys.KEY, "intact");
                    acField.set(CalimochoKeys.DB, "intact");
                    acField.set(CalimochoKeys.VALUE, interactor.getAc());

                    if (!hasFoundIdentity){

                        if (isFirst){
                            row.addField(InteractionKeys.KEY_ID_A, acField);
                        }
                        else {
                            row.addField(InteractionKeys.KEY_ID_B, acField);
                        }
                    }
                    else {
                        otherIdentifiers.add(acField);
                    }
                }

                if (!otherIdentifiers.isEmpty()){
                    if (isFirst){
                        row.addFields(InteractionKeys.KEY_ALTID_A, otherIdentifiers);
                    }
                    else {
                        row.addFields(InteractionKeys.KEY_ALTID_B, otherIdentifiers);
                    }
                }
                if (!otherXrefs.isEmpty()){
                    if (isFirst){
                        row.addFields(InteractionKeys.KEY_XREFS_A, otherXrefs);
                    }
                    else {
                        row.addFields(InteractionKeys.KEY_XREFS_B, otherXrefs);
                    }
                }

                // convert aliases
                if (!aliases.isEmpty()){
                    Collection<Field> aliasFields = new ArrayList<Field>(aliases.size());
                    for (Alias alias : aliases){
                        Field aliasField = aliasConverter.intactToCalimocho((AbstractIntactAlias) alias);

                        if (aliasField != null){
                            aliasFields.add(aliasField);
                        }
                    }
                    if (!aliasFields.isEmpty()){
                        if (isFirst){
                            row.addFields(InteractionKeys.KEY_ALIAS_A, aliasFields);
                        }
                        else {
                            row.addFields(InteractionKeys.KEY_ALIAS_B, aliasFields);
                        }
                    }
                }               

                // convert annotations at the level of interactor
                if (!annotations.isEmpty()){
                    Collection<Field> annotFields = new ArrayList<Field>(annotations.size());
                    for (Annotation annots : annotations){
                        Field annotField = annotationConverter.intactToCalimocho((AbstractIntactAnnotation) annots);

                        if (annotField != null){
                            annotFields.add(annotField);
                        }
                    }
                    if (!annotFields.isEmpty()){
                        if (isFirst){
                            row.addFields(InteractionKeys.KEY_ANNOTATIONS_A, annotFields);
                        }
                        else {
                            row.addFields(InteractionKeys.KEY_ANNOTATIONS_B, annotFields);
                        }
                    }
                }               

                // convert organism(s)
                if (interactor.getOrganism() != null){
                    Collection<Field> bioSourceField = bioSourceConverter.intactToCalimocho((IntactOrganism) interactor.getOrganism());

                    if (!bioSourceField.isEmpty()){
                        if (isFirst){
                            row.addFields(InteractionKeys.KEY_TAXID_A, bioSourceField);
                        }
                        else {
                            row.addFields(InteractionKeys.KEY_TAXID_B, bioSourceField);
                        }
                    }
                }
                
                // convert interactor type
                if (interactor.getInteractorType() != null){
                    Field type = cvObjectConverter.intactToCalimocho((IntactCvTerm) interactor.getInteractorType());
                    if (type != null){
                        if (isFirst){
                            row.addField(InteractionKeys.KEY_INTERACTOR_TYPE_A, type);
                        }
                        else {
                            row.addField(InteractionKeys.KEY_INTERACTOR_TYPE_B, type);
                        }
                    }
                }
                
                // convert checksum (crc64 : only if sequence available)
                if (interactor instanceof IntactPolymer){
                    IntactPolymer polymer = (IntactPolymer) interactor;
                    Checksum crc64 = ChecksumUtils.collectFirstChecksumWithMethod(polymer.getChecksums(), null, "crc64");
                    if (crc64 != null) {
                        Field crcField = new DefaultField();
                        crcField.set(CalimochoKeys.KEY, CRC64);
                        crcField.set(CalimochoKeys.DB, CRC64);
                        crcField.set(CalimochoKeys.VALUE, crc64.getValue());

                        if (isFirst){
                            row.addField(InteractionKeys.KEY_CHECKSUM_A, crcField);
                        }
                        else {
                            row.addField(InteractionKeys.KEY_CHECKSUM_B, crcField);
                        }
                    }
                }
            }
            
            // convert biological role
            Field bioRole = null;
            if (participant.getBiologicalRole() != null){
                bioRole = cvObjectConverter.intactToCalimocho((IntactCvTerm) participant.getBiologicalRole());
            }
            
            if (bioRole == null){
                bioRole = new DefaultField();

                bioRole.set(CalimochoKeys.KEY, CvTerm.PSI_MI);
                bioRole.set(CalimochoKeys.DB, CvTerm.PSI_MI);
                bioRole.set(CalimochoKeys.VALUE, Participant.UNSPECIFIED_ROLE_MI);
                bioRole.set(CalimochoKeys.TEXT, Participant.UNSPECIFIED_ROLE);
            }
            if (isFirst){
                row.addField(InteractionKeys.KEY_BIOROLE_A, bioRole);
            }
            else {
                row.addField(InteractionKeys.KEY_BIOROLE_B, bioRole);
            }

            // convert experimental role
            Field expRoleField = null;
            if (participant.getExperimentalRole() != null) {
                expRoleField = cvObjectConverter.intactToCalimocho((IntactCvTerm) participant.getExperimentalRole());
            } else {
                expRoleField = new DefaultField();
                expRoleField.set(CalimochoKeys.KEY, CvTerm.PSI_MI);
                expRoleField.set(CalimochoKeys.DB, CvTerm.PSI_MI);
                expRoleField.set(CalimochoKeys.VALUE, Participant.UNSPECIFIED_ROLE_MI);
                expRoleField.set(CalimochoKeys.TEXT, Participant.UNSPECIFIED_ROLE);
            }
            if (isFirst){
                row.addField(InteractionKeys.KEY_EXPROLE_A, expRoleField);
            }
            else {
                row.addField(InteractionKeys.KEY_EXPROLE_B, expRoleField);
            }

            // convert features
            if (!participant.getFeatures().isEmpty()){
                Collection<FeatureEvidence> features = participant.getFeatures();
                Collection<Field> featFields = new ArrayList<Field>(features.size());
                
                for (FeatureEvidence feature : features){
                     Field featureField = featureConverter.intactToCalimocho((IntactFeatureEvidence) feature);
                    if (featureField != null){
                        featFields.add(featureField);
                    }
                }
                
                if (!featFields.isEmpty()){
                    if (isFirst){
                        row.addFields(InteractionKeys.KEY_FEATURE_A, featFields);
                    }
                    else {
                        row.addFields(InteractionKeys.KEY_FEATURE_B, featFields);
                    }
                }
            }

            // convert stoichiometry
            if (participant.getStoichiometry() != null) {
                Field stoichiometry = new DefaultField();
                stoichiometry.set(CalimochoKeys.VALUE, Integer.toString(participant.getStoichiometry().getMinValue()));
                if (isFirst){
                    row.addField(InteractionKeys.KEY_STOICHIOMETRY_A, stoichiometry);
                }
                else {
                    row.addField(InteractionKeys.KEY_STOICHIOMETRY_B, stoichiometry);
                }
            }
            
            // convert participant identification methods
            if (!participant.getIdentificationMethods().isEmpty()){
                Collection<Field> detMethodFields = new ArrayList<Field>(participant.getIdentificationMethods().size());

                for (CvTerm detMethod : participant.getIdentificationMethods()){
                    Field methodField = cvObjectConverter.intactToCalimocho((IntactCvTerm) detMethod);
                    if (methodField != null){
                        detMethodFields.add(methodField);
                    }
                }

                if (!detMethodFields.isEmpty()){
                    if (isFirst){
                        row.addFields(InteractionKeys.KEY_PART_IDENT_METHOD_A, detMethodFields);
                    }
                    else {
                        row.addFields(InteractionKeys.KEY_PART_IDENT_METHOD_B, detMethodFields);
                    }
                }
            }

            // convert annotations at the level of participant
            if (!participant.getAnnotations().isEmpty()){
                Collection<Field> annotFields = isFirst ? row.getFields(InteractionKeys.KEY_ANNOTATIONS_A) : row.getFields(InteractionKeys.KEY_ANNOTATIONS_B);
                
                if (annotFields == null){
                   annotFields = new ArrayList<Field>(participant.getAnnotations().size());
                }

                for (Annotation annots : participant.getAnnotations()){
                    Field annotField = annotationConverter.intactToCalimocho((AbstractIntactAnnotation) annots);

                    if (annotField != null){
                        annotFields.add(annotField);
                    }
                }
                if (!annotFields.isEmpty()){
                    if (isFirst){
                        row.addFields(InteractionKeys.KEY_ANNOTATIONS_A, annotFields);
                    }
                    else {
                        row.addFields(InteractionKeys.KEY_ANNOTATIONS_B, annotFields);
                    }
                }
            }

            // convert xrefs at the level of participant
            if (!participant.getXrefs().isEmpty()){
                Collection<Field> xrefFields = isFirst ? row.getFields(InteractionKeys.KEY_XREFS_A) : row.getFields(InteractionKeys.KEY_XREFS_B);

                if (xrefFields == null){
                    xrefFields = new ArrayList<Field>(participant.getXrefs().size());
                }

                for (Xref xrefs : participant.getXrefs()){
                    Field xrefField = xrefConverter.intactToCalimocho((ParticipantEvidenceXref) xrefs, true);

                    if (xrefField != null){
                        xrefFields.add(xrefField);
                    }
                }
                if (!xrefFields.isEmpty()){
                    if (isFirst){
                        row.addFields(InteractionKeys.KEY_XREFS_A, xrefFields);
                    }
                    else {
                        row.addFields(InteractionKeys.KEY_XREFS_B, xrefFields);
                    }
                }
            }
        }        
    }
}
