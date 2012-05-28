package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import psidev.psi.mi.tab.utils.MitabEscapeUtils;
import uk.ac.ebi.intact.model.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Converts an interactor in a list of fields
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/05/12</pre>
 */

public class InteractorConverter {
    private CrossReferenceConverter xrefConverter;
    private AliasConverter aliasConverter;
    private AnnotationConverter annotationConverter;
    private BioSourceConverter bioSourceConverter;
    private CvObjectConverter cvObjectConverter;
    private FeatureConverter featureConverter;
    
    public static String CRC64 = "crc64";

    public InteractorConverter(){
        this.xrefConverter = new CrossReferenceConverter();
        this.aliasConverter = new AliasConverter();
        this.annotationConverter = new AnnotationConverter();
        this.bioSourceConverter = new BioSourceConverter();
        this.cvObjectConverter = new CvObjectConverter();
        this.featureConverter = new FeatureConverter();
    }

    public void toCalimocho(Component participant, Row row, boolean isFirst){

        if (participant != null){
            Interactor interactor = participant.getInteractor();

            if (interactor != null){
                Collection<InteractorXref> interactorXrefs = interactor.getXrefs();
                Collection<Annotation>  annotations = interactor.getAnnotations();
                Collection<InteractorAlias> aliases = interactor.getAliases();

                boolean hasFoundIdentity = false;

                Collection<Field> otherIdentifiers = new ArrayList<Field>(interactorXrefs.size());
                Collection<Field> otherXrefs = new ArrayList<Field>(interactorXrefs.size());
                
                // convert xrefs, and identity
                for (InteractorXref ref : interactorXrefs){

                    // identity xrefs
                    if (ref.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(ref.getCvXrefQualifier().getIdentifier())){
                        // first identity
                        if (!hasFoundIdentity){

                            Field identity = xrefConverter.toCalimocho(ref, false);
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
                            Field identity = xrefConverter.toCalimocho(ref, false);
                            if (identity != null){
                                otherIdentifiers.add(identity);
                            }
                        }
                    }
                    // other xrefs
                    else {
                        Field refField = xrefConverter.toCalimocho(ref, true);
                        if (refField != null){
                            otherXrefs.add(refField);
                        }
                    }
                }

                // convert ac as identity or secondary identifier
                if (interactor.getAc() != null){
                    Field acField = new DefaultField();

                    String db = CvDatabase.INTACT;

                    acField.set(CalimochoKeys.KEY, db);
                    acField.set(CalimochoKeys.DB, db);
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
                Collection<Field> aliasFields = new ArrayList<Field>(aliases.size());
                for (InteractorAlias alias : aliases){
                    Field aliasField = aliasConverter.toCalimocho(alias);

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

                // convert annotations
                Collection<Field> annotFields = new ArrayList<Field>(annotations.size());
                for (Annotation annots : annotations){
                    Field annotField = annotationConverter.toCalimocho(annots);

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

                // convert organism
                if (interactor.getBioSource() != null){
                    Collection<Field> bioSourceField = bioSourceConverter.toCalimocho(interactor.getBioSource());

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
                if (interactor.getCvInteractorType() != null){
                    Field type = cvObjectConverter.toCalimocho(interactor.getCvInteractorType());
                    if (type != null){
                        if (isFirst){
                            row.addField(InteractionKeys.KEY_INTERACTOR_TYPE_A, type);
                        }
                        else {
                            row.addField(InteractionKeys.KEY_INTERACTOR_TYPE_B, type);
                        }
                    }
                }
                
                // convert checksum
                if (interactor instanceof Polymer){
                    Polymer polymer = (Polymer) interactor;
                    if (polymer.getCrc64() != null){
                        Field crc64 = new DefaultField();
                        crc64.set(CalimochoKeys.KEY, CRC64);
                        crc64.set(CalimochoKeys.DB, CRC64);
                        crc64.set(CalimochoKeys.VALUE, polymer.getCrc64());

                        if (isFirst){
                            row.addField(InteractionKeys.KEY_CHECKSUM_A, crc64);
                        }
                        else {
                            row.addField(InteractionKeys.KEY_CHECKSUM_B, crc64);
                        }
                    }
                }
            }
            
            // convert biological role
            Field bioRole = null;
            if (participant.getCvBiologicalRole() != null){
                bioRole = cvObjectConverter.toCalimocho(participant.getCvBiologicalRole());
            }
            
            if (bioRole == null){
                bioRole = new DefaultField();

                bioRole.set(CalimochoKeys.KEY, CvDatabase.PSI_MI);
                bioRole.set(CalimochoKeys.DB, CvDatabase.PSI_MI);
                bioRole.set(CalimochoKeys.VALUE, MitabEscapeUtils.escapeFieldElement(CvBiologicalRole.UNSPECIFIED_PSI_REF));
                bioRole.set(CalimochoKeys.TEXT, CvBiologicalRole.UNSPECIFIED);
            }
            if (isFirst){
                row.addField(InteractionKeys.KEY_BIOROLE_A, bioRole);
            }
            else {
                row.addField(InteractionKeys.KEY_BIOROLE_B, bioRole);
            }

            // convert experimental roles
            Collection<Field> roleFields = new ArrayList<Field>(participant.getExperimentalRoles().size());
            
            for (CvExperimentalRole expRole : participant.getExperimentalRoles()){
                Field expRoleField = cvObjectConverter.toCalimocho(expRole);
                if (expRoleField != null){
                    roleFields.add(expRoleField);
                }
            }            

            if (roleFields.isEmpty()){
                Field expRoleField = new DefaultField();

                expRoleField.set(CalimochoKeys.KEY, CvDatabase.PSI_MI);
                expRoleField.set(CalimochoKeys.DB, CvDatabase.PSI_MI);
                expRoleField.set(CalimochoKeys.VALUE, MitabEscapeUtils.escapeFieldElement(CvExperimentalRole.UNSPECIFIED_PSI_REF));
                expRoleField.set(CalimochoKeys.TEXT, CvExperimentalRole.UNSPECIFIED);
                roleFields.add(expRoleField);
            }
            if (isFirst){
                row.addFields(InteractionKeys.KEY_EXPROLE_A, roleFields);
            }
            else {
                row.addFields(InteractionKeys.KEY_EXPROLE_B, roleFields);
            }

            // convert features
            if (!participant.getFeatures().isEmpty()){
                Collection<Feature> features = participant.getFeatures();
                Collection<Field> featFields = new ArrayList<Field>(features.size());
                
                for (Feature feature : features){
                     Field featureField = featureConverter.toCalimocho(feature);
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
            if (participant.hasStoichiometry()){
                Field stoichiometry = new DefaultField();
                stoichiometry.set(CalimochoKeys.VALUE, Float.toString(participant.getStoichiometry()));
                if (isFirst){
                    row.addField(InteractionKeys.KEY_STOICHIOMETRY_A, stoichiometry);
                }
                else {
                    row.addField(InteractionKeys.KEY_STOICHIOMETRY_B, stoichiometry);
                }
            }
        }        
    }
}
