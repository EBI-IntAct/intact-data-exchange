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
    public Collection<String> getPublicationsWithoutImexButWithInteractionImex() throws ParseException;
    /**
     * Collects the publications which do not have a IMEx identifier but contain experiments having a IMEx primary reference
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsWithoutImexButWithExperimentImex() throws ParseException;
    /**
     * Collects the publications which have a IMEx curation depth annotation but are not eligible IMEx (have an IMEx id or is form eligible journal/dataset and contains at least one PPI)
     * @return a collection of publication acs.
     */
    public Collection<String> getPublicationsHavingImexCurationLevelButAreNotEligibleImex() throws ParseException;
    public Collection<String> getPublicationsHavingIMExIdAndNotImexCurationLevel() throws ParseException;
    public Collection<String> getPublicationsHavingIMExIdAndNoPPIInteractions() throws ParseException;
    public Collection<String> getPublicationsHavingIMExIdToUpdate() throws ParseException;
    public Collection<String> getPublicationsNeedingAnImexId() throws ParseException;

    /**
     * Re-initialise the queries and caches of the publicationCollector
     */
    public void initialise() throws ParseException;
}
