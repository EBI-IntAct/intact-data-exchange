package uk.ac.ebi.intact.util.uniprotExport.results.contexts;

import uk.ac.ebi.intact.util.uniprotExport.results.MethodAndTypePair;

import java.util.Map;
import java.util.Set;

/**
 * Interface to implement for the context of the export
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

    public void clear();
}
