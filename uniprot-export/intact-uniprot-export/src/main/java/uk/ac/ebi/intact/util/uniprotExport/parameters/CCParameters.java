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

    public String getFirstInteractor();

    public String getSecondInteractor();

    public String getFirstGeneName();

    public String getSecondGeneName();

    public String getFirstTaxId();

    public String getSecondTaxId();

    public String getFirstOrganismName();

    public String getSecondOrganismName();

    public SortedSet<InteractionDetails> getInteractionDetails();
}
