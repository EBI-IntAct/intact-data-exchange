package uk.ac.ebi.intact.util.uniprotExport.results.contexts;

import uk.ac.ebi.intact.util.uniprotExport.results.MethodAndTypePair;

import java.util.Map;
import java.util.Set;

/**
 * Interface to implement for the context of the export. Must contain :
 * - gene names associated with the uniprot ac
 * - MI terms associated with the full name for both detection methods and interaction types
 * - The interaction acs of spoke expanded binary interactions
 * - the detection method/interaction type associated with each interaction ac
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public interface ExportContext {

       /**
     *
     * @return Map associating a uniprot ac to a gene name
     */
    public Map<String, String> getGeneNames();

    /**
     *
     * @return map associating MI number to a full name
     */
    public Map<String, String> getMiTerms();

    /**
     *
     * @return Map associating an Intact interaction AC to a couple {method, interaction type}
     */
    public Map<String, MethodAndTypePair> getInteractionToMethod_type();

    /**
     *
     * @return list of intact ACs which are spoke expanded
     */
    public Set<String> getSpokeExpandedInteractions();

    /**
     * Clear the context
     */
    public void clear();
}
