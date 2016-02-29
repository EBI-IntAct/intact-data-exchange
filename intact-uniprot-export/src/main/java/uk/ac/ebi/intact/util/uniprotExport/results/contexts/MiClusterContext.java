package uk.ac.ebi.intact.util.uniprotExport.results.contexts;

import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;

import java.util.*;

/**
 * Context of the mi cluster
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class MiClusterContext implements ExportContext {

    private Map<String, String> geneNames = new HashMap<String, String>();
    private Map<String, String> miTerms = new HashMap<String, String>();
    private Map<String, MethodTypePair> interactionToType_Method = new HashMap<String, MethodTypePair>();
    private Set<String> spokeExpandedInteractions = new HashSet<String>();

    /**
     * The map of intact isoform proteins pointing to a parent with a different uniprot entry
     */
    private Map<String, Set<IntactTransSplicedProteins>> transcriptsWithDifferentParentAcs;

    /**
     * The map of interaction acs to GO component xrefs
     */
    private Map<String, Set<String>> interactionComponentXrefs;

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
    public Map<String, MethodTypePair> getInteractionToMethod_type() {
        return interactionToType_Method;
    }

    /**
     *
     * @return list of intact ACs which are spoke expanded
     */
    public Set<String> getSpokeExpandedInteractions() {
        return spokeExpandedInteractions;
    }

    @Override
    public Map<String, Set<String>> getInteractionComponentXrefs() {
        if (this.interactionComponentXrefs == null){
            this.interactionComponentXrefs = new HashMap<String, Set<String>>();
        }

        return interactionComponentXrefs;
    }

    public void setInteractionComponentXrefs(Map<String, Set<String>> interactionComponentXrefs) {
        this.interactionComponentXrefs = interactionComponentXrefs;
    }

    @Override
    public Map<String, Set<IntactTransSplicedProteins>> getTranscriptsWithDifferentMasterAcs() {
        if (this.transcriptsWithDifferentParentAcs == null){
            this.transcriptsWithDifferentParentAcs = new HashMap<String, Set<IntactTransSplicedProteins>>();
        }

        return this.transcriptsWithDifferentParentAcs;
    }

    public void setTranscriptsWithDifferentMasterAcs(Map<String, Set<IntactTransSplicedProteins>> mapOfIsoformsWithDifferentParents){
        this.transcriptsWithDifferentParentAcs = mapOfIsoformsWithDifferentParents;
    }



    public void clear(){
        this.getGeneNames().clear();
        this.getInteractionToMethod_type().clear();
        this.spokeExpandedInteractions.clear();
        this.getMiTerms().clear();
        this.interactionComponentXrefs.clear();
    }
}
