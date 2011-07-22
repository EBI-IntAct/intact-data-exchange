package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.*;

/**
 * Contains the default parameters of a CCLine version 2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class DefaultCCParameters2 implements CCParameters<SecondCCParameters2> {

    private String masterAc;

    private String geneName;

    private String taxId;

    private SortedSet<SecondCCParameters2> secondCCParameters;

    public DefaultCCParameters2(String firstInteractor, String firstGeneName,
                                String firstTaxId, SortedSet<SecondCCParameters2> secondInteractors){

        this.masterAc = firstInteractor;

        this.geneName = firstGeneName != null ? firstGeneName : "-";
        this.taxId = firstTaxId;

        if (secondInteractors == null){
            this.secondCCParameters = new TreeSet<SecondCCParameters2>();
        }
        else{
            this.secondCCParameters = secondInteractors;
        }
    }

    @Override
    public String getMasterUniprotAc() {
        return masterAc;
    }

    @Override
    public String getGeneName() {
        return geneName;
    }

    @Override
    public String getTaxId() {
        return taxId;
    }

    @Override
    public SortedSet<SecondCCParameters2> getSecondCCParameters() {
        return this.secondCCParameters;
    }
}
