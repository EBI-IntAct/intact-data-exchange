package uk.ac.ebi.intact.util.uniprotExport.parameters;

import java.util.List;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public interface CCParameters {

    /**
     * the uniprot AC of the first interactor
     */
    public String getFirstInteractor();

    /**
     * The gene name of the first interactor
     */
    public String getFirstGeneName();

    /**
     * The taxid of the first interactor
     * @return
     */
    public String getFirstTaxId();

    public List<SecondCCInteractor> getSecondCCInteractors();
}
