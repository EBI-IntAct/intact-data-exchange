package uk.ac.ebi.intact.util.uniprotExport.parameters;

/**
 * This class contains parameters to write a DR Line
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class DRParametersImpl implements DRParameters{

    public String uniprotAc;
    public int numberOfInteractions;

    public DRParametersImpl(String uniprotAc, int numberOfInteractions){
        if (uniprotAc == null){
            throw new IllegalArgumentException("A DR parameter must have a non null uniprot AC");
        }
        this.uniprotAc = uniprotAc;
        this.numberOfInteractions = numberOfInteractions;
    }

    public String getUniprotAc() {
        return uniprotAc;
    }

    public int getNumberOfInteractions() {
        return numberOfInteractions;
    }
}
