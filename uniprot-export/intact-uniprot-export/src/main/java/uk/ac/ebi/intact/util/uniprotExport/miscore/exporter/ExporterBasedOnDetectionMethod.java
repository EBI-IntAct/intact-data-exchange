package uk.ac.ebi.intact.util.uniprotExport.miscore.exporter;

import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.InteractionUtils;
import uk.ac.ebi.intact.util.uniprotExport.CvInteractionStatus;
import uk.ac.ebi.intact.util.uniprotExport.LineExport;
import uk.ac.ebi.intact.util.uniprotExport.LineExportConfig;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.IntactFilter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MethodAndTypePair;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.FileWriter;
import java.io.IOException;
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
public class ExporterBasedOnDetectionMethod extends AbstractInteractionExporter {

    private QueryFactory queryProvider;
    protected LineExportConfig config;

    /**
     * Cache the CvInteraction property for the export. CvInteraction.ac -> CvInteractionStatus.
     */
    protected Map<String, CvInteractionStatus> cvInteractionExportStatusCache = new HashMap<String, CvInteractionStatus>();

    public ExporterBasedOnDetectionMethod(){
        this.queryProvider = new QueryFactory();
        buildCvInteractionStatusCache();
        config = new LineExportConfig();
    }

    private void buildCvInteractionStatusCache(){
        List<Object []> methodStatus = this.queryProvider.getMethodStatusInIntact();

        for (Object [] method : methodStatus){
            if (method.length == 2){
                String methodMi = (String) method[0];
                String export = (String) method[1];

                if (cvInteractionExportStatusCache.containsKey(methodMi)) {

                    if (!cvInteractionExportStatusCache.get(methodMi).doNotExport()){
                        cvInteractionExportStatusCache.put(methodMi, new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT));
                    }
                } else {

                    if (null != export) {
                        export = export.toLowerCase().trim();
                    }

                    if (LineExport.METHOD_EXPORT_KEYWORK_EXPORT.equals(export)) {

                        cvInteractionExportStatusCache.put(methodMi, new CvInteractionStatus(CvInteractionStatus.EXPORT));

                    } else if (LineExport.METHOD_EXPORT_KEYWORK_DO_NOT_EXPORT.equals(export)) {

                        cvInteractionExportStatusCache.put(methodMi, new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT));

                    } else {

                        // it must be an integer value, let's check it.
                        try {
                            Integer value = new Integer(export);
                            int i = value;

                            if (i >= 2) {

                                // value is >= 2
                                cvInteractionExportStatusCache.put(methodMi, new CvInteractionStatus(CvInteractionStatus.CONDITIONAL_EXPORT, i));

                            } else if (i == 1) {

                                cvInteractionExportStatusCache.put(methodMi, new CvInteractionStatus(CvInteractionStatus.EXPORT));

                            } else {
                                cvInteractionExportStatusCache.put(methodMi, new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT));
                            }

                        } catch (NumberFormatException e) {
                            // not an integer !

                            cvInteractionExportStatusCache.put(methodMi, new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT));
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param cvInteraction : the interaction detection method
     * @return true if the interaction passed the dr-export constraints on the interaction detection method
     */
    /*private boolean hasPassedInteractionDetectionMethodRules(CvInteraction cvInteraction, Collection<Experiment> experiments){
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
    } */

    public final CvInteractionStatus getMethodExportStatus(final String cvInteraction, String logPrefix) {

        CvInteractionStatus status = null;

        // cache the CvInteraction status
        if (null != cvInteraction) {

            CvInteractionStatus cache = cvInteractionExportStatusCache.get(cvInteraction);
            if (null != cache) {

                status = cache;

            } else {

                if (config.isIgnoreUniprotDrExportAnnotation()) {
                    status = new CvInteractionStatus(CvInteractionStatus.EXPORT);
                } else {

                    status = new CvInteractionStatus(CvInteractionStatus.DO_NOT_EXPORT);
                }
            }
        }

        System.out.println("\t\t CvInteractionExport status: " + status);

        return status;
    }

    /**
     *
     * @return true if the interaction passed the dr-export constraints on the interaction detection method
     */
    private boolean hasPassedInteractionDetectionMethodRules(String methodMi, int numberOfExperiments){
        // Then check the experimental method (CvInteraction)

        if (null == methodMi) {
            return false;
        }

        CvInteractionStatus methodStatus = getMethodExportStatus(methodMi, "\t\t");

        if (methodStatus.doExport()) {
            return true;
        }
        else if (methodStatus.isConditionalExport()) {

            // if the threshold is not reached, iterates over all available interactions to check if
            // there is (are) one (many) that could allow to reach the threshold.

            int threshold = methodStatus.getMinimumOccurence();

            if (numberOfExperiments >= threshold) {
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

                if (InteractionUtils.isBinaryInteraction(interaction)){
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

    private int computeNumberOfExperimentsHavingDetectionMethod(String method, MiClusterContext context, EncoreInteraction interaction){

        Map<MethodAndTypePair, List<String>> invertedMap = WriterUtils.invertMapFromKeySelection(context.getInteractionToMethod_type(), interaction.getExperimentToPubmed().keySet());

        int numberOfExperiment = 0;

        for (Map.Entry<MethodAndTypePair, List<String>> entry : invertedMap.entrySet()){

            if (entry.getKey().getMethod().equals(method)){
                numberOfExperiment += entry.getValue().size();
            }
        }

        return numberOfExperiment;
    }

    private int computeForBinaryInteractionNumberOfExperimentsHavingDetectionMethod(String method, MiClusterContext context, BinaryInteraction interaction){
        Set<String> intactAcs = FilterUtils.extractIntactAcFrom(interaction.getInteractionAcs());

        Map<MethodAndTypePair, List<String>> invertedMap = WriterUtils.invertMapFromKeySelection(context.getInteractionToMethod_type(), intactAcs);

        int numberOfExperiment = 0;

        for (Map.Entry<MethodAndTypePair, List<String>> entry : invertedMap.entrySet()){

            if (entry.getKey().getMethod().equals(method)){
                numberOfExperiment += entry.getValue().size();
            }
        }

        return numberOfExperiment;
    }

    /**
     * Apply the rules on the interaction detection method for all the interactions in the cluster
     * @param cluster : the cluster containing the interactions
     * @param eligibleInteractions : the list of eligible encore interaction ids for uniprot export
     */
    private void processEligibleExperiments(IntActInteractionClusterScore cluster, MiClusterContext context, Set<Integer> eligibleInteractions) throws UniprotExportException {

        // process each interaction of the list
        for (Map.Entry<Integer, EncoreInteraction> interactionEntry : cluster.getInteractionMapping().entrySet()) {

            EncoreInteraction interaction = interactionEntry.getValue();

            if(canExportEncoreInteraction(interaction, context)){
                eligibleInteractions.add(interactionEntry.getKey());
            }
        } // i
    }

    /**
     * Apply the rules on interaction detection method for all the interactions with two uniprot proteis
     * @param cluster
     * @param eligibleInteractions
     */
    private void filterNoUniprotProteinsAndProcessEligibleExperiments(IntActInteractionClusterScore cluster, MiClusterContext context, List<Integer> eligibleInteractions) {

        // process each interaction of the list
        for (Map.Entry<Integer, EncoreInteraction> interactionEntry : cluster.getInteractionMapping().entrySet()) {

            EncoreInteraction interaction = interactionEntry.getValue();

            // get the Encore interaction object
            if (interaction != null){
                System.out.println("\t\t Interaction: Id:" + interaction.getId());

                String A = interaction.getInteractorA(IntactFilter.UNIPROT_DATABASE);
                String B = interaction.getInteractorB(IntactFilter.UNIPROT_DATABASE);

                if (A != null && B!= null){
                    //Collection<String> interactionsAcs = interaction.getExperimentToPubmed().keySet();

                    //TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

                    Set<String> detectionMethods = interaction.getMethodToPubmed().keySet();

                    for (String method : detectionMethods){
                        int numberOfExperimentWithThisMethod = computeNumberOfExperimentsHavingDetectionMethod(method, context, interaction);

                        if (hasPassedInteractionDetectionMethodRules(method, numberOfExperimentWithThisMethod)){
                            eligibleInteractions.add(interactionEntry.getKey());
                            break;
                        }
                    }

                    /*Collection<InteractionImpl> interactions = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionsAcs);
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
                    } // i's experiments*/
                    //IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);
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
    public Set<Integer> extractEligibleInteractionsFrom(IntActInteractionClusterScore cluster, MiClusterContext context, String fileForListOfInteractions) throws SQLException, IOException, UniprotExportException {

        System.out.println(cluster.getInteractionMapping().size() + " interactions to process.");
        Set<Integer> eligibleInteractions = new HashSet<Integer>();

        processEligibleExperiments(cluster, context, eligibleInteractions);

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

        processEligibleExperiments(results.getClusterScore(), results.getClusterContext(), eligibleInteractions);

        results.setInteractionsToExport(eligibleInteractions);
    }

    @Override
    public boolean canExportEncoreInteraction(EncoreInteraction interaction, MiClusterContext context) throws UniprotExportException {

        System.out.println("\t\t Interaction: Id:" + interaction.getId());

        //Collection<String> interactionsAcs = interaction.getExperimentToPubmed().keySet();

        //TransactionStatus status = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        Set<String> detectionMethods = interaction.getMethodToPubmed().keySet();

        for (String method : detectionMethods){
            int numberOfExperimentWithThisMethod = computeNumberOfExperimentsHavingDetectionMethod(method, context, interaction);

            if (hasPassedInteractionDetectionMethodRules(method, numberOfExperimentWithThisMethod)){
                return true;
            }
        }
        /*Collection<InteractionImpl> interactions = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionsAcs);
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
     } // i's experiments*/
        //IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);

        return false;
    }

    @Override
    public boolean canExportEBinaryInteraction(BinaryInteraction interaction, MiClusterContext context) throws UniprotExportException {

        Set<String> detectionMethods = new HashSet(interaction.getDetectionMethods());

        for (String method : detectionMethods){
            int numberOfExperimentWithThisMethod = computeForBinaryInteractionNumberOfExperimentsHavingDetectionMethod(method, context, interaction);

            if (hasPassedInteractionDetectionMethodRules(method, numberOfExperimentWithThisMethod)){
                return true;
            }
        }

        return false;
    }
}
