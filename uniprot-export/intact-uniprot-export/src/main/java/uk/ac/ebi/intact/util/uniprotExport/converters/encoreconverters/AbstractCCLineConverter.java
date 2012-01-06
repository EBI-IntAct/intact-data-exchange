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
 * Abstract class for CC line converters. A CC line converter can convert an Encore interaction to a CC parameter representing
 * the CC line.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public abstract class AbstractCCLineConverter implements CCLineConverter {

    private static final Logger logger = Logger.getLogger(AbstractCCLineConverter.class);

    /**
     * This map allows to cache information about organisms. The key is the taxId and the value is the scientific name
     */
    private Map<String, String> taxIdToScientificName;

    /**
     * The uniprot taxonomy service to retrieve scientific names using taxIds
     */
    private UniprotTaxonomyService taxonomyService;

    public AbstractCCLineConverter(){
        taxonomyService = new UniprotTaxonomyService();
        taxIdToScientificName = new HashMap<String, String>();
    }

    /**
     * This methods can extract taxIds and organism names from a CrossReference.
     * It returns a String [] of length 2 with :
     * - taxId found in the cross reference
     * - organism name found in cross reference
     *
     * If no taxId/organism name can be found, the taxId/organism name which is returned is '-'
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
     * This method will retrieve a scientific name given the taxId using the uniprot taxonomy service
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

    /**
     *
     * @param firstInteractor : uniprot ac of the master uniprot
     * @param interactor : uniprot ac of the interactor
     * @param transSplicedProteins : set of trans spliced proteins which can be associated with the master uniprot entry
     * @return true if this interactor is from the same uniprot entry as the master uniprot ac, false otherwise
     */
    protected boolean isFromSameUniprotEntry(String firstInteractor, String interactor, Set<IntactTransSplicedProteins> transSplicedProteins){

        // the interactor starts with master uniprot so we consider it as the first interactor
        if (interactor.startsWith(firstInteractor)){
           return true;
        }
        // if proteins from this uniprot entry are trans spliced variants
        else if (transSplicedProteins != null){
            for (IntactTransSplicedProteins prot : transSplicedProteins){
                // the interactor is a transpliced variant of this uniprot entry so we consider it as the first interactor
                if (interactor.equalsIgnoreCase(prot.getUniprotAc())){
                    return true;
                }
            }
        }
        return false;
    }
}
