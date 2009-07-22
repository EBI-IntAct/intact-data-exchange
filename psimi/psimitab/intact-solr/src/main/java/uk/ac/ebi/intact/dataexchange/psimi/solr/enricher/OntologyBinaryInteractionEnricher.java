/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.solr.enricher;

import org.apache.solr.client.solrj.SolrServerException;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.intact.bridges.ontologies.term.OntologyTerm;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;

import java.util.Collection;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyBinaryInteractionEnricher implements BinaryInteractionEnricher{

    private final OntologyFieldEnricher fieldEnricher;

    public OntologyBinaryInteractionEnricher(OntologySearcher ontologySearcher) {
        this.fieldEnricher = new OntologyFieldEnricher(ontologySearcher);
    }

    public void enrich(BinaryInteraction binaryInteraction) throws Exception {
        enrich(binaryInteraction.getInteractorA());
        enrich(binaryInteraction.getInteractorB());

        enrich(binaryInteraction.getInteractionTypes());
        enrich(binaryInteraction.getDetectionMethods());

        if (binaryInteraction instanceof IntactBinaryInteraction) {
            IntactBinaryInteraction ibi = (IntactBinaryInteraction) binaryInteraction;
            enrich(ibi.getHostOrganism());
        }
    }

    public void enrich(Interactor interactor) throws Exception {
        if (interactor.getOrganism() != null) {
            enrich(interactor.getOrganism().getIdentifiers());
        }

        if (interactor instanceof ExtendedInteractor) {
            ExtendedInteractor ei = (ExtendedInteractor) interactor;
            enrich(ei.getBiologicalRoles());
            enrich(ei.getExperimentalRoles());
            enrich(ei.getInteractorType());
            enrich(ei.getProperties());
        }
    }

    public void enrich(Collection<CrossReference> xrefs) throws SolrServerException {
        if (xrefs == null) {
            return;
        }
        
        for (CrossReference xref : xrefs) {
            enrich(xref);
        }
    }

    public void enrich(CrossReference xref) throws SolrServerException {

        if (fieldEnricher.isExpandableOntology(xref.getDatabase())) {
            String id = xref.getIdentifier();
            OntologyTerm ontologyTerm = fieldEnricher.findOntologyTerm(id, null);
            xref.setText(ontologyTerm.getName());
        }
    }

    public OntologyFieldEnricher getFieldEnricher() {
        return fieldEnricher;
    }
}
