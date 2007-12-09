package uk.ac.ebi.intact.dataexchange.imex.repository;

import uk.ac.ebi.intact.dataexchange.imex.repository.dao.ImexServiceProvider;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RepositoryStatistics {

    private ImexRepositoryContext context;

    private Long entriesCount;
    private Long entrySetsCount;


    RepositoryStatistics(ImexRepositoryContext context) {
        this.context = context;
        refresh();
    }

    public void refresh() {
        final ImexServiceProvider provider = context.getImexServiceProvider();
        entrySetsCount = provider.getRepoEntrySetService().countAll();
        entriesCount = provider.getRepoEntryService().countAll();
    }

    public Long getEntriesCount() {
        return entriesCount;
    }

    public Long getEntrySetsCount() {
        return entrySetsCount;
    }
}
