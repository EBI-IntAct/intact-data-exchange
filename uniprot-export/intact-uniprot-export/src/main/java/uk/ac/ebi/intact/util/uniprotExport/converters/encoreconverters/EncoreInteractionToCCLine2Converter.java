package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.bridges.taxonomy.TaxonomyServiceException;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.*;
import uk.ac.ebi.intact.util.uniprotExport.results.MethodAndTypePair;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.*;

/**
 * Converter of an EncoreInteraction into a CCParameter2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/03/11</pre>
 */

public class EncoreInteractionToCCLine2Converter extends AbstractEncoreInteractionToCCLineConverter {

    private static final Logger logger = Logger.getLogger(EncoreInteractionToCCLine1Converter.class);

    public EncoreInteractionToCCLine2Converter(){
        super();
    }

    /**
     *
     * @param interaction : the interaction
     * @param context : the context of the export
     * @return a sorted list of InteractionDetails extracted from the interaction
     */
    private SortedSet<InteractionDetails> sortInteractionDetails(EncoreInteraction interaction, MiClusterContext context){

        // map which associates IntAct interaction Ac to pubmed id
        Map<String, String> interactionToPubmed = interaction.getExperimentToPubmed();

        // map which associates a pubmed Id to a list of IntAct interaction acs
        Map<String, List<String>> pubmedToInteraction = WriterUtils.invertMapOfTypeStringToString(interactionToPubmed);

        // map which associates a couple {interaction detection method, interaction type} to a set of Pubmed ids
        Map<MethodAndTypePair, Set<String>> distinctInformationDetails = collectDistinctInteractionDetails(interaction, context);

        // map which associates a couple {interaction detection method, interaction type} to a set of IntAct interaction Acs
        Map<MethodAndTypePair, List<String>> method_typeToInteractions = WriterUtils.invertMapFromKeySelection(context.getInteractionToMethod_type(), interactionToPubmed.keySet());

        // the list with the interaction details
        SortedSet<InteractionDetails> sortedInteractionDetails = new TreeSet<InteractionDetails>();

        // for each couple {interaction detection method, interaction type}, sort the pubmed ids :
        // - pubmed ids only associated with spoke expanded interactions
        // - pubmed ids only associated with true binary interactions
        // - pubmed ids associated with both spoke expanded and true binary interactions. In this case, we consider the pubmed id as only associated with true binary interaction
        for (Map.Entry<MethodAndTypePair, Set<String>> ip : distinctInformationDetails.entrySet()){
            // the method is the key of the entry
            String method = context.getMiTerms().containsKey(ip.getKey().getMethod()) ? context.getMiTerms().get(ip.getKey().getMethod()) : ip.getKey().getMethod();

            // the type is the value of the entry
            String type = context.getMiTerms().containsKey(ip.getKey().getType()) ? context.getMiTerms().get(ip.getKey().getType()) : ip.getKey().getType();

            // the list of pubmed ids associated with the couple {method, type}
            Set<String> pubmedIds = ip.getValue();

            logger.debug("Process method " + method + ", type " + type + ", " + pubmedIds.size() + " publications");

            // the list which will contain the pubmeds Ids ONLY associated with spoke expanded interactions for the couple {method, type}
            Set<String> pubmedSpokeExpanded = new HashSet<String>(pubmedIds.size());
            // the list which will contain the pubmeds Ids associated with AT LEAST one true binary interaction for the couple {method, type}
            Set<String> pubmedTrueBinary = new HashSet<String>(pubmedIds.size());

            // the list of IntAct interaction acs associated with the couple {method, type}
            List<String> totalInteractions = method_typeToInteractions.containsKey(ip.getKey()) ? method_typeToInteractions.get(ip.getKey()) : Collections.EMPTY_LIST;

            // for each pubmed associated with couple {method, type}
            for (String pubmedId : pubmedIds){
                List<String> interactionsAcsAttachedToThisPubmed = pubmedToInteraction.containsKey(pubmedId) ? pubmedToInteraction.get(pubmedId) : Collections.EMPTY_LIST;

                logger.debug("Process pubmed " + pubmedId + ", having " + interactionsAcsAttachedToThisPubmed.size() + " interactions acs.");

                // get the list of IntAct interaction Ids which are linked to this pubmed ids and also associated with the couple {method, type}
                List<String> interactionForPubmedAndTypeAndMethod = new ArrayList(CollectionUtils.intersection(interactionsAcsAttachedToThisPubmed, totalInteractions));

                // number of spoke expanded interactions associated with this pubmed id and couple {method, type}
                int numberSpokeExpanded = 0;

                for (String intAc : interactionForPubmedAndTypeAndMethod){
                    // the interaction is spoke expanded
                    if (context.getSpokeExpandedInteractions().contains(intAc)){
                        numberSpokeExpanded++;
                    }
                }

                // if all interactions associated with this pubmed id and couple {method, type} are spoke expanded, add the pubmed id to the
                // list of pubmed Ids 'spoke expanded'
                if (numberSpokeExpanded == interactionForPubmedAndTypeAndMethod.size()){
                    pubmedSpokeExpanded.add(pubmedId);
                }
                // if at least one interaction is not spoke expanded, the pubmed id goes to the list of pubmed ids 'true binary'
                else {
                    pubmedTrueBinary.add(pubmedId);
                }
            }

            // if we have spoke expanded interactions, create an InteractionDetails 'spoke expanded' and add it to the list of InteractionDetails
            if (!pubmedSpokeExpanded.isEmpty()){
                InteractionDetails details = new DefaultInteractionDetails(type, method, true, pubmedSpokeExpanded);
                sortedInteractionDetails.add(details);
            }

            // if we have true binary interactions, create an InteractionDetails 'true binary' and add it to the list of InteractionDetails
            if (!pubmedTrueBinary.isEmpty()){
                InteractionDetails details = new DefaultInteractionDetails(type, method, false, pubmedTrueBinary);
                sortedInteractionDetails.add(details);
            }
        }

        return sortedInteractionDetails;
    }

    /**
     * Merge the map interaction type -> list of pubmed ids with the map detection method -> list of pubmed ids which are in the interaction.
     * @param interaction : the interaction
     * @param context
     * @return a map composed with :
     * - key = couple {method, type}
     * - value = set of pubmed ids
     */
    private Map<MethodAndTypePair, Set<String>> collectDistinctInteractionDetails(EncoreInteraction interaction, MiClusterContext context){
        // map which associates the interaction type to the pubmed id
        Map<String, List<String>> typeToPubmed = interaction.getTypeToPubmed();

        // map which associates the detection method with the pubmed id
        Map<String, List<String>> methodToPubmed = interaction.getMethodToPubmed();

        // the map which is a merge of the two previous maps
        Map<MethodAndTypePair, Set<String>> distinctLines = new HashMap<MethodAndTypePair, Set<String>>();

        // for each method of this interaction
        for (Map.Entry<String, List<String>> methodEntry : methodToPubmed.entrySet()){
            // list of pubmeds associated with this method
            List<String> pubmeds1 = methodEntry.getValue();
            // the name of the method
            String method = methodEntry.getKey();

            // for each interaction type of this interaction
            for (Map.Entry<String, List<String>> typeEntry : typeToPubmed.entrySet()){
                // list of pubmeds associated with this interaction type
                List<String> pubmeds2 = typeEntry.getValue();
                // the name of the interaction type
                String type = typeEntry.getKey();

                // the list of pubmed ids associated with the couple {method, type}
                Set<String> associatedPubmeds = new HashSet(CollectionUtils.intersection(pubmeds1, pubmeds2));

                // if it is not empty, we can add a new entry in the map
                if (!associatedPubmeds.isEmpty()){

                    distinctLines.put(new MethodAndTypePair(method, type), associatedPubmeds);
                }
            }
        }

        return distinctLines;
    }

    /**
     * Converts an EncoreInteraction into a CCParameter

     * @return the converted CCParameter
     */
    public CCParameters2 convertPositiveAndNegativeInteractionsIntoCCLines(List<EncoreInteraction> positiveInteractions, List<EncoreInteraction> negativeInteractions, MiClusterContext context, String firstInteractor){
        String firstIntactAc = null;
        String geneName1 = null;
        String taxId1 = null;

        List<SecondCCParameters2> secondCCInteractors = new ArrayList<SecondCCParameters2>(positiveInteractions.size() + negativeInteractions.size());

        if (positiveInteractions != null && !positiveInteractions.isEmpty()){
            for (EncoreInteraction interaction : positiveInteractions){
                // get the uniprot acs of the first and second interactors
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

                String intact1;
                String intact2;

                if (interaction.getInteractorAccsA().containsKey(WriterUtils.INTACT)){
                    intact1 = FilterUtils.extractIntactAcFromAccs(interaction.getInteractorAccsA());
                }
                else {
                    intact1 = FilterUtils.extractIntactAcFromOtherAccs(interaction.getOtherInteractorAccsA());
                }

                if (interaction.getInteractorAccsB().containsKey(WriterUtils.INTACT)){
                    intact2 = FilterUtils.extractIntactAcFromAccs(interaction.getInteractorAccsB());
                }
                else {
                    intact2 = FilterUtils.extractIntactAcFromOtherAccs(interaction.getOtherInteractorAccsB());
                }

                // if the uniprot acs are not null, it is possible to convert into a CCParameters2
                if (uniprot1 != null && uniprot2 != null && intact1 != null && intact2 != null){
                    // the complete uniprot ac of the first interactor
                    String firstUniprot = null;
                    // extract second interactor
                    String secondUniprot = null;

                    String secondIntactAc = null;

                    // extract gene names (present in the context and not in the interaction)
                    String geneName2 = null;
                    // extract organisms
                    String [] organismsA;
                    String [] organismsB;
                    organismsA = extractOrganismFrom(interaction.getOrganismsA());
                    organismsB = extractOrganismFrom(interaction.getOrganismsB());
                    // extract taxIds
                    String taxId2 = null;

                    // extract organism names
                    String organism2 = null;

                    if (uniprot1.startsWith(firstInteractor)){
                        firstUniprot = uniprot1;
                        secondUniprot = uniprot2;
                        geneName2 = context.getGeneNames().get(uniprot2);
                        geneName1 = context.getGeneNames().get(uniprot1);
                        taxId2 = organismsB[0];
                        try {
                            organism2 = retrieveOrganismScientificName(taxId2);
                        } catch (TaxonomyServiceException e) {
                            logger.fatal("Impossible to retrieve scientific name of " + taxId2 + ", we will take the common name instead.");
                            organism2 = organismsB[1];
                        }
                        secondIntactAc = intact2;

                        taxId1 = organismsA[0];
                        firstIntactAc = intact1;
                    }
                    else{
                        firstUniprot = uniprot2;
                        secondUniprot = uniprot1;
                        geneName2 = context.getGeneNames().get(uniprot1);
                        geneName1 = context.getGeneNames().get(uniprot2);
                        taxId2 = organismsA[0];
                        try {
                            organism2 = retrieveOrganismScientificName(taxId2);
                        } catch (TaxonomyServiceException e) {
                            logger.fatal("Impossible to retrieve scientific name of " + taxId2 + ", we will take the common name instead.");
                            organism2 = organismsA[1];
                        }
                        secondIntactAc = intact1;

                        taxId1 = organismsB[0];
                        firstIntactAc = intact2;
                    }

                    if (geneName1 != null && geneName2 != null && taxId1 != null && taxId2 != null && organism2 != null){
                        // collect all pubmeds and spoke expanded information
                        SortedSet<InteractionDetails> sortedInteractionDetails = sortInteractionDetails(interaction, context);

                        if (!sortedInteractionDetails.isEmpty()){
                            logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " to process");
                            SecondCCParameters2 secondCCInteractor = new DefaultSecondCCParameters2(firstUniprot, firstIntactAc, secondUniprot, secondIntactAc, geneName2, taxId2, organism2, sortedInteractionDetails, true);
                            secondCCInteractors.add(secondCCInteractor);
                        }
                        else{
                            logger.debug("Interaction "  + uniprot1 + " and " + uniprot2 +  " doesn't have any interaction details.");
                        }
                    }
                    else{
                        logger.debug("Interaction "  + uniprot1 + " and " + uniprot2 +  " has one of the gene names or taxIds which is null.");
                    }
                }
                else{
                    logger.debug("Interaction "  + uniprot1 + " and " + uniprot2 +  " has one of the unipprot acs/ intact acs which is null.");
                }
            }
        }

        if (negativeInteractions != null && !negativeInteractions.isEmpty()){
            for (EncoreInteraction interaction : negativeInteractions){
                // get the uniprot acs of the first and second interactors
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

                String intact1;
                String intact2;

                if (interaction.getInteractorAccsA().containsKey(WriterUtils.INTACT)){
                    intact1 = FilterUtils.extractIntactAcFromAccs(interaction.getInteractorAccsA());
                }
                else {
                    intact1 = FilterUtils.extractIntactAcFromOtherAccs(interaction.getOtherInteractorAccsA());
                }

                if (interaction.getInteractorAccsB().containsKey(WriterUtils.INTACT)){
                    intact2 = FilterUtils.extractIntactAcFromAccs(interaction.getInteractorAccsB());
                }
                else {
                    intact2 = FilterUtils.extractIntactAcFromOtherAccs(interaction.getOtherInteractorAccsB());
                }

                // if the uniprot acs are not null, it is possible to convert into a CCParameters2
                if (uniprot1 != null && uniprot2 != null && intact1 != null && intact2 != null){
                    // the complete uniprot ac of the first interactor
                    String firstUniprot = null;
                    // extract second interactor
                    String secondUniprot = null;

                    String secondIntactAc = null;

                    // extract gene names (present in the context and not in the interaction)
                    String geneName2 = null;
                    // extract organisms
                    String [] organismsA;
                    String [] organismsB;
                    organismsA = extractOrganismFrom(interaction.getOrganismsA());
                    organismsB = extractOrganismFrom(interaction.getOrganismsB());
                    // extract taxIds
                    String taxId2 = null;

                    // extract organism names
                    String organism2 = null;

                    if (uniprot1.startsWith(firstInteractor)){
                        firstUniprot = uniprot1;
                        secondUniprot = uniprot2;
                        geneName2 = context.getGeneNames().get(uniprot2);
                        geneName1 = context.getGeneNames().get(uniprot1);
                        taxId2 = organismsB[0];
                        try {
                            organism2 = retrieveOrganismScientificName(taxId2);
                        } catch (TaxonomyServiceException e) {
                            logger.fatal("Impossible to retrieve scientific name of " + taxId2 + ", we will take the common name instead.");
                            organism2 = organismsB[1];
                        }
                        secondIntactAc = intact2;

                        taxId1 = organismsA[0];
                        firstIntactAc = intact1;
                    }
                    else{
                        firstUniprot = uniprot2;
                        secondUniprot = uniprot1;
                        geneName2 = context.getGeneNames().get(uniprot1);
                        geneName1 = context.getGeneNames().get(uniprot2);
                        taxId2 = organismsA[0];
                        try {
                            organism2 = retrieveOrganismScientificName(taxId2);
                        } catch (TaxonomyServiceException e) {
                            logger.fatal("Impossible to retrieve scientific name of " + taxId2 + ", we will take the common name instead.");
                            organism2 = organismsA[1];
                        }
                        secondIntactAc = intact1;

                        taxId1 = organismsB[0];
                        firstIntactAc = intact2;
                    }

                    if (geneName1 != null && geneName2 != null && taxId1 != null && taxId2 != null && organism2 != null){
                        // collect all pubmeds and spoke expanded information
                        SortedSet<InteractionDetails> sortedInteractionDetails = sortInteractionDetails(interaction, context);

                        if (!sortedInteractionDetails.isEmpty()){
                            logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " to process");
                            SecondCCParameters2 secondCCInteractor = new DefaultSecondCCParameters2(firstUniprot, firstIntactAc, secondUniprot, secondIntactAc, geneName2, taxId2, organism2, sortedInteractionDetails, false);
                            secondCCInteractors.add(secondCCInteractor);
                        }
                        else{
                            logger.debug("Interaction "  + uniprot1 + " and " + uniprot2 +  " doesn't have any interaction details.");
                        }
                    }
                    else{
                        logger.debug("Interaction "  + uniprot1 + " and " + uniprot2 +  " has one of the gene names or taxIds which is null.");
                    }
                }
                else{
                    logger.debug("Interaction "  + uniprot1 + " and " + uniprot2 +  " has one of the unipprot acs/ intact acs which is null.");
                }
            }
        }

        if (!secondCCInteractors.isEmpty()){
            return new DefaultCCParameters2(firstInteractor, geneName1, taxId1, secondCCInteractors);
        }

        logger.debug("Interactor " + firstInteractor + " doesn't have any valid second CC parameters and will be skipped.");

        return null;
    }

    @Override
    public BasicCCParameters convertInteractionsIntoCCLines(List<EncoreInteraction> positiveInteractions, MiClusterContext context, String firstInteractor) {
        return convertPositiveAndNegativeInteractionsIntoCCLines(positiveInteractions, Collections.EMPTY_LIST, context, firstInteractor);
    }
}
