package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

/**
 * Basic CC parameters contains the master uniprot ac, gene name and taxId of the first interactor and a list of basic SecondCCparameters
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public interface BasicCCParameters {

   /**
     * the uniprot AC of the first interactor (it is a master uniprot ac)
     */
    public String getMasterUniprotAc();

    /**
     * The gene name of the first interactor
     */
    public String getGeneName();

    /**
     * The taxid of the first interactor
     * @return
     */
    public String getTaxId();
}
