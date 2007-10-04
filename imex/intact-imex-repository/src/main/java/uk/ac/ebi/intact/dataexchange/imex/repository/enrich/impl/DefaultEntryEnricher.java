package uk.ac.ebi.intact.dataexchange.imex.repository.enrich.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.imex.repository.ImexRepositoryContext;
import uk.ac.ebi.intact.dataexchange.imex.repository.Repository;
import uk.ac.ebi.intact.dataexchange.imex.repository.RepositoryHelper;
import uk.ac.ebi.intact.dataexchange.imex.repository.enrich.EntryEnricher;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.util.RepoEntryUtils;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.enricher.PsiEnricher;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;

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

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(DefaultEntryEnricher.class);

    public void enrichEntry(RepoEntry repoEntry) throws IOException
    {
        if (repoEntry == null) {
            throw new NullPointerException("Trying to enrich a null RepoEntry?");
        }

        if (repoEntry.isEnriched()) {
            throw new IllegalStateException("Entry is already enriched: "+repoEntry.getPmid());
        }

        if (!repoEntry.isValid()) {
            if (log.isWarnEnabled()) log.warn("Entry not enriched because is not valid: "+repoEntry.getPmid());
            return;
        }

        EnricherContext.getInstance().getConfig().setUpdateInteractionShortLabels(true);

        Repository repository = ImexRepositoryContext.getInstance().getRepository();
        RepositoryHelper helper = new RepositoryHelper(repository);

        File entryBeforeEnrichFile = helper.getEntryFile(repoEntry.getPmid(), repoEntry.getRepoEntrySet().getName(), false);
        File entryAfterEnrichFile = helper.getEntryFile(repoEntry.getPmid(), repoEntry.getRepoEntrySet().getName(), true);

        try {
            PsiEnricher.enrichPsiXml(entryBeforeEnrichFile, entryAfterEnrichFile, new DefaultEnricherConfig());

            repoEntry.setEnriched(true);

        } catch (PsiConversionException e) {
            final String errorMessage = "Conversion problem during enrichment";

            if (log.isErrorEnabled()) log.error(errorMessage, e);
            RepoEntryUtils.failEntry(repoEntry, errorMessage, e);
            
        } catch (Throwable e) {
            final String errorMessage = "Unexpected exception during enrichment";

            if (log.isErrorEnabled()) log.error(errorMessage, e);
            RepoEntryUtils.failEntry(repoEntry, errorMessage, e);
        }
    }
}
