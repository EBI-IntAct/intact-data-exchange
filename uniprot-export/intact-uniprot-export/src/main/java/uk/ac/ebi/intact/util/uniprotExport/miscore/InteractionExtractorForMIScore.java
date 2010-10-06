package uk.ac.ebi.intact.util.uniprotExport.miscore;

import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.uniprotExport.LineExport;

import javax.persistence.Query;
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

    private List<String> getInteractionsToBeProcessedForUniprotExport(){

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery("select distinct(i.ac) from InteractionImpl i join i.components c join c.interactor p " +
                "where i.ac in (select distinct(comp.interaction.ac) from Component comp)" +
                "and i.ac not in (select distinct(i2.ac) from Component c2 join c2.interaction i2 join c2.interactor p2 join p2.annotations a " +
                "where a.cvTopic.shortLabel = :noUniprotUpdate) " +
                "and i.ac not in (select distinct(i3.ac) from Component c3 join c3.interaction i3 join i3.annotations a2 " +
                "where a2.cvTopic.shortLabel = :negative) " +
                "and i.ac not in (select distinct(i4.ac) from Component c4 join c4.interaction i4 join c4.interactor p3 " +
                "where p3.ac not in (select distinct(p4.ac) from InteractorImpl p4 join p4.xrefs refs " +
                "where refs.cvDatabase.identifier = :uniprot " +
                "and refs.cvXrefQualifier.identifier = :identity)" +
                "or p3.ac in (select distinct(p5.ac) from InteractorImpl p5 " +
                "where p5.objClass <> :protein))");
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        return query.getResultList();
    }

    private List<String> getInteractionsFromReleasedExperimentsToBeProcessedForUniprotExport(){

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery("select distinct(i.ac) from InteractionImpl i join i.components c join c.interactor p " +
                "where i.ac in (select ie from Component c5 join c5.interaction ie join ie.experiments e join e.annotations an " +
                "where an.cvTopic.shortLabel = :accepted)" +
                "and i.ac not in (select distinct(i2.ac) from Component c2 join c2.interaction i2 join c2.interactor p2 join p2.annotations a " +
                "where a.cvTopic.shortLabel = :noUniprotUpdate) " +
                "and i.ac not in (select distinct(i3.ac) from Component c3 join c3.interaction i3 join i3.annotations a2 " +
                "where a2.cvTopic.shortLabel = :negative) " +
                "and i.ac not in (select distinct(i4.ac) from Component c4 join c4.interaction i4 join c4.interactor p3 " +
                "where p3.ac not in (select distinct(p4.ac) from InteractorImpl p4 join p4.xrefs refs " +
                "where refs.cvDatabase.identifier = :uniprot " +
                "and refs.cvXrefQualifier.identifier = :identity)" +
                "or p3.ac in (select distinct(p5.ac) from InteractorImpl p5 " +
                "where p5.objClass <> :protein))");
        query.setParameter("accepted", CvTopic.ACCEPTED);
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        return query.getResultList();
    }

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

                if (cvInteraction.equals(method)) {
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

                    if (cvInteraction.equals(method)) {
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
     * This method processes the interactionAcs to determine if each interaction is elligible for uniprot export (uniprot-dr-export is ok)
     * @param interactionAcs : the list of interaction accessions in IntAct we want to process
     * @param eligibleInteractions : the list of interactions which are elligible for a uniprot export
     */
    private void processEligibleExperiments(List<String> interactionAcs, List<String> eligibleInteractions) {

        // process each interaction of the list
        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

            // get the IntAct interaction object
            String interactionAc = interactionAcs.get(i);
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
        } // i
    }

    /**
     * This method processes the interactionAcs to determine if each interaction is exported in uniprot (uniprot-dr-export is ok and interaction detection method is ok)
     * @param interactionAcs : the list of interaction accessions in IntAct we want to process
     * @param eligibleInteractions : the list of interactions which are elligible for a uniprot export
     */
    private void processEligibleExperimentsWithCurrentRules(List<String> interactionAcs, List<String> eligibleInteractions) {

        // process each interaction of the list
        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

            String interactionAc = interactionAcs.get(i);
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
                    else if (hasPassedDrExportAnnotation(interaction, experiment)){
                        eligibleInteractions.add(interaction.getAc());
                        break;
                    }
                } // i's experiments
            }
        } // i
    }

    /**
     * This method processes the interactionAcs to determine if each interaction is exported in uniprot (uniprot-dr-export is ok and interaction detection method is ok)
     * @param interactionAcs : the list of interaction accessions in IntAct we want to process
     * @param eligibleInteractions : the list of interactions which are elligible for a uniprot export
     */
    private void collectEligibleInteractionsWithoutRulesForInteractionDetectionMethod(List<String> interactionAcs, List<String> eligibleInteractions) {

        // process each interaction of the list
        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

            String interactionAc = interactionAcs.get(i);
            Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            // get the IntAct interaction object
            if (interaction != null){
                System.out.println("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

                Collection experiments = interaction.getExperiments();

                for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                    Experiment experiment = (Experiment) iterator2.next();

                    LineExport.ExperimentStatus experimentStatus = super.getCCLineExperimentExportStatus(experiment, "\t\t\t\t");

                    if (experimentStatus.isNotSpecified()){

                        CvInteraction cvInteraction = experiment.getCvInteraction();

                        if (null != cvInteraction) {
                            CvInteractionStatus methodStatus = getMethodExportStatus(cvInteraction, "\t\t");

                            if (methodStatus.isNotSpecified()) {
                                eligibleInteractions.add(interaction.getAc());
                                break;
                            }
                        }
                    }
                } // i's experiments
            }
        } // i
    }

    /**
     * Extracts the interactions which are possible to export in uniprot
     * @param useCurrentRules : boolean value to know if we want to use the existing rules on the interaction detection method for exporting interactions in uniprot
     * @param fileForListOfInteractions : the name of the file where we want to write the list of interactions Acs possible to export
     * @return the list of IntAct interaction accessions of the interactions which can be exported using the rules on the interaction detection method or not
     * @throws SQLException
     * @throws IOException
     */
    public List<String> extractInteractionsPossibleToExport(boolean useCurrentRules, String fileForListOfInteractions) throws SQLException, IOException {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        List<String> interactionsToBeProcessedForExport = getInteractionsToBeProcessedForUniprotExport();

        System.out.println(interactionsToBeProcessedForExport.size() + " will be processed for a possible uniprot export.");

        List<String> interactions;

        // if we want to apply the current rules on the interaction detection method
        if (useCurrentRules){
            interactions = extractInteractionsCurrentlyExported(interactionsToBeProcessedForExport, fileForListOfInteractions);
        }
        else {
            interactions = extractInteractionsPossibleToExport(interactionsToBeProcessedForExport, fileForListOfInteractions);
        }
        System.out.println(interactions.size() + " will be kept for Mi scoring.");

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> extractInteractionsFromReleasedExperimentsPossibleToExport(boolean useCurrentRules, String fileForListOfInteractions) throws SQLException, IOException {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        List<String> interactionsToBeProcessedForExport = getInteractionsFromReleasedExperimentsToBeProcessedForUniprotExport();

        System.out.println(interactionsToBeProcessedForExport.size() + " will be processed for a possible uniprot export.");

        List<String> interactions;

        // if we want to apply the current rules on the interaction detection method
        if (useCurrentRules){
            interactions = extractInteractionsCurrentlyExported(interactionsToBeProcessedForExport, fileForListOfInteractions);
        }
        else {
            interactions = extractInteractionsPossibleToExport(interactionsToBeProcessedForExport, fileForListOfInteractions);
        }
        System.out.println(interactions.size() + " will be kept for Mi scoring.");

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> extractInteractionsWithoutRulesForInteractionDetectionMethod(String fileForListOfInteractions) throws SQLException, IOException {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        List<String> interactionsToBeProcessedForExport = getInteractionsToBeProcessedForUniprotExport();

        System.out.println(interactionsToBeProcessedForExport.size() + " will be processed for a possible uniprot export.");

        List<String> interactions = extractInteractionsWithoutRuleForInteractionDetectionMethod(interactionsToBeProcessedForExport, fileForListOfInteractions);

        System.out.println(interactions.size() + " will be kept for Mi scoring.");

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    /**
     * This method is ignoring the current rules on the interaction detection method to decide if an interaction can be exported or not
     * @param potentiallyEligibleInteraction : the list of interaction accessions to process
     * @param fileForListOfInteractions : the name of the file where we want to write the list of interactions Acs possible to export
     * @return the list of interactions accessions which can be exported in uniprot
     * @throws SQLException
     * @throws IOException
     */
    private List<String> extractInteractionsPossibleToExport(List<String> potentiallyEligibleInteraction, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        processEligibleExperiments(potentiallyEligibleInteraction, eligibleInteractions );

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : eligibleInteractions){
            writer.write(ac + "\n");
            writer.flush();
        }

        writer.close();

        return eligibleInteractions;
    }

    /**
     * This method is using the current rules on the interaction detection method to decide if an interaction can be exported or not
     * @param potentiallyEligibleInteraction : the list of interaction accessions to process
     * @param fileForListOfInteractions : the name of the file where we want to write the list of interactions Acs currently exported
     * @return the list of interactions accessions which are exported in uniprot
     * @throws SQLException
     * @throws IOException
     */
    private List<String> extractInteractionsCurrentlyExported(List<String> potentiallyEligibleInteraction, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        processEligibleExperimentsWithCurrentRules(potentiallyEligibleInteraction, eligibleInteractions );

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : eligibleInteractions){
            writer.write(ac + "\n");
            writer.flush();
        }

        writer.close();
        return eligibleInteractions;
    }

    /**
     * This method is using the current rules on the interaction detection method and only return interactions with an interaction detection method undetermined
     * @param potentiallyEligibleInteraction : the list of interaction accessions to process
     * @param fileForListOfInteractions : the name of the file where we want to write the list of interactions Acs currently exported
     * @return the list of interactions accessions which are exported in uniprot
     * @throws SQLException
     * @throws IOException
     */
    private List<String> extractInteractionsWithoutRuleForInteractionDetectionMethod(List<String> potentiallyEligibleInteraction, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        collectEligibleInteractionsWithoutRulesForInteractionDetectionMethod(potentiallyEligibleInteraction, eligibleInteractions );

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
}
