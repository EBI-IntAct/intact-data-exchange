package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParametersVersion1Impl;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The CCLineConverterVersion1 can only convert positive encore interactions to CC parameters for the CC line format, version 1.
 * Self interations involving two isoforms cannot be converted.
 * Interactions involving two isoforms of the same uniprot entry cannot be converted neither because one of the isoform info will be lost
 * Feature chains are remapped to the master uniprot entry
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class CCLineConverterVersion1 extends AbstractCCLineConverter {

    private static final Logger logger = Logger.getLogger(CCLineConverterVersion1.class);

    public CCLineConverterVersion1(){
        super();
    }

    @Override
    public CCParameters<SecondCCParametersVersion1> convertPositiveAndNegativeInteractionsIntoCCLines(Set<EncoreInteraction> positiveInteractions, Set<EncoreInteraction> negativeInteractions, MiClusterContext context, String firstInteractor) {
        logger.warn("The CCline format version 1 doesn't accept negative interactions so they will be ignored.");
        return convertInteractionsIntoCCLines(positiveInteractions, context, firstInteractor);
    }

    @Override
    public CCParameters<SecondCCParametersVersion1> convertInteractionsIntoCCLines(Set<EncoreInteraction> interactions, MiClusterContext context, String masterUniprot){

        String taxId1 = null;
        String geneName1 = context.getGeneNames().get(masterUniprot);

        SortedSet<SecondCCParametersVersion1> secondCCInteractors = new TreeSet<SecondCCParametersVersion1>();

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

                // if the uniprot acs are not null, it is possible to convert into a CCParametersVersion1
                if (uniprot1 != null && uniprot2 != null && intact1 != null && intact2 != null){
                    // first interactor
                    String firstUniprot = null;
                    String firstIntactAc = null;
                    //  second interactor
                    String secondUniprot = null;
                    String secondIntactAc = null;

                    // extract gene names (present in the context and not in the interaction)
                    String geneName2 = null;

                    // extract taxIds
                    String taxId2 = null;

                    // extract organisms
                    String [] organismsA;
                    String [] organismsB;
                    organismsA = extractOrganismFrom(interaction.getOrganismsA());
                    organismsB = extractOrganismFrom(interaction.getOrganismsB());

                    // boolean to know if uniprot 1 is from same uniprot entry than uniprot master
                    boolean isUniprot1FromSameUniprotEntry = UniprotExportUtils.isFromSameUniprotEntry(masterUniprot, uniprot1, transSplicedVariants.get(masterUniprot));
                    // boolean to know if uniprot 2 is from same uniprot entry than uniprot master
                    boolean isUniprot2FromSameUniprotEntry = UniprotExportUtils.isFromSameUniprotEntry(masterUniprot, uniprot2, transSplicedVariants.get(masterUniprot));

                    // the first uniprot is from the same uniprot entry as the master uniprot but the uniprot 2 is from another uniprot entry
                    if (isUniprot1FromSameUniprotEntry && !isUniprot2FromSameUniprotEntry) {
                        firstUniprot = uniprot1;
                        secondUniprot = uniprot2;
                        geneName2 = context.getGeneNames().get(uniprot2);
                        // mi-cluster assigned "-" as empty gene name, this creates problems in the sorting algoritm.
                        // We remove this character to avoid problems
                        if (geneName2 != null && geneName2.equalsIgnoreCase("-")) {
                            geneName2 = null;
                        }
                        taxId2 = organismsB[0];
                        secondIntactAc = intact2;

                        taxId1 = organismsA[0];
                        firstIntactAc = intact1;

                        if (geneName1 == null) {
                            // mi-cluster assigned "-" as empty gene name, this creates problems in the sorting algoritm.
                            // We remove this character to avoid problems
                            geneName1 = context.getGeneNames().get(uniprot1);
                            if (geneName1 != null && geneName1.equalsIgnoreCase("-")) {
                                geneName1 = null;
                            }
                        }
                    }
                    // the second uniprot is from the same uniprot entry as the master uniprot but the uniprot 1 is from another uniprot entry
                    else if (!isUniprot1FromSameUniprotEntry && isUniprot2FromSameUniprotEntry){
                        firstUniprot = uniprot2;
                        secondUniprot = uniprot1;
                        geneName2 = context.getGeneNames().get(uniprot1);
                        // mi-cluster assigned "-" as empty gene name, this creates problems in the sorting algoritm.
                        // We remove this character to avoid problems
                        if (geneName2 != null && geneName2.equalsIgnoreCase("-")) {
                            geneName2 = null;
                        }
                        taxId2 = organismsA[0];
                        secondIntactAc = intact1;

                        taxId1 = organismsB[0];
                        firstIntactAc = intact2;

                        if (geneName1 == null){
                            // mi-cluster assigned "-" as empty gene name, this creates problems in the sorting algoritm.
                            // We remove this character to avoid problems
                            geneName1 = context.getGeneNames().get(uniprot2);
                            if (geneName1 != null && geneName1.equalsIgnoreCase("-")) {
                                geneName1 = null;
                            }
                        }
                    }
                    // we have an interaction which can be self interaction or which involves isoforms of same uniprot entry. They must have same gene name
                    else if (isUniprot1FromSameUniprotEntry && isUniprot2FromSameUniprotEntry) {
                        // if uniprot 1 is the master uniprot, we can convert this interaction even if second interactor is an isoform 
                        if (uniprot1.equalsIgnoreCase(masterUniprot) ){
                            firstUniprot = uniprot1;
                            secondUniprot = uniprot2;
                            taxId2 = organismsB[0];
                            secondIntactAc = intact2;

                            taxId1 = organismsA[0];
                            firstIntactAc = intact1;

                            if (geneName1 == null){
                                geneName1 = context.getGeneNames().get(uniprot1);
                                if (geneName1 != null && geneName1.equalsIgnoreCase("-")) {
                                    geneName1 = null;
                                }
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
                                if (geneName1 != null && geneName1.equalsIgnoreCase("-")) {
                                    geneName1 = null;
                                }
                            }
                            geneName2 = geneName1;
                        }
                        else { //Two isoform or feature chains interacting with each other
                            // TODO we should sort the isoforms by number
                            // Same with feature chains.
                            firstUniprot = uniprot1;
                            secondUniprot = uniprot2;
                            taxId1 = organismsA[0];
                            firstIntactAc = intact1;

                            taxId2 = organismsB[0];
                            secondIntactAc = intact2;

                            if (geneName1 == null){
                                geneName1 = context.getGeneNames().get(uniprot2);
                                if (geneName1 != null && geneName1.equalsIgnoreCase("-")) {
                                    geneName1 = null;
                                }
                            }
                            geneName2 = geneName1;
                            logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " is  converted but the two interactors are isoforms of the same uniprot entry " + masterUniprot);
                        }
                    }
                    else {
                        logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " is not converted because the two interactors are not related to the master uniprot " + masterUniprot);
                    }

                    if (taxId1 != null && taxId2 != null){
                        // The experimentToPubmed in a encoreInteraction is filled with the interactionAc in the conversion
                        // check Binary2Encore.convertEncoreInteraction()
                        int numberEvidences = interaction.getExperimentToPubmed().size();

                        if (numberEvidences > 0){
                            logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " to process");

                            SecondCCParametersVersion1 secondCCInteractor = new SecondCCParametersVersion1Impl(firstUniprot, firstIntactAc, taxId1, secondUniprot, secondIntactAc, taxId2, geneName2, numberEvidences);
                           if(!secondCCInteractors.add(secondCCInteractor)){
                               logger.error("Trying to insert a interactor pair that has been already inserted");
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
                    logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " has one of the uniprot acs/ intact acs which is null.");
                }
            }

            if (!secondCCInteractors.isEmpty()){
                return new CCParametersVersion1(masterUniprot, geneName1, taxId1, secondCCInteractors);
            }
        }

        logger.debug("Interactor " + masterUniprot + " doesn't have any valid second CC parameters and will be skipped.");

        return null;
    }
}
