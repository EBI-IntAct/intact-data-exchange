package uk.ac.ebi.intact.util.uniprotExport.miscore.writer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.CcLine;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.writers.CCLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.CCLineWriterImpl;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.IOException;
import java.util.*;

/**
 * Write CC lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>27/01/11</pre>
 */

public class CCLineConverter extends AbstractConverter {

    private CCLineWriter writer;
    public CCLineConverter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        super(clusterScore, fileName);
        writer = new CCLineWriterImpl(fileName);
    }

    public IntActInteractionClusterScore getClusterScore() {
        return clusterScore;
    }

    private List<CcLine> convertInteractionsIntoCCLines(Map.Entry<String, List<Integer>> interactor){
        String uniprotAc = interactor.getKey();
        List<Integer> interactions = interactor.getValue();

        List<CcLine> ccLines = new ArrayList<CcLine>();

        for (Integer interactionId : interactions){
            EncoreInteraction interaction = this.clusterScore.getInteractionMapping().get(interactionId);

            if (interaction != null){
                CcLine line = createCCLine(interaction, uniprotAc);

                if (line != null){
                    ccLines.add(line);
                }
            }
        }

        return ccLines;
    }

    public void write() throws IOException {

        for (Map.Entry<String, List<Integer>> interactor : this.clusterScore.getInteractorMapping().entrySet()){

            List<CcLine> ccLines = convertInteractionsIntoCCLines(interactor);

            writer.writeCCLine(interactor.getKey(), ccLines);
        }
    }

    /**
     * create the output of a CC line for a set of exportable interactions.
     *
     * @param interaction
     */
    private CcLine createCCLine( EncoreInteraction interaction, String firstInteractor) {

        String uniprot1 = interaction.getInteractorA(WriterUtils.UNIPROT);
        String uniprot2 = interaction.getInteractorB(WriterUtils.UNIPROT);

        if (uniprot1 != null && uniprot2 != null && firstInteractor != null){
            // produce the CC lines for the 1st protein
            CcLine cc1 = null;

            if (firstInteractor.equals(uniprot1)){
                formatCCLines(interaction, uniprot1, uniprot2);
            }
            else {
                formatCCLines(interaction, uniprot2, uniprot1);
            }

            return cc1;
        }

        return null;
    }

    private String [] extractOrganismFrom(Collection<CrossReference> references){

        String taxId = "-";
        String organismName = "-";

        for (CrossReference ref : references){
            if (WriterUtils.TAXID.equalsIgnoreCase(ref.getDatabase())){
                taxId = ref.getIdentifier();
                if (ref.getText() != null){
                    organismName = ref.getText();
                }
            }
        }

        return new String [] {taxId, organismName};
    }

    private Map<Map.Entry<String, String>, Set<String>> collectSpokeExpandedInteractions(EncoreInteraction interaction){
        Map<String, String> interactionToPubmed = interaction.getExperimentToPubmed();
        Map<String, List<String>> pubmedToInteraction = WriterUtils.invertMapOfTypeStringToString(interactionToPubmed);

        Map<Map.Entry<String, String>, List<String>> spokeExpandedInteractions = WriterUtils.invertMapFromKeySelection(this.getClusterScore().getSpokeExpandedInteractions(), interactionToPubmed.keySet());
        Map<Map.Entry<String, String>, Set<String>> spokeExpandedPubmeds = new HashMap<Map.Entry<String, String>, Set<String>>();

        for (Map.Entry<Map.Entry<String, String>, List<String>> ip : spokeExpandedInteractions.entrySet()){
            Map.Entry<String, String> detType = ip.getKey();
            List<String> interactionAcs = ip.getValue();
            List<String> duplicatedInteractions = new ArrayList(interactionAcs);

            Set<String> pubmedIds = new HashSet<String>(interactionAcs.size());

            for (String interactionAc : duplicatedInteractions){
                String pubmedAc = interactionToPubmed.get(interactionAc);

                List<String> otherInteractions = pubmedToInteraction.get(pubmedAc);

                if (interactionAcs.containsAll(otherInteractions)){
                    pubmedIds.add(pubmedAc);
                }
            }

            spokeExpandedPubmeds.put(detType, pubmedIds);
        }

        return spokeExpandedPubmeds;
    }

    public Map<Map.Entry<String, String>, Set<String>> collectDistinctInteractionDetails(EncoreInteraction interaction){
        Map<String, List<String>> typeToPubmed = interaction.getTypeToPubmed();
        Map<String, List<String>> methodToPubmed = interaction.getMethodToPubmed();
        Map<Map.Entry<String, String>, Set<String>> distinctLines = new HashMap<Map.Entry<String, String>, Set<String>>();

        for (Map.Entry<String, List<String>> methodEntry : methodToPubmed.entrySet()){
            List<String> pubmeds1 = methodEntry.getValue();
            String method = this.clusterScore.getMiTerms().get(methodEntry.getKey());

            for (Map.Entry<String, List<String>> typeEntry : typeToPubmed.entrySet()){
                List<String> pubmeds2 = typeEntry.getValue();
                String type = this.clusterScore.getMiTerms().get(typeEntry.getKey());

                Set<String> associatedPubmeds = new HashSet(CollectionUtils.intersection(pubmeds1, pubmeds2));

                if (!associatedPubmeds.isEmpty()){

                    distinctLines.put(new DefaultMapEntry(method, type), associatedPubmeds);
                }
            }
        }

        return distinctLines;
    }

    /**
     * Format introduced on July 29th 2009.
     *
     * Generate the CC line content based on the Interaction and its two interactor.
     * <br> protein1 is the entry in which that CC content will appear.
     * <p/>
     * <pre>
     *          <font color=gray>ID   rr44_HUMAN     STANDARD;      PRT;   123 AA.</font>
     *          <font color=gray>AC   P01232</font>
     *          <font color=gray>GN   rr44.</font>
     *          CC   -!- INTERACTION:
     *          CC       Interact=Yes (PubMed:12344567); Xref=IntAct:EBI-375446,EBI-389883;
     *          CC         Protein1=rr44 [P01232];
     *          CC         Protein2=tsr [P10981];
     * </pre>
     * @param interaction : the interaction to convert
     *
     * @return a CCLine
     */
    private CcLine formatCCLines(EncoreInteraction interaction, String uniprot1, String uniprot2) {

        StringBuffer buffer = new StringBuffer(256); // average size is 160 char

        // extract gene names
        String geneName1 = this.clusterScore.getGeneNames().get(uniprot1);
        String geneName2 = this.clusterScore.getGeneNames().get(uniprot2);

        // extract organisms
        String [] organismsA;
        String [] organismsB;
        if (interaction.getInteractorA().equals(uniprot1)){
            organismsA = extractOrganismFrom(interaction.getOrganismsA());
            organismsB = extractOrganismFrom(interaction.getOrganismsB());
        }
        else {
            organismsA = extractOrganismFrom(interaction.getOrganismsB());
            organismsB = extractOrganismFrom(interaction.getOrganismsA());
        }
        String taxId1 = organismsA[0];
        String taxId2 = organismsB[0];

        String organism2 = organismsB[1];

        // collect all pubmeds and spoke expanded information
        Map<String, List<String>> typeToPubmed = interaction.getTypeToPubmed();
        Map<String, List<String>> methodToPubmed = interaction.getMethodToPubmed();

        Map<Map.Entry<String, String>, Set<String>> distinctInformationDetails = collectDistinctInteractionDetails(interaction);
        Map<Map.Entry<String, String>, Set<String>> distinctInformationDetailsWithSpokeExpanded = collectSpokeExpandedInteractions(interaction);
        Map<Map.Entry<String, String>, Set<String>> distinctInformationDetailsTrueBinary = new HashMap<Map.Entry<String, String>, Set<String>>();

        for (Map.Entry<Map.Entry<String, String>, Set<String>> detail : distinctInformationDetails.entrySet()){
            String type = detail.getKey().getValue();
            String method = detail.getKey().getKey();

            if (distinctInformationDetailsWithSpokeExpanded.containsKey(detail.getKey())){
                Set<String> pubmedsWithoutExpansion =  new HashSet(CollectionUtils.subtract(detail.getValue(), distinctInformationDetailsWithSpokeExpanded.get(detail.getKey())));

                if (!pubmedsWithoutExpansion.isEmpty()){
                    distinctInformationDetailsTrueBinary.put(detail.getKey(), pubmedsWithoutExpansion);
                }
            }
            else{
                distinctInformationDetailsTrueBinary.put(detail.getKey(), detail.getValue());
            }
        }

        return writer.createCCline(uniprot1, geneName1, taxId1, uniprot2, geneName2, taxId2, organism2, distinctInformationDetailsTrueBinary, distinctInformationDetailsWithSpokeExpanded);
    }
}
