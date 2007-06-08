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
package uk.ac.ebi.intact.psixml.converter.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.xml.model.DbReference;
import psidev.psi.mi.xml.model.Names;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.psixml.converter.shared.XrefConverter;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactConverterUtils {

    private static final Log log = LogFactory.getLog(IntactConverterUtils.class);

    private static final int SHORT_LABEL_LENGTH = 20;

    private IntactConverterUtils() {
    }

    public static void populateNames(Names names, AnnotatedObject<?, ?> annotatedObject) {
        String shortLabel = getShortLabelFromNames(names);
        annotatedObject.setShortLabel(shortLabel);

        if (names != null) {
            annotatedObject.setFullName(names.getFullName());
        }
    }

    public static <X extends Xref> void populateXref(psidev.psi.mi.xml.model.Xref psiXref, AnnotatedObject<X, ?> annotatedObject, XrefConverter<X> xrefConverter) {
        if (psiXref == null) {
            return;
        }

        if (psiXref.getPrimaryRef() != null) {
            addXref(psiXref.getPrimaryRef(), annotatedObject, xrefConverter);
        }

        for (DbReference secondaryRef : psiXref.getSecondaryRef()) {
            addXref(secondaryRef, annotatedObject, xrefConverter);
        }
    }

    private static <X extends Xref> void addXref(DbReference dbReference, AnnotatedObject<X, ?> annotatedObject, XrefConverter<X> xrefConverter) {
        X xref = xrefConverter.psiToIntact(dbReference);
        annotatedObject.addXref(xref);
    }

    public static CvXrefQualifier createCvXrefQualifier(Institution institution, DbReference dbReference) {
        String xrefType = dbReference.getRefType();
        CvXrefQualifier xrefQual = null;

        if (xrefType != null) {
            xrefQual = new CvXrefQualifier(institution, xrefType);
        }

        return xrefQual;
    }

    public static String getShortLabelFromNames(Names names) {
        if (names == null) {
            return IntactConverterUtils.createTempShortLabel();
        }

        String shortLabel = names.getShortLabel();
        String fullName = names.getFullName();

        // If the short label is null, but not the full name, use the full name as short label.
        // Truncate the full name if its length > SHORT_LABEL_LENGTH
        if (shortLabel == null) {
            if (fullName != null) {
                if (log.isWarnEnabled()) log.warn("Short label is null. Using full name as short label: " + fullName);
                shortLabel = fullName;

            } else {
                throw new NullPointerException("Both fullName and shortLabel are null");
            }
        }

        if (shortLabel.length() > SHORT_LABEL_LENGTH) {
            shortLabel = fullName.substring(0, SHORT_LABEL_LENGTH);
            if (log.isWarnEnabled()) log.warn("\tFull name to short label truncated: " + shortLabel);
        }

        return shortLabel;
    }

    public static String createTempShortLabel() {
        return "ns-" + System.currentTimeMillis();
    }


}