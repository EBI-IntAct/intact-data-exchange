package uk.ac.ebi.intact.util.uniprotExport.results.clusters;

import org.apache.log4j.Logger;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.Binary2Encore;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        /* Retrieve results */

        Map<Integer, BinaryInteraction<Interactor>> interactionMapping = getBinaryInteractionCluster();

        try {
            File file = new File(fileName);
            FileWriter fstream = new FileWriter(fileName + ".txt");

            for(Integer mappingId:interactionMapping.keySet()){
                BinaryInteraction<Interactor> eI = interactionMapping.get(mappingId);

                // convert and write in mitab
                if (eI != null){
                    double score = FilterUtils.getMiClusterScoreFor(eI);

                    // write score in a text file
                    fstream.write(Integer.toString(mappingId));
                    fstream.write("-");
                    fstream.write(eI.getInteractorA().getIdentifiers().iterator().next().getIdentifier());
                    fstream.write("-");
                    fstream.write(eI.getInteractorB().getIdentifiers().iterator().next().getIdentifier());
                    fstream.write(":" + score);
                    fstream.write("\n");
                    fstream.flush();

                    writer.writeOrAppend(eI, file, false);
                }
            }

            //Close the output stream
            fstream.close();

        } catch (Exception e) {
            logger.error("It is not possible to write the results in the mitab file " + fileName);
            e.printStackTrace();
        }
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
                    fstream.write(Integer.toString(mappingId));
                    fstream.write("-");
                    fstream.write(eI.getInteractorA().getIdentifiers().iterator().next().getIdentifier());
                    fstream.write("-");
                    fstream.write(eI.getInteractorB().getIdentifiers().iterator().next().getIdentifier());
                    fstream.write(":" + score);
                    fstream.write("\n");
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
