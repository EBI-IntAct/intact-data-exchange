package uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.impl;

import edu.ucla.mbi.imex.central.ws.v20.Identifier;
import edu.ucla.mbi.imex.central.ws.v20.Publication;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexCentralManager;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.ImexCentralUpdater;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationIdentifierSynchronizer;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.actions.PublicationImexUpdaterException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Class which update identifiers in IMEx central if a submitted publication has been published in PubMed
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29/03/12</pre>
 */

public class PublicationIdentifierSynchronizerImpl extends ImexCentralUpdater implements PublicationIdentifierSynchronizer {
    private static final Log log = LogFactory.getLog(PublicationIdentifierSynchronizerImpl.class);

    private static String IMEX = "imex";
    private static String PUBMED = "pmid";
    private static String DOI = "doi";
    private static String INTERNAL = "jint";

    public boolean isIntactPublicationIdentifierInSyncWithImexCentral(String intactPubId, Publication imexPublication) throws ImexCentralException {

        List<Identifier> imexIdentifiers = imexPublication.getIdentifier();

        // no existing identifiers in IMEx central for this record. Or the publication is unassigned or DOI
        if (imexIdentifiers == null){

            Publication existingPub = imexCentral.getPublicationById(intactPubId);

            // the publication found in IMEx central is the same as the one found in IntAct so the identifiers are in sync
            return areIdenticalPublications(imexPublication, existingPub);
        }
        // existing identifiers
        if (imexIdentifiers != null) {
            // we have a pmid in IMEx central so we should have a pmid in IntAct
            for (Identifier id : imexIdentifiers){
                if (intactPubId.equalsIgnoreCase(id.getAc())){
                    return true;
                }
            }
        }

        // check if identifier is not in IMEx central
        Publication existingPub = imexCentral.getPublicationById(intactPubId);

        // the publication found in IMEx central is the same as the one found in IntAct so the identifiers are in sync
        return areIdenticalPublications(imexPublication, existingPub);
    }

    public void synchronizePublicationIdentifier(uk.ac.ebi.intact.model.Publication intactPublication, Publication imexPublication) throws PublicationImexUpdaterException, ImexCentralException {

        String pubId = extractPubIdFromIntactPublication(intactPublication);

        // if the publication id is not in IMEx central, we need to synchronize
        List<Identifier> imexIdentifiers = imexPublication.getIdentifier();

        // no existing (pubmed) identifiers in IMEx central for this record
        if (imexIdentifiers == null || (imexIdentifiers != null && imexIdentifiers.isEmpty())){

            // the identifier in Intact is not registered in IMEx central (check done by isIntactPublicationIdentifierInSyncWithImexCentral) and the record has a valid IMEx id so we can update the record
            if (imexPublication.getImexAccession() != null && !imexPublication.getImexAccession().equals(ImexCentralManager.NO_IMEX_ID)){
                imexPublication = imexCentral.updatePublicationIdentifier(imexPublication.getImexAccession(), pubId);
                log.info("Updated identifier " + pubId + " for the IMEx accession " + imexPublication.getImexAccession());
            }
            log.error("Cannot updated identifier " + pubId + " because no valid IMEx accession");
        }
        // existing identifiers
        else {
            String pubmed = null;
            String doi = null;
            String internal = null;

            // boolean to know if publication id in IntAct is a valid pubmed id
            boolean hasIntactPubmed = Pattern.matches(ImexCentralManager.PUBMED_REGEXP.toString(), pubId);

            for (Identifier id : imexIdentifiers){
                // we have a pubmed id in imex central
                if (PUBMED.equalsIgnoreCase(id.getNs())){
                    pubmed = id.getAc();
                }
                // we have a DOI in IMEx central
                else if (DOI.equalsIgnoreCase(id.getNs())) {
                    doi = id.getAc();
                }
                // we have an internal identifier in IMEx central
                else if (INTERNAL.equalsIgnoreCase(id.getNs())) {
                    internal = id.getAc();
                }
            }

            // there is a pubmed identifier in IMEx central and we have a pubmed identifier in IntAct
            if (pubmed != null && hasIntactPubmed){
                // pubmed id not in sync with IMEx central, we have a conflict
                if (pubmed.length() > 0 && !pubId.equalsIgnoreCase(pubmed)){
                    throw new PublicationImexUpdaterException("pubmed id conflict with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact pubmed = " + pubId + ", imex pubmed id = " + pubmed);
                }
            }
            // there is a DOI identifier in IMEx central and we have a potential DOI identifier in IntAct
            else if (doi != null && !hasIntactPubmed && !pubId.startsWith(UNASSIGNED_PREFIX)){
                // doi id not in sync with IMEx central, we have a conflict
                if (doi.length() > 0 && !pubId.equalsIgnoreCase(doi)){
                    throw new PublicationImexUpdaterException("DOI conflict with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact doi = " + pubId + ", imex doi = " + pubmed);
                }
            }
            // there is an internal identifier in IMEx central and we have a unassigned identifier in IntAct
            else if (internal != null && pubId.startsWith(UNASSIGNED_PREFIX)){
                // internal id not in sync with IMEx central, we have a conflict
                if (internal.length() > 0 && !pubId.equalsIgnoreCase(internal)){
                    throw new PublicationImexUpdaterException("Internal identifier conflict with IMEx central : imex = " + imexPublication.getImexAccession() + ", intact internal identifier = " + pubId + ", imex internal identifier = " + pubmed);
                }
            }
            // the IMEx record does not have any pubmed id but intact does have a valid Pubmed id, we need to update the record
            else if ((pubmed == null || (pubmed != null && pubmed.length() == 0)) && hasIntactPubmed){
                imexPublication = imexCentral.updatePublicationIdentifier(imexPublication.getImexAccession(), pubId);
                log.info("Updated identifier " + pubId + " for the IMEx accession " + imexPublication.getImexAccession());
            }
            // the imex record does not have any DOI numbers but intact dooes have a publication id which is not pubmed or unassigned, we need to update the record
            else if ((doi == null || (doi != null && doi.length() == 0)) && !hasIntactPubmed && !pubId.startsWith(UNASSIGNED_PREFIX)){
                imexPublication = imexCentral.updatePublicationIdentifier(imexPublication.getImexAccession(), pubId);
                log.info("Updated identifier " + pubId + " for the IMEx accession " + imexPublication.getImexAccession());
            }
            // the imex record does not have any internal identifiers but intact does have a publication id which is unassigned, we need to update the record
            else if ((internal == null || (internal != null && internal.length() == 0)) && pubId.startsWith(UNASSIGNED_PREFIX)){
                imexPublication = imexCentral.updatePublicationIdentifier(imexPublication.getImexAccession(), pubId);
                log.info("Updated identifier " + pubId + " for the IMEx accession " + imexPublication.getImexAccession());
            }

            // pubmed in imex central and not in intact, this is an error
            if (pubmed != null && pubmed.length() > 0 && !hasIntactPubmed){
                throw new PublicationImexUpdaterException("Publication has a valid pubmed id in IMEx central but not in intact : imex = " + imexPublication.getImexAccession() + ", intact publication identifier = " + pubId + ", imex pubmed id = " + pubmed);
            }
        }
    }

    public boolean areIdenticalPublications(Publication p1, Publication p2){

        if ((p1 == null && p2 != null) || (p1 != null && p2 == null)){
            return false;
        }
        else if (p1 != null && p2 != null){

            if (p1.getImexAccession() != null && p1.getImexAccession().equals(p2.getImexAccession())){
                return true;
            }
            else if (p1.getImexAccession() != null && !p1.getImexAccession().equals(p2.getImexAccession())){
                return false;
            }
            else if (p2.getImexAccession() != null ){
                return false;
            }

            if (p1.getAuthor() != null && !p1.getAuthor().equals(p2.getAuthor())){
                return false;
            }
            else if (p2.getAuthor() != null ){
                return false;
            }

            if (p1.getIdentifier() != null && p2.getIdentifier() != null && CollectionUtils.isEqualCollection(p1.getIdentifier(), p2.getIdentifier())){
                return false;
            }
            else if ((p1.getIdentifier() == null && p2.getIdentifier() != null) || (p2.getIdentifier() == null && p1.getIdentifier() != null) ){
                return false;
            }

            if (p1.getOwner() != null && !p1.getOwner().equals(p2.getOwner())){
                return false;
            }
            else if (p2.getOwner() != null ){
                return false;
            }

            if (p1.getPaperAbstract() != null && !p1.getPaperAbstract().equals(p2.getPaperAbstract())){
                return false;
            }
            else if (p2.getPaperAbstract() != null ){
                return false;
            }

            if (p1.getStatus() != null && !p1.getStatus().equals(p2.getStatus())){
                return false;
            }
            else if (p2.getStatus() != null ){
                return false;
            }

            if (p1.getTitle() != null && !p1.getTitle().equals(p2.getTitle())){
                return false;
            }
            else if (p2.getTitle() != null ){
                return false;
            }
        }

        return true;
    }
}
