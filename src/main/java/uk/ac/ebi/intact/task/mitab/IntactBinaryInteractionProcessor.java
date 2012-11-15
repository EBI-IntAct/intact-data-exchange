package uk.ac.ebi.intact.task.mitab;

import org.springframework.batch.item.ItemProcessor;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.converters.expansion.ExpansionStrategy;

import java.util.Collection;
import java.util.List;

/**
 * General interface for Intact to binary interaction processors
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */

public interface IntactBinaryInteractionProcessor extends ItemProcessor<Interaction, Collection<? extends BinaryInteraction>> {

    public void setExpansionStategy(ExpansionStrategy expansionStategy);

    public void setBinaryItemProcessors(List<BinaryInteractionItemProcessor> delegates);
}
