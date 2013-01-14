package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.Set;

/**
 * Contains the details of a binary interaction (interaction type, detection method, list of pubmeds for spoke expanded data, list of pubmeds for non spoke expanded data)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class InteractionDetailsImpl implements InteractionDetails{

    private String interactionType;
    private String detectionMethod;

    private boolean isSpokeExpanded;

    private Set<String> pubmedIds;

    public InteractionDetailsImpl(String interactionType, String detectionMethod, boolean isSpokeExpanded,
                                  Set<String> pubmedIds){


        if (detectionMethod == null){
            throw  new IllegalArgumentException("The InteractionDetailsImpl must contain a non null detection method.");
        }
        else if (pubmedIds == null){
            throw  new IllegalArgumentException("The InteractionDetailsImpl must contain a non null set of Pubmed Ids.");
        }
        else if (pubmedIds.isEmpty()){
            throw  new IllegalArgumentException("The InteractionDetailsImpl must contain a non empty set of Pubmed Ids.");

        }

        this.interactionType = interactionType;
        this.detectionMethod = detectionMethod;
        this.isSpokeExpanded = isSpokeExpanded;
        this.pubmedIds = pubmedIds;
    }
    @Override
    public int compareTo(InteractionDetailsImpl o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if ( this == o ) return EQUAL;

        int comparison = this.detectionMethod.compareTo(o.getDetectionMethod());
        if ( comparison != EQUAL ) {
            if (this.isSpokeExpanded == o.isSpokeExpanded()){
                return comparison;
            }
            else if (this.isSpokeExpanded && !o.isSpokeExpanded()){
                return BEFORE;
            }
            else {
                return AFTER;
            }
        }

        comparison = this.interactionType.compareTo(o.getInteractionType());
        if ( comparison != EQUAL ) {
            if (this.isSpokeExpanded == o.isSpokeExpanded()){
                return comparison;
            }
            else if (this.isSpokeExpanded && !o.isSpokeExpanded()){
                return BEFORE;
            }
            else {
                return AFTER;
            }
        }

        if (this.isSpokeExpanded == o.isSpokeExpanded()){
            return EQUAL;
        }
        else if (this.isSpokeExpanded && !o.isSpokeExpanded()){
            return BEFORE;
        }
        else {
            return AFTER;
        }
    }

    public String getInteractionType() {
        return interactionType;
    }

    public String getDetectionMethod() {
        return detectionMethod;
    }

    public boolean isSpokeExpanded() {
        return isSpokeExpanded;
    }

    public Set<String> getPubmedIds() {
        return pubmedIds;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        InteractionDetailsImpl details2 = (InteractionDetailsImpl) o;

        if (!getDetectionMethod().equals(details2.getDetectionMethod()))
        {
            return false;
        }
        if (!getInteractionType().equals(details2.getInteractionType()))
        {
            return false;
        }
        if (isSpokeExpanded() != details2.isSpokeExpanded())
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = detectionMethod.hashCode();
        result = 31 * result + interactionType.hashCode();
        result = 31 * result + Boolean.valueOf(isSpokeExpanded).hashCode();
        return result;
    }
}
