package uk.ac.ebi.intact.dataexchange.cvutils.model;

/**
 * Ontology config for internal IntAct ontology terms
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/02/12</pre>
 */

public class IntactOntologyConfig implements ExternalOntologyConfig{

    private static final String INTACT_ALIAS_IDENTIFIER = "INTACT-alternate";
    private static final String INTACT_SHORTLABEL_IDENTIFIER = "INTACT-short";
    private static final String INTACT_NAMESPACE = "INTACT";
    private static final String INTACT_ALIAS_DEF = "Alternate label curated by INTACT";
    private static final String INTACT_SHORTLABEL_DEF = "Unique short label curated by INTACT";

    @Override
    public String getDefaultNamespace() {
        return INTACT_NAMESPACE;
    }

    @Override
    public String getShortLabelSynonymCategory() {
        return INTACT_SHORTLABEL_IDENTIFIER;
    }

    @Override
    public String getAliasSynonymCategory() {
        return INTACT_ALIAS_IDENTIFIER;
    }

    @Override
    public String getAliasSynonymDefinition() {
        return INTACT_ALIAS_DEF;
    }

    @Override
    public String getShortLabelSynonymDefinition() {
        return INTACT_SHORTLABEL_DEF;
    }
}
