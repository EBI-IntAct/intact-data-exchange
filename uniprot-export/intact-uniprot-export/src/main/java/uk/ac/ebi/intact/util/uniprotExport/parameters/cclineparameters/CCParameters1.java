package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.List;

/**
 * CC parameters1 contains the master uniprot ac, gene name and taxId of the first interactor and a list of SecondCCparameters1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/02/11</pre>
 */

public interface CCParameters1 extends BasicCCParameters{

    /**
     *
     * @return The list of second CC parameters
     */
    public List<SecondCCParameters1> getSecondCCParameters();
}
