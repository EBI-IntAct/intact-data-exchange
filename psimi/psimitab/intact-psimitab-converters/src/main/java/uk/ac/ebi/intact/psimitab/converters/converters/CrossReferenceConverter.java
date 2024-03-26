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
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.CrossReferenceImpl;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactXref;

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
public class CrossReferenceConverter<T extends AbstractIntactXref> {

    public static final Log logger = LogFactory.getLog( CrossReferenceConverter.class );
    public static String DATABASE_UNKNOWN = "unknown";

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

        List<CrossReference> crossReferences = new ArrayList<CrossReference>(xrefs.size());

        for (T xref : xrefs) {

                if (onlyIdentity) {
                    if (xref.getQualifier() != null && Xref.IDENTITY_MI.equals(xref.getQualifier().getMIIdentifier())) {
                        CrossReference ref = createCrossReference(xref, withText);
                        if (ref != null) crossReferences.add(ref);
                    }
                } else {
                    if (xref.getQualifier() == null || !Xref.IDENTITY_MI.equals(xref.getQualifier().getMIIdentifier())) {
                        CrossReference ref = createCrossReference(xref, withText);
                        if (ref != null) crossReferences.add(ref);
                    }
                }
        }
        return crossReferences;
    }

   /**
     * Converts a Collection of Xrefs into a suitable format for PSIMITAB filtering out only the particular CvDatabase
     *
     * @param xrefs        is a Collection of intact.model.Xref
     * @param onlyIdentity if is true only CrossReferences with CvXrefQualifier equals identiy will be returned
     *                     if is false all CrossReferences without CvXrefQualifier equals identiy will be returned
     * @param databaseFilterMiRef the MI id of the Database to be filtered for
     * @return Non null list of CrossReferences sorted by CvXrefQualifier
     */
    public List<CrossReference> toCrossReferences(Collection<T> xrefs, boolean onlyIdentity, boolean withText, String databaseFilterMiRef) {
        if (xrefs == null) {
            throw new IllegalArgumentException("Xref must not be null. ");
        }
        if (databaseFilterMiRef == null) {
            throw new NullPointerException("You must give a non null databaseFilterMiRef");
        }

        List<CrossReference> crossReferences = new ArrayList<CrossReference>(xrefs.size());

        for (T xref : xrefs) {

            if (xref.getDatabase() != null && databaseFilterMiRef.equals(xref.getDatabase().getMIIdentifier())) {
                if (onlyIdentity) {
                    if (xref.getQualifier() != null && Xref.IDENTITY_MI.equals(xref.getQualifier().getMIIdentifier())) {
                        CrossReference ref = createCrossReference(xref, withText);
                        if (ref != null) crossReferences.add(ref);
                    }
                } else {
                    if (xref.getQualifier() == null || !Xref.IDENTITY_MI.equals(xref.getQualifier().getMIIdentifier())) {
                        CrossReference ref = createCrossReference(xref, withText);
                        if (ref != null) crossReferences.add(ref);
                    }
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

        List<CrossReference> crossReferences = new ArrayList<CrossReference>(xrefs.size());

        for (T xref : xrefs) {
            CrossReference ref = createCrossReference(xref, withText);
            if (ref != null) crossReferences.add(ref);
        }
        return crossReferences;
    }

    public CrossReference createCrossReference(T xref, boolean withText) {
        CrossReference ref = null;
        String db = xref.getDatabase() != null ? xref.getDatabase().getShortName() : DATABASE_UNKNOWN;
        String id = xref.getId();

        if (id != null && db != null) {
            String secondaryId = (withText && xref.getSecondaryId() != null) ? xref.getSecondaryId() : null;
            String cvXrefQualifier = (withText && xref.getQualifier() != null) ? xref.getQualifier().getShortName() : null;

            if (secondaryId != null) {
                ref = new CrossReferenceImpl(db, id, secondaryId);
            } else if (cvXrefQualifier != null) {
                ref = new CrossReferenceImpl(db, id, cvXrefQualifier);
            } else {
                ref = new CrossReferenceImpl(db, id, null);
            }
        }
        return ref;
    }
}
