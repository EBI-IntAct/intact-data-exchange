package uk.ac.ebi.intact.util.uniprotExport.miscore.results;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Context of the cluster
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class MiClusterContext {

    private Map<String, String> geneNames = new HashMap<String, String>();
    private Map<String, String> miTerms = new HashMap<String, String>();
    private Map<String, MethodAndTypePair> interactionToType_Method = new HashMap<String, MethodAndTypePair>();
    private Set<String> spokeExpandedInteractions = new HashSet<String>();

    /**
     *
     * @return Map associating a uniprot ac to a gene name
     */
    public Map<String, String> getGeneNames() {
        return geneNames;
    }

    /**
     *
     * @return map associating MI number to a full name
     */
    public Map<String, String> getMiTerms() {
        return miTerms;
    }

    /**
     *
     * @return Map associating an Intact interaction AC to a couple {method, interaction type}
     */
    public Map<String, MethodAndTypePair> getInteractionToMethod_type() {
        return interactionToType_Method;
    }

    /**
     *
     * @return list of intact ACs which are spoke expanded
     */
    public Set<String> getSpokeExpandedInteractions() {
        return spokeExpandedInteractions;
    }

    public void clear(){
        this.getGeneNames().clear();
        this.getInteractionToMethod_type().clear();
        this.spokeExpandedInteractions.clear();
        this.getMiTerms().clear();
    }
}