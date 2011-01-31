package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.parameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.GOLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.GOLineWriterImpl;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Writer of GO Lines
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class MiClusterToGOLineConverter extends AbstractConverter {

    private GOLineWriter writer;
    public MiClusterToGOLineConverter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        super(clusterScore, fileName);
        this.writer = new GOLineWriterImpl(fileName);
    }

    private Set<String> extractPubmedIdsFrom(EncoreInteraction interaction){
        Set<String> pubmedIds = new HashSet<String>(interaction.getPublicationIds().size());

        for (CrossReference ref : interaction.getPublicationIds()){
            if (WriterUtils.PUBMED.equalsIgnoreCase(ref.getDatabase())){
                pubmedIds.add(ref.getIdentifier());
            }
        }

        return pubmedIds;
    }

    private void convertInteractionIntoGOLines(EncoreInteraction interaction) throws IOException {
        String uniprot1 = interaction.getInteractorA(WriterUtils.UNIPROT);
        String uniprot2 = interaction.getInteractorB(WriterUtils.UNIPROT);

        if (uniprot1 != null && uniprot2 != null){
            // build a pipe separated list of pubmed IDs
            Set<String> pubmedIds = extractPubmedIdsFrom(interaction);

            GOParameters parameters = new GOParameters(uniprot1, uniprot2, pubmedIds);
            writer.writeGOLine(parameters);
        }

        writer.close();
    }

    public void write() throws IOException {

        for (Map.Entry<Integer, EncoreInteraction> interaction : this.clusterScore.getInteractionMapping().entrySet()){

            convertInteractionIntoGOLines(interaction.getValue());
        }
    }
}
