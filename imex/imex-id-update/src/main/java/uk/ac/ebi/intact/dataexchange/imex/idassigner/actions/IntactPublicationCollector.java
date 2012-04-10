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
     * Collect publications having an IMEx primary reference and no imex curation annotation
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsHavingIMExIdAndNotImexCurationLevel();

    /**
     * Collect publications having IMEx primary reference and without any PPI
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsHavingIMExIdAndNoPPI();

    /**
     * Collect publications having IMEx primary reference and imex-curation annotation which contain at least one PPI
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsHavingIMExIdToUpdate();

    /**
     * Collect publications which do not have any IMEx primary references but are eligible IMEx (journal, dataset) and
     * have at least one PPI
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsNeedingAnImexId();

    /**
     * Re-initialise the queries and caches of the publicationCollector
     */
    public void initialise();
}
