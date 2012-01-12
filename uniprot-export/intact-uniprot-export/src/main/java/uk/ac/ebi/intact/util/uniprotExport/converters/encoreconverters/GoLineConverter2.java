package uk.ac.ebi.intact.util.uniprotExport.converters.encoreconverters;

import org.apache.log4j.Logger;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.intact.util.uniprotExport.UniprotExportUtils;
import uk.ac.ebi.intact.util.uniprotExport.filters.FilterUtils;
import uk.ac.ebi.intact.util.uniprotExport.parameters.golineparameters.GOParameters2;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.IntactTransSplicedProteins;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.*;

/**
 * Encore converter for GO lines, format 2
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>04/01/12</pre>
 */

public class GoLineConverter2 implements GoLineConverter<GOParameters2> {
    private static final Logger logger = Logger.getLogger(GoLineConverter2.class);

    /**
     * Converts an EncoreInteraction into GOParameters
     * @param interaction
     * @param firstInteractor uniprot ac of first interactor
     * @return The converted GOParameters
     */
    public List<GOParameters2> convertInteractionIntoGOParameters(EncoreInteractionForScoring interaction, String firstInteractor, MiClusterContext context){
        // extract the uniprot acs of the firts and second interactors
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

        // if the uniprot acs are not null, it is possible to create a GOParameter
        if (uniprot1 != null && uniprot2 != null){

            // build a pipe separated list of pubmed IDs
            Set<String> pubmedIds = FilterUtils.extractPubmedIdsFrom(interaction.getPublicationIds());
            
            // collect the interaction acs to collect go component xrefs
            Set<String> interactionAcs = interaction.getExperimentToPubmed().keySet();
            Set<String> goRefs = collectGoComponentRefsFrom(interactionAcs, context);

            // if the list of pubmed ids is not empty, the GOParameter is created
            if (!pubmedIds.isEmpty()){
                logger.debug("convert GO parameters for " + uniprot1 + ", " + uniprot2 + ", " + pubmedIds.size() + " pubmed ids");
                List<GOParameters2> parameters = new ArrayList<GOParameters2>();

                if (uniprot1.equalsIgnoreCase(firstInteractor)){
                    if (!UniprotExportUtils.isMasterProtein(uniprot1)){
                        
                        for (String pub : pubmedIds){
                            parameters.add(new GOParameters2(uniprot1, uniprot2, pub, UniprotExportUtils.extractMasterProteinFrom(uniprot1), goRefs));
                        }
                    }
                    else {
                        for (String pub : pubmedIds){
                            parameters.add(new GOParameters2(uniprot1, uniprot2, pub, uniprot1, goRefs));
                        }
                    }
                }
                else{
                    if (!UniprotExportUtils.isMasterProtein(uniprot2)){
                        for (String pub : pubmedIds){
                            parameters.add(new GOParameters2(uniprot2, uniprot1, pub, UniprotExportUtils.extractMasterProteinFrom(uniprot2), goRefs));
                        }
                    }
                    else {
                        for (String pub : pubmedIds){
                            parameters.add(new GOParameters2(uniprot2, uniprot2, pub, uniprot2, goRefs));
                        }
                    }
                }

                return parameters;
            }
            logger.warn("No pubmed ids for "+uniprot1+" and "+uniprot2+", cannot convert into GOLines");
        }

        logger.warn("one of the uniprot ac is null, cannot convert into GOLines");
        return Collections.EMPTY_LIST;
    }
    
    private Set<String> collectGoComponentRefsFrom(Set<String> interactionAcs, MiClusterContext context){
        
        Set<String> goRefs = new HashSet<String>();
        Map<String, Set<String>> mapOfGoRefs = context.getInteractionComponentXrefs();
        
        for (String ac : interactionAcs){
            if (mapOfGoRefs.containsKey(ac)){
                goRefs.addAll(mapOfGoRefs.get(ac));
            }
        }
        
        return goRefs;
    }

    /**
     * Converts a list of EncoreInteractions into a single GOParameters (only the master uniprot ac of the interactors of the first interaction will be used )
     * @param interactions : list of encore interactions involving the same interactors or feature chains of a same entry
     * @return The converted GOParameters
     */
    public List<GOParameters2> convertInteractionsIntoGOParameters(Set<EncoreInteractionForScoring> interactions, String parentAc, MiClusterContext context){
        List<GOParameters2> goParameters = new ArrayList<GOParameters2>(interactions.size());

        // for each binary interaction associated with the same uniprot entry given with parentAc
        for (EncoreInteractionForScoring interaction : interactions){
            // extract the uniprot acs of the first and second interactors for the first interaction
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

            // if the uniprot acs are not null, it is possible to create a GOParameter
            if (uniprot1 != null && uniprot2 != null){

                // build a pipe separated list of pubmed IDs
                Set<String> pubmedIds = FilterUtils.extractPubmedIdsFrom(interaction.getPublicationIds());
                // collect the interaction acs to collect go component xrefs
                Set<String> interactionAcs = interaction.getExperimentToPubmed().keySet();
                Set<String> goRefs = collectGoComponentRefsFrom(interactionAcs, context);

                // if the list of pubmed ids is not empty, the GOParameter is created
                if (!pubmedIds.isEmpty()){
                    Map<String, Set<IntactTransSplicedProteins>> transSplicedVariants = context.getTranscriptsWithDifferentMasterAcs();
                    boolean isUniprot1Isoform = isIsoformOfAnotherUniprotEntry(uniprot1, transSplicedVariants.get(parentAc));
                    boolean isUniprot2Isoform = isIsoformOfAnotherUniprotEntry(uniprot2, transSplicedVariants.get(parentAc));

                    // the first interactor is uniprot1 and the second uniprot is a different uniprot entry
                    if ((uniprot1.startsWith(parentAc) || isUniprot1Isoform) && !uniprot2.startsWith(parentAc) && !isUniprot2Isoform){
                        for (String pub : pubmedIds){
                            goParameters.add(new GOParameters2(uniprot1, uniprot2, pub, parentAc, goRefs));
                        }
                    }
                    // the first interactor is uniprot2 and the uniprot 1 is a different uniprot entry
                    else if ((uniprot2.startsWith(parentAc) || isUniprot2Isoform) && !uniprot1.startsWith(parentAc) && !isUniprot1Isoform) {
                        for (String pub : pubmedIds){
                            goParameters.add(new GOParameters2(uniprot2, uniprot1, pub, parentAc, goRefs));
                        }
                    }
                    // the two interactors are identical, we have a self interaction
                    else if (uniprot1.equalsIgnoreCase(uniprot2)){
                        for (String pub : pubmedIds){
                            goParameters.add(new GOParameters2(uniprot1, uniprot2, pub, parentAc, goRefs));
                        }
                    }
                    // the two interactors are from the same uniprot entry but are different isoforms/feature chains : we have a single interaction but two lines
                    else if (uniprot2.startsWith(parentAc) && uniprot1.startsWith(parentAc)) {
                        for (String pub : pubmedIds){
                            goParameters.add(new GOParameters2(uniprot1, uniprot2, pub, parentAc, goRefs));
                            goParameters.add(new GOParameters2(uniprot2, uniprot1, pub, parentAc, goRefs));
                        }
                    }
                    // the two interactors are from the same uniprot entry but are different isoforms/feature chains : one of the isoform does not match the master uniprot entry so we need to write it twice with the current parent ac
                    else if (uniprot1.startsWith(parentAc) && isUniprot2Isoform) {
                        for (String pub : pubmedIds){
                            goParameters.add(new GOParameters2(uniprot1, uniprot2, pub, parentAc, goRefs));
                            goParameters.add(new GOParameters2(uniprot2, uniprot1, pub, parentAc, goRefs));
                        }
                    }
                    // the two interactors are from the same uniprot entry but are different isoforms/feature chains : one of the isoform does not match the master uniprot entry so we need to write it twice with the current parent ac
                    else if (uniprot2.startsWith(parentAc) && isUniprot1Isoform) {
                        for (String pub : pubmedIds){
                            goParameters.add(new GOParameters2(uniprot2, uniprot1, pub, parentAc, goRefs));
                            goParameters.add(new GOParameters2(uniprot2, uniprot1, pub, parentAc, goRefs));
                        }
                    }
                    // the two interactors are from the same uniprot entry but are different isoforms/feature chains : both isoforms do not match the master uniprot entry so we need to write it twice with the current parent ac
                    else if (isUniprot1Isoform && isUniprot2Isoform){
                        for (String pub : pubmedIds){
                            goParameters.add(new GOParameters2(uniprot2, uniprot1, pub, parentAc, goRefs));
                            goParameters.add(new GOParameters2(uniprot2, uniprot1, pub, parentAc, goRefs));
                        }
                    }
                    else {
                        logger.info("The interaction "+uniprot1+" and "+uniprot2+" is ignored because both interactors are not matching the master uniprot ac");
                    }
                }
                else{
                    logger.error("No pubmed ids for "+uniprot1+" and "+uniprot2+", cannot convert into GOLines");
                }
            }
            else{
                logger.error("one of the uniprot ac is null, cannot convert into GOLines");
            }
        }

        return goParameters;
    }

    protected boolean isIsoformOfAnotherUniprotEntry(String interactor, Set<IntactTransSplicedProteins> transSplicedProteins){

        if (transSplicedProteins != null){
            for (IntactTransSplicedProteins prot : transSplicedProteins){
                if (interactor.equalsIgnoreCase(prot.getUniprotAc())){
                    return true;
                }
            }
        }
        return false;
    }
}
