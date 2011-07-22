package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Default implementation of the SecondCCinteractor2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public class DefaultSecondCCParameters2 implements SecondCCParameters2 {

    // duplicated line because of splice variants and feature chains!!!
    private String firstUniprotAc;
    private String secondUniprotAc;

    private String secondIntact;
    private String firstIntact;

    private String geneName;
    private String taxId;
    private String organismName;

    private boolean doesInteract;

    private SortedSet<InteractionDetails> interactionDetails;

    public DefaultSecondCCParameters2(String firstInteractor, String firstIntactAc, String secondInteractor, String secondIntactAc, String secondGeneName,
                                      String secondTaxId, String secondOrganismName,
                                      SortedSet<InteractionDetails> interactionDetails, boolean doesInteract){

        this.firstUniprotAc = firstInteractor;
        this.secondUniprotAc = secondInteractor;
        this.firstIntact = firstIntactAc;
        this.secondIntact = secondIntactAc;

        this.geneName = secondGeneName;
        this.organismName = secondOrganismName;
        this.taxId = secondTaxId;

        if (interactionDetails == null){
            this.interactionDetails = new TreeSet<InteractionDetails>();
        }
        else {
            this.interactionDetails = interactionDetails;
        }

        this.doesInteract = doesInteract;
    }

    @Override
    public String getSecondUniprotAc() {
        return this.secondUniprotAc;
    }

    @Override
    public String getSecondIntactAc() {
        return this.secondIntact;
    }

    @Override
    public String getGeneName() {
        return this.geneName;
    }

    @Override
    public String getTaxId() {
        return this.taxId;
    }

    @Override
    public String getOrganismName() {
        return this.organismName;
    }

    @Override
    public SortedSet<InteractionDetails> getInteractionDetails() {
        return this.interactionDetails;
    }

    @Override
    public boolean doesInteract() {
        return this.doesInteract;
    }

    @Override
    public String getFirstIntacAc() {
        return this.firstIntact;
    }

    @Override
    public String getFirstUniprotAc() {
        return this.firstUniprotAc;
    }

    public int compareTo( Object o ) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        DefaultSecondCCParameters2 cc2 = null;
        cc2 = (DefaultSecondCCParameters2) o;

        final String gene1 = getGeneName();
        final String gene2 = cc2.getGeneName();

        final String firstUniprotAc1 = firstUniprotAc;
        final String firstUniprotAc2 = cc2.getFirstUniprotAc();
        // the current string comes first if it's before in the alphabetical order

        if (doesInteract() && !cc2.doesInteract()){
            return BEFORE;
        }
        else if (cc2.doesInteract() && !doesInteract()){
            return AFTER;
        }
        else if( firstUniprotAc1.equals(secondUniprotAc) && !firstUniprotAc2.equals(cc2.getSecondUniprotAc())) {

            // we put first the Self interaction
            return BEFORE;

        }
        else if( firstUniprotAc2.equals(cc2.getSecondUniprotAc()) && !firstUniprotAc1.equals(secondUniprotAc)) {

            return AFTER;

        } else {

            String lovercaseGene1 = gene1.toLowerCase();
            String lovercaseGene2 = gene2.toLowerCase();

            // TODO ask Elizabeth if we still need to do the upper AND lowercase check for gene-name

            int score = lovercaseGene1.compareTo( lovercaseGene2 );

            if( score == 0 ) {
                score = gene1.compareTo( gene2 );

                if( score == 0 ) {
                    // gene names are the same, then compare the uniprotID
                    String uniprotID1 = getSecondUniprotAc();
                    String uniprotID2 = cc2.getSecondUniprotAc();

                    if( uniprotID1 != null && uniprotID2 != null ) {
                        score = uniprotID1.compareTo( uniprotID2 );
                    }
                }
            }

            return score;
        }
    }


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

        DefaultSecondCCParameters1 ccLine1 = (DefaultSecondCCParameters1) o;

        if (geneName != null ? !geneName.equals(ccLine1.getGeneName()) : ccLine1.getGeneName() != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (geneName != null ? geneName.hashCode() : 0);
        return result;
    }
}
