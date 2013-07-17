package uk.ac.ebi.intact.dataexchange;

/**
 * A sentence property contains a singular and plural verb, and an optional conjunction
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public class SentenceProperty {

    private String singularVerb;
    private String pluralVerb;
    private String conjunction;

    public SentenceProperty(String singularVerb, String pluralVerb, String conjunction) {
        this.singularVerb = singularVerb != null ? singularVerb : "";
        this.pluralVerb = pluralVerb != null ? pluralVerb : "";
        this.conjunction = conjunction != null ? conjunction : "";
    }

    public String getSingularVerb() {
        return singularVerb;
    }

    public String getPluralVerb() {
        return pluralVerb;
    }

    public String getConjunction() {
        return conjunction;
    }
}
