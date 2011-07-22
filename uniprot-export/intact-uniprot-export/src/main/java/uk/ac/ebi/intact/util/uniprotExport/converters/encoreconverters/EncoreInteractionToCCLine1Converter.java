package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.CCParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.DefaultCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.DefaultSecondCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.parameters.cclineparameters.SecondCCParameters1;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
    public CCParameters<SecondCCParameters1> convertInteractionsIntoCCLines(List<EncoreInteractionForScoring> interactions, MiClusterContext context, String firstInteractor){
        String firstIntactAc = null;
        String geneName1 = null;
        String taxId1 = null;

        SortedSet<SecondCCParameters1> secondCCInteractors = new TreeSet<SecondCCParameters1>();

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

                    if (uniprot1.contains(WriterUtils.CHAIN_PREFIX)){
                        uniprot1 = uniprot1.substring(0, uniprot1.indexOf(WriterUtils.CHAIN_PREFIX));
                    }
                    if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                        uniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
                    }

                    if (uniprot1.startsWith(firstInteractor)){
                        firstUniprot = uniprot1;
                        secondUniprot = uniprot2;
                        geneName2 = context.getGeneNames().get(uniprot2);
                        geneName1 = context.getGeneNames().get(uniprot1);
                        taxId2 = organismsB[0];
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
                        secondIntactAc = intact1;

                        taxId1 = organismsB[0];
                        firstIntactAc = intact2;
                    }

                    if (geneName1 != null && geneName2 != null && taxId1 != null && taxId2 != null){
                        int numberEvidences = interaction.getExperimentToPubmed().size();

                        if (numberEvidences > 0){
                            logger.info("Interaction " + uniprot1 + " and " + uniprot2 + " to process");

                            SecondCCParameters1 secondCCInteractor = new DefaultSecondCCParameters1(firstUniprot, firstIntactAc, secondUniprot, secondIntactAc, geneName2, taxId2, numberEvidences);
                            secondCCInteractors.add(secondCCInteractor);
                        }
                        else{
                            logger.warn("Interaction " + uniprot1 + " and " + uniprot2 + " doesn't have valid evidences.");
                        }
                    }
                    else{
                        logger.warn("Interaction " + uniprot1 + " and " + uniprot2 + " has one of the gene names or taxIds which is null.");
                    }
                }
                else{
                    logger.warn("Interaction " + uniprot1 + " and " + uniprot2 + " has one of the unipprot acs/ intact acs which is null.");
                }
            }

            if (!secondCCInteractors.isEmpty()){
                return new DefaultCCParameters1(firstInteractor, geneName1, taxId1, secondCCInteractors);
            }
        }

        logger.debug("Interactor " + firstInteractor + " doesn't have any valid second CC parameters and will be skipped.");

        return null;
    }
}
