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
}
