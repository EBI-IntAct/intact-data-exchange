package uk.ac.ebi.intact.dataexchange.cvutils;

import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;

import java.util.*;

import uk.ac.ebi.intact.core.persister.stats.StatsUnit;
import uk.ac.ebi.intact.core.persister.stats.StatsUnitFactory;
import uk.ac.ebi.intact.core.persister.stats.impl.CvObjectStatsUnit;
import uk.ac.ebi.intact.model.CvObject;

/**
 * Summary of statistics for the CvUpdater
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUpdaterStatistics {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private Collection<StatsUnit> rootCvs;
    private Multimap<Class, StatsUnit> createdCvs;
    private Multimap<Class,StatsUnit> updatedCvs;
    private Collection<StatsUnit> orphanCvs;
    private Map<String,String> obsoleteCvs;
    private Map<String,String> invalidTerms;

    public CvUpdaterStatistics() {
        this.rootCvs = new ArrayList<StatsUnit>();
        this.createdCvs = new ArrayListMultimap<Class,StatsUnit>();
        this.updatedCvs = new ArrayListMultimap<Class,StatsUnit>();
        this.orphanCvs = new ArrayList<StatsUnit>();
        this.obsoleteCvs = new HashMap<String,String>();
        this.invalidTerms = new HashMap<String,String>();
    }

    // root CVs

    public void addRootCv(CvObject cvObject) {
        rootCvs.add(StatsUnitFactory.createStatsUnit(cvObject));
    }

    public Collection<StatsUnit> getRootCvs() {
        return rootCvs;
    }

    // created CVs

    public void addCreatedCv(CvObject cvObject) {
        createdCvs.put(cvObject.getClass(), StatsUnitFactory.createStatsUnit(cvObject));
    }

    public Multimap<Class, StatsUnit> getCreatedCvs() {
        return createdCvs;
    }

    // updated CVs

    public void addUpdatedCv(CvObject cvObject) {
        updatedCvs.put(cvObject.getClass(), StatsUnitFactory.createStatsUnit(cvObject));
    }

    public Multimap<Class, StatsUnit> getUpdatedCvs() {
        return updatedCvs;
    }

    // orphan CVs

    public void addOrphanCv(CvObject cvObject) {
        orphanCvs.add(StatsUnitFactory.createStatsUnit(cvObject));
    }

    public Collection<StatsUnit> getOrphanCvs() {
        return orphanCvs;
    }

    // absolete CVs

    public void addObsoleteCv(CvObject cvObject) {
        CvObjectStatsUnit statsUnit = (CvObjectStatsUnit) StatsUnitFactory.createStatsUnit(cvObject);
        obsoleteCvs.put(statsUnit.getIdentifier(), statsUnit.getShortLabel());
    }

    public Map<String, String> getObsoleteCvs() {
        return obsoleteCvs;
    }

    // invalid

    public Map<String, String> getInvalidTerms() {
        return invalidTerms;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CvUpdater Stats:").append(NEW_LINE);
        sb.append("----------------").append(NEW_LINE);
        sb.append("CVs Created: ").append(multimapToString(getCreatedCvs())).append(NEW_LINE);
        sb.append("CVs Updated: ").append(multimapToString(getUpdatedCvs())).append(NEW_LINE);
        sb.append("Orphan CVs: ").append(getOrphanCvs().size()).append(NEW_LINE);
        sb.append("Obsolete: ").append(getObsoleteCvs().size()).append(NEW_LINE);
        sb.append("Invalid Terms: ").append(getInvalidTerms().size()).append(NEW_LINE);

        return sb.toString();
    }

    private String multimapToString(Multimap<Class,StatsUnit> multimap) {
        StringBuilder sb = new StringBuilder();

        sb.append(multimap.size());

        if (!multimap.isEmpty()) {

            sb.append(" { ");

            for (Iterator<Class> iterator = multimap.keySet().iterator(); iterator.hasNext();) {
                Class key = iterator.next();

                sb.append(key.getSimpleName() + " (" + multimap.get(key).size() + ")");

                if (iterator.hasNext()) {
                    sb.append(", ");
                }
            }

            sb.append(" }");
        }

        return sb.toString();
    }
}
