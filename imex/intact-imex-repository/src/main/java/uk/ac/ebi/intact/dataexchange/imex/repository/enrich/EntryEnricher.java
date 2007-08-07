package uk.ac.ebi.intact.dataexchange.imex.repository.enrich;

import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;

import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface EntryEnricher
{
    void enrichEntry(RepoEntry entry) throws IOException;
}
