package uk.ac.ebi.intact.util.uniprotExport.miscore;

import org.apache.commons.collections.CollectionUtils;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

import java.io.*;
import java.util.*;

/**
 * The client of MiClusterScore to compute the score of interactions in IntAct
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15-Sep-2010</pre>
 */

public class MiScoreClient {

    /**
     * the interaction cluster score
     */
    private IntActInteractionClusterScore interactionClusterScore;

    /**
     * the binary interaction converter
     */
    private Intact2BinaryInteractionConverter interactionConverter;

    /**
     * we create a new MiScoreClient
     */
    public MiScoreClient(){
        this.interactionConverter = new Intact2BinaryInteractionConverter();
        this.interactionClusterScore = new IntActInteractionClusterScore();
    }

    /**
     * Computes the MI cluster score for the list of interactions and write the results in a file
     * @param interactions : the list of interactions for what we want to compute the MI cluster score
     * @param fileName : the name of the file where we want the results
     */
    public void computeMiScoresFor(List<String> interactions, String fileName){

        int i = 0;
        // the chunk of binary interactions to process
        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();

        System.out.println(interactions.size() + " interactions in IntAct will be processed.");

        // we process all the interactions of the list per chunk of 200 interactions
        while (i < interactions.size()){
            // we clear the previous chunk
            binaryInteractions.clear();

            System.out.println("Process the interactions " + i + " to " + Math.min(i+199, interactions.size()) + "...");

            // we computes the MI cluster score for 200 interactions at one time
            for (int j = i; j<i+200 && j<interactions.size();j++){
                // get the IntAct interaction object
                String interactionAc = interactions.get(j);
                Interaction intactInteraction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

                if (intactInteraction != null){
                    // the interaction can be converted into binary interaction
                    if (this.interactionConverter.getExpansionStrategy().isExpandable(intactInteraction)){
                        try {
                            // we convert the interaction in binary interaction
                            Collection<IntactBinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);
                            binaryInteractions.addAll(toBinary);
                        } catch (Exception e) {
                            System.out.println("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                        }
                    }
                    // if the interaction cannot be converted into binary interaction, we ignore the interaction.
                    else {
                        System.out.println("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                    }
                }
                else {
                    System.out.println("The interaction " + interactionAc + " doesn't exist in the database and is excluded.");                    
                }
            }

            try {

                // we compute the MI cluster score
                this.interactionClusterScore.setBinaryInteractionList(binaryInteractions);
                this.interactionClusterScore.runService();
            } catch (Exception e){
                System.out.println("The score cannot be computed for the list of binary interactions of size " + binaryInteractions.size());

                for (BinaryInteraction binaryInteraction : binaryInteractions){
                    System.out.println(binaryInteraction.getInteractorA() + ", " + binaryInteraction.getInteractorB());

                    if (binaryInteraction.getInteractorA().getOrganism() == null){
                        System.out.println(binaryInteraction.getInteractorA() + " has a null organism.");
                    }
                    if (binaryInteraction.getInteractorB().getOrganism() == null){
                        System.out.println(binaryInteraction.getInteractorB() + " has a null organism.");
                    }
                }
            }
            i += 200;

            int interactionsToProcess = interactions.size() - Math.min(i, interactions.size());
            System.out.println("Still " + interactionsToProcess + " interactions to process in IntAct.");
        }

        System.out.println("Saving the scores ...");
        // saves the scores
        this.interactionClusterScore.saveScores(fileName);
    }

    /**
     * Extracts the MI cluster score for each interaction exported in uniprot and write the results in a file. the results for the interactions not exported in uniprot
     * is also written in a file. This supposes that the MI score has been computed for all the interactions in IntAct before.
     * @param interactions : the list of interactions currently exported in uniprot
     * @param fileContainingDataExported : the files for the score results of the interactions exported in uniprot
     * @param fileContainingDataNotExported : the files for the score results of the interactions not exported in uniprot
     */
    public void extractMiScoresFor(List<String> interactions, String fileContainingDataExported, String fileContainingDataNotExported){
        try {
            // list of interactions ids exported in uniprot
            Set<Integer> interactionIdentifiersExported = new HashSet<Integer>();
            // list of interactions ids not exported in uniprot
            Set<Integer> interactionIdentifiersAlreadyProcessed = new HashSet<Integer>();

            // file writer for interactions exported in uniprot
            FileWriter writer1 = new FileWriter(fileContainingDataExported);
            // file writer for interactions not exported in uniprot
            FileWriter writer2 = new FileWriter(fileContainingDataNotExported);

            int i = 0;
            Set<BinaryInteraction> binaryInteractions = new HashSet<BinaryInteraction>();

            System.out.println(interactions.size() + " interactions in IntAct will be processed.");

            // each interaction will be processed
            while (i < interactions.size()){
                binaryInteractions.clear();

                System.out.println("Process the interactions " + i + " to " + Math.min(i+199, interactions.size()) + "...");

                for (int j = i; j<i+200 && j<interactions.size();j++){

                    // get the IntAct object and convert it in binary interactions
                    String interactionAc = interactions.get(j);
                    Interaction intactInteraction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

                    if (this.interactionConverter.getExpansionStrategy().isExpandable(intactInteraction)){
                        try {
                            Collection<IntactBinaryInteraction> toBinary = this.interactionConverter.convert(intactInteraction);
                            binaryInteractions.addAll(toBinary);
                        } catch (Exception e) {
                            System.out.println("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                        }
                    }
                    else {
                        System.out.println("The interaction " + interactionAc + ", " + intactInteraction.getShortLabel() + " cannot be converted into binary interactions and is excluded.");
                    }
                }

                // filter the computed scores
                extractMiScoreForBinaryInteractions(binaryInteractions, writer1, writer2, interactionIdentifiersExported, interactionIdentifiersAlreadyProcessed);
                i += 200;
                int interactionsToProcess = interactions.size() - Math.min(i, interactions.size());
                System.out.println("Still " + interactionsToProcess + " interactions to process in IntAct.");
            }

            // close the writers
            writer1.close();
            writer2.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("We cannot write the results in " + fileContainingDataExported + " or " + fileContainingDataNotExported);
        }
    }

    /**
     *
     * @param binaryInteractions : binary interactions exported in uniprot
     * @param writer1 : writer for the interactions exported in uniprot
     * @param writer2 : writer for the interactions not exported in uniprot
     * @param interactionIdentifiersExported : the list of interaction ids exported in uniprot
     * @param interactionIdentifiersAlreadyProcessed : the list of interactions already processed
     */
    private void extractMiScoreForBinaryInteractions(Set<BinaryInteraction> binaryInteractions, FileWriter writer1, FileWriter writer2, Set<Integer> interactionIdentifiersExported, Set<Integer> interactionIdentifiersAlreadyProcessed){

        try {

            // for each binary interaction exported in uniprot, add he Encore interaction Id to the list of exported interactions
            for (BinaryInteraction binary : binaryInteractions){
                List<Integer> listOfInteractionsA = this.interactionClusterScore.getInteractorMapping().get(binary.getInteractorA());
                List<Integer> listOfInteractionsB = this.interactionClusterScore.getInteractorMapping().get(binary.getInteractorA());

                // add all the interaction ids for interactor A with interactor B
                interactionIdentifiersExported.addAll(CollectionUtils.intersection(listOfInteractionsA, listOfInteractionsB));
            }

            // filter the scores for each interaction
            for (Map.Entry<Integer, EncoreInteraction> entry : this.interactionClusterScore.getInteractionMapping().entrySet()){
                // the interaction Id
                int interactionId = entry.getKey();
                // the Encore interaction
                EncoreInteraction encoreInteraction = entry.getValue();

                // extract the Mi score value for this interaction
                List<Confidence> confidenceValues = encoreInteraction.getConfidenceValues();
                Double score = null;
                for(Confidence confidenceValue:confidenceValues){
                    if(confidenceValue.getType().equalsIgnoreCase("intactPsiscore")){
                        score = Double.parseDouble(confidenceValue.getValue());
                    }
                }

                // if this interaction has not already been processed
                if (!interactionIdentifiersAlreadyProcessed.contains(interactionId)){
                    // if the interaction has been exported in uniprot, use the writer 1
                    if (interactionIdentifiersExported.contains(interactionId)){
                        writer1.write(interactionId + "-" + encoreInteraction.getInteractorA() + "-" + encoreInteraction.getInteractorB() + ":" + score);
                    }
                    // if the interaction has not been exported in uniprot, use the writer 2
                    else {
                        writer2.write(interactionId + "-" + encoreInteraction.getInteractorA() + "-" + encoreInteraction.getInteractorB() + ":" + score);
                    }
                    interactionIdentifiersAlreadyProcessed.add(interactionId);
                }
            }

            // flush the writers
            writer1.flush();
            writer2.flush();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("We cannot write the results");
        }
    }
}
