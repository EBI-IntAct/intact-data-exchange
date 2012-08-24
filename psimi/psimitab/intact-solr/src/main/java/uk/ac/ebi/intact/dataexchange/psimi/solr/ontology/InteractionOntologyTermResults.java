package uk.ac.ebi.intact.dataexchange.psimi.solr.ontology;

/**
 * Wrapper containing database name, field name search name and interaction count
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/08/12</pre>
 */

public class InteractionOntologyTermResults {

    private String databaseLabel;
    private String searchField;
    private long count;

    public InteractionOntologyTermResults(String db, String searchField, long count){
        this.databaseLabel = db;
        this.searchField = searchField;
        this.count = count;
    }

    public String getDatabaseLabel() {
        return databaseLabel;
    }

    public String getSearchField() {
        return searchField;
    }

    public long getCount() {
        return count;
    }

    public void addToCount(long count) {
        this.count+=count;
    }
}
