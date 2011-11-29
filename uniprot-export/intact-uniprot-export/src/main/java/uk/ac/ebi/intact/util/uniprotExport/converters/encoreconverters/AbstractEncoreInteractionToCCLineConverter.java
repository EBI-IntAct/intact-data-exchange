package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyServiceException;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyTerm;
import uk.ac.ebi.intact.bridges.taxonomy.UniprotTaxonomyService;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract encore interaction to CC line converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public abstract class AbstractEncoreInteractionToCCLineConverter implements EncoreInteractionToCCLineConverter{

    private static final Logger logger = Logger.getLogger(AbstractEncoreInteractionToCCLineConverter.class);

    private Map<String, String> taxIdToScientificName = new HashMap<String, String>();

    private UniprotTaxonomyService taxonomyService;

    public AbstractEncoreInteractionToCCLineConverter(){
        taxonomyService = new UniprotTaxonomyService();
    }

    /**
     *
     * @param references : the organism cross references
     * @return a String [2] with the taxId of the organism and the organism name
     */
    protected String [] extractOrganismFrom(Collection<CrossReference> references){

        String taxId = "-";
        String organismName = "-";

        for (CrossReference ref : references){
            // look for the taxId cross reference and get the identifier (taxId) and the organism name (text of a cross reference)
            if (WriterUtils.TAXID.equalsIgnoreCase(ref.getDatabase())){
                taxId = ref.getIdentifier();
                if (ref.getText() != null){
                    organismName = ref.getText();
                }
            }
        }

        return new String [] {taxId, organismName};
    }

    /**
     *
     * @param taxId
     * @return The scientific organism name associated with this taxId, null if no scientific name is associated with this taxId
     * @throws uk.ac.ebi.intact.bridges.taxonomy.TaxonomyServiceException
     */
    protected String retrieveOrganismScientificName(String taxId) throws TaxonomyServiceException {
        if (this.taxIdToScientificName.containsKey(taxId)){
            return this.taxIdToScientificName.get(taxId);
        }

        TaxonomyTerm term = this.taxonomyService.getTaxonomyTerm(Integer.parseInt(taxId));

        if (term == null){
            this.taxIdToScientificName.put(taxId, null);
            return null;
        }

        String scientificName = term.getScientificName();
        this.taxIdToScientificName.put(taxId, scientificName);

        return scientificName;
    }

    protected boolean isFirstInteractor(String firstInteractor, String interactor,Set<IntactTransSplicedProteins> transSplicedProteins){

        if (interactor.startsWith(firstInteractor)){
           return true;
        }
        else if (transSplicedProteins != null){
            for (IntactTransSplicedProteins prot : transSplicedProteins){
                if (interactor.equalsIgnoreCase(prot.getUniprotAc())){
                    return true;
                }
            }
        }
        return false;
    }
}
