package uk.ac.ebi.intact.util.uniprotExport.results.clusters;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.Binary2Encore;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Cluster containing binary interactions
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public class BinaryClusterScore implements IntactCluster {
    private static final Logger logger = Logger.getLogger(BinaryClusterScore.class);

    private Map<Integer, BinaryInteraction<Interactor>> interactionMapping = new HashMap<Integer, BinaryInteraction<Interactor>>();
    private Map<String, List<Integer>> interactorMapping = new HashMap<String, List<Integer>>();

    private PsimiTabWriter writer;

    public BinaryClusterScore(){
        writer = new PsimiTabWriter();
    }

    @Override
    public Map<Integer, BinaryInteraction<Interactor>> getBinaryInteractionCluster() {
        return this.interactionMapping;
    }

    @Override
    public Map<Integer, EncoreInteraction> getEncoreInteractionCluster() {
        Binary2Encore iConverter = new Binary2Encore();

        Map<Integer, EncoreInteraction> encoreInteractionCluster = new HashMap<Integer, EncoreInteraction>();

        for(Integer mappingId:getBinaryInteractionCluster().keySet()){
            BinaryInteraction bI = getBinaryInteractionCluster().get(mappingId);
            if (bI != null){
                EncoreInteraction eI = iConverter.getEncoreInteraction(bI);
                encoreInteractionCluster.put(mappingId, eI);
            }
        }

        return encoreInteractionCluster;
    }

    @Override
    public Map<String, List<Integer>> getInteractorCluster() {
        return this.interactorMapping;
    }

    @Override
    public Set<Integer> getAllInteractionIds() {
        return this.interactionMapping.keySet();
    }

    @Override
    public void saveCluster(String fileName) {
        String scoreListCSV = getScoresPerInteraction();
        try{
            // Create file
            FileWriter fstream = new FileWriter(fileName + ".txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(scoreListCSV);
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
            logger.error("Error: " + e.getMessage());
        }

        /* Retrieve results */

        Map<Integer, BinaryInteraction<Interactor>> interactionMapping = getBinaryInteractionCluster();

        try {
            File file = new File(fileName);

            for(Integer mappingId:interactionMapping.keySet()){
                BinaryInteraction eI = interactionMapping.get(mappingId);
                if (eI != null){
                    writer.writeOrAppend(eI, file, false);
                }
            }

        } catch (Exception e) {
            logger.error("It is not possible to write the results in the mitab file " + fileName);
            e.printStackTrace();
        }
    }

    /**
     *
     * @return a list of formatted scores for each interaction
     */
    public String getScorePerInteractions(Collection<Integer> interactionIds, String scoreListCSV, String[] scoreList){
        if(scoreList == null){
            int scoreListSize = interactionIds.size();
            scoreList = new String[scoreListSize];
            scoreListCSV = "";
            String delimiter = "\n";

            int i = 0;
            for (Integer eId : interactionIds){
                BinaryInteraction bI = this.getBinaryInteractionCluster().get(eId);

                if (bI != null){
                    List<Confidence> confidenceValues = bI.getConfidenceValues();
                    Double score = null;
                    for(Confidence confidenceValue:confidenceValues){
                        if(confidenceValue.getType().equalsIgnoreCase("intactPsiscore")){
                            score = Double.parseDouble(confidenceValue.getValue());
                        }
                    }

                    scoreList[i] = eId + "-" +bI.getInteractorA().toString() + "-" + bI.getInteractorB().toString() + ":" + score;
                    scoreListCSV = scoreListCSV + scoreList[i];
                    i++;
                    if(scoreListSize > i){
                        scoreListCSV = scoreListCSV + delimiter;
                    }
                }
            }
        }
        return scoreListCSV;
    }

    /**
     *
     * @return a list of formatted scores for each interaction
     */
    public String getScoresPerInteraction(){

        int scoreListSize = this.getBinaryInteractionCluster().size();
        String [] scoreList = new String[scoreListSize];
        String scoreListCSV = "";
        String delimiter = "\n";
        int i = 0;
        for(Map.Entry<Integer, BinaryInteraction<Interactor>> eI:this.getBinaryInteractionCluster().entrySet()){
            List<Confidence> confidenceValues = eI.getValue().getConfidenceValues();
            Double score = null;
            for(Confidence confidenceValue:confidenceValues){
                if(confidenceValue.getType().equalsIgnoreCase("intactPsiscore")){
                    score = Double.parseDouble(confidenceValue.getValue());
                }
            }

            scoreList[i] = eI.getKey() + "-" + eI.getValue().getInteractorA().getIdentifiers().iterator().next().getIdentifier() + "-" + eI.getValue().getInteractorB().getIdentifiers().iterator().next().getIdentifier() + ":" + score;
            scoreListCSV = scoreListCSV + scoreList[i];
            i++;
            if(scoreListSize > i){
                scoreListCSV = scoreListCSV + delimiter;
            }
        }
        return scoreListCSV;
    }

    /**
     * Save the scores of the specific interaction ids
     * @param fileName
     * @param interactionIds
     */
    public void saveClusteredInteractions(String fileName, Set<Integer> interactionIds){

        /* Retrieve results */

        Map<Integer, BinaryInteraction<Interactor>> interactionMapping = getBinaryInteractionCluster();
        logger.info("Saving scores...");

        try {
            File file = new File(fileName);

            FileWriter fstream = new FileWriter(fileName + ".txt");

            for(Integer mappingId:interactionIds){
                BinaryInteraction<Interactor> eI = interactionMapping.get(mappingId);

                // convert and write in mitab
                if (eI != null){
                    double score = FilterUtils.getMiClusterScoreFor(eI);

                    // write score in a text file
                    fstream.write(mappingId);
                    fstream.write("-");
                    fstream.write(eI.getInteractorA().getIdentifiers().iterator().next().getIdentifier());
                    fstream.write("-");
                    fstream.write(eI.getInteractorB().getIdentifiers().iterator().next().getIdentifier());
                    fstream.write(":" + score);
                    fstream.flush();

                    writer.writeOrAppend(eI, file, false);
                }
            }

            //Close the output stream
            fstream.close();

        } catch (Exception e) {
            logger.error("It is not possible to write the results in the mitab file " + fileName + " or in the text file " + fileName + ".txt");
            e.printStackTrace();
        }
    }
}
