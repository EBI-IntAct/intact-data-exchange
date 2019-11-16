package uk.ac.ebi.intact.util.uniprotExport.converters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.parameters.drlineparameters.DRParametersImpl;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Converts an interactor into a DR line
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>31/01/11</pre>
 */

public class DRLineConverterVersion1 implements DRLineConverter {
    private static final Logger logger = Logger.getLogger(DRLineConverterVersion1.class);
    
    private Set<String> processedSecondInteractors;

    public DRLineConverterVersion1(){
        processedSecondInteractors = new HashSet<String>();
    }

    /**
     * Converts an interactor into a DR line.
     *
     * The number of distinct interactors does not take into account isoforms of a same uniprot entry as different interactors and does not take into account
     * self interactions
     * @param interactorAc
     * @param interactions : the interactions the interactor is involved in
     * @param context : the context of the cluster
     * @return the converted DRParameter
     * @throws IOException
     */
    public DRParameters convertInteractorIntoDRLine(String interactorAc, Set<EncoreInteraction> interactions, MiClusterContext context){
        processedSecondInteractors.clear();

        // if the interactor ac is not null, we can create a DRParameter
        if (interactorAc != null){
            logger.debug("Convert DR parameters for " + interactorAc + ", " + interactions.size());

            Map<String, Set<IntactTransSplicedProteins>> transSplicedVariants = context.getTranscriptsWithDifferentMasterAcs();

            Set<IntactTransSplicedProteins> transIsoforms = transSplicedVariants.get(interactorAc);

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

                // if the uniprot acs are not null, it is possible to convert into a DRParameter
                if (uniprot1 != null && uniprot2 != null){

                    // boolean to know if uniprot 1 is from same uniprot entry than uniprot master
                    boolean isUniprot1FromSameUniprotEntry = UniprotExportUtils.isFromSameUniprotEntry(interactorAc, uniprot1, transSplicedVariants.get(interactorAc));
                    // boolean to know if uniprot 2 is from same uniprot entry than uniprot master
                    boolean isUniprot2FromSameUniprotEntry = UniprotExportUtils.isFromSameUniprotEntry(interactorAc, uniprot2, transSplicedVariants.get(interactorAc));

                    // the first uniprot is from the same uniprot entry as the master uniprot but the uniprot 2 is from another uniprot entry
                    if (isUniprot1FromSameUniprotEntry && !isUniprot2FromSameUniprotEntry){
                        // we add the master uniprot of second uniprot
                        processedSecondInteractors.add(UniprotExportUtils.extractMasterProteinFrom(uniprot2));
                    }
                    // the second uniprot is from the same uniprot entry as the master uniprot but the uniprot 1 is from another uniprot entry
                    else if (!isUniprot1FromSameUniprotEntry && isUniprot2FromSameUniprotEntry){
                        // we add the master uniprot of the first uniprot
                        processedSecondInteractors.add(UniprotExportUtils.extractMasterProteinFrom(uniprot1));
                    }
                    // we don't count self interactions or interactions of a master with one of its isoforms
                    else {
                        logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " is not converted because the two interactors are not related to the master uniprot " + interactorAc);
                    }
                }
                else{
                    logger.error("Interaction " + uniprot1 + " and " + uniprot2 + " has one of the unipprot acs/ intact acs which is null.");
                }
            }

            if (!processedSecondInteractors.isEmpty()){
                return new DRParametersImpl(interactorAc, processedSecondInteractors.size());
            }
        }

        logger.warn("interactor Ac is null, cannot convert into DRLines");

        processedSecondInteractors.clear();
        return null;
    }
}
