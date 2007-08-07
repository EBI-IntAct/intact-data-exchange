package uk.ac.ebi.intact.dataexchange.imex.repository.split;

import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntrySet;

import java.io.IOException;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface EntrySetSplitter
{
    List<RepoEntry> splitRepoEntrySet(RepoEntrySet repoEntrySet) throws IOException;
}
