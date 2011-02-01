package uk.ac.ebi.intact.util.uniprotExport.miscore.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, Map.Entry<String,String>> interactionToType_Method = new HashMap<String, Map.Entry<String,String>>();;
    private List<String> spokeExpandedInteractions = new ArrayList<String>();

    public Map<String, String> getGeneNames() {
        return geneNames;
    }

    public Map<String, String> getMiTerms() {
        return miTerms;
    }

    public Map<String, Map.Entry<String, String>> getInteractionToType_Method() {
        return interactionToType_Method;
    }

    public List<String> getSpokeExpandedInteractions() {
        return spokeExpandedInteractions;
    }

    public void clear(){
        this.getGeneNames().clear();
        this.getInteractionToType_Method().clear();
        this.spokeExpandedInteractions.clear();
        this.getMiTerms().clear();
    }
}
