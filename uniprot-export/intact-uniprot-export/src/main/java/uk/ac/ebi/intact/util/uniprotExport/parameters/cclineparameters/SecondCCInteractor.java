package uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters;

import java.util.SortedSet;

/**
 * Interface for the CC parameters attached to the second interactor
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/02/11</pre>
 */

public interface SecondCCInteractor {

    /**
     * the Intact AC of the first interactor
     */
    public String getFirstIntacAc();

    /**
     * The uniprot ac of the first interactor (can be isoform and feature chain)
     * @return
     */
    public String getFirstInteractor();

    /**
     * the uniprot AC of the second interactor
     */
    public String getSecondInteractor();

    /**
     * the Intact AC of the second interactor
     */
    public String getSecondIntactAc();

    /**
     * The gene name of the second interactor
     */
    public String getSecondGeneName();

    /**
     * The taxId of the second interactor
     * @return
     */
    public String getSecondTaxId();

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
