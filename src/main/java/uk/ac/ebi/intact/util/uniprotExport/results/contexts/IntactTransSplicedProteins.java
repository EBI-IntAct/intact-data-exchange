package uk.ac.ebi.intact.util.uniprotExport.results.contexts;

/**
 * This class contains intact ac of the isoform in intact and the uniprot ac of the isoform
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/07/11</pre>
 */

public class IntactTransSplicedProteins {

    private String intactAc;
    private String uniprotAc;

    public IntactTransSplicedProteins(String intactAc, String uniprotAc){
        this.intactAc = intactAc;
        this.uniprotAc = uniprotAc;
    }

    public String getIntactAc() {
        return intactAc;
    }

    public String getUniprotAc() {
        return uniprotAc;
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

        IntactTransSplicedProteins bi2 = (IntactTransSplicedProteins) o;

        if (!uniprotAc.equalsIgnoreCase(bi2.getUniprotAc()))
        {
            return false;
        }

        if (!intactAc.equalsIgnoreCase(bi2.getIntactAc()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = uniprotAc.hashCode();
        result = 31 * result + intactAc.hashCode();
        return result;
    }
}
