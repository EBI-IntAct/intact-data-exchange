package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.DefaultGOParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.Set;

/**
 * Converts an EncoreInteraction into a GOParameter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class EncoreInteractionToGoLineConverter {
    private static final Logger logger = Logger.getLogger(EncoreInteractionToGoLineConverter.class);

    /**
     * Converts an EncoreInteraction into GOParameters
     * @param interaction
     * @return The converted GOParameters
     */
    public GOParameters convertInteractionIntoGOParameters(EncoreInteraction interaction){
        // extract the uniprot acs of the firts and second interactors
        String uniprot1 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsA());
        String uniprot2 = FilterUtils.extractUniprotAcFromAccs(interaction.getInteractorAccsB());

        // if the uniprot acs are not null, it is possible to create a GOParameter
        if (uniprot1 != null && uniprot2 != null){
            if (uniprot1.contains(WriterUtils.CHAIN_PREFIX)){
                uniprot1 = uniprot1.substring(0, uniprot1.indexOf(WriterUtils.CHAIN_PREFIX));
            }

            if (uniprot2.contains(WriterUtils.CHAIN_PREFIX)){
                uniprot2 = uniprot2.substring(0, uniprot2.indexOf(WriterUtils.CHAIN_PREFIX));
            }
            // build a pipe separated list of pubmed IDs
            Set<String> pubmedIds = FilterUtils.extractPubmedIdsFrom(interaction.getPublicationIds());

            // if the list of pubmed ids is not empty, the GOParameter is created
            if (!pubmedIds.isEmpty()){
                logger.debug("convert GO parameters for " + uniprot1 + ", " + uniprot2 + ", " + pubmedIds.size() + " pubmed ids");
                GOParameters parameters = new DefaultGOParameters(uniprot1, uniprot2, pubmedIds);

                return parameters;
            }
            logger.debug("No pubmed ids for "+uniprot1+" and "+uniprot2+", cannot convert into GOLines");
        }

        logger.debug("one of the uniprot ac is null, cannot convert into GOLines");
        return null;
    }
}
