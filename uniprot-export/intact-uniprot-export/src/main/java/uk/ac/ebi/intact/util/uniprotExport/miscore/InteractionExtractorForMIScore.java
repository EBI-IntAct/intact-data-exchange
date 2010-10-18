package uk.ac.ebi.intact.util.uniprotExport.miscore;

import org.springframework.transaction.TransactionStatus;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.core.context.DataContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.uniprotExport.LineExport;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

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

    private List<String> getInteractionAcsToBeProcessedForUniprotExport(){

        // interactions associated with components
        String interactionsInvolvedInComponents = "select distinct(c1.interaction.ac) from Component c1";
        // interactions with at least one interactor with the annotation 'no-uniprot-update'
        String interactionsInvolvingInteractorsNoUniprotUpdate = "select distinct(i2.ac) from Component c2 join " +
                "c2.interaction as i2 join c2.interactor as p join p.annotations as a where a.cvTopic.shortLabel = :noUniprotUpdate";
        // negative interactions
        String negativeInteractions = "select distinct(i3.ac) from Component c3 join c3.interaction as i3 join " +
                "i3.annotations as a2 where a2.cvTopic.shortLabel = :negative";
        // interactors with an uniprot identity cross reference
        String interactorUniprotIdentity = "select distinct(p2.ac) from InteractorImpl p2 join p2.xrefs as refs where " +
                "refs.cvDatabase.identifier = :uniprot and refs.cvXrefQualifier.identifier = :identity";
        // interactors which are not a protein
        String nonProteinInteractor = "select distinct(p3.ac) from InteractorImpl p3 where p3.objClass <> :protein)";
        // interactions with at least one interactor which either doesn't have any uniprot identity cross reference or is not a protein
        String interactionInvolvingNonUniprotOrNonProtein = "select distinct(i4.ac) from Component c4 join " +
                "c4.interaction as i4 join c4.interactor as p4 where p4.ac not in ("+interactorUniprotIdentity+") or p4.ac " +
                "in ("+nonProteinInteractor+")";

        String interactionsFromExperimentNoExport = "select distinct(i7.ac) from InteractionImpl i7 " +
                "join i7.experiments as e3 join e3.annotations as an3 where an3.cvTopic.shortLabel = :drExport " +
                "and trim(upper(an3.annotationText)) = :no";
        String interactionsFromExperimentExportYes = "select distinct(i8.ac) from InteractionImpl i8 " +
                "join i8.experiments as e4 join e4.annotations as an4 where an4.cvTopic.shortLabel = :drExport " +
                "and trim(upper(an4.annotationText)) = :yes";
        String interactionsFromExperimentExportConditional = "select distinct(i9.ac) from InteractionImpl i9 " +
                "join i9.annotations as an5 join i9.experiments as e5 join e5.annotations as an6 where " +
                "an6.cvTopic.shortLabel = :drExport and an5.cvTopic.identifier = :confidence and trim(upper(an5.annotationText)) = trim(upper(an6.annotationText))";
        String interactionsFromExperimentExportSpecified = "select distinct(i10.ac) from InteractionImpl i10 " +
                "join i10.experiments as e6 join e6.annotations as an7 where an7.cvTopic.shortLabel = :drExport";

        String interactionsDrExportNotPassed = "select distinct(i11.ac) from InteractionImpl i11 " +
                "where i11.ac in (" + interactionsFromExperimentExportSpecified + ") " +
                "and i11.ac not in (" + interactionsFromExperimentExportYes + ") " +
                "and i11.ac not in ("+interactionsFromExperimentExportConditional+")";

        String queryString = "select distinct(i.ac) from InteractionImpl i where i.ac in ("+interactionsInvolvedInComponents + ") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ")";
        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        /*Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery("select distinct(i.ac) from InteractionImpl i join i.components c join c.interactor p " +
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
                "where p5.objClass <> :protein))");*/
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", CvTopic.AUTHOR_CONFIDENCE_MI_REF);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);
        query.setParameter("protein", "uk.ac.ebi.intact.model.ProteinImpl");

        return query.getResultList();
    }

    private List<String> getInteractionAcsFromReleasedExperimentsToBeProcessedForUniprotExport(){

        // interactions with at least one 'accepted' experiment
        String interactionsAccepted = "select distinct(i2.ac) from Component c1 join c1.interaction as i2 join i2.experiments as e " +
                "join e.annotations as an where an.cvTopic.shortLabel = :accepted or trunc(e.created) < to_date(:september2005, :dateFormat)";
        // interactions with at least one experiment 'on-hold'
        String interactionsOnHold = "select distinct(i3.ac) from Component c2 join c2.interaction as i3 join i3.experiments" +
                " as e2 join e2.annotations as an2 where an2.cvTopic.shortLabel = :onhold";
        // interactions with at least one interactor with the annotation 'no-uniprot-update'
        String interactionsInvolvingInteractorsNoUniprotUpdate = "select distinct(i4.ac) from Component c3 join " +
                "c3.interaction as i4 join c3.interactor as p join p.annotations as a where a.cvTopic.shortLabel = :noUniprotUpdate";
        // negative interactions
        String negativeInteractions = "select distinct(i5.ac) from Component c4 join c4.interaction as i5 join " +
                "i5.annotations as a2 where a2.cvTopic.shortLabel = :negative";
        // interactors with an uniprot identity cross reference
        String interactorUniprotIdentity = "select distinct(p2.ac) from InteractorImpl p2 join p2.xrefs as refs where " +
                "refs.cvDatabase.identifier = :uniprot and refs.cvXrefQualifier.identifier = :identity";
        // interactors which are not a protein
        String nonProteinInteractor = "select distinct(p3.ac) from InteractorImpl p3 where p3.objClass <> :protein)";
        // interactions with at least one interactor which either doesn't have any uniprot identity cross reference or is not a protein
        String interactionInvolvingNonUniprotOrNonProtein = "select distinct(i6.ac) from Component c5 join " +
                "c5.interaction as i6 join c5.interactor as p4 where p4.ac not in ("+interactorUniprotIdentity+") or p4.ac " +
                "in ("+nonProteinInteractor+")";

        String interactionsFromExperimentNoExport = "select distinct(i7.ac) from InteractionImpl i7 " +
                "join i7.experiments as e3 join e3.annotations as an3 where an3.cvTopic.shortLabel = :drExport " +
                "and trim(upper(an3.annotationText)) = :no";
        String interactionsFromExperimentExportYes = "select distinct(i8.ac) from InteractionImpl i8 " +
                "join i8.experiments as e4 join e4.annotations as an4 where an4.cvTopic.shortLabel = :drExport " +
                "and trim(upper(an4.annotationText)) = :yes";
        String interactionsFromExperimentExportConditional = "select distinct(i9.ac) from InteractionImpl i9 " +
                "join i9.annotations as an5 join i9.experiments as e5 join e5.annotations as an6 where " +
                "an6.cvTopic.shortLabel = :drExport and an5.cvTopic.identifier = :confidence and trim(upper(an5.annotationText)) = trim(upper(an6.annotationText))";
        String interactionsFromExperimentExportSpecified = "select distinct(i10.ac) from InteractionImpl i10 " +
                "join i10.experiments as e6 join e6.annotations as an7 where an7.cvTopic.shortLabel = :drExport";

        String interactionsDrExportNotPassed = "select distinct(i11.ac) from InteractionImpl i11 " +
                "where i11.ac in (" + interactionsFromExperimentExportSpecified + ") " +
                "and i11.ac not in (" + interactionsFromExperimentExportYes + ") " +
                "and i11.ac not in ("+interactionsFromExperimentExportConditional+")";

        String queryString = "select distinct(i.ac) from InteractionImpl i " +
                "where i.ac in ("+interactionsAccepted + ") " +
                "and i.ac not in ("+interactionsOnHold+") " +
                "and i.ac not in (" + interactionsDrExportNotPassed + ") " +
                "and i.ac not in (" + interactionsFromExperimentNoExport + ") " +
                "and i.ac not in ("+interactionsInvolvingInteractorsNoUniprotUpdate+") " +
                "and i.ac not in ("+negativeInteractions+") " +
                "and i.ac not in ("+interactionInvolvingNonUniprotOrNonProtein+")";

        // we want all the interactions which :
        // no participant has a 'no-uniprot-update' annotation
        // the interaction doesn't have any 'negative' annotation
        // the participants have a uniprot 'identity' cross reference
        // the participants are proteins
        /*Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery("select distinct(i.ac) from InteractionImpl i join i.components c join c.interactor p " +
                "where i.ac in (select ie from Component c5 join c5.interaction ie join ie.experiments e join e.annotations an " +
                "where an.cvTopic.shortLabel = :accepted) " +
                "and i.ac not in (select ie from Component c6 join c6.interaction ie join ie.experiments e join e.annotations an " +
                "where an.cvTopic.shortLabel = :onhold)" +
                "and i.ac not in (select distinct(i2.ac) from Component c2 join c2.interaction i2 join c2.interactor p2 join p2.annotations a " +
                "where a.cvTopic.shortLabel = :noUniprotUpdate) " +
                "and i.ac not in (select distinct(i3.ac) from Component c3 join c3.interaction i3 join i3.annotations a2 " +
                "where a2.cvTopic.shortLabel = :negative) " +
                "and i.ac not in (select distinct(i4.ac) from Component c4 join c4.interaction i4 join c4.interactor p3 " +
                "where p3.ac not in (select distinct(p4.ac) from InteractorImpl p4 join p4.xrefs refs " +
                "where refs.cvDatabase.identifier = :uniprot " +
                "and refs.cvXrefQualifier.identifier = :identity)" +
                "or p3.ac in (select distinct(p5.ac) from InteractorImpl p5 " +
                "where p5.objClass <> :protein))");*/
        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery(queryString);

        query.setParameter("accepted", CvTopic.ACCEPTED);
        query.setParameter("september2005", "01/09/2005");
        query.setParameter("dateFormat", "dd/mm/yyyy");
        query.setParameter("onhold", CvTopic.ON_HOLD);
        query.setParameter("drExport", CvTopic.UNIPROT_DR_EXPORT);
        query.setParameter("no", "NO");
        query.setParameter("yes", "YES");
        query.setParameter("confidence", CvTopic.AUTHOR_CONFIDENCE_MI_REF);
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

    private void processEligibleBinaryInteractions(List<String> interactionAcs, List<String> eligibleInteractions) {

        // process each interaction of the list
        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

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
                    //else if (hasPassedDrExportAnnotation(interaction, experiment)){
                    else {
                        eligibleInteractions.add(interaction.getAc());
                        break;
                    }
                } // i's experiments
            }
        } // i
    }

    private void processEligibleExperimentsWithCurrentRules(IntActInteractionClusterScore cluster, List<Integer> eligibleInteractions) {

        // process each interaction of the list
        for (Map.Entry<Integer, EncoreInteraction> interactionEntry : cluster.getInteractionMapping().entrySet()) {

            EncoreInteraction interaction = interactionEntry.getValue();

            // get the Encore interaction object
            if (interaction != null){
                System.out.println("\t\t Interaction: Id:" + interaction.getId());

                Collection<String> interactionsAcs = interaction.getExperimentToPubmed().keySet();

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

    public List<String> extractInteractionsFromReleasedExperimentsPossibleToExport(String fileForListOfInteractions) throws SQLException, IOException {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        List<String> interactionsToBeProcessedForExport = getInteractionAcsFromReleasedExperimentsToBeProcessedForUniprotExport();

        System.out.println(interactionsToBeProcessedForExport.size() + " will be processed for a possible uniprot export.");

        List<String> interactions = extractInteractionsPossibleToExport(interactionsToBeProcessedForExport, fileForListOfInteractions);
        System.out.println(interactions.size() + " will be kept for Mi scoring.");

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<Integer> extractInteractionsFromReleasedExperimentsExportedInUniprot(IntActInteractionClusterScore cluster, String fileForListOfInteractions) throws SQLException, IOException {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        System.out.println(cluster.getInteractionMapping().size() + " will be processed for a possible uniprot export.");

        List<Integer> interactions = extractInteractionsCurrentlyExported(cluster, fileForListOfInteractions);
        System.out.println(interactions.size() + " will be kept for Mi scoring.");

        dataContext.commitTransaction(transactionStatus);

        return interactions;
    }

    public List<String> extractInteractionsWithoutRulesForInteractionDetectionMethod(String fileForListOfInteractions) throws SQLException, IOException {

        final DataContext dataContext = IntactContext.getCurrentInstance().getDataContext();

        TransactionStatus transactionStatus = dataContext.beginTransaction();

        List<String> interactionsToBeProcessedForExport = getInteractionAcsToBeProcessedForUniprotExport();

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
        //List<String> eligibleInteractions = new ArrayList<String>();

        //processEligibleExperiments(potentiallyEligibleInteraction, eligibleInteractions );

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : potentiallyEligibleInteraction){
            writer.write(ac + "\n");
            writer.flush();
        }

        writer.close();

        return potentiallyEligibleInteraction;
    }

    /**
     * This method is using the current rules on the interaction detection method to decide if an interaction can be exported or not
     * @param cluster : the cluster containing the list of interaction to process
     * @param fileForListOfInteractions : the name of the file where we want to write the list of interactions Acs currently exported
     * @return the list of encore interactions id which are exported in uniprot
     * @throws SQLException
     * @throws IOException
     */
    private List<Integer> extractInteractionsCurrentlyExported(IntActInteractionClusterScore cluster, String fileForListOfInteractions) throws SQLException, IOException {

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
                break;
            }
        }

        writer.close();
        return eligibleInteractions;
    }

    public List<String> extractBinaryInteractionsPossibleToExport(List<String> potentiallyEligibleInteraction, String fileForListOfInteractions) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        processEligibleBinaryInteractions(potentiallyEligibleInteraction, eligibleInteractions );

        FileWriter writer = new FileWriter(fileForListOfInteractions);

        for (String ac : potentiallyEligibleInteraction){
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
