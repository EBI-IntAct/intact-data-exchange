package uk.ac.ebi.intact.util.uniprotExport.exporters;

import org.apache.commons.collections.CollectionUtils;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.results.ExportedClusteredInteractions;
import uk.ac.ebi.intact.util.uniprotExport.results.clusters.IntactCluster;

import java.util.*;

/**
 * Contains utility methods for the exporter rules
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/03/11</pre>
 */

public class ExporterUtils {

    /**
     *
     * @param interaction : the negative interaction to process
     * @param positiveInteractions : the clustered positive interactions with the list of exported positive interactions
     * @return true if the negative interaction is matching a positive interaction having the same pubmed id
     */
    public static boolean isNegativeInteractionEligibleForUniprotExport(EncoreInteractionForScoring interaction, ExportedClusteredInteractions positiveInteractions){

        // the negative interaction must be not null and the results of exported positive interactions must be non null
        if (interaction != null && positiveInteractions != null){

            // collect first uniprot ac
            String uniprot1 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsA());

            if (uniprot1 == null){
                uniprot1 = FilterUtils.extractUniprotAcFromOtherAccs(interaction.getOtherInteractorAccsA());
            }

            // collect second uniprot ac
            String uniprot2 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsB());

            if (uniprot2 == null){
                uniprot2 = FilterUtils.extractUniprotAcFromOtherAccs(interaction.getOtherInteractorAccsB());
            }

            // both uniprot ac must be non null
            if (uniprot1 != null && uniprot2 != null){
                // extract parent ac 1
                String parentAc1 = uniprot1;

                if (uniprot1.contains("-")){
                    int index = uniprot1.indexOf("-");
                    parentAc1 = uniprot1.substring(0, index);
                }

                // extract parent ac 2
                String parentAc2 = uniprot2;

                if (uniprot2.contains("-")){
                    int index = uniprot2.indexOf("-");
                    parentAc2 = uniprot2.substring(0, index);
                }

                // the positive cluster
                IntactCluster cluster = positiveInteractions.getCluster();

                // list of positive interaction ids attached to the first interactor
                Set<Integer> positiveInteractions1 = new HashSet<Integer>();
                // list of negative interaction ids attached to the second interactor
                Set<Integer> positiveInteractions2 = new HashSet<Integer>();

                // the sorted list of interactors in the cluster
                SortedSet<String> interactors = new TreeSet(cluster.getInteractorCluster().keySet());

                // colect interactors starting with first or second parent ac
                for (String interactor : interactors){

                    // TODO self interactions cannnot be negative?

                    // interactor belongs to the first uniprot entry
                    if (interactor.startsWith(parentAc1)){
                        positiveInteractions1.addAll(cluster.getInteractorCluster().get(interactor));
                    }
                    // interactor belongs to the second uniprot entry
                    else if (interactor.startsWith(parentAc2)){
                        positiveInteractions2.addAll(cluster.getInteractorCluster().get(interactor));
                    }
                }

                // we have positive interaction ids for the uniprot entry 1 and the uniprot entry 2
                if (!positiveInteractions1.isEmpty() && !positiveInteractions2.isEmpty()){

                    // collect the list of interactions ids wich are common to the uniprot entry 1 and 2
                    Collection<Integer> intersectionPositiveInteractions = CollectionUtils.intersection(positiveInteractions1, positiveInteractions2);

                    // collect the interaction ids which are common to the uniprot entry 1 and 2 and which are exported
                    Collection<Integer> intersectionExported = CollectionUtils.intersection(positiveInteractions.getInteractionsToExport(), intersectionPositiveInteractions);

                    // if we have positive interaction exported
                    if (!intersectionExported.isEmpty()){

                        // list of pubmed ids for the negative interaction
                        Set<String> pubmedIdsNegative = FilterUtils.extractPubmedIdsFrom(interaction.getPublicationIds());

                        // for each exported positive interaction
                        for (Integer positiveId : intersectionExported){
                            // the positive interaction
                            EncoreInteractionForScoring positiveInteraction = cluster.getEncoreInteractionCluster().get(positiveId);

                            // the list of pubmed ids for the positive interaction
                            Set<String> pubmedIdsPositive = FilterUtils.extractPubmedIdsFrom(positiveInteraction.getPublicationIds());

                            // the common list of pubmed ids for the negative and positive interaction
                            Collection<String> pubmedIntersection = CollectionUtils.intersection(pubmedIdsNegative, pubmedIdsPositive);

                            // if at least one pubmed id is common, the negative interaction is eligible for uniprot export
                            if (!pubmedIntersection.isEmpty()){
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

        /**
     *
     * @param interaction : the negative interaction to process
     * @param positiveInteractions : the clustered positive interactions with the list of exported positive interactions
     * @return true if the negative interaction is matching a positive interaction having the same pubmed id
     */
    public static boolean isNegativeInteractionEligibleForUniprotExport(BinaryInteraction<Interactor> interaction, ExportedClusteredInteractions positiveInteractions){

        // the negative interaction must be not null and the results of exported positive interactions must be non null
        if (interaction != null && positiveInteractions != null){

            // collect first uniprot ac
            String uniprot1 = FilterUtils.extractUniprotAcFromCrossReferences(interaction.getInteractorA().getIdentifiers());

            if (uniprot1 == null){
                uniprot1 = FilterUtils.extractUniprotAcFromCrossReferences(interaction.getInteractorA().getAlternativeIdentifiers());
            }

            // collect second uniprot ac
            String uniprot2 = FilterUtils.extractUniprotAcFromCrossReferences(interaction.getInteractorB().getIdentifiers());

            if (uniprot2 == null){
                uniprot2 = FilterUtils.extractUniprotAcFromCrossReferences(interaction.getInteractorB().getAlternativeIdentifiers());
            }

            // both uniprot ac must be non null
            if (uniprot1 != null && uniprot2 != null){
                // extract parent ac 1
                String parentAc1 = uniprot1;

                if (uniprot1.contains("-")){
                    int index = uniprot1.indexOf("-");
                    parentAc1 = uniprot1.substring(0, index);
                }

                // extract parent ac 2
                String parentAc2 = uniprot2;

                if (uniprot2.contains("-")){
                    int index = uniprot2.indexOf("-");
                    parentAc2 = uniprot2.substring(0, index);
                }

                // the positive cluster
                IntactCluster cluster = positiveInteractions.getCluster();

                // list of positive interaction ids attached to the first interactor
                Set<Integer> positiveInteractions1 = new HashSet<Integer>();
                // list of negative interaction ids attached to the second interactor
                Set<Integer> positiveInteractions2 = new HashSet<Integer>();

                // the sorted list of interactors in the cluster
                SortedSet<String> interactors = new TreeSet(cluster.getInteractorCluster().keySet());

                // colect interactors starting with first or second parent ac
                for (String interactor : interactors){

                    // TODO self interactions cannnot be negative?

                    // interactor belongs to the first uniprot entry
                    if (interactor.startsWith(parentAc1)){
                        positiveInteractions1.addAll(cluster.getInteractorCluster().get(interactor));
                    }
                    // interactor belongs to the second uniprot entry
                    else if (interactor.startsWith(parentAc2)){
                        positiveInteractions2.addAll(cluster.getInteractorCluster().get(interactor));
                    }
                }

                // we have positive interaction ids for the uniprot entry 1 and the uniprot entry 2
                if (!positiveInteractions1.isEmpty() && !positiveInteractions2.isEmpty()){

                    // collect the list of interactions ids wich are common to the uniprot entry 1 and 2
                    Collection<Integer> intersectionPositiveInteractions = CollectionUtils.intersection(positiveInteractions1, positiveInteractions2);

                    // collect the interaction ids which are common to the uniprot entry 1 and 2 and which are exported
                    Collection<Integer> intersectionExported = CollectionUtils.intersection(positiveInteractions.getInteractionsToExport(), intersectionPositiveInteractions);

                    // if we have positive interaction exported
                    if (!intersectionExported.isEmpty()){

                        // list of pubmed ids for the negative interaction
                        Set<String> pubmedIdsNegative = FilterUtils.extractPubmedIdsFrom(interaction.getPublications());

                        // for each exported positive interaction
                        for (Integer positiveId : intersectionExported){
                            // the positive interaction
                            BinaryInteraction<Interactor> positiveInteraction = cluster.getBinaryInteractionCluster().get(positiveId);

                            // the list of pubmed ids for the positive interaction
                            Set<String> pubmedIdsPositive = FilterUtils.extractPubmedIdsFrom(positiveInteraction.getPublications());

                            // the common list of pubmed ids for the negative and positive interaction
                            Collection<String> pubmedIntersection = CollectionUtils.intersection(pubmedIdsNegative, pubmedIdsPositive);

                            // if at least one pubmed id is common, the negative interaction is eligible for uniprot export
                            if (!pubmedIntersection.isEmpty()){
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
