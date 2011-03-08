package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of CCParameters1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public class DefaultCCParameters1 implements CCParameters<SecondCCParameters1>{

    private String masterAc;

    private String geneName;

    private String taxId;

    private List<SecondCCParameters1> secondCCParameters;

    public DefaultCCParameters1(String firstInteractor, String firstGeneName,
                               String firstTaxId, List<SecondCCParameters1> secondInteractors){

        this.masterAc = firstInteractor;

        this.geneName = firstGeneName;
        this.taxId = firstTaxId;

        if (secondInteractors == null){
            this.secondCCParameters = new ArrayList<SecondCCParameters1>();
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
    public List<SecondCCParameters1> getSecondCCParameters() {
        return this.secondCCParameters;
    }
}
