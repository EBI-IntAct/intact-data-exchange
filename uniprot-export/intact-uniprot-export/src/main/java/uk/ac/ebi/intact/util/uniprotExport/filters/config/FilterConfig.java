package uk.ac.ebi.intact.util.uniprotExport.filters.config;

/**
 * Configuration of the filters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public class FilterConfig {

    private boolean excludeSpokeExpandedInteractions = false;
    private boolean excludeLowConfidenceInteractions = true;
    private boolean excludeNegativeInteractions = true;
    private boolean excludeNonUniprotInteractors = true;

    public boolean excludeSpokeExpandedInteractions() {
        return excludeSpokeExpandedInteractions;
    }

    public void setExcludeSpokeExpandedInteractions(boolean excludeSpokeExpandedInteractions) {
        this.excludeSpokeExpandedInteractions = excludeSpokeExpandedInteractions;
    }

    public boolean excludeLowConfidenceInteractions() {
        return excludeLowConfidenceInteractions;
    }

    public void setExcludeLowConfidenceInteractions(boolean excludeLowConfidenceInteractions) {
        this.excludeLowConfidenceInteractions = excludeLowConfidenceInteractions;
    }

    public boolean excludeNegativeInteractions() {
        return excludeNegativeInteractions;
    }

    public void setExcludeNegativeInteractions(boolean excludeNegativeInteractions) {
        this.excludeNegativeInteractions = excludeNegativeInteractions;
    }

    public boolean excludeNonUniprotInteractors() {
        return excludeNonUniprotInteractors;
    }

    public void setExcludeNonUniprotInteractors(boolean excludeNonUniprotInteractors) {
        this.excludeNonUniprotInteractors = excludeNonUniprotInteractors;
    }
}
