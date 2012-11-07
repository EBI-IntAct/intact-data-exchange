package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;

/**
 * Enricher for interactor and interaction xrefs
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>24/07/12</pre>
 */

public class OntologyCrossReferenceEnricher extends OntologyBinaryInteractionEnricher{

    public OntologyCrossReferenceEnricher(OntologySearcher ontologySearcher) {
        super(ontologySearcher);
    }

    @Override
    public void enrich(BinaryInteraction binaryInteraction) throws Exception {
        // enrich interactors
        enrich(binaryInteraction.getInteractorA());
        enrich(binaryInteraction.getInteractorB());

        // enrich xrefs of interaction
        enrich(binaryInteraction.getXrefs());
    }

    @Override
    public void enrich(Interactor interactor) throws Exception {

        // enrich xrefs
        if (interactor != null && interactor.getXrefs()!= null) {
            enrich(interactor.getXrefs());
        }
    }
}
