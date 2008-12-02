package uk.ac.ebi.intact.psimitab.rsc;

import psidev.psi.mi.tab.model.builder.Row;


import java.util.Properties;

/**
 * 
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.2-SNAPSHOT
 */
public interface RelevanceScoreCalculator {

    String calculateScore( Row row);
    Properties getWeights();
}
