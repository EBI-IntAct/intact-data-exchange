package uk.ac.ebi.intact.util.uniprotExport.exporters.rules;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.model.*;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.util.InteractionUtils;
import uk.ac.ebi.intact.util.uniprotExport.CvInteractionStatus;
import uk.ac.ebi.intact.util.uniprotExport.LineExport;
import uk.ac.ebi.intact.util.uniprotExport.LineExportConfig;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.exporters.AbstractInteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.exporters.QueryFactory;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.UniprotExportResults;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.BinaryClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.ExportContext;
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
    private static final Logger logger = Logger.getLogger(ExporterBasedOnDetectionMethod.class);

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

        if (methodStatus.doExport() && numberOfExperiments > 0) {
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
                logger.info("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

                if (InteractionUtils.isBinaryInteraction(interaction)){
                    eligibleInteractions.add(interactionAc);
                }
            }
            IntactContext.getCurrentInstance().getDataContext().commitTransaction(status);
        } // i
    }

    private int computeNumberOfExperimentsHavingDetectionMethod(String method, ExportContext context, EncoreInteractionForScoring interaction, Set<String> validIntactIds){

        Map<MethodTypePair, List<String>> invertedMap = WriterUtils.invertMapFromKeySelection(context.getInteractionToMethod_type(), interaction.getExperimentToPubmed().keySet());

        int numberOfExperiment = 0;

        for (Map.Entry<MethodTypePair, List<String>> entry : invertedMap.entrySet()){

            if (entry.getKey().getMethod().equals(method)){
                numberOfExperiment += entry.getValue().size();
                validIntactIds.addAll(entry.getValue());
            }
        }

        return numberOfExperiment;
    }

    private int computeForBinaryInteractionNumberOfExperimentsHavingDetectionMethod(String method, ExportContext context, BinaryInteraction interaction, Set<String> validIntactIds){
        Set<String> intactAcs = FilterUtils.extractIntactAcFrom(interaction.getInteractionAcs());

        Map<MethodTypePair, List<String>> invertedMap = WriterUtils.invertMapFromKeySelection(context.getInteractionToMethod_type(), intactAcs);

        int numberOfExperiment = 0;

        for (Map.Entry<MethodTypePair, List<String>> entry : invertedMap.entrySet()){

            if (entry.getKey().getMethod().equals(method)){

                for (String intactId : entry.getValue()){
                    if (!context.getSpokeExpandedInteractions().contains(intactId)){
                        numberOfExperiment ++;
                        validIntactIds.add(intactId);
                    }
                }
            }
        }

        return numberOfExperiment;
    }

    /**
     * Apply the rules on the interaction detection method for all the interactions in the cluster
     * @param cluster : the cluster containing the interactions
     * @param eligibleInteractions : the list of eligible encore interaction ids for uniprot export
     */
    private void processEligibleExperiments(IntactCluster cluster, ExportContext context, Set<Integer> eligibleInteractions) throws UniprotExportException {

        if (cluster != null){
            if (cluster instanceof BinaryClusterScore){
                BinaryClusterScore clusterScore = (BinaryClusterScore) cluster;

                for (Map.Entry<Integer, BinaryInteraction<Interactor>> entry : clusterScore.getBinaryInteractionCluster().entrySet()){

                    if (canExportBinaryInteraction(entry.getValue(), context)){
                        eligibleInteractions.add(entry.getKey());
                    }
                }
            }
            else {
                for (Map.Entry<Integer, EncoreInteractionForScoring> entry : cluster.getEncoreInteractionCluster().entrySet()){

                    if (canExportEncoreInteraction(entry.getValue(), context)){
                        eligibleInteractions.add(entry.getKey());
                    }
                }
            }
        }
    }

    /**
     * Apply the rules on the interaction detection method for all the negative interactions in the cluster
     * @param cluster : the cluster containing the negative interactions
     * @param eligibleInteractions : the list of eligible negative encore interaction ids for uniprot export
     * @param positiveInteractions : result of uniprot export for positive interactions
     */
    private void processEligibleExperimentsForNegativeInteraction(IntactCluster cluster, ExportContext context, Set<Integer> eligibleInteractions, ExportedClusteredInteractions positiveInteractions) throws UniprotExportException {

        if (cluster != null){
            if (cluster instanceof BinaryClusterScore){
                BinaryClusterScore clusterScore = (BinaryClusterScore) cluster;

                for (Map.Entry<Integer, BinaryInteraction<Interactor>> entry : clusterScore.getBinaryInteractionCluster().entrySet()){

                    if (canExportNegativeBinaryInteraction(entry.getValue(), context, positiveInteractions)){
                        eligibleInteractions.add(entry.getKey());
                    }
                }
            }
            else {
                for (Map.Entry<Integer, EncoreInteractionForScoring> entry : cluster.getEncoreInteractionCluster().entrySet()){

                    if (canExportNegativeEncoreInteraction(entry.getValue(), context, positiveInteractions)){
                        eligibleInteractions.add(entry.getKey());
                    }
                }
            }
        }
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
    public void exportInteractionsFrom(UniprotExportResults results) throws UniprotExportException {
        ExportedClusteredInteractions positiveInteractions = results.getPositiveClusteredInteractions();
        ExportedClusteredInteractions negativeInteractions = results.getNegativeClusteredInteractions();

        Set<Integer> eligibleInteractions = positiveInteractions.getInteractionsToExport();
        Set<Integer> negativeEligibleInteractions = negativeInteractions.getInteractionsToExport();

        processEligibleExperiments(positiveInteractions.getCluster(), results.getExportContext(), eligibleInteractions);
        processEligibleExperimentsForNegativeInteraction(negativeInteractions.getCluster(), results.getExportContext(), negativeEligibleInteractions, positiveInteractions);
    }

    @Override
    public boolean canExportEncoreInteraction(EncoreInteractionForScoring interaction, ExportContext context) throws UniprotExportException {

        Set<String> detectionMethods = new HashSet(interaction.getMethodToPubmed().keySet());

        boolean passRules = false;

        Set<String> validIntactIds = new HashSet<String>();
        Set<String> invalidIntactIds = new HashSet<String>();

        for (String method : detectionMethods){
            validIntactIds.clear();
            int numberOfExperimentWithThisMethod = computeNumberOfExperimentsHavingDetectionMethod(method, context, interaction, validIntactIds);

            if (hasPassedInteractionDetectionMethodRules(method, numberOfExperimentWithThisMethod)){
                logger.info("Binary Interaction " + interaction.getId() + " : the method " + method + " has passed the rules");
                passRules = true;
            }
            else {
                logger.info("Binary Interaction " + interaction.getId() + " : the method " + method + " doesn't pass the rules and will be removed");
                invalidIntactIds.addAll(validIntactIds);
            }
        }

        if (passRules){
            removeInteractionEvidencesFrom(interaction, invalidIntactIds, context);
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

        return passRules;
    }

    @Override
    public boolean canExportBinaryInteraction(BinaryInteraction<Interactor> interaction, ExportContext context) throws UniprotExportException {

        Set<InteractionDetectionMethod> detectionMethods = new HashSet(interaction.getDetectionMethods());

        Set<String> intactIds = new HashSet<String>();
        Set<String> invalidIntactIds = new HashSet<String>();

        boolean passRules = false;

        for (InteractionDetectionMethod method : detectionMethods){
            intactIds.clear();
            int numberOfExperimentWithThisMethod = computeForBinaryInteractionNumberOfExperimentsHavingDetectionMethod(method.getIdentifier(), context, interaction, intactIds);

            if (hasPassedInteractionDetectionMethodRules(method.getIdentifier(), numberOfExperimentWithThisMethod)){
                logger.info("The method " + method + " has passed the rules");

                passRules = true;
            }
            else {
                logger.info("The method " + method + " doesn't pass the rules and will be removed");
                invalidIntactIds.addAll(intactIds);
            }
        }

        if (passRules){
            removeInteractionEvidencesFrom(interaction, invalidIntactIds, context);
        }

        return passRules;
    }

    @Override
    public boolean canExportNegativeEncoreInteraction(EncoreInteractionForScoring interaction, ExportContext context, ExportedClusteredInteractions positiveInteractions) throws UniprotExportException {
        // no negative interaction can be exported
        return false;
    }

    @Override
    public boolean canExportNegativeBinaryInteraction(BinaryInteraction<Interactor> interaction, ExportContext context, ExportedClusteredInteractions positiveInteractions) throws UniprotExportException {
        // no negative interaction can be exported
        return false;
    }

    protected void removeInteractionEvidencesFrom(EncoreInteractionForScoring encore, Set<String> wrongInteractions, ExportContext context){
        List<CrossReference> publicationsToRemove = new ArrayList(encore.getPublicationIds());
        List<String> publicationIdsToKeep = new ArrayList<String>(encore.getPublicationIds().size());
        List<String> methodsToRemove = new ArrayList(encore.getMethodToPubmed().keySet());
        List<String> typesToRemove = new ArrayList(encore.getTypeToPubmed().keySet());

        Collection<String> validInteractions = CollectionUtils.subtract(encore.getExperimentToPubmed().keySet(), wrongInteractions);

        for (String interactionAc : validInteractions){

            MethodTypePair pair = context.getInteractionToMethod_type().get(interactionAc);

            String detectionMI = pair.getMethod();

            String typeMi = pair.getType();

            String pubmedId = encore.getExperimentToPubmed().get(interactionAc);

            for (CrossReference ref : encore.getPublicationIds()){
                if (ref.getIdentifier().equals(pubmedId)){
                    publicationsToRemove.remove(ref);
                    publicationIdsToKeep.add(ref.getIdentifier());
                    break;
                }
            }

            for (String method : encore.getMethodToPubmed().keySet()){
                if (method.equals(detectionMI)){
                    methodsToRemove.remove(method);
                    break;
                }
            }

            for (String type : encore.getTypeToPubmed().keySet()){
                if (type.equals(typeMi)){
                    typesToRemove.remove(type);
                    break;
                }
            }
        }

        encore.getPublicationIds().removeAll(publicationsToRemove);

        for (String methodToRemove : methodsToRemove){
            encore.getMethodToPubmed().remove(methodToRemove);
        }

        for (String typeToRemove : typesToRemove){
            encore.getTypeToPubmed().remove(typeToRemove);
        }

        List<String> pubmedsToRemove = new ArrayList<String>();

        for (Map.Entry<String, List<String>> entry : encore.getMethodToPubmed().entrySet()){
            pubmedsToRemove.clear();

            for (String pubmed : entry.getValue()){
                if (!publicationIdsToKeep.contains(pubmed)){
                    pubmedsToRemove.add(pubmed);
                }
            }

            entry.getValue().remove(pubmedsToRemove);
        }

        for (Map.Entry<String, List<String>> entry : encore.getTypeToPubmed().entrySet()){
            pubmedsToRemove.clear();

            for (String pubmed : entry.getValue()){
                if (!publicationIdsToKeep.contains(pubmed)){
                    pubmedsToRemove.add(pubmed);
                }
            }

            entry.getValue().remove(pubmedsToRemove);
        }

        for (String interactionAc : wrongInteractions){
            encore.getExperimentToPubmed().remove(interactionAc);
            encore.getExperimentToDatabase().remove(interactionAc);
        }
    }

    protected void removeInteractionEvidencesFrom(BinaryInteraction<Interactor> binary, Set<String> invalidInteractions, ExportContext context){
        List<CrossReference> interactionsToRemove = new ArrayList(binary.getInteractionAcs());
        List<InteractionDetectionMethod> methodsToRemove = new ArrayList(binary.getDetectionMethods());
        List<InteractionType> typesToRemove = new ArrayList(binary.getInteractionTypes());

        Collection<String> validInteractions = new ArrayList<String>(binary.getInteractionAcs().size());

        for (CrossReference intact : binary.getInteractionAcs()){
            if (!invalidInteractions.contains(intact.getIdentifier())){
                validInteractions.add(intact.getIdentifier());
            }
        }

        for (String interaction : validInteractions){

            MethodTypePair pair = context.getInteractionToMethod_type().get(interaction);

            String detectionMI = pair.getMethod();

            String typeMi = pair.getType();

            for (InteractionDetectionMethod method : binary.getDetectionMethods()){
                if (method.getIdentifier().equals(detectionMI)){
                    methodsToRemove.remove(method);
                    break;
                }
            }

            for (InteractionType type : binary.getInteractionTypes()){
                if (type.getIdentifier().equals(typeMi)){
                    typesToRemove.remove(type);
                    break;
                }
            }

            for (CrossReference ref : binary.getInteractionAcs()){
                if (ref.getIdentifier().equals(interaction)){
                    interactionsToRemove.remove(ref);
                    break;
                }
            }
        }

        binary.getDetectionMethods().removeAll(methodsToRemove);
        binary.getInteractionTypes().removeAll(typesToRemove);
        binary.getInteractionAcs().removeAll(interactionsToRemove);
    }
}
