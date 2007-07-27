/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.persister;

import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntrySetTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntryTag;
import uk.ac.ebi.intact.business.IntactException;

import java.util.Collection;
import java.util.Iterator;

/**
 * That class make the data persitent in the Intact database. <br> That class takes care of a set of experiments. <br>
 * It assumes that the data are already parsed and passed the validity check successfully.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class EntrySetPersister {

    public static void persist( final EntrySetTag entrySet )
            throws IntactException {

        final Collection entries = entrySet.getEntries();
        for ( Iterator iterator = entries.iterator(); iterator.hasNext(); ) {
            final EntryTag entry = (EntryTag) iterator.next();
            EntryPersister.persist( entry );
        }
    }
}
