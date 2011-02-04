package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.miscore.filter.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.GOParametersImpl;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Converts an EncoreInteraction into a GOParameter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class EncoreInteractionToGoLineConverter {

    /**
     *
     * @param interaction
     * @return a set of pubmed Ids for this interaction
     */
    private Set<String> extractPubmedIdsFrom(EncoreInteraction interaction){
        Set<String> pubmedIds = new HashSet<String>(interaction.getPublicationIds().size());

        for (CrossReference ref : interaction.getPublicationIds()){
            if (WriterUtils.PUBMED.equalsIgnoreCase(ref.getDatabase())){
                pubmedIds.add(ref.getIdentifier());
            }
        }

        return pubmedIds;
    }

    /**
     * Converts an EncoreInteraction into GOParameters
     * @param interaction
     * @return The converted GOParameters
     */
    public GOParameters convertInteractionIntoGOParameters(EncoreInteraction interaction){
        // extract the uniprot acs of the firts and second interactors
        String [] interactorA = FilterUtils.extractUniprotAndIntactAcFromAccs(interaction.getInteractorAccsA());
        String [] interactorB = FilterUtils.extractUniprotAndIntactAcFromAccs(interaction.getInteractorAccsB());

        String uniprot1 = interactorA[0];
        String uniprot2 = interactorB[0];

        // if the uniprot acs are not null, it is possible to create a GOParameter
        if (uniprot1 != null && uniprot2 != null){
            // build a pipe separated list of pubmed IDs
            Set<String> pubmedIds = extractPubmedIdsFrom(interaction);

            // if the list of pubmed ids is not empty, the GOParameter is created
            if (!pubmedIds.isEmpty()){
                GOParameters parameters = new GOParametersImpl(uniprot1, uniprot2, pubmedIds);

                return parameters;
            }

        }

        return null;
    }
}
