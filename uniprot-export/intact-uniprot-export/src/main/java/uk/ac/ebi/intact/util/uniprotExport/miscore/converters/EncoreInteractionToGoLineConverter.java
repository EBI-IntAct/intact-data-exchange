package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.parameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class EncoreInteractionToGoLineConverter {

    private Set<String> extractPubmedIdsFrom(EncoreInteraction interaction){
        Set<String> pubmedIds = new HashSet<String>(interaction.getPublicationIds().size());

        for (CrossReference ref : interaction.getPublicationIds()){
            if (WriterUtils.PUBMED.equalsIgnoreCase(ref.getDatabase())){
                pubmedIds.add(ref.getIdentifier());
            }
        }

        return pubmedIds;
    }

    public GOParameters convertInteractionIntoGOParameters(EncoreInteraction interaction){
        String uniprot1 = interaction.getInteractorA(WriterUtils.UNIPROT);
        String uniprot2 = interaction.getInteractorB(WriterUtils.UNIPROT);

        if (uniprot1 != null && uniprot2 != null){
            // build a pipe separated list of pubmed IDs
            Set<String> pubmedIds = extractPubmedIdsFrom(interaction);

            if (!pubmedIds.isEmpty()){
                GOParameters parameters = new GOParameters(uniprot1, uniprot2, pubmedIds);

                return parameters;
            }

        }

        return null;
    }
}
