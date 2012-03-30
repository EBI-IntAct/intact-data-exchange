package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationIdentifierSynchronizer;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Class which update identifiers in IMEx central if a submitted dataset has been published in IMEx central
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public class PublicationIdentifierSynchronizerImpl extends ImexCentralUpdater implements PublicationIdentifierSynchronizer {

    public static String NO_IMEX_ID="N/A";
    private static int UNKNOWN_IDENTIFIER = 5;
    private static String IMEX = "imex";
    private static String PUBMED = "pmid";
    private static String DOI = "doi";
    private static Pattern PUBMED_REGEXP = Pattern.compile("\\d+");
    
    public boolean isIntactPublicationIdentifierInSyncWithImexCentral(String intactPubId, Publication imexPublication){

        List<Identifier> imexIdentifiers = imexPublication.getIdentifier();

        // no existing identifiers in IMEx central for this record
        if (imexIdentifiers == null || (imexIdentifiers != null && imexIdentifiers.isEmpty())){
            return false;
        }
        // existing identifiers
        else {
            
            for (Identifier id : imexIdentifiers){
                if (intactPubId.equalsIgnoreCase(id.getAc())){
                    return true;
                }
            }

            return false;
        }
    }

    public void synchronizePublicationIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException, ImexCentralException {

        String pubId = extractPubIdFromIntactPublication(intactPublication);

        List<Identifier> imexIdentifiers = imexPublication.getIdentifier();

        // no existing identifiers in IMEx central for this record
        if (imexIdentifiers == null || (imexIdentifiers != null && imexIdentifiers.isEmpty())){

            // the publication identifier is not unassigned, amd the record has a valid IMEx id so we can update the record
            if (!pubId.startsWith(UNASSIGNED_PREFIX) && imexPublication.getImexAccession() != null && !imexPublication.getImexAccession().equals(NO_IMEX_ID)){
                imexCentral.updatePublicationIdentifier(imexPublication.getImexAccession(), pubId);
            }
        }
        // existing identifiers
        else {
            String pubmed = null;
            String doi = null;
            boolean testDoi = false;
            boolean testPubmed = false;
            boolean hasIntactPubmed = Pattern.matches(PUBMED_REGEXP.toString(), pubId);

            for (Identifier id : imexIdentifiers){
                // we only compare non IMEx ids
                if (!IMEX.equalsIgnoreCase(id.getNs())){
                    // we have a pubmed id in imex central
                    if (PUBMED.equalsIgnoreCase(id.getNs())){
                        pubmed = id.getAc();

                        // the identifier in INTACT is a valid pubmed id
                        if (hasIntactPubmed){
                            // pubmed id in sync with imex central, nothing to do
                            if (pubId.equalsIgnoreCase(id.getAc())){
                                break; 
                            }
                            // pubmed id not in sync with IMEx central, we have a conflict
                            else{
                                throw new PublicationImexUpdaterException("Conflict in pubmed id with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact pubmed = " + pubId + ", imex pubmed id = " + id.getAc());
                            }
                        }
                        // in intact the publication is unassigned and in imex central we have a valid pubmed id, need to be looked at by a curator
                        else if (pubId.startsWith(UNASSIGNED_PREFIX)){
                            throw new PublicationImexUpdaterException("Conflict in pubmed id with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact unassigned pubmed = " + pubId + ", imex pubmed id = " + id.getAc());

                        }
                        // the intact identifier may be a doi, to test later
                        else {
                           testDoi = true;
                        }
                    }
                    // we have a DOI in IMEx central
                    else if (DOI.equalsIgnoreCase(id.getNs())) {
                        doi = id.getAc();

                        // the identifier in INTACT is a potential DOI number
                        if (!hasIntactPubmed){
                            // doi id in sync with imex central, nothing to do
                            if (pubId.equalsIgnoreCase(id.getAc())){
                                break;
                            }
                            // the intact publication is not unassigned, we have a conflict with imex central
                            else if (!pubId.startsWith(UNASSIGNED_PREFIX)) {
                                throw new PublicationImexUpdaterException("Conflict in doi number with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact doi = " + pubId + ", imex doi id = " + id.getAc());
                            }
                        }
                        // in intact the publication is not unassigned, and is a pubmed id. To test later
                        else if (!pubId.startsWith(UNASSIGNED_PREFIX)){
                            testPubmed = true;
                        }
                    }
                }
            }

            if (pubmed != null && testPubmed){
                // pubmed id not in sync with IMEx central, we have a conflict
                if (!pubId.equalsIgnoreCase(pubmed)){
                    throw new PublicationImexUpdaterException("Conflict in pubmed id with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact pubmed = " + pubId + ", imex pubmed id = " + pubmed);
                }
            }

            if (pubmed != null && testPubmed){
                // pubmed id not in sync with IMEx central, we have a conflict
                if (!pubId.equalsIgnoreCase(pubmed)){
                    throw new PublicationImexUpdaterException("Conflict in pubmed id with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact pubmed = " + pubId + ", imex pubmed id = " + pubmed);
                }
            }
            else if (doi != null && testDoi){
                // doi id not in sync with IMEx central, we have a conflict
                if (!pubId.equalsIgnoreCase(doi)){
                    throw new PublicationImexUpdaterException("Conflict in doi with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact doi = " + pubId + ", imex doi = " + pubmed);
                }
            }
            // the IMEx record does not have any pubmed id but intact does have a valid Pubmed id, we need to update the record
            else if (pubmed == null && hasIntactPubmed && !pubId.startsWith(UNASSIGNED_PREFIX)){
                imexCentral.updatePublicationIdentifier(imexPublication.getImexAccession(), pubId);
            }
            // the imex record does not have any DOI numbers but intact dooes have a publication id which is not pubmed or unassigned, we need to update the record
            else if (doi == null && !hasIntactPubmed && !pubId.startsWith(UNASSIGNED_PREFIX)){
                imexCentral.updatePublicationIdentifier(imexPublication.getImexAccession(), pubId);
            }
        }

    }
}
