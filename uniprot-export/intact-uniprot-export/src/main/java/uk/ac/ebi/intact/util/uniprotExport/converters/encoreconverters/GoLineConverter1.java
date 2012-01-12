package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.*;

/**
 * Converts an EncoreInteraction into a GOParameter, format 1.
 *
 * Cannot convert feature chains, need to remap to the parent.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class GoLineConverter1 implements GoLineConverter<GOParameters1> {
    private static final Logger logger = Logger.getLogger(GoLineConverter1.class);

    /*
     *map containing the second interactor as a key and the list of pubmed ids associated with this second interactor
      */
    private Map<String, Set<String>> clusteredInteractionWithFeatureChains = new HashMap<String, Set<String>>();
    /*
     * map containing the second interactor as a key and the list of pubmed ids associated with this second interactor. The first interactor is an isoform
      */
    private Map<String, Set<String>> isoformClusteredInteractionWithFeatureChains = new HashMap<String, Set<String>>();
    /*
     *map associating for each second interactor what is the first interactor (can be master protein or isoform)
      */
    private Map<String, String> mapOfFirstInteractors = new HashMap<String, String>();

    public GoLineConverter1(){
        clusteredInteractionWithFeatureChains = new HashMap<String, Set<String>>();
        isoformClusteredInteractionWithFeatureChains = new HashMap<String, Set<String>>();
        mapOfFirstInteractors = new HashMap<String, String>();
    }

    private void clear(){
        clusteredInteractionWithFeatureChains.clear();
        isoformClusteredInteractionWithFeatureChains.clear();
        mapOfFirstInteractors.clear();
    }

    /**
     * Converts an EncoreInteraction into GOParameters
     * @param interaction
     * @param firstInteractor
     * @return The converted GOParameters
     */
    public List<GOParameters1> convertInteractionIntoGOParameters(EncoreInteractionForScoring interaction, String firstInteractor, MiClusterContext context){
        // extract the uniprot acs of the firts and second interactors
        String uniprot1;
        String uniprot2;

        if (interaction.getInteractorAccsA().containsKey(WriterUtils.UNIPROT)){
            uniprot1 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsA());
        }
        else {
            uniprot1 = FilterUtils.extractUniprotAcFromOtherAccs(interaction.getOtherInteractorAccsA());
        }
        if (interaction.getInteractorAccsB().containsKey(WriterUtils.UNIPROT)){
            uniprot2 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsB());
        }
        else {
            uniprot2 = FilterUtils.extractUniprotAcFromOtherAccs(interaction.getOtherInteractorAccsB());
        }

        // if the uniprot acs are not null, it is possible to create a GOParameter
        if (uniprot1 != null && uniprot2 != null){
            String fixedUniprot1 = uniprot1;
            String fixedUniprot2 = uniprot2;

            if (uniprot1.contains(WriterUtils.CHAIN_PREFIX)){
                fixedUniprot1 = uniprot1.substring(0, uniprot1.indexOf(WriterUtils.CHAIN_PREFIX));
            }

            if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                fixedUniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
            }
            // build a pipe separated list of pubmed IDs
            Set<String> pubmedIds = FilterUtils.extractPubmedIdsFrom(interaction.getPublicationIds());

            // if the list of pubmed ids is not empty, the GOParameter is created
            if (!pubmedIds.isEmpty()){
                logger.debug("convert GO parameters for " + uniprot1 + ", " + uniprot2 + ", " + pubmedIds.size() + " pubmed ids");
                GOParameters1 parameters;

                if (uniprot1.equalsIgnoreCase(firstInteractor)){
                    parameters = new GOParameters1(fixedUniprot1, fixedUniprot2, pubmedIds);
                }
                else{
                    parameters = new GOParameters1(fixedUniprot2, fixedUniprot1, pubmedIds);
                }

                return Arrays.asList(parameters);
            }
            logger.warn("No pubmed ids for "+uniprot1+" and "+uniprot2+", cannot convert into GOLines");
        }

        logger.warn("one of the uniprot ac is null, cannot convert into GOLines");
        return Collections.EMPTY_LIST;
    }

    /**
     * Converts a list of EncoreInteractions into a single GOParameters (only the master uniprot ac of the interactors of the first interaction will be used )
     * @param interactions : list of encore interactions involving the same interactors or feature chains of a same entry
     * @return The converted GOParameters
     */
    public List<GOParameters1> convertInteractionsIntoGOParameters(Set<EncoreInteractionForScoring> interactions, String parentAc, MiClusterContext context){
        List<GOParameters1> goParameters = new ArrayList<GOParameters1>(interactions.size());

        clear();

        // for each binary interaction associated with the same uniprot entry given with parentAc
        for (EncoreInteractionForScoring interaction : interactions){
            // extract the uniprot acs of the first and second interactors for the first interaction
            String uniprot1;
            String uniprot2;

            if (interaction.getInteractorAccsA().containsKey(WriterUtils.UNIPROT)){
                uniprot1 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsA());
            }
            else {
                uniprot1 = FilterUtils.extractUniprotAcFromOtherAccs(interaction.getOtherInteractorAccsA());
            }
            if (interaction.getInteractorAccsB().containsKey(WriterUtils.UNIPROT)){
                uniprot2 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsB());
            }
            else {
                uniprot2 = FilterUtils.extractUniprotAcFromOtherAccs(interaction.getOtherInteractorAccsB());
            }

            // if the uniprot acs are not null, it is possible to create a GOParameter
            if (uniprot1 != null && uniprot2 != null){

                // build a pipe separated list of pubmed IDs
                Set<String> pubmedIds = FilterUtils.extractPubmedIdsFrom(interaction.getPublicationIds());

                // if the list of pubmed ids is not empty, the GOParameter is created
                if (!pubmedIds.isEmpty()){

                    // the first interactor is uniprot1 and the second uniprot is a different uniprot entry
                    if (uniprot1.startsWith(parentAc) && !uniprot2.startsWith(parentAc)){
                        processGoParameters(parentAc, goParameters, clusteredInteractionWithFeatureChains, isoformClusteredInteractionWithFeatureChains, mapOfFirstInteractors, uniprot1, uniprot2, pubmedIds);
                    }
                    // the first interactor is uniprot2 and the uniprot 1 is a different uniprot entry
                    else if (uniprot2.startsWith(parentAc) && !uniprot1.startsWith(parentAc)) {
                        processGoParameters(parentAc, goParameters, clusteredInteractionWithFeatureChains, isoformClusteredInteractionWithFeatureChains, mapOfFirstInteractors, uniprot2, uniprot1, pubmedIds);
                    }
                    // the two interactors are identical, we have a self interaction
                    else if (uniprot1.equalsIgnoreCase(uniprot2)){
                        GOParameters1 parameter = new GOParameters1(uniprot1, uniprot2, pubmedIds);

                        goParameters.add(parameter);
                    }
                    // the two interactors are from the same uniprot entry but are different isoforms/feature chains : we have a single interaction but two lines
                    else if (uniprot2.startsWith(parentAc) && uniprot1.startsWith(parentAc)) {
                        processGoParameters(parentAc, goParameters, clusteredInteractionWithFeatureChains, isoformClusteredInteractionWithFeatureChains, mapOfFirstInteractors, uniprot1, uniprot2, pubmedIds);
                        processGoParameters(parentAc, goParameters, clusteredInteractionWithFeatureChains, isoformClusteredInteractionWithFeatureChains, mapOfFirstInteractors, uniprot2, uniprot1, pubmedIds);
                    }
                    else {
                        logger.info("The interaction "+uniprot1+" and "+uniprot2+" is ignored because both interactors are not matching the master uniprot ac");
                    }
                }
                else{
                    logger.error("No pubmed ids for "+uniprot1+" and "+uniprot2+", cannot convert into GOLines");
                }
            }
            else{
                logger.error("one of the uniprot ac is null, cannot convert into GOLines");
            }
        }

        // we need to build go parameters when we have master proteins/feature chains = first interactor
        if (!clusteredInteractionWithFeatureChains.isEmpty()){
            for (Map.Entry<String, Set<String>> entry : clusteredInteractionWithFeatureChains.entrySet()){
                String secondInteractor = entry.getKey();
                // by default the first interactor of merged entries is the master protein
                String master = parentAc;

                GOParameters1 parameter = new GOParameters1(master, secondInteractor, entry.getValue());
                goParameters.add(parameter);
            }
        }

        // we need to build go parameters when we have isoform = first interactor and feature chain = second interactor
        if (!isoformClusteredInteractionWithFeatureChains.isEmpty()){
            for (Map.Entry<String, Set<String>> entry : isoformClusteredInteractionWithFeatureChains.entrySet()){
                String secondInteractor = entry.getKey();
                // by default the first interactor of merged entries is the master protein but it should be an isoform
                String isoform = parentAc;

                // if an isoform did interact with feature chain, we collect the first interactor
                if (mapOfFirstInteractors.containsKey(secondInteractor)){

                    isoform = mapOfFirstInteractors.get(secondInteractor);
                }
                GOParameters1 parameter = new GOParameters1(isoform, secondInteractor, entry.getValue());
                goParameters.add(parameter);
            }
        }

        clear();

        return goParameters;
    }

    private void processGoParameters(String parentAc, List<GOParameters1> goParameters, Map<String, Set<String>> clusteredInteractionWithFeatureChains, Map<String, Set<String>> isoformClusteredInteractionWithFeatureChains, Map<String, String> mapOfFirstInteractors, String uniprot1, String uniprot2, Set<String> pubmedIds) {
        // the first uniprotAc is the master protein or is a feature chain of the master protein
        if (uniprot1.equalsIgnoreCase(parentAc) || (uniprot1.startsWith(parentAc) && uniprot1.contains(WriterUtils.CHAIN_PREFIX))){
            // if the second interactor is a feature chain, we must remap to its parent
            if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                uniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
            }

            // we add the list of pubmed ids associated with second interactor in the map in case we need to merge feature chain information
            if (clusteredInteractionWithFeatureChains.containsKey(uniprot2)){

                Set<String> interactionList = clusteredInteractionWithFeatureChains.get(uniprot2);
                interactionList.addAll(pubmedIds);
            }
            else{

                clusteredInteractionWithFeatureChains.put(uniprot2, pubmedIds);
            }
        }
        // the first uniprotAc is an isoform of the master uniprot entry and the second uniprot ac is a feature chain or master uniprot
        else if (uniprot1.startsWith(parentAc) && (uniprot2.contains(WriterUtils.CHAIN_PREFIX) || UniprotExportUtils.isMasterProtein(uniprot2))){
            // we must remap the feature chain to its parent
            if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                uniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
            }

            // we must merge the information of this interaction with the one of the master protein if it exists
            if (isoformClusteredInteractionWithFeatureChains.containsKey(uniprot2)){

                Set<String> interactionList = isoformClusteredInteractionWithFeatureChains.get(uniprot2);
                interactionList.addAll(pubmedIds);
            }
            else{

                isoformClusteredInteractionWithFeatureChains.put(uniprot2, pubmedIds);
            }

            // it is not the master protein so we need to keep a trace of what was the first interactor
            if (!mapOfFirstInteractors.containsKey(uniprot2)){

                mapOfFirstInteractors.put(uniprot2, uniprot1);
            }
        }
        // the first interactor is an isoform and the second interactor is isoform or master protein
        else{

            GOParameters1 parameter = new GOParameters1(uniprot1, uniprot2, pubmedIds);

            goParameters.add(parameter);
        }
    }
}
