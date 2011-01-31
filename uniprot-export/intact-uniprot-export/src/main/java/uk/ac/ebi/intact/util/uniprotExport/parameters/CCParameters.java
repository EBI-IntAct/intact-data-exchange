package uk.ac.ebi.intact.util.uniprotExport.parameters;

import java.util.SortedSet;

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
     * the uniprot AC of the second interactor
     */
    public String getSecondInteractor();

    /**
     * The gene name of the first interactor
     */
    public String getFirstGeneName();

    /**
     * The gene name of the second interactor
     */
    public String getSecondGeneName();

    /**
     * The taxid of the first interactor
     * @return
     */
    public String getFirstTaxId();

    /**
     * The taxId of the second interactor
     * @return
     */
    public String getSecondTaxId();

    /**
     * the organism name of the first interactor
     * @return
     */
    public String getFirstOrganismName();

    /**
     * the organism name of the second interactor
     * @return
     */
    public String getSecondOrganismName();

    /**
     * the list of interaction details
     * @return
     */
    public SortedSet<InteractionDetails> getInteractionDetails();
}
