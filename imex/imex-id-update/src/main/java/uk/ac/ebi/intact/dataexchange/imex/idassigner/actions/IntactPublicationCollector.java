package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions;

import java.text.ParseException;
import java.util.Collection;

/**
 * Interface for collecting publications having specific criterias
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30/03/12</pre>
 */

public interface IntactPublicationCollector {

    /**
     * Collects the publications which do not have a IMEx identifier but contain interactions having a IMEx primary reference
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsWithoutImexButWithInteractionImex();
    /**
     * Collects the publications which do not have a IMEx identifier but contain experiments having a IMEx primary reference
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsWithoutImexButWithExperimentImex();
    /**
     * Collects the publications which have a IMEx curation depth annotation but are not eligible IMEx (have an IMEx id or is form eligible journal/dataset and contains at least one PPI)
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsHavingImexCurationLevelButAreNotEligibleImex();

    /**
     *
     * @return a collection of publication acs.
     * @throws ParseException
     */
    public Collection<String> getPublicationsHavingIMExIdAndNotImexCurationLevel();
    public Collection<String> getPublicationsHavingIMExIdAndNoPPIInteractions();
    public Collection<String> getPublicationsHavingIMExIdToUpdate();
    public Collection<String> getPublicationsNeedingAnImexId();

    /**
     * Re-initialise the queries and caches of the publicationCollector
     */
    public void initialise();
}
