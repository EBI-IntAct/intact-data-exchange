package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

/**
 * Default implementation of SecondCCParameters1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public class DefaultSecondCCParameters1 implements SecondCCParameters1{

    private String firstUniprotAc;
    private String secondUniprotAc;

    private String secondIntact;
    private String firstIntact;

    private String geneName;
    private String taxId;

    private int numberOfInteractionEvidences;

    public DefaultSecondCCParameters1(String firstUniprotAc, String firstIntactAc, String secondUniprotAc,
                                      String secondIntactAc, String geneName, String taxId, int numberInteractions){

        this.firstUniprotAc = firstUniprotAc;
        this.secondUniprotAc = secondUniprotAc;
        this.firstIntact = firstIntactAc;
        this.secondIntact = secondIntactAc;

        this.geneName = geneName;
        this.taxId = taxId;
        this.numberOfInteractionEvidences = numberInteractions;

    }

    @Override
    public String getFirstIntacAc() {
        return this.firstIntact;
    }

    @Override
    public String getFirstUniprotAc() {
        return this.firstUniprotAc;
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
    public int getNumberOfInteractionEvidences() {
        return this.numberOfInteractionEvidences;
    }

    public int compareTo( Object o ) {
        DefaultSecondCCParameters1 cc2 = null;
        cc2 = (DefaultSecondCCParameters1) o;

        final String gene1 = getGeneName();
        final String gene2 = cc2.getGeneName();

        final String firstUniprotAc1 = firstUniprotAc;
        final String firstUniprotAc2 = cc2.getFirstUniprotAc();

        // the current string comes first if it's before in the alphabetical order

        if( gene1 == null ) {
            System.out.println( this );
        }

        if( firstUniprotAc1.equals(secondUniprotAc) ) {

            // we put first the Self interaction
            return -1;

        } else if( firstUniprotAc2.equals(cc2.getSecondUniprotAc()) ) {

            return 1;

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

        if (firstUniprotAc != null ? !firstUniprotAc.equals(ccLine1.firstUniprotAc) : ccLine1.firstUniprotAc != null)
        {
            return false;
        }
        if (geneName != null ? !geneName.equals(ccLine1.geneName) : ccLine1.geneName != null)
        {
            return false;
        }
        if (secondUniprotAc != null ? !secondUniprotAc.equals(ccLine1.secondUniprotAc) : ccLine1.secondUniprotAc != null)
        {
            return false;
        }
        if (firstIntact != null ? !firstIntact.equals(ccLine1.firstIntact) : ccLine1.firstIntact != null)
        {
            return false;
        }
        if (secondIntact != null ? !secondIntact.equals(ccLine1.secondIntact) : ccLine1.secondIntact != null)
        {
            return false;
        }
        if (taxId != null ? !taxId.equals(ccLine1.taxId) : ccLine1.taxId != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (firstUniprotAc != null ? firstUniprotAc.hashCode() : 0);
        result = 31 * result + (geneName != null ? geneName.hashCode() : 0);
        result = 31 * result + (secondUniprotAc != null ? secondUniprotAc.hashCode() : 0);
        result = 31 * result + (firstIntact != null ? firstIntact.hashCode() : 0);
        result = 31 * result + (secondIntact != null ? secondIntact.hashCode() : 0);
        result = 31 * result + (taxId != null ? taxId.hashCode() : 0);
        return result;
    }
}
