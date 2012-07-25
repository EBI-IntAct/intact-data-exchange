package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.apache.solr.client.solrj.SolrServerException;
import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.key.InteractionKeys;
import org.hupo.psi.calimocho.model.Field;
import org.hupo.psi.calimocho.model.Row;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

import java.util.Collection;

/**
 * The default implementation to enrich calimocho row .
 *
 * Only retrieves names from ontology id and add it in the text.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23/07/12</pre>
 */

public class OntologyCalimochoRowEnricher implements CalimochoRowEnricher{
    protected final OntologyFieldEnricher fieldEnricher;

    public OntologyCalimochoRowEnricher(OntologySearcher ontologySearcher) {
        this.fieldEnricher = new OntologyFieldEnricher(ontologySearcher);
    }

    public void enrich(Row binaryInteraction) throws Exception {
        // enrich interactors
        enrichInteractors(binaryInteraction);

        // enrich cvs of interaction
        enrich(binaryInteraction.getFields(InteractionKeys.KEY_DETMETHOD));
        enrich(binaryInteraction.getFields(InteractionKeys.KEY_INTERACTION_TYPE));
        enrich(binaryInteraction.getFields(InteractionKeys.KEY_SOURCE));
        enrich(binaryInteraction.getFields(InteractionKeys.KEY_EXPANSION));

        // enrich xrefs of interaction
        enrich(binaryInteraction.getFields(InteractionKeys.KEY_XREFS_I));

        // enrich organism of interaction
        enrich(binaryInteraction.getFields(InteractionKeys.KEY_HOST_ORGANISM));
    }

    public void enrichInteractors(Row row) throws Exception {

        // enrich organisms
        enrich(row.getFields(InteractionKeys.KEY_TAXID_A));
        enrich(row.getFields(InteractionKeys.KEY_TAXID_B));

        // enrich biological role
        enrich(row.getFields(InteractionKeys.KEY_BIOROLE_A));
        enrich(row.getFields(InteractionKeys.KEY_BIOROLE_B));

        // enrich experimental roles
        enrich(row.getFields(InteractionKeys.KEY_EXPROLE_A));
        enrich(row.getFields(InteractionKeys.KEY_EXPROLE_B));

        // enrich interactor types
        enrich(row.getFields(InteractionKeys.KEY_INTERACTOR_TYPE_A));
        enrich(row.getFields(InteractionKeys.KEY_INTERACTOR_TYPE_B));

        // enrich xrefs
        enrich(row.getFields(InteractionKeys.KEY_XREFS_A));
        enrich(row.getFields(InteractionKeys.KEY_XREFS_B));

        // enrich participant detection methods
        enrich(row.getFields(InteractionKeys.KEY_PART_IDENT_METHOD_A));
        enrich(row.getFields(InteractionKeys.KEY_PART_IDENT_METHOD_B));
    }

    public void enrich(Collection<Field> fields) throws SolrServerException {
        if (fields == null) {
            return;
        }

        for (Field xref : fields) {
            enrich(xref);
        }
    }

    public void enrich(Field field) throws SolrServerException {
        String db = field.get(CalimochoKeys.DB);

        if (fieldEnricher.isExpandableOntology(db)) {
            String id = field.get(CalimochoKeys.VALUE);
            OntologyTerm ontologyTerm = fieldEnricher.findOntologyTerm(id, null);
            field.set(CalimochoKeys.TEXT, ontologyTerm.getName());
        }
    }

    public OntologyFieldEnricher getFieldEnricher() {
        return fieldEnricher;
    }
}
