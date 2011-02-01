package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.uniprotExport.LineExport;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.IntactFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.IntActInteractionClusterScore;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * This exporter will use rules on the interaction detection methods of each interaction. It will look at the
 * 'uniprot-dr-export' annotation attched to each interaction detection method which can be :
 * - no : the interaction detection method cannot be exported. If one binary interaction contains only this interaction
 * detection method, it will not be exported.
 * - yes : the interaction detection method can be exported. If one binary interaction has at least one interaction detection method with export = yes, the interaction is exported
 * - condition : conditional export (number of interactions having this method, etc.)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class ExporterBasedOnDetectionMethod extends LineExport implements InteractionExporter{

    private QueryFactory queryProvider;

    public ExporterBasedOnDetectionMethod(){
        this.queryProvider = new QueryFactory();
    }

    /**
     *
     * @param cvInteraction : the interaction detection method
     * @param experiments : the list of experiments reporting the same interaction
     * @return true if the interaction passed the dr-export constraints on the interaction detection method
     */
    private boolean hasPassedInteractionDetectionMethodRules(CvInteraction cvInteraction, Collection<Experiment> experiments){
        // Then check the experimental method (CvInteraction)

        if (null == cvInteraction) {
            return false;
        }

        LineExport.CvInteractionStatus methodStatus = getMethodExportStatus(cvInteraction, "\t\t");

        if (methodStatus.doExport()) {
            return true;
        }
        else if (methodStatus.isConditionalExport()) {

            // if the threshold is not reached, iterates over all available interactions to check if
            // there is (are) one (many) that could allow to reach the threshold.

            int threshold = methodStatus.getMinimumOccurence();

            // we create a non redondant set of experiment identifier
            // TODO couldn't that be a static collection that we empty regularly ?
            int experimentAcs = 0;

            // check if there are other experiments attached to the current interaction that validate it.
            boolean enoughExperimentFound = false;

            for (Iterator iterator = experiments.iterator(); iterator.hasNext();) {

                Experiment e = (Experiment) iterator.next();
                CvInteraction method = e.getCvInteraction();

                if (cvInteraction.equals(method)) {

                    experimentAcs++;

                    // we only update if we found one
                    enoughExperimentFound = (experimentAcs >= threshold);
                }
            }

            if (enoughExperimentFound) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filter the binary interactions of a given list of interactions and return a list composed with only binary interactions
     * @param interactionAcs : the list of interaction accessions
     * @param eligibleInteractions : the list of eligible interactions for uniprot export
     */
    private void filterTrueBinaryInteractions(List<String> interactionAcs, List<String> eligibleInteractions) {

        // process each interaction of the list
        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {
            TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            // get the IntAct interaction object
            String interactionAc = interactionAcs.get(i);
            Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            // the interaction exists in IntAct
            if (interaction != null){
                System.out.println("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

                if (isBinary(interaction)){
                    eligibleInteractions.add(interactionAc);
                }
            }
            // the interaction doesn't exist in IntAct
            else {
                System.out.println("\t\t\t That interaction "+interactionAc +" is null, skip it.");
                continue; // skip that interaction
            }
            IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);
        } // i
    }

    /**
     * Apply the rules on the interaction detection method for all the interactions in the cluster
     * @param cluster : the cluster containing the interactions
     * @param eligibleInteractions : the list of eligible encore interaction ids for uniprot export
     */
    private void processEligibleExperiments(IntActInteractionClusterScore cluster, Set<Integer> eligibleInteractions) {

        // process each interaction of the list
        for (Map.Entry<Integer, EncoreInteraction> interactionEntry : cluster.getInteractionMapping().entrySet()) {

            EncoreInteraction interaction = interactionEntry.getValue();

            // get the Encore interaction object
            if (interaction != null){
                System.out.println("\t\t Interaction: Id:" + interaction.getId());

                Collection<String> interactionsAcs = interaction.getExperimentToPubmed().keySet();

                TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

                Collection<InteractionImpl> interactions = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionsAcs);
                Set<Experiment> experiments = new HashSet<Experiment>();

                for (Interaction inter : interactions){
                    experiments.addAll(inter.getExperiments());
                }

                for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                    Experiment experiment = (Experiment) iterator2.next();

                    if (hasPassedInteractionDetectionMethodRules(experiment.getCvInteraction(), experiments)){
                        eligibleInteractions.add(interactionEntry.getKey());
                        break;
                    }
                } // i's experiments
                IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);
            }
        } // i
    }

    /**
     * Apply the rules on interaction detection method for all the interactions with two uniprot proteis
     * @param cluster
     * @param eligibleInteractions
     */
    private void filterNoUniprotProteinsAndProcessEligibleExperiments(IntActInteractionClusterScore cluster, List<Integer> eligibleInteractions) {

        // process each interaction of the list
        for (Map.Entry<Integer, EncoreInteraction> interactionEntry : cluster.getInteractionMapping().entrySet()) {

            EncoreInteraction interaction = interactionEntry.getValue();

            // get the Encore interaction object
            if (interaction != null){
                System.out.println("\t\t Interaction: Id:" + interaction.getId());

                String A = interaction.getInteractorA(IntactFilter.UNIPROT_DATABASE);
                String B = interaction.getInteractorB(IntactFilter.UNIPROT_DATABASE);

                if (A != null && B!= null){
                    Collection<String> interactionsAcs = interaction.getExperimentToPubmed().keySet();

                    TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

                    Collection<InteractionImpl> interactions = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionsAcs);
                    Set<Experiment> experiments = new HashSet<Experiment>();

                    for (Interaction inter : interactions){
                        experiments.addAll(inter.getExperiments());
                    }

                    for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                        Experiment experiment = (Experiment) iterator2.next();

                        if (hasPassedInteractionDetectionMethodRules(experiment.getCvInteraction(), experiments)){
                            eligibleInteractions.add(interactionEntry.getKey());
                            break;
                        }
                    } // i's experiments
                    IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);
                }
            }
        } // i
    }

    /**
     * This method will get the interactions from released experiment which can be processed for uniprot export and which passed the
     * dr export constraints at the level of the experiment. Write the interaction accessions in a file
     * @param fileForListOfInteractions
     * @return the list of interaction Acs which are elligible for uniprot export
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public List<String> filterOnNonUnprotProteinsAndCollectInteractionsFromReleasedExperiments(String fileForListOfInteractions) throws SQLException, IOException {

        List<String> interactionsToBeProcessedForExport = this.queryProvider.getInteractionAcsFromReleasedExperimentsToBeProcessedForUniprotExport();

        System.out.println(interactionsToBeProcessedForExport.size() + " will be processed for a possible uniprot export.");

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : interactionsToBeProcessedForExport){
            writer.write(ac + "\n");
            writer.flush();
        }

        writer.close();
        System.out.println(interactionsToBeProcessedForExport.size() + " will be kept for Mi scoring.");

        return interactionsToBeProcessedForExport;
    }

    /**
     * Collect all the interactions from released experiments which passed the dr-export filter but can contain non uniprot proteins
     * @param fileForListOfInteractions
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public List<String> collectAllInteractionsFromReleasedExperiments(String fileForListOfInteractions) throws SQLException, IOException {

        List<String> interactionsToBeProcessedForExport = this.queryProvider.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport();

        System.out.println(interactionsToBeProcessedForExport.size() + " will be processed for a possible uniprot export.");

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : interactionsToBeProcessedForExport){
            writer.write(ac + "\n");
            writer.flush();
        }

        writer.close();

        return interactionsToBeProcessedForExport;
    }

    /**
     *
     * @param cluster
     * @param fileForListOfInteractions
     * @return the list of Encore interaction ids which can be exported in uniprot. The mi score has been computed for the interactions which passed the dr export constraints at the level of the experiment
     * Write the interaction accessions in a file
     * @throws SQLException
     * @throws IOException
     */
    public Set<Integer> extractEligibleInteractionsFrom(IntActInteractionClusterScore cluster, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(cluster.getInteractionMapping().size() + " interactions to process.");
        Set<Integer> eligibleInteractions = new HashSet<Integer>();

        processEligibleExperiments(cluster, eligibleInteractions);

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (Integer id : eligibleInteractions){
            EncoreInteraction interaction = cluster.getInteractionMapping().get(id);

            Map<String, String> refs = interaction.getExperimentToPubmed();
            for (String ref : refs.keySet()){
                writer.write(ref + "\n");
                writer.flush();
            }
        }

        writer.close();
        return eligibleInteractions;
    }

    /**
     *
     * @param potentiallyEligibleInteraction
     * @param fileForListOfInteractions
     * @return the list of interaction accessions which can be exported in uniprot. The mi score has been computed for the interactions which passed the dr export constraints at the level of the experiment
     * and which are binary
     * Write the interaction accessions in a file
     * @throws SQLException
     * @throws IOException
     */
    public List<String> filterBinaryInteractionsFrom(List<String> potentiallyEligibleInteraction, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        filterTrueBinaryInteractions(potentiallyEligibleInteraction, eligibleInteractions);

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : eligibleInteractions){
            writer.write(ac + "\n");
            writer.flush();
        }

        writer.close();

        return eligibleInteractions;
    }

    @Override
    public void exportInteractionsFrom(MiScoreResults results) throws UniprotExportException {
        Set<Integer> eligibleInteractions = new HashSet<Integer>();

        processEligibleExperiments(results.getClusterScore(), eligibleInteractions);

        results.setInteractionsToExport(eligibleInteractions);
    }
}
