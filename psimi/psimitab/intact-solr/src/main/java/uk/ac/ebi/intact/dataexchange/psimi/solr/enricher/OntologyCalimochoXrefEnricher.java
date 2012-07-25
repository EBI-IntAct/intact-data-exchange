package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

/**
 * This enricher will only enirch xrefs for interaction and interactors in a calimocho row
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/07/12</pre>
 */

public class OntologyCalimochoXrefEnricher extends OntologyCalimochoRowEnricher {

    public OntologyCalimochoXrefEnricher(OntologySearcher ontologySearcher) {
        super(ontologySearcher);
    }

    @Override
    public void enrich(Row binaryInteraction) throws Exception {
        // enrich interactors
        enrichInteractors(binaryInteraction);

        // enrich cvs of interaction
        enrich(binaryInteraction.getFields(InteractionKeys.KEY_XREFS_I));
    }

    @Override
    public void enrichInteractors(Row row) throws Exception {

        // enrich xrefs
        enrich(row.getFields(InteractionKeys.KEY_XREFS_A));
        enrich(row.getFields(InteractionKeys.KEY_XREFS_B));
    }
}
