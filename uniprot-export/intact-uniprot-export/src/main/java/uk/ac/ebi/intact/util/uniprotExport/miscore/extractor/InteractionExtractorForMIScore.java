package uk.ac.ebi.intact.util.uniprotExport.miscore.extractor;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.uniprotExport.LineExport;
import uk.ac.ebi.intact.util.uniprotExport.miscore.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * This class is extracting interactions in Intact which are only PPI interactions, non negative and dr-uniprot-export annotation is taken into account.
 * It is also possible to extract the interactions exported in uniprot with current rules on the interaction detection method.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Sep-2010</pre>
 */

public class InteractionExtractorForMIScore extends LineExport {

    private IntactQueryProvider queryProvider;
    private static final double EXPORT_THRESHOLD = 0.43;
    private static final String CONFIDENCE_NAME = "intactPsiscore";
    private static final String COLOCALIZATION = "MI:0403";

    public InteractionExtractorForMIScore(){
        this.queryProvider = new IntactQueryProvider();
    }

    /**
     *
     * @param interaction : the interaction to check
     * @param experiment : one experiment reporting this interaction
     * @return true if the interaction passed the dr-export constraint (if it exists) of this experiment
     */
    private boolean hasPassedDrExportAnnotation(Interaction interaction, Experiment experiment){

        // the interaction exists in IntAct
        if (interaction != null){

            // we need the status of the experiment to process
            LineExport.ExperimentStatus experimentStatus = super.getCCLineExperimentExportStatus(experiment, "\t\t\t\t");

            // we cannot export the experiment
            if (experimentStatus.doNotExport()) {
                return false;
            }
            // we can export the experiment
            else if (experimentStatus.doExport()) {
                return true;

            }
            // the experiment can be exported if some conditions are fulfilled
            else if (experimentStatus.isLargeScale()) {

                // if my interaction has one of those keywords as annotation for DR line export, do export.
                Collection keywords = experimentStatus.getKeywords();
                Collection annotations = interaction.getAnnotations();
                boolean annotationFound = false;

                CvTopic authorConfidenceTopic = getAuthorConfidence();

                // We assume here that an interaction has a single Annotation of type 'uniprot-dr-export'.
                for (Iterator iterator3 = annotations.iterator(); iterator3.hasNext() && !annotationFound;) {
                    final Annotation annotation = (Annotation) iterator3.next();

                    if (authorConfidenceTopic.equals(annotation.getCvTopic())) {
                        String text = annotation.getAnnotationText();

                        System.out.println("\t\t\t Interaction has " + authorConfidenceTopic.getShortLabel() + ": '" + text + "'");

                        if (text != null) {
                            text = text.trim();
                        }

                        for (Iterator iterator4 = keywords.iterator(); iterator4.hasNext() && !annotationFound;) {
                            String kw = (String) iterator4.next();
                            // NOT case sensitive

                            System.out.println("\t\t\t\t Compare it with '" + kw + "'");

                            if (kw.equalsIgnoreCase(text)) {
                                annotationFound = true;
                                System.out.println("\t\t\t\t\t Equals !");
                            }
                        }
                    }
                }

                if (annotationFound) {
                    return true;
                }
            }
            // the experiment can be exported, it depends on the MI score
            else if (experimentStatus.isNotSpecified()) {
                return true;
            } // experiment status not specified
        }

        return false;
    }

    /**
     *
     * @param interaction : the interaction to check
     * @param experiment : the experiment reporting this interaction
     * @param currentIndexOfInteraction : the index of this interaction among the lits of interactions
     * @param interactionAcs : the list of interaction accessions
     * @return  true if the interaction passed the dr-export constraints at the level of the interaction detection method
     */
    private boolean hasPassedInteractionDetectionMethodRules(Interaction interaction, Experiment experiment, int currentIndexOfInteraction, List<String> interactionAcs){
        // Then check the experimental method (CvInteraction)
        // Nothing specified at the experiment level, check for the method (CvInteraction)
        CvInteraction cvInteraction = experiment.getCvInteraction();

        if (null == cvInteraction) {
            return false;
        }

        CvInteractionStatus methodStatus = getMethodExportStatus(cvInteraction, "\t\t");

        if (methodStatus.doExport()) {
            return true;
        }
        else if (methodStatus.isConditionalExport()) {

            // if the threshold is not reached, iterates over all available interactions to check if
            // there is (are) one (many) that could allow to reach the threshold.

            int threshold = methodStatus.getMinimumOccurence();

            // we create a non redondant set of experiment identifier
            // TODO couldn't that be a static collection that we empty regularly ?
            Set experimentAcs = new HashSet(threshold);

            // check if there are other experiments attached to the current interaction that validate it.
            boolean enoughExperimentFound = false;
            for (Iterator iterator = interaction.getExperiments().iterator(); iterator.hasNext();) {
                Experiment experiment1 = (Experiment) iterator.next();

                CvInteraction method = experiment1.getCvInteraction();

                LineExport.ExperimentStatus experimentStatus = super.getCCLineExperimentExportStatus(experiment1, "\t\t\t\t");

                if (cvInteraction.equals(method) && !experimentStatus.doNotExport()) {
                    experimentAcs.add(experiment1.getAc());

                    // we only update if we found one
                    enoughExperimentFound = (experimentAcs.size() >= threshold);
                }
            }

            for (int j = 0; j < interactionAcs.size() && !enoughExperimentFound; j++) {

                if (currentIndexOfInteraction == j) {
                    continue;
                }

                //
                // Have that conditionalMethods at the interaction scope.
                //
                // for a interaction
                //      for each experiment e
                //          if e.CvInteraction <> cvInteraction -> continue
                //          else is experiment already processed ? if no, add and check the count >= threashold.
                //                                                 if reached, stop, esle carry on.
                //

                String interaction2ac = interactionAcs.get(j);
                Interaction interaction2 = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interaction2ac);


                Collection experiments2 = interaction2.getExperiments();

                for (Iterator iterator6 = experiments2.iterator(); iterator6.hasNext() && !enoughExperimentFound;)
                {
                    Experiment experiment2 = (Experiment) iterator6.next();

                    CvInteraction method = experiment2.getCvInteraction();

                    LineExport.ExperimentStatus experimentStatus = super.getCCLineExperimentExportStatus(experiment2, "\t\t\t\t");

                    if (cvInteraction.equals(method) && !experimentStatus.doNotExport()) {
                        experimentAcs.add(experiment2.getAc());
                        // we only update if we found one
                        enoughExperimentFound = (experimentAcs.size() >= threshold);
                    }
                } // j's experiments

            } // j

            if (enoughExperimentFound) {
                return true;
            }
        }
        return false;
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

        CvInteractionStatus methodStatus = getMethodExportStatus(cvInteraction, "\t\t");

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
     * This method processes the interactionAcs to determine if each interaction is elligible for uniprot export (uniprot-dr-export is ok)
     * @param interactionAcs : the list of interaction accessions in IntAct we want to process
     * @param eligibleInteractions : the list of interactions which are elligible for a uniprot export
     * @deprecated This method contains a bug because all the interactions passing the dr-export constraints at the level of the experiment should pass the rules
     * on the interaction detection method
     */
    @Deprecated
    private void processEligibleExperiments(List<String> interactionAcs, List<String> eligibleInteractions) {

        // process each interaction of the list
        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

            // get the IntAct interaction object
            String interactionAc = interactionAcs.get(i);
            TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            // the interaction exists in IntAct
            if (interaction != null){
                System.out.println("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

                // get the experiments for this interaction. We would expect only one experiment per interaction in IntAct
                Collection experiments = interaction.getExperiments();

                // we process each experiment of the interaction
                for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                    // get the experiment
                    Experiment experiment = (Experiment) iterator2.next();

                    if (hasPassedDrExportAnnotation(interaction, experiment)){
                        eligibleInteractions.add(interactionAc);
                        break;
                    }
                } // i's experiments
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
     * Filter the binary interactions of a given list of interactions and return a list composed with only binary interactions
     * @param interactionAcs : the list of interaction accessions
     * @param eligibleInteractions : the list of eligible interactions for uniprot export
     */
    private void processEligibleBinaryInteractions(List<String> interactionAcs, List<String> eligibleInteractions) {

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
     * This method processes the interactionAcs to determine if each interaction is exported in uniprot (uniprot-dr-export is ok and interaction detection method is ok)
     * @param interactionAcs : the list of interaction accessions in IntAct we want to process
     * @param eligibleInteractions : the list of interactions which are elligible for a uniprot export
     * @deprecated This method contains a bug because all the interactions passing the dr-export constraints at the level of the experiment should pass the rules
     * on the interaction detection method
     */
    @Deprecated
    private void processEligibleExperimentsWithCurrentRules(List<String> interactionAcs, List<String> eligibleInteractions) {

        // process each interaction of the list
        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

            String interactionAc = interactionAcs.get(i);
            TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            // get the IntAct interaction object
            if (interaction != null){
                System.out.println("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

                Collection experiments = interaction.getExperiments();

                for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                    Experiment experiment = (Experiment) iterator2.next();

                    LineExport.ExperimentStatus experimentStatus = super.getCCLineExperimentExportStatus(experiment, "\t\t\t\t");

                    if (experimentStatus.isNotSpecified()){
                        if (hasPassedInteractionDetectionMethodRules(interaction, experiment, i, interactionAcs)){
                            eligibleInteractions.add(interaction.getAc());
                            break;
                        }

                    }
                    //else if (hasPassedDrExportAnnotation(interaction, experiment)){
                    else {
                        eligibleInteractions.add(interaction.getAc());
                        break;
                    }
                } // i's experiments
            }
            IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);
        } // i
    }

    /**
     * Apply the rules on the interaction detection method for the interactions in the cluster with no uniprot-dr-export annotation
     * @param cluster : the cluster containing the interactions
     * @param eligibleInteractions : the list of eligible encore interaction ids for uniprot export
     * @deprecated This method contains a bug because all the interactions passing the dr-export constraints at the level of the experiment should pass the rules
     * on the interaction detection method
     */
    @Deprecated
    private void processEligibleExperimentsWithCurrentRules(IntActInteractionClusterScore cluster, List<Integer> eligibleInteractions) {

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

                    LineExport.ExperimentStatus experimentStatus = super.getCCLineExperimentExportStatus(experiment, "\t\t\t\t");

                    if (experimentStatus.isNotSpecified()){
                        if (hasPassedInteractionDetectionMethodRules(experiment.getCvInteraction(), experiments)){
                            eligibleInteractions.add(interactionEntry.getKey());
                            break;
                        }

                    }
                    //else if (hasPassedDrExportAnnotation(interaction, experiment)){
                    else {
                        eligibleInteractions.add(interactionEntry.getKey());
                        break;
                    }
                } // i's experiments
                IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);
            }
        } // i
    }

    /**
     * Apply the rules at the experiment level (dr-export annotation) and the rules on the interaction detection method for the interactions in the cluster with no uniprot-dr-export annotation
     * @param cluster : the cluster containing the interactions
     * @param eligibleInteractions : the list of eligible encore interaction ids for uniprot export
     */
    private void processEligibleExperiments(IntActInteractionClusterScore cluster, List<Integer> eligibleInteractions) {

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

                for (Iterator iterator2 = interactions.iterator(); iterator2.hasNext();) {

                    Interaction intactInteraction = (Interaction) iterator2.next();

                    Collection<Experiment> experimentsForInteraction = intactInteraction.getExperiments();

                    for (Experiment e : experimentsForInteraction){
                        if (hasPassedDrExportAnnotation(intactInteraction, e)){
                            experiments.add(e);
                        }
                    }
                } // i's interactions

                for (Iterator iterator = experiments.iterator(); iterator.hasNext();) {
                    Experiment experiment = (Experiment) iterator.next();

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
     * Apply the rules on the interaction detection method for all the interactions in the cluster
     * @param cluster : the cluster containing the interactions
     * @param eligibleInteractions : the list of eligible encore interaction ids for uniprot export
     */
    private void processAllEligibleExperimentsWithCurrentRules(IntActInteractionClusterScore cluster, List<Integer> eligibleInteractions) {

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
    private void excludeNoUniprotProteinAndProcessAllEligibleExperimentsWithCurrentRules(IntActInteractionClusterScore cluster, List<Integer> eligibleInteractions) {

        // process each interaction of the list
        for (Map.Entry<Integer, EncoreInteraction> interactionEntry : cluster.getInteractionMapping().entrySet()) {

            EncoreInteraction interaction = interactionEntry.getValue();

            // get the Encore interaction object
            if (interaction != null){
                System.out.println("\t\t Interaction: Id:" + interaction.getId());

                String A = interaction.getInteractorA(MiScoreClient.UNIPROT_DATABASE);
                String B = interaction.getInteractorB(MiScoreClient.UNIPROT_DATABASE);

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
     * @throws SQLException
     * @throws IOException
     */
    public List<String> collectInteractionsFromReleasedExperimentsPossibleToExport(String fileForListOfInteractions) throws SQLException, IOException {

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
    public List<String> collectInteractionsFromReleasedExperimentsContainingNoUniprotProteinsPossibleToExport(String fileForListOfInteractions) throws SQLException, IOException {

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
     * Collect all the interactions from released experiments without adding the filter on dr-export
     * @param fileForListOfInteractions
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public List<String> collectInteractionsFromReleasedExperiments(String fileForListOfInteractions) throws SQLException, IOException {


        List<String> interactionsToBeProcessedForExport = this.queryProvider.getInteractionAcsFromReleasedExperimentsNoFilterDrExport();

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
     * This method is using the current rules on the interaction detection method to decide if an interaction can be exported or not
     * Write the interaction accessions in a file
     * @param cluster : the cluster containing the list of interaction to process
     * @param fileForListOfInteractions : the name of the file where we want to write the list of interactions Acs currently exported
     * @return the list of encore interactions id which are exported in uniprot
     * @throws SQLException
     * @throws IOException
     * @deprecated : contains a bug because export interaction of high confidence without looking at the interaction detection method
     */
    @Deprecated
    public List<Integer> extractInteractionsCurrentlyExported(IntActInteractionClusterScore cluster, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(cluster.getInteractionMapping().size() + " interactions to process.");
        List<Integer> eligibleInteractions = new ArrayList<Integer>();

        processEligibleExperimentsWithCurrentRules(cluster, eligibleInteractions );

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
     * @param cluster
     * @param fileForListOfInteractions
     * @return the list of Encore interaction ids which can be exported in uniprot. The mi score has been computed for all the interactions, even those with low confidence
     * @throws SQLException
     * @throws IOException
     */
    public List<Integer> extractInteractionsCurrentlyExportedWithScoreOfAllInteractions(IntActInteractionClusterScore cluster, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(cluster.getInteractionMapping().size() + " interactions to process.");
        List<Integer> eligibleInteractions = new ArrayList<Integer>();

        processEligibleExperiments(cluster, eligibleInteractions );

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
     * @param cluster
     * @param fileForListOfInteractions
     * @return the list of Encore interaction ids which can be exported in uniprot. The mi score has been computed for the interactions which passed the dr export constraints at the level of the experiment
     * Write the interaction accessions in a file
     * @throws SQLException
     * @throws IOException
     */
    public List<Integer> extractInteractionsExportedWithCurrentRulesForAllExperiment(IntActInteractionClusterScore cluster, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(cluster.getInteractionMapping().size() + " interactions to process.");
        List<Integer> eligibleInteractions = new ArrayList<Integer>();

        processAllEligibleExperimentsWithCurrentRules(cluster, eligibleInteractions );

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
     * Collect all interactions from released experiments and then apply a filter on non uniprot proteins.
     * It will apply the current rule for uniprot export and return the list of interactions which passed all the filters
     * @param cluster
     * @param fileForListOfInteractions
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public List<Integer> extractInteractionsExportedWithCurrentRulesForAllExperimentContainingNoUniprotProteins(IntActInteractionClusterScore cluster, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(cluster.getInteractionMapping().size() + " interactions to process.");
        List<Integer> eligibleInteractions = new ArrayList<Integer>();

        excludeNoUniprotProteinAndProcessAllEligibleExperimentsWithCurrentRules(cluster, eligibleInteractions );

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
    public List<String> extractBinaryInteractionsPossibleToExport(List<String> potentiallyEligibleInteraction, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        processEligibleBinaryInteractions(potentiallyEligibleInteraction, eligibleInteractions );

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : eligibleInteractions){
            writer.write(ac + "\n");
            writer.flush();
        }

        writer.close();

        return eligibleInteractions;
    }

    /**
     * @param fileName : the list of interaction accessions to process in a file
     * @return the list of interactions accessions listed in a file
     * @throws SQLException
     * @throws IOException
     */
    public List<String> extractInteractionsFromFile(String fileName) throws SQLException, IOException {

        File file = new File(fileName);

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line = reader.readLine();
        List<String> potentiallyElligibleInteractions = new ArrayList<String>();

        while (line != null){
            potentiallyElligibleInteractions.add(line);
            line = reader.readLine();
        }

        reader.close();

        System.out.println(potentiallyElligibleInteractions.size() + " interactions to process.");

        return potentiallyElligibleInteractions;
    }

    /**
     *
     * @param interaction
     * @return the computed Mi cluster score for this interaction
     */
    private double getMiClusterScoreFor(EncoreInteraction interaction){
        List<psidev.psi.mi.tab.model.Confidence> confidenceValues = interaction.getConfidenceValues();
        double score = 0;
        for(psidev.psi.mi.tab.model.Confidence confidenceValue:confidenceValues){
            if(confidenceValue.getType().equalsIgnoreCase(CONFIDENCE_NAME)){
                score = Double.parseDouble(confidenceValue.getValue());
            }
        }

        return score;
    }

    /**
     * For each binary interaction in the intactMiClusterScore : filter on a threshold value of the score and then, depending on 'filterBinary',
     * will add a filter on true binary interaction
     * @param context
     * @param miScore
     * @param filterBinary
     * @return
     * @throws UniprotExportException
     */
    public List<Integer> processExportWithMiClusterScore(MiClusterContext context, IntActInteractionClusterScore miScore, boolean filterBinary) throws UniprotExportException {
        List<Integer> interactionsPossibleToExport = new ArrayList<Integer>();
        Map<String, Map.Entry<String, String>> interactionType_Method = context.getInteractionToType_Method();
        List<String> spokeExpandedInteractions = context.getSpokeExpandedInteractions();

        for (Map.Entry<Integer, EncoreInteraction> entry : miScore.getInteractionMapping().entrySet()){
            EncoreInteraction encore = entry.getValue();

            double score = getMiClusterScoreFor(encore);

            if (score >= EXPORT_THRESHOLD){

                if (encore.getExperimentToDatabase() == null){
                    throw new UniprotExportException("The interaction " + entry.getKey() + ":" + encore.getInteractorA() + "-" + encore.getInteractorB() +" doesn't have any references to IntAct.");
                }
                List<String> intactInteractions = new ArrayList<String>();
                
                intactInteractions.addAll(encore.getExperimentToPubmed().keySet());

                if (intactInteractions.isEmpty()){
                    throw new UniprotExportException("The interaction " + entry.getKey() + ":" + encore.getInteractorA() + "-" + encore.getInteractorB() +" doesn't have any references to IntAct.");
                }

                for (String ac : intactInteractions){
                    if (filterBinary){
                        if (!spokeExpandedInteractions.contains(ac)){

                            String method = interactionType_Method.get(ac).getKey();

                            if (!method.equals(COLOCALIZATION)){
                                interactionsPossibleToExport.add(entry.getKey());
                                break;
                            }
                        }
                    }
                    else{
                        interactionsPossibleToExport.add(entry.getKey());
                    }
                }
            }
        }

        return interactionsPossibleToExport;
    }
}
