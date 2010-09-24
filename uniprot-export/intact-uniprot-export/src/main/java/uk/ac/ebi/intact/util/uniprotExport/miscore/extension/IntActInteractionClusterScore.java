package uk.ac.ebi.intact.util.uniprotExport.miscore.extension;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.score.InteractionClusterScore;
import uk.ac.ebi.enfin.mi.score.ols.MIOntology;
import uk.ac.ebi.enfin.mi.score.scores.MIScore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension of the InteractionClusterScore : use a different format to export the scores
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Sep-2010</pre>
 */

public class IntActInteractionClusterScore extends InteractionClusterScore{

    private static final Logger logger = Logger.getLogger(IntActInteractionClusterScore.class);
    private String[] scoreList = null;
    private String scoreListCSV;
    protected String fileName;
    private MIOntology MIO = new MIOntology();

    public IntActInteractionClusterScore(){
        super();
        setMappingIdDbNames("uniprotkb");
    }

    @Override
    public void runService() {
        logger.debug("runService");
        super.runService();
    }

    /**
     *
     * @return a list of formatted scores for each interaction
     */
    public String[] getScoresPerInteraction(){
        if(this.getInteractionMapping() == null){
            runService();
        }
        if(scoreList == null){
            int scoreListSize = this.getInteractionMapping().size();
            scoreList = new String[scoreListSize];
            scoreListCSV = "";
            String delimiter = "\n";
            int i = 0;
            for(EncoreInteraction eI:this.getInteractionMapping().values()){
                List<Confidence> confidenceValues = eI.getConfidenceValues();
                Double score = null;
                for(Confidence confidenceValue:confidenceValues){
                    if(confidenceValue.getType().equalsIgnoreCase("intactPsiscore")){
                        score = Double.parseDouble(confidenceValue.getValue());
                    }
                }
                if(score == null){
                    logger.error("No score for this interaction: " + eI.getId());
                }
                scoreList[i] = eI.getId() + "-" +eI.getInteractorA() + "-" + eI.getInteractorB() + ":" + score;
                scoreListCSV = scoreListCSV + scoreList[i];
                i++;
                if(scoreListSize > i){
                    scoreListCSV = scoreListCSV + delimiter;
                }
            }
        }
        return scoreList;
    }

    /**
     * Saves the score using a formatted String for each interaction
     */
    public void saveScores(){
        fileName = "scores.txt";
        saveScores(fileName);
    }

    @Override
    public void saveScores(String fileName){
        if(scoreList == null){
            getScoresPerInteraction();
        }
        try{
            // Create file
            FileWriter fstream = new FileWriter(fileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(scoreListCSV);
            logger.info("Saving scores on ... " + fileName);
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            logger.error("Error: " + e.getMessage());
        }
    }

}
