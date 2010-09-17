package uk.ac.ebi.intact.util.uniprotExport;

import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.XrefDao;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.converters.Intact2BinaryInteractionConverter;
import uk.ac.ebi.intact.psimitab.converters.expansion.NotExpandableInteractionException;
import uk.ac.ebi.intact.util.uniprotExport.event.DrLineProcessedEvent;
import uk.ac.ebi.intact.util.uniprotExport.event.NonBinaryInteractionFoundEvent;

import javax.persistence.Query;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>16-Sep-2010</pre>
 */

public class InteractionExtractorForMIScore extends LineExport {

    private void processEligibleExperiments(List<String> interactionAcs, List<String> eligibleInteractions) {
        Set<Experiment> experimentNotExported = new HashSet<Experiment>();

        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

            String interactionAc = interactionAcs.get(i);
            Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            if (interaction != null){
                System.out.println("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

                Collection experiments = interaction.getExperiments();

                int expCount = experiments.size();
                System.out.println("\t\t\t interaction related to " + expCount + " experiment" + (expCount > 1 ? "s" : "") + ".");

                for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                    Experiment experiment = (Experiment) iterator2.next();

                    boolean experimentExport = false;

                    if (!experimentNotExported.contains(experiment)){
                        System.out.println("\t\t\t\t Experiment: Shortlabel:" + experiment.getShortLabel() + "  AC: " + experiment.getAc());

                        LineExport.ExperimentStatus experimentStatus = super.getCCLineExperimentExportStatus(experiment, "\t\t\t\t");

                        if (experimentStatus.doNotExport()) {
                            // forbid export for all interactions of that experiment (and their proteins).
                            System.out.println("\t\t\t\t\t No interactions of that experiment will be exported.");

                        } else if (experimentStatus.doExport()) {
                            // Authorise export for all interactions of that experiment (and their proteins),
                            // This overwrite the setting of the CvInteraction concerning the export.
                            System.out.println("\t\t\t\t\t All interaction of that experiment will be exported.");

                            experimentExport = true;

                        } else if (experimentStatus.isLargeScale()) {

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

                                /*
                                * We don't need to check an eventual threshold on the method level because
                                * in the current state, the annotation is on the experiment level that is
                                * lower and hence is dominant on the method's one.
                                */

                                experimentExport = true;
                                System.out.println("\t\t\t that interaction is eligible for export in the context of a large scale experiment");

                            } else {

                                System.out.println("\t\t\t interaction not eligible");
                            }

                        } else if (experimentStatus.isNotSpecified()) {

                            System.out.println("\t\t\t\t No experiment status, will computes the MI score of the interactions later to decide if the interaction can be exported or not.");

                            experimentExport = true;
                        } // experiment status not specified

                        if (experimentExport) {
                            eligibleInteractions.add(interaction.getAc());
                            System.out.println("The interaction " + interaction.getAc() + ", " + interaction.getShortLabel() + " has been kept for MI scoring");

                        }
                        else {
                            experimentNotExported.add(experiment);
                        }
                    }

                } // i's experiments
            }
            else {
                System.out.println("\t\t\t That interaction "+interactionAc +" is null, skip it.");
                continue; // skip that interaction
            }
        } // i
    }

    private void processEligibleExperimentsWithCurrentRules(List<String> interactionAcs, List<String> eligibleInteractions) {

        final int interactionCount = interactionAcs.size();
        for (int i = 0; i < interactionCount; i++) {

            String interactionAc = interactionAcs.get(i);
            Interaction interaction = IntactContext.getCurrentInstance().getDaoFactory().getInteractionDao().getByAc(interactionAc);

            getOut().println("\t\t Interaction: Shortlabel:" + interaction.getShortLabel() + "  AC: " + interaction.getAc());

            Collection experiments = interaction.getExperiments();

            int expCount = experiments.size();
            getOut().println("\t\t\t interaction related to " + expCount + " experiment" + (expCount > 1 ? "s" : "") + ".");

            for (Iterator iterator2 = experiments.iterator(); iterator2.hasNext();) {
                Experiment experiment = (Experiment) iterator2.next();

                boolean experimentExport = false;
                getOut().println("\t\t\t\t Experiment: Shortlabel:" + experiment.getShortLabel() + "  AC: " + experiment.getAc());

                ExperimentStatus experimentStatus = getCCLineExperimentExportStatus(experiment, "\t\t\t\t\t");
                if (experimentStatus.doNotExport()) {
                    // forbid export for all interactions of that experiment (and their proteins).
                    getOut().println("\t\t\t\t\t No interactions of that experiment will be exported.");

                } else if (experimentStatus.doExport()) {
                    // Authorise export for all interactions of that experiment (and their proteins),
                    // This overwrite the setting of the CvInteraction concerning the export.
                    getOut().println("\t\t\t\t\t All interaction of that experiment will be exported.");

                    experimentExport = true;

                } else if (experimentStatus.isLargeScale()) {

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

                            getOut().println("\t\t\t Interaction has " + authorConfidenceTopic.getShortLabel() + ": '" + text + "'");

                            if (text != null) {
                                text = text.trim();
                            }

                            for (Iterator iterator4 = keywords.iterator(); iterator4.hasNext() && !annotationFound;) {
                                String kw = (String) iterator4.next();
                                // NOT case sensitive

                                getOut().println("\t\t\t\t Compare it with '" + kw + "'");

                                if (kw.equalsIgnoreCase(text)) {
                                    annotationFound = true;
                                    getOut().println("\t\t\t\t\t Equals !");
                                }
                            }
                        }
                    }

                    if (annotationFound) {

                        /*
                        * We don't need to check an eventual threshold on the method level because
                        * in the current state, the annotation is on the experiment level that is
                        * lower and hence is dominant on the method's one.
                        */

                        experimentExport = true;
                        getOut().println("\t\t\t that interaction is eligible for export in the context of a large scale experiment");

                    } else {

                        getOut().println("\t\t\t interaction not eligible");
                    }

                } else if (experimentStatus.isNotSpecified()) {

                    getOut().println("\t\t\t\t No experiment status, check the experimental method.");

                    // Then check the experimental method (CvInteraction)
                    // Nothing specified at the experiment level, check for the method (CvInteraction)
                    CvInteraction cvInteraction = experiment.getCvInteraction();

                    if (null == cvInteraction) {
                        // we need to check because cvInteraction is not mandatory in an experiment.
                        continue; // skip it, go to next experiment
                    }

                    CvInteractionStatus methodStatus = getMethodExportStatus(cvInteraction, "\t\t");

                    if (methodStatus.doExport()) {

                        experimentExport = true;

                    } else if (methodStatus.doNotExport()) {

                        // do nothing

                    } else if (methodStatus.isNotSpecified()) {

                        // we should never get in here but just in case...
                        // do nothing

                    } else if (methodStatus.isConditionalExport()) {

                        getOut().println("\t\t\t\t As conditional export, check the count of distinct experiment for that method.");

                        // if the threshold is not reached, iterates over all available interactions to check if
                        // there is (are) one (many) that could allow to reach the threshold.

                        int threshold = methodStatus.getMinimumOccurence();

                        // we create a non redondant set of experiment identifier
                        // TODO couldn't that be a static collection that we empty regularly ?
                        Set experimentAcs = new HashSet(threshold);

                        // check if there are other experiments attached to the current interaction that validate it.
                        boolean enoughExperimentFound = false;
                        for (Iterator iterator = experiments.iterator(); iterator.hasNext();) {
                            Experiment experiment1 = (Experiment) iterator.next();

                            getOut().println("\t\t\t\t Experiment: Shortlabel:" + experiment1.getShortLabel() + "  AC: " + experiment1.getAc());

                            CvInteraction method = experiment1.getCvInteraction();

                            if (cvInteraction.equals(method)) {
                                experimentAcs.add(experiment1.getAc());

                                // we only update if we found one
                                enoughExperimentFound = (experimentAcs.size() >= threshold);
                            }
                        }

                        getOut().println("\t\t\tLooking for other interactions that support that method in other experiments...");

                        for (int j = 0; j < interactionCount && !enoughExperimentFound; j++) {

                            if (i == j) {
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

                            getOut().println("\t\t Interaction: Shortlabel:" + interaction2.getShortLabel() + "  AC: " + interaction2.getAc());

                            Collection experiments2 = interaction2.getExperiments();

                            for (Iterator iterator6 = experiments2.iterator(); iterator6.hasNext() && !enoughExperimentFound;)
                            {
                                Experiment experiment2 = (Experiment) iterator6.next();
                                getOut().println("\t\t\t\t Experiment: Shortlabel:" + experiment2.getShortLabel() + "  AC: " + experiment2.getAc());

                                CvInteraction method = experiment2.getCvInteraction();

                                if (cvInteraction.equals(method)) {
                                    experimentAcs.add(experiment2.getAc());
                                    // we only update if we found one
                                    enoughExperimentFound = (experimentAcs.size() >= threshold);
                                }
                            } // j's experiments

                            getOut().println("\t\t\t\t " + cvInteraction.getShortLabel() + ", threshold: " +
                                    threshold + " #experiment: " +
                                    (experimentAcs == null ? "none" : "" + experimentAcs.size()));
                        } // j

                        if (enoughExperimentFound) {
                            getOut().println("\t\t\t\t Enough experiemnt found");
                            experimentExport = true;
                        } else {
                            getOut().println("\t\t\t\t Not enough experiemnt found");
                        }

                    } // conditional status
                } // experiment status not specified

                if (experimentExport) {
                    eligibleInteractions.add(interaction.getAc());
                    System.out.println("The interaction " + interaction.getAc() + ", " + interaction.getShortLabel() + " has been kept for MI scoring");

                }
            } // i's experiments
        } // i
    }

    public List<String> extractInteractionsPossibleToExport(boolean useCurrentRules) throws SQLException, IOException {

        Query query = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().createQuery("select distinct(i.ac) from InteractionImpl i join i.components c join c.interactor p " +
                "where i.ac not in (select distinct(i2.ac) from Component c2 join c2.interaction i2 join i2.annotations a2 join c2.interactor p2 join p2.annotations a " +
                "where a.cvTopic.shortLabel = :noUniprotUpdate " +
                "or a2.cvTopic.shortLabel = :negative) " +
                "and p.ac in (select p3.ac from InteractorImpl p3 join p3.xrefs refs " +
                "where refs.cvDatabase.identifier = :uniprot and refs.cvXrefQualifier.identifier = :identity)");
        query.setParameter("noUniprotUpdate", CvTopic.NON_UNIPROT);
        query.setParameter("negative", CvTopic.NEGATIVE);
        query.setParameter("uniprot", CvDatabase.UNIPROT_MI_REF);
        query.setParameter("identity", CvXrefQualifier.IDENTITY_MI_REF);

        if (useCurrentRules){
            return extractInteractionsCurrentlyExported(query.getResultList());
        }
        return extractInteractionsPossibleToExport(query.getResultList());
    }

    public List<String> extractInteractionsPossibleToExport(List<String> potentiallyEligibleInteraction) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        processEligibleExperiments(potentiallyEligibleInteraction, eligibleInteractions );
        return eligibleInteractions;
    }

    public List<String> extractInteractionsCurrentlyExported(List<String> potentiallyEligibleInteraction) throws SQLException, IOException {

        System.out.println(potentiallyEligibleInteraction.size() + " interactions to process.");
        List<String> eligibleInteractions = new ArrayList<String>();

        processEligibleExperimentsWithCurrentRules(potentiallyEligibleInteraction, eligibleInteractions );
        return eligibleInteractions;
    }
}
