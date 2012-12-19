package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

/**
 * CCParameters contains the uniprot AC of the two interactors (can be isoforms of feature chains),
 * the intact AC of the two interactors, the gene name and taxId of the second interactor
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public abstract interface SecondCCParameters extends Comparable{

    /**
     * the Intact AC of the first interactor
     */
    public String getFirstIntactAc();

    /**
     * The uniprot ac of the first interactor (can be isoform and feature chain)
     * @return
     */
    public String getFirstUniprotAc();

    /**
     * the uniprot AC of the second interactor
     */
    public String getSecondUniprotAc();

    /**
     * the Intact AC of the second interactor
     */
    public String getSecondIntactAc();

    /**
     * The gene name of the second interactor
     */
    public String getGeneName();

    /**
     * The taxId of the second interactor
     * @return
     */
    public String getTaxId();
}
