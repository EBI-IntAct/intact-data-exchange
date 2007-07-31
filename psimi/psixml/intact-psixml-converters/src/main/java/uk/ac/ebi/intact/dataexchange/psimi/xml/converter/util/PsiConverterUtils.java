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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import psidev.psi.mi.xml.model.*;
import psidev.psi.mi.xml.model.Xref;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.AbstractCvConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.XrefConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.AnnotationConverter;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;
import java.util.HashSet;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiConverterUtils {

    private static final String UNSPEFICIED_ROLE = "unspeficied role";

    private PsiConverterUtils() {
    }

    public static void populate(AnnotatedObject<?, ?> annotatedObject, Object objectToPopulate) {
        if (objectToPopulate instanceof HasId) {
            populateId((HasId) objectToPopulate);
        }

        if (objectToPopulate instanceof NamesContainer) {
            populateNames(annotatedObject, (NamesContainer) objectToPopulate);
        }

        if (objectToPopulate instanceof XrefContainer) {
            populateXref(annotatedObject, (XrefContainer) objectToPopulate);
        }

        if (objectToPopulate instanceof AttributeContainer) {
            populateAttributes(annotatedObject, (AttributeContainer) objectToPopulate);
        }
    }

    private static void populateNames(AnnotatedObject<?, ?> annotatedObject, NamesContainer namesContainer) {
        Names names = namesContainer.getNames();

        if (names == null) {
            names = new Names();
        }

        String shortLabel = annotatedObject.getShortLabel();
        String fullName = annotatedObject.getFullName();

        names.setShortLabel(shortLabel);
        names.setFullName(fullName);

        namesContainer.setNames(names);
    }

    private static void populateXref(AnnotatedObject<?, ?> annotatedObject, XrefContainer xrefContainer) {
        if (annotatedObject.getXrefs().isEmpty()) {
            return;
        }

        Xref xref = xrefContainer.getXref();

        if (xref == null) {
            xref = new Xref();
        }

        Collection<DbReference> dbRefs = toDbReferences(annotatedObject.getXrefs());

        // normally the primary reference is the identity reference, but for bibliographic references
        // it is the primary-reference and it does not contain secondary refs
        if (xrefContainer instanceof Bibref) {
            DbReference primaryRef = getPrimaryReference(dbRefs);
            xref.setPrimaryRef(primaryRef);
        } else {
            DbReference primaryRef = getIdentity(dbRefs);
            xref.setPrimaryRef(primaryRef);

            // remove the primary ref and the bibref (primary-ref) in case of being an experiment
            // from the collection and add the rest as secondary refs
            dbRefs.remove(primaryRef);

            if (annotatedObject instanceof Experiment) {
                dbRefs.remove(getPrimaryReference(dbRefs));
            }

            for (DbReference secDbRef : dbRefs) {
                if (!xref.getSecondaryRef().contains(secDbRef)) {
                    xref.getSecondaryRef().add(secDbRef);
                }
            }
        }


        xrefContainer.setXref(xref);
    }

    private static int populateId(HasId hasIdElement) {
        if (hasIdElement.getId() > 0) {
            return hasIdElement.getId();
        }
        
        int id = IdSequenceGenerator.getInstance().nextId();
        hasIdElement.setId(id);

        return id;
    }

    private static void populateAttributes(AnnotatedObject<?, ?> annotatedObject, AttributeContainer attributeContainer) {
        AnnotationConverter annotationConverter = new AnnotationConverter(annotatedObject.getOwner());

        for (Annotation annotation : annotatedObject.getAnnotations()) {
            Attribute attribute = annotationConverter.intactToPsi(annotation);
            attributeContainer.getAttributes().add(attribute);
        }
    }

    public static CvType toCvType(CvObject cvObject, AbstractCvConverter converter) {
        if (cvObject == null) {
            throw new NullPointerException("cvObject");
        }

        CvType cvType = converter.intactToPsi(cvObject);
        populate(cvObject, cvType);

        return cvType;
    }

    public static BiologicalRole createUnspecifiedBiologicalRole() {
        return createUnspecifiedRole(BiologicalRole.class);
    }

    public static ExperimentalRole createUnspecifiedExperimentalRole() {
        return createUnspecifiedRole(ExperimentalRole.class);
    }

    private static <T extends CvType> T createUnspecifiedRole(Class<T> roleClass) {
        T role;

        try {
            role = roleClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Names names = new Names();
        names.setShortLabel(UNSPEFICIED_ROLE);
        names.setFullName(UNSPEFICIED_ROLE);

        role.setNames(names);

        Xref xref = new Xref();

        String unspecifiedRoleMiRef = "MI:0499";

        DbReference dbRef = new DbReference(unspecifiedRoleMiRef, CvDatabase.PSI_MI);
        dbRef.setRefType(CvXrefQualifier.IDENTITY);
        dbRef.setRefTypeAc(CvXrefQualifier.IDENTITY_MI_REF);
        dbRef.setDbAc(CvDatabase.PSI_MI_MI_REF);

        xref.setPrimaryRef(dbRef);
        role.setXref(xref);

        return role;
    }

    private static Collection<DbReference> toDbReferences(Collection<? extends uk.ac.ebi.intact.model.Xref> intactXrefs) {
        Collection<DbReference> dbRefs = new HashSet<DbReference>(intactXrefs.size());

        for (uk.ac.ebi.intact.model.Xref intactXref : intactXrefs) {
            XrefConverter xrefConverter = new XrefConverter(null, intactXref.getClass());

            DbReference dbRef = xrefConverter.intactToPsi(intactXref);
            dbRefs.add(dbRef);
        }

        return dbRefs;
    }

    /**
     * The primary ref is the one that contains the qualifier 'identity', choosing the one with uniprot accession
     * if there is more than one "identities"
     * @param dbRefs
     * @return
     */
    protected static DbReference getIdentity(Collection<DbReference> dbRefs) {
        Collection<DbReference> identityRefs = new HashSet<DbReference>();

        for (DbReference dbRef : dbRefs) {
            if (dbRef.getRefTypeAc() != null && dbRef.getRefTypeAc().equals(CvXrefQualifier.IDENTITY_MI_REF)) {

                if (dbRef.getDbAc() != null && dbRef.getDbAc().equals(CvDatabase.PSI_MI_MI_REF)) {
                    return dbRef;
                }

                identityRefs.add(dbRef);
            }
        }

        if (!identityRefs.isEmpty()) {
            // return the one for uniprot, if present. Otherwise return a random one.
            for (DbReference dbRef : identityRefs) {
                if (dbRef.getDbAc().equals(CvDatabase.UNIPROT_MI_REF)) {

                    return dbRef;
                }
            }

             return identityRefs.iterator().next();
        }

        if (!dbRefs.isEmpty()) {
            return dbRefs.iterator().next();
        }

        return null;
    }

    private static DbReference getPrimaryReference(Collection<DbReference> dbRefs) {
        for (DbReference dbRef : dbRefs) {
            if (dbRef.getRefTypeAc() != null && dbRef.getRefTypeAc().equals(CvXrefQualifier.PRIMARY_REFERENCE_MI_REF)) {
                return dbRef;
            }
        }

        if (!dbRefs.isEmpty()) {
            return dbRefs.iterator().next();
        }

        return null;
    }

}