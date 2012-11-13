package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Default implementation of CCParameters1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public class CCParameters1 implements CCParameters<SecondCCParameters1>{

    private String masterAc;

    private String geneName;

    private String taxId;

    private SortedSet<SecondCCParameters1> secondCCParameters;

    public CCParameters1(String firstInteractor, String firstGeneName,
                         String firstTaxId, SortedSet<SecondCCParameters1> secondInteractors){

        this.masterAc = firstInteractor;

        this.geneName = firstGeneName;
        this.taxId = firstTaxId;

        if (secondInteractors == null){
            this.secondCCParameters = new TreeSet<SecondCCParameters1>();
        }
        else{
            this.secondCCParameters = secondInteractors;
        }
    }

    @Override
    public String getMasterUniprotAc() {
        return this.masterAc;
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
    public SortedSet<SecondCCParameters1> getSecondCCParameters() {
        return this.secondCCParameters;
    }
}
