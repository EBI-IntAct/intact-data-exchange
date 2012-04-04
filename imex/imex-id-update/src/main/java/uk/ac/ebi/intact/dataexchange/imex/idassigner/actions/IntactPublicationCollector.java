package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import java.util.Collection;

/**
 * Interface for collecting publications having specific criterias
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/03/12</pre>
 */

public interface IntactPublicationCollector {
    public Collection<String> getPublicationsWithoutImexButWithInteractionImex();
    public Collection<String> getPublicationsWithoutImexButWithExperimentImex();
    public Collection<String> getPublicationsHavingImexCurationLevelButAreNotEligibleImex();
    public Collection<String> getPublicationsHavingIMExIdAndNotImexCurationLevel();
    public Collection<String> getPublicationsHavingIMExIdToUpdate();
    public Collection<String> getPublicationsNeedingAnImexId();
}
