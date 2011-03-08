package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.List;

/**
 * CCParameters2 contains the master uniprot ac, gene name and taxId of the first interactor and a list of SecondCCparameters2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public interface CCParameters2 extends BasicCCParameters{

    /**
     *
     * @return The list of second CC parameters
     */
    public List<SecondCCParameters2> getSecondCCParameters();
}
