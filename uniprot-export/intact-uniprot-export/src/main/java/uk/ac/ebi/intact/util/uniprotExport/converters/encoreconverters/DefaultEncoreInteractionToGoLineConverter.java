package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.DefaultGOParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.*;

/**
 * Converts an EncoreInteraction into a GOParameter. It is the default converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class DefaultEncoreInteractionToGoLineConverter implements EncoreInteractionToGoLineConverter{
    private static final Logger logger = Logger.getLogger(DefaultEncoreInteractionToGoLineConverter.class);

    /**
     * Converts an EncoreInteraction into GOParameters
     * @param interaction
     * @return The converted GOParameters
     */
    public GOParameters convertInteractionIntoGOParameters(EncoreInteraction interaction, String firstInteractor){
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
            if (uniprot1.contains(WriterUtils.CHAIN_PREFIX)){
                uniprot1 = uniprot1.substring(0, uniprot1.indexOf(WriterUtils.CHAIN_PREFIX));
            }

            if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                uniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
            }
            // build a pipe separated list of pubmed IDs
            Set<String> pubmedIds = FilterUtils.extractPubmedIdsFrom(interaction.getPublicationIds());

            // if the list of pubmed ids is not empty, the GOParameter is created
            if (!pubmedIds.isEmpty()){
                logger.debug("convert GO parameters for " + uniprot1 + ", " + uniprot2 + ", " + pubmedIds.size() + " pubmed ids");
                GOParameters parameters;

                if (uniprot1.equalsIgnoreCase(firstInteractor)){
                    parameters = new DefaultGOParameters(uniprot1, uniprot2, pubmedIds);
                }
                else{
                    parameters = new DefaultGOParameters(uniprot2, uniprot1, pubmedIds);
                }

                return parameters;
            }
            logger.debug("No pubmed ids for "+uniprot1+" and "+uniprot2+", cannot convert into GOLines");
        }

        logger.debug("one of the uniprot ac is null, cannot convert into GOLines");
        return null;
    }

    /**
     * Converts a list of EncoreInteractions into a single GOParameters (only the master uniprot ac of the interactors of the first interaction will be used )
     * @param interactions : list of encore interactions involving the same interactors or feature chains of a same entry
     * @return The converted GOParameters
     */
    public List<GOParameters> convertInteractionsIntoGOParameters(List<EncoreInteraction> interactions, String parentAc){
        List<GOParameters> goParameters = new ArrayList<GOParameters>(interactions.size());

        Map<String, Set<String>> clusteredInteractionWithFeatureChains = new HashMap<String, Set<String>>();

        for (EncoreInteraction interaction : interactions){
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
                    logger.debug("convert GO parameters for " + uniprot1 + ", " + uniprot2 + ", " + pubmedIds.size() + " pubmed ids");
                    GOParameters parameters;

                    if (uniprot1.equalsIgnoreCase(parentAc) || (uniprot1.startsWith(parentAc) && uniprot1.contains(WriterUtils.CHAIN_PREFIX))){
                        if (clusteredInteractionWithFeatureChains.containsKey(uniprot2)){
                            Set<String> interactionList = clusteredInteractionWithFeatureChains.get(uniprot2);
                            interactionList.addAll(pubmedIds);
                        }
                        else{

                            clusteredInteractionWithFeatureChains.put(uniprot2, pubmedIds);
                        }
                    }
                    else if (uniprot2.equalsIgnoreCase(parentAc) || (uniprot2.startsWith(parentAc) && uniprot2.contains(WriterUtils.CHAIN_PREFIX))) {
                        if (clusteredInteractionWithFeatureChains.containsKey(uniprot1)){
                            Set<String> interactionList = clusteredInteractionWithFeatureChains.get(uniprot1);
                            interactionList.addAll(pubmedIds);
                        }
                        else{

                            clusteredInteractionWithFeatureChains.put(uniprot1, pubmedIds);
                        }
                    }
                    else{
                        if (uniprot1.contains(WriterUtils.CHAIN_PREFIX)){
                            uniprot1 = uniprot1.substring(0, uniprot1.indexOf(WriterUtils.CHAIN_PREFIX));
                        }

                        if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                            uniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
                        }

                        logger.debug("convert GO parameters for " + uniprot1 + ", " + uniprot2 + ", " + pubmedIds.size() + " pubmed ids");
                        GOParameters parameter;

                        if (uniprot1.equalsIgnoreCase(parentAc)){
                            parameter = new DefaultGOParameters(uniprot1, uniprot2, pubmedIds);
                        }
                        else{
                            parameter = new DefaultGOParameters(uniprot2, uniprot1, pubmedIds);
                        }

                        goParameters.add(parameter);
                    }
                }
                else{
                    logger.debug("No pubmed ids for "+uniprot1+" and "+uniprot2+", cannot convert into GOLines");
                }
            }
            else{
                logger.debug("one of the uniprot ac is null, cannot convert into GOLines");
            }
        }

        if (!clusteredInteractionWithFeatureChains.isEmpty()){
            for (Map.Entry<String, Set<String>> entry : clusteredInteractionWithFeatureChains.entrySet()){
                String secondInteractor = entry.getKey();

                if (secondInteractor.contains(WriterUtils.CHAIN_PREFIX)){
                    secondInteractor = entry.getKey().substring(0, entry.getKey().indexOf(WriterUtils.CHAIN_PREFIX));
                }

                GOParameters parameter = new DefaultGOParameters(parentAc, secondInteractor, entry.getValue());

                goParameters.add(parameter);
            }
        }

        return goParameters;
    }
}
