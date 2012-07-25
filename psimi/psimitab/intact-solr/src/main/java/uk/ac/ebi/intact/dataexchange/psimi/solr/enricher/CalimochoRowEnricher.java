package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.hupo.psi.calimocho.model.Row;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/12</pre>
 */

public interface CalimochoRowEnricher {

    void enrich(Row binaryInteraction) throws Exception;
}
