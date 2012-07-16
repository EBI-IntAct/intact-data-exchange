package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParameters1Impl;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.*;

/**
 * The CCLineConverter1 can only convert positive encore interactions to CC parameters for the CC line format, version 1.
 * Self interations involving two isoforms cannot be converted.
 * Interactions involving two isoforms of the same uniprot entry cannot be converted neither because one of the isoform info will be lost
 * Feature chains are remapped to the master uniprot entry
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class CCLineConverter1 extends AbstractCCLineConverter {
    private static final Logger logger = Logger.getLogger(CCLineConverter1.class);

    // set containing the SecondCCParameters in case of feature chains
    private Set<SecondCCParameters1> processedCCParametersForFeatureChains;

    public CCLineConverter1(){
        super();
        processedCCParametersForFeatureChains = new HashSet<SecondCCParameters1>();
    }

    @Override
    public CCParameters convertPositiveAndNegativeInteractionsIntoCCLines(Set<EncoreInteraction> positiveInteractions, Set<EncoreInteraction> negativeInteractions, MiClusterContext context, String firstInteractor) {
        logger.warn("The CCline format version 1 doesn't accept negative interactions so they will be ignored.");
        return convertInteractionsIntoCCLines(positiveInteractions, context, firstInteractor);
    }

    @Override
    public CCParameters<SecondCCParameters1> convertInteractionsIntoCCLines(Set<EncoreInteraction> interactions, MiClusterContext context, String masterUniprot){
        processedCCParametersForFeatureChains.clear();

        String firstIntactAc = null;
        String geneName1 = context.getGeneNames().get(masterUniprot);
        String taxId1 = null;

        SortedSet<SecondCCParameters1> secondCCInteractors = new TreeSet<SecondCCParameters1>();

        if (!interactions.isEmpty()){

            Map<String, Set<IntactTransSplicedProteins>> transSplicedVariants = context.getTranscriptsWithDifferentMasterAcs();

            for (EncoreInteraction interaction : interactions){
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

                    // boolean to know if uniprot 1 is from same uniprot entry than uniprot master
                    boolean isUniprot1FromSameUniprotEntry = UniprotExportUtils.isFromSameUniprotEntry(masterUniprot, uniprot1, transSplicedVariants.get(masterUniprot));
                    // boolean to know if uniprot 2 is from same uniprot entry than uniprot master
                    boolean isUniprot2FromSameUniprotEntry = UniprotExportUtils.isFromSameUniprotEntry(masterUniprot, uniprot2, transSplicedVariants.get(masterUniprot));

                    // the first uniprot is from the same uniprot entry as the master uniprot but the uniprot 2 is from another uniprot entry
                    if (isUniprot1FromSameUniprotEntry && !isUniprot2FromSameUniprotEntry){
                        firstUniprot = uniprot1;
                        secondUniprot = uniprot2;
                        geneName2 = context.getGeneNames().get(uniprot2);
                        taxId2 = organismsB[0];
                        secondIntactAc = intact2;

                        taxId1 = organismsA[0];
                        firstIntactAc = intact1;

                        if (geneName1 == null){
                            geneName1 = context.getGeneNames().get(uniprot1);
                        }
                    }
                    // the second uniprot is from the same uniprot entry as the master uniprot but the uniprot 1 is from another uniprot entry
                    else if (!isUniprot1FromSameUniprotEntry && isUniprot2FromSameUniprotEntry){
                        firstUniprot = uniprot2;
                        secondUniprot = uniprot1;
                        geneName2 = context.getGeneNames().get(uniprot1);
                        taxId2 = organismsA[0];
                        secondIntactAc = intact1;

                        taxId1 = organismsB[0];
                        firstIntactAc = intact2;

                        if (geneName1 == null){
                            geneName1 = context.getGeneNames().get(uniprot2);
                        }
                    }
                    // we have an interaction which can be self interaction or which involves isoforms of same uniprot entry. They must have same gene name
                    else if (isUniprot1FromSameUniprotEntry && isUniprot2FromSameUniprotEntry) {
                        // if uniprot 1 is the master uniprot, we can convert this interaction even if second interactor is an isoform 
                        if (uniprot1.equalsIgnoreCase(masterUniprot)){
                            firstUniprot = uniprot1;
                            secondUniprot = uniprot2;
                            taxId2 = organismsB[0];
                            secondIntactAc = intact2;

                            taxId1 = organismsA[0];
                            firstIntactAc = intact1;

                            if (geneName1 == null){
                                geneName1 = context.getGeneNames().get(uniprot1);
                            }
                            geneName2 = geneName1;
                        }
                        // if uniprot 2 is the master uniprot, we can convert this interaction even if first interactor is an isoform 
                        else if (uniprot2.equalsIgnoreCase(masterUniprot)){
                            firstUniprot = uniprot2;
                            secondUniprot = uniprot1;
                            taxId2 = organismsA[0];
                            secondIntactAc = intact1;

                            taxId1 = organismsB[0];
                            firstIntactAc = intact2;

                            if (geneName1 == null){
                                geneName1 = context.getGeneNames().get(uniprot2);
                            }
                            geneName2 = geneName1;
                        }
                        // we don't allow self interactions with isoforms or isoforms interacting with isoforms
                        else {
                            logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " is not converted because the two interactors are isoforms of the same uniprot entry " + masterUniprot);
                        }
                    }
                    else {
                        logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " is not converted because the two interactors are not related to the master uniprot " + masterUniprot);
                    }

                    if (geneName1 != null && geneName2 != null && taxId1 != null && taxId2 != null){
                        int numberEvidences = interaction.getExperimentToPubmed().size();

                        if (numberEvidences > 0){
                            logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " to process");

                            SecondCCParameters1 secondCCInteractor = new SecondCCParameters1Impl(firstUniprot, firstIntactAc, secondUniprot, secondIntactAc, geneName2, taxId2, numberEvidences);

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
                        logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " has one of the gene names or taxIds which is null.");
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
                return new CCParameters1(masterUniprot, geneName1, taxId1, secondCCInteractors);
            }
        }

        logger.debug("Interactor " + masterUniprot + " doesn't have any valid second CC parameters and will be skipped.");

        processedCCParametersForFeatureChains.clear();
        return null;
    }
}
