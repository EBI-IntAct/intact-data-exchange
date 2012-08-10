package uk.ac.ebi.intact.task.mitab;

import org.springframework.batch.item.ItemProcessor;
import psidev.psi.mi.tab.model.BinaryInteraction;

/**
 * This interface is the interface for all BinaryInteractionItemProcessor that will process binary interactions.
 * This interface is aware of expanded binary interaction so it does not process twice the common elements of an expanded binary interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/08/12</pre>
 */

public interface BinaryIteractionItemProcessor extends ItemProcessor<BinaryInteraction,BinaryInteraction> {

    /**
     * This methods will disable/enable the processing of the all binary interaction or just the processing of the interactors
     * @param onlyInteractors
     */
    public void onlyProcessInteractors(boolean onlyInteractors);

}
