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
import psidev.psi.mi.tab.model.CrossReferenceFactory;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * CrossReference Converter.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class CrossReferenceConverter<T extends Xref> {

    public static final Log logger = LogFactory.getLog( CrossReferenceConverter.class );

    /**
     * Converts a Collection of Xrefs into a suitable format for PSIMITAB
     *
     * @param xrefs        is a Collection of intact.model.Xref
     * @param onlyIdentity if is true only CrossReferences with CvXrefQualifier equals identiy will be returned
     *                     if is false all CrossReferences without CvXrefQualifier equals identiy will be returned
     * @return Non null list of CrossReferences sorted by CvXrefQualifier
     */
    public List<CrossReference> toCrossReferences(Collection<T> xrefs, boolean onlyIdentity, boolean withText) {
        if (xrefs == null) {
            throw new IllegalArgumentException("Xref must not be null. ");
        }

        List<CrossReference> crossReferences = new ArrayList<CrossReference>();

        for (Xref xref : xrefs) {

                if (onlyIdentity) {
                    if (xref.getCvXrefQualifier() != null && CvXrefQualifier.IDENTITY_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                        CrossReference ref = createCrossReference(xref, withText);
                        if (ref != null) crossReferences.add(ref);
                    }
                } else {
                    if (xref.getCvXrefQualifier() == null || !CvXrefQualifier.IDENTITY_MI_REF.equals(xref.getCvXrefQualifier().getIdentifier())) {
                        CrossReference ref = createCrossReference(xref, withText);
                        if (ref != null) crossReferences.add(ref);
                    }
                }
        }
        return crossReferences;
    }

    /**
     * Converts a Collection of Xrefs into a suitable format for PSIMITAB
     *
     * @param xrefs        is a Collection of intact.model.Xref
     * @return Non null list of CrossReferences sorted by CvXrefQualifier
     */
    public List<CrossReference> toCrossReferences(Collection<T> xrefs, boolean withText) {
        if (xrefs == null) {
            throw new IllegalArgumentException("Xref must not be null. ");
        }

        List<CrossReference> crossReferences = new ArrayList<CrossReference>();

        for (Xref xref : xrefs) {
            CrossReference ref = createCrossReference(xref, withText);
            if (ref != null) crossReferences.add(ref);
        }
        return crossReferences;
    }

    private CrossReference createCrossReference(Xref xref, boolean withText) {
        CrossReference ref = null;
        String db = xref.getCvDatabase().getShortLabel();
        String id = xref.getPrimaryId();

        if (id != null && db != null) {
            String secondaryId = (withText && xref.getSecondaryId() != null) ? xref.getSecondaryId() : null;
            String cvXrefQualifier = (withText && xref.getCvXrefQualifier() != null) ? xref.getCvXrefQualifier().getShortLabel() : null;

            if (secondaryId != null) {
                ref = CrossReferenceFactory.getInstance().build(db, id, secondaryId);
            } else if (cvXrefQualifier != null) {
                ref = CrossReferenceFactory.getInstance().build(db, id, cvXrefQualifier);
            } else {
                ref = CrossReferenceFactory.getInstance().build(db, id, null);
            }
        }
        return ref;
    }
}
