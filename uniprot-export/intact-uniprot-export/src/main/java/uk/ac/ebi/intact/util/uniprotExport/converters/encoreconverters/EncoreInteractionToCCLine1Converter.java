package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.DefaultCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.DefaultSecondCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.*;

/**
 * Converter of an EncoreInteraction into a CCParameter1
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class EncoreInteractionToCCLine1Converter extends AbstractEncoreInteractionToCCLineConverter {
    private static final Logger logger = Logger.getLogger(EncoreInteractionToCCLine1Converter.class);

    public EncoreInteractionToCCLine1Converter(){
        super();
    }

    @Override
    public CCParameters convertPositiveAndNegativeInteractionsIntoCCLines(List<EncoreInteractionForScoring> positiveInteractions, List<EncoreInteractionForScoring> negativeInteractions, MiClusterContext context, String firstInteractor) {
        logger.warn("The CCline format version 1 doesn't accept negative interactions so they will be ignored.");
        return convertInteractionsIntoCCLines(positiveInteractions, context, firstInteractor);
    }

    @Override
    public CCParameters<SecondCCParameters1> convertInteractionsIntoCCLines(List<EncoreInteractionForScoring> interactions, MiClusterContext context, String masterUniprot){
        String firstIntactAc = null;
        String geneName1 = null;
        String taxId1 = null;

        // set containing the SecondCCParameters in case of feature chains
        Set<SecondCCParameters1> processedCCParametersForFeatureChains = new HashSet<SecondCCParameters1>();

        SortedSet<SecondCCParameters1> secondCCInteractors = new TreeSet<SecondCCParameters1>();

        Map<String, Set<IntactTransSplicedProteins>> transSplicedVariants = context.getTranscriptsWithDifferentMasterAcs();

        if (!interactions.isEmpty()){

            for (EncoreInteractionForScoring interaction : interactions){
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
                    if (uniprot1.startsWith(masterUniprot) && !uniprot1.equals(masterUniprot) && uniprot2.startsWith(masterUniprot) && !uniprot2.equals(masterUniprot)){
                        logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " is not converted because is a self interaction with two isoforms of same protein");
                    }
                    else {
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

                        boolean containsFeatureChain = false;

                        // remap to parent in case of feature chain
                        if (uniprot1.contains(WriterUtils.CHAIN_PREFIX)){
                            uniprot1 = uniprot1.substring(0, uniprot1.indexOf(WriterUtils.CHAIN_PREFIX));
                            containsFeatureChain = true;
                        }
                        if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                            uniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
                            containsFeatureChain = true;
                        }

                        if (uniprot1.startsWith(masterUniprot)){
                            firstUniprot = uniprot1;
                            secondUniprot = uniprot2;
                            geneName2 = context.getGeneNames().get(uniprot2);
                            geneName1 = context.getGeneNames().get(uniprot1);
                            taxId2 = organismsB[0];
                            secondIntactAc = intact2;

                            taxId1 = organismsA[0];
                            firstIntactAc = intact1;
                        }
                        else if (uniprot2.startsWith(masterUniprot)) {
                            firstUniprot = uniprot2;
                            secondUniprot = uniprot1;
                            geneName2 = context.getGeneNames().get(uniprot1);
                            geneName1 = context.getGeneNames().get(uniprot2);
                            taxId2 = organismsA[0];
                            secondIntactAc = intact1;

                            taxId1 = organismsB[0];
                            firstIntactAc = intact2;
                        }
                        else {
                            Set<IntactTransSplicedProteins> transSplicedProteins = transSplicedVariants.get(masterUniprot);
                            boolean startsWithUniprot1 = false;
                            boolean startsWithUniprot2 = false;

                            if (transSplicedProteins != null){
                                for (IntactTransSplicedProteins prot : transSplicedProteins){
                                    if (uniprot1.equalsIgnoreCase(prot.getUniprotAc())){
                                        startsWithUniprot1 = true;
                                    }
                                    else if (uniprot2.equalsIgnoreCase(prot.getUniprotAc())){
                                        startsWithUniprot2 = true;
                                    }
                                }
                            }

                            if (startsWithUniprot1 && startsWithUniprot2){
                                logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " is not converted because is a self interaction with two isoforms of same protein");
                                geneName1 = null;
                                geneName2 = null;
                                taxId1 = null;
                                taxId2 = null;
                            }
                            else {
                                if (startsWithUniprot1){
                                    firstUniprot = uniprot1;
                                    secondUniprot = uniprot2;
                                    geneName2 = context.getGeneNames().get(uniprot2);
                                    geneName1 = context.getGeneNames().get(uniprot1);
                                    taxId2 = organismsB[0];
                                    secondIntactAc = intact2;

                                    taxId1 = organismsA[0];
                                    firstIntactAc = intact1;
                                }
                                else {
                                    firstUniprot = uniprot2;
                                    secondUniprot = uniprot1;
                                    geneName2 = context.getGeneNames().get(uniprot1);
                                    geneName1 = context.getGeneNames().get(uniprot2);
                                    taxId2 = organismsA[0];
                                    secondIntactAc = intact1;

                                    taxId1 = organismsB[0];
                                    firstIntactAc = intact2;
                                }
                            }
                        }

                        if (geneName1 != null && geneName2 != null && taxId1 != null && taxId2 != null){
                            int numberEvidences = interaction.getExperimentToPubmed().size();

                            if (numberEvidences > 0){
                                logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " to process");

                                SecondCCParameters1 secondCCInteractor = new DefaultSecondCCParameters1(firstUniprot, firstIntactAc, secondUniprot, secondIntactAc, geneName2, taxId2, numberEvidences);

                                if (!containsFeatureChain){
                                    secondCCInteractors.add(secondCCInteractor);
                                }
                                else {
                                    processedCCParametersForFeatureChains.add(secondCCInteractor);
                                }
                            }
                            else{
                                logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " doesn't have valid evidences.");
                            }
                        }
                        else{
                            logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " has one of the gene names or taxIds which is null.");
                        }
                    }
                }
                else{
                    logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " has one of the unipprot acs/ intact acs which is null.");
                }
            }

            // update existing secondCCParameters if we had feature chains to merge information
            if (!processedCCParametersForFeatureChains.isEmpty()){
                for (SecondCCParameters1 secondParameter : processedCCParametersForFeatureChains){
                    String firstUniprot = secondParameter.getFirstUniprotAc();
                    String secondUniprotAc = secondParameter.getSecondUniprotAc();

                    boolean hasFoundExistingSecondCC = false;

                    for (SecondCCParameters1 existingParameters : secondCCInteractors){
                        if (firstUniprot.equals(existingParameters.getFirstUniprotAc())){
                            if (secondUniprotAc.equals(existingParameters.getSecondUniprotAc())){
                                hasFoundExistingSecondCC = true;

                                int newNumberOfExp = existingParameters.getNumberOfInteractionEvidences() + secondParameter.getNumberOfInteractionEvidences();
                                existingParameters.setNumberOfInteractionEvidences(newNumberOfExp);

                                if (existingParameters.getGeneName().equals("-") && !secondParameter.getGeneName().equals("-")){
                                    existingParameters.setGeneName(secondParameter.getGeneName());
                                }
                                break;
                            }
                        }
                    }

                    // if this information could not be merged, add it to the list of secondCCParameters
                    if (!hasFoundExistingSecondCC){
                        secondCCInteractors.add(secondParameter);
                    }
                }
            }

            if (!secondCCInteractors.isEmpty()){
                return new DefaultCCParameters1(masterUniprot, geneName1, taxId1, secondCCInteractors);
            }
        }

        logger.debug("Interactor " + masterUniprot + " doesn't have any valid second CC parameters and will be skipped.");

        return null;
    }
}
