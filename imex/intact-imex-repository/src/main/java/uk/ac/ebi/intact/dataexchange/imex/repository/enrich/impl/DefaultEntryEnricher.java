package uk.ac.ebi.intact.dataexchange.imex.repository.enrich.impl;

import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.enrich.EntryEnricher;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.psimi.xml.enricher.PsiEnricher;

import java.io.File;
import java.io.IOException;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DefaultEntryEnricher implements EntryEnricher
{
    public void enrichEntry(RepoEntry entry) throws IOException
    {
        if (entry.isEnriched()) {
            throw new IllegalStateException("Entry is already enriched: "+entry.getName());
        }

        Repository repository = ImexRepositoryContext.getInstance().getRepository();
        RepositoryHelper helper = new RepositoryHelper(repository);

        File entryBeforeEnrichFile = helper.getEntryFile(entry.getName(), false);
        File entryAfterEnrichFile = helper.getEntryFile(entry.getName(), true);

        PsiEnricher.enrichPsiXml(entryBeforeEnrichFile, entryAfterEnrichFile, new DefaultEnricherConfig());

        entry.setEnriched(true);
    }
}
