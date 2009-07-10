/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntrySetTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntryTag;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;
import uk.ac.ebi.intact.util.protein.UpdateProteinsI;

import java.util.Collection;
import java.util.Iterator;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class EntrySetChecker {

    public static void check( final EntrySetTag entrySet,
                              final UpdateProteinsI proteinFactory,
                              final BioSourceFactory bioSourceFactory ) {

        final Collection entries = entrySet.getEntries();
        for ( Iterator iterator = entries.iterator(); iterator.hasNext(); ) {
            final EntryTag entry = (EntryTag) iterator.next();
            EntryChecker.check( entry, proteinFactory, bioSourceFactory );
        }
    }
}
