package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.parameters.*;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.*;

/**
 * Converter of an EncoreInteraction into a CCParameter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class EncoreInteractionToCCLineConverter {

    /**
     *
     * @param references : the organism cross references
     * @return a String [2] with the taxId of the organism and the organism name
     */
    private String [] extractOrganismFrom(Collection<CrossReference> references){

        String taxId = "-";
        String organismName = "-";

        for (CrossReference ref : references){
            // look for the taxId cross reference and get the identifier (taxId) and the organism name (text of a cross reference)
            if (WriterUtils.TAXID.equalsIgnoreCase(ref.getDatabase())){
                taxId = ref.getIdentifier();
                if (ref.getText() != null){
                    organismName = ref.getText();
                }
            }
        }

        return new String [] {taxId, organismName};
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
        Map<Map.Entry<String, String>, Set<String>> distinctInformationDetails = collectDistinctInteractionDetails(interaction, context);

        // map which associates a couple {interaction detection method, interaction type} to a set of IntAct interaction Acs
        Map<Map.Entry<String, String>, List<String>> method_typeToInteractions = WriterUtils.invertMapFromKeySelection(context.getInteractionToMethod_type(), interactionToPubmed.keySet());

        // the list with the interaction details
        SortedSet<InteractionDetails> sortedInteractionDetails = new TreeSet<InteractionDetails>();

        // for each couple {interaction detection method, interaction type}, sort the pubmed ids :
        // - pubmed ids only associated with spoke expanded interactions
        // - pubmed ids only associated with true binary interactions
        // - pubmed ids associated with both spoke expanded and true binary interactions. In this case, we consider the pubmed id as only associated with true binary interaction
        for (Map.Entry<Map.Entry<String, String>, Set<String>> ip : distinctInformationDetails.entrySet()){
            // the method is the key of the entry
            String method = context.getMiTerms().get(ip.getKey().getValue());

            // the type is the value of the entry
            String type = context.getMiTerms().get(ip.getKey().getKey());

            // the list of pubmed ids associated with the couple {method, type}
            Set<String> pubmedIds = ip.getValue();

            // the list which will contain the pubmeds Ids ONLY associated with spoke expanded interactions for the couple {method, type}
            Set<String> pubmedSpokeExpanded = new HashSet<String>(pubmedIds.size());
            // the list which will contain the pubmeds Ids associated with AT LEAST one true binary interaction for the couple {method, type}
            Set<String> pubmedTrueBinary = new HashSet<String>(pubmedIds.size());

            // the list of IntAct interaction acs associated with the couple {method, type}
            List<String> totalInteractions = method_typeToInteractions.get(ip);

            // for each pubmed associated with couple {method, type}
            for (String pubmedId : pubmedIds){
                // get the list of IntAct interaction Ids which are linked to this pubmed ids and also associated with the couple {method, type}
                List<String> interactionForPubmedAndTypeAndMethod = new ArrayList(CollectionUtils.intersection(pubmedToInteraction.get(pubmedId), totalInteractions));

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
                InteractionDetails details = new InteractionDetailsImpl(type, method, true, pubmedSpokeExpanded);
                sortedInteractionDetails.add(details);
            }

            // if we have true binary interactions, create an InteractionDetails 'true binary' and add it to the list of InteractionDetails
            if (!pubmedTrueBinary.isEmpty()){
                InteractionDetails details = new InteractionDetailsImpl(type, method, false, pubmedSpokeExpanded);
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
    public Map<Map.Entry<String, String>, Set<String>> collectDistinctInteractionDetails(EncoreInteraction interaction, MiClusterContext context){
        // map which associates the interaction type to the pubmed id
        Map<String, List<String>> typeToPubmed = interaction.getTypeToPubmed();

        // map which associates the detection method with the pubmed id
        Map<String, List<String>> methodToPubmed = interaction.getMethodToPubmed();

        // the map which is a merge of the two previous maps
        Map<Map.Entry<String, String>, Set<String>> distinctLines = new HashMap<Map.Entry<String, String>, Set<String>>();

        // for each method of this interaction
        for (Map.Entry<String, List<String>> methodEntry : methodToPubmed.entrySet()){
            // list of pubmeds associated with this method
            List<String> pubmeds1 = methodEntry.getValue();
            // the name of the method
            String method = context.getMiTerms().get(methodEntry.getKey());

            // for each interaction type of this interaction
            for (Map.Entry<String, List<String>> typeEntry : typeToPubmed.entrySet()){
                // list of pubmeds associated with this interaction type
                List<String> pubmeds2 = typeEntry.getValue();
                // the name of the interaction type
                String type = context.getMiTerms().get(typeEntry.getKey());

                // the list of pubmed ids associated with the couple {method, type}
                Set<String> associatedPubmeds = new HashSet(CollectionUtils.intersection(pubmeds1, pubmeds2));

                // if it is not empty, we can add a new entry in the map
                if (!associatedPubmeds.isEmpty()){

                    distinctLines.put(new DefaultMapEntry(method, type), associatedPubmeds);
                }
            }
        }

        return distinctLines;
    }

    private String extractIntactAcFromAccs(Map<String, String> interactorAccs){
        String interactorAcc = null;
        for(Map.Entry<String, String> entry : interactorAccs.entrySet()){
            if(WriterUtils.INTACT.equalsIgnoreCase(entry.getKey())){
                interactorAcc =  entry.getValue();
                break;
            }
        }

        return interactorAcc;
    }

    /**
     * Converts an EncoreInteraction into a CCParameter

     * @return the converted CCParameter
     */
    public CCParameters convertInteractionsIntoCCLines(List<EncoreInteraction> interactions, MiClusterContext context, String firstInteractor){
        String firstIntactAc = null;
        String geneName1 = context.getGeneNames().get(firstInteractor);
        String taxId1 = null;

        List<SecondCCInteractor> secondCCInteractors = new ArrayList<SecondCCInteractor>(interactions.size());

        if (!interactions.isEmpty()){

            for (EncoreInteraction interaction : interactions){
                // get the uniprot acs of the first and second interactors
                String uniprot1 = interaction.getInteractorA();
                String uniprot2 = interaction.getInteractorB();

                String intact1 = extractIntactAcFromAccs(interaction.getInteractorAccsA());
                String intact2 = extractIntactAcFromAccs(interaction.getInteractorAccsB());

                // if the uniprot acs are not null, it is possible to convert into a CCParameters
                if (uniprot1 != null && uniprot2 != null && intact1 != null && intact2 != null){
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

                    if (uniprot1.equals(firstInteractor)){
                        secondUniprot = uniprot2;
                        geneName2 = context.getGeneNames().get(uniprot2);
                        taxId2 = organismsB[0];
                        organism2 = organismsB[1];
                        secondIntactAc = intact2;

                        taxId1 = organismsA[0];
                        firstIntactAc = intact1;
                    }
                    else{
                        secondUniprot = uniprot1;
                        geneName2 = context.getGeneNames().get(uniprot1);
                        taxId2 = organismsA[0];
                        organism2 = organismsA[1];
                        secondIntactAc = intact1;

                        taxId1 = organismsB[0];
                        firstIntactAc = intact2;
                    }

                    // collect all pubmeds and spoke expanded information
                    SortedSet<InteractionDetails> sortedInteractionDetails = sortInteractionDetails(interaction, context);

                    SecondCCInteractor secondCCInteractor = new SecondCCInteractorImpl(secondUniprot, secondIntactAc, geneName2, taxId2, organism2, sortedInteractionDetails);
                    secondCCInteractors.add(secondCCInteractor);
                }
            }

            return new CCParametersImpl(firstInteractor, firstIntactAc,geneName1, taxId1, secondCCInteractors);
        }

        return null;
    }
}
