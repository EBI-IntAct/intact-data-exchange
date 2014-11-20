package uk.ac.ebi.intact.dataexchange.psimi.exporter.pmid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.InteractionEvidence;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

import java.util.*;

/**
 * Processor which will convert a publication into PublicationFileEntry.
 * *
 * If the experiment contains a negative interaction, the file will be split into two different files : one for positive interactions, one for negative interactions
 * It will give a unique id for each processed object for the all step
 *
 * Some properties can be customized :
 * - fileName generator which generates the proper file name (initialized by default)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/09/11</pre>
 */

public class PublicationExportProcessor implements ItemProcessor<IntactPublication, SortedSet<PublicationFileEntry>>, ItemStream {

    /**
     * The fileName generator
     */
    private FileNameGenerator publicationNameGenerator;

    /*
    * the currentIntactEntry for generating chunks
     */
    private Collection<InteractionEvidence> currentIntactEntry;

    /*
   * the currentNegativeIntactEntry for generating xml
    */
    private Collection<InteractionEvidence> currentNegativeIntactEntry;

    private static final Log log = LogFactory.getLog(PublicationExportProcessor.class);

    @Autowired
    @Qualifier("intactDao")
    private IntactDao intactDao;

    public PublicationExportProcessor(){
        currentIntactEntry = new ArrayList<InteractionEvidence>();
        currentNegativeIntactEntry = new ArrayList<InteractionEvidence>();
        publicationNameGenerator = new FileNameGenerator();
    }

    @Override
    @Transactional(value = "jamiTransactionManager", readOnly = true, propagation = Propagation.REQUIRED)
    public SortedSet<PublicationFileEntry> process(IntactPublication item) throws Exception {

        // reattach the publication object to the entity manager because connection may have been closed after reading the object
        IntactPublication publication = item;
        if (!intactDao.getEntityManager().contains(item)){
            publication = intactDao.getEntityManager().merge(item);
        }

        log.info("Start processing publication : " + publication.getPubmedId());
        // if the publication does not have any experiments, we skip it
        if (publication.getExperiments().isEmpty()){
            log.info("Skip publication " + publication.getShortLabel() + " because does not contain any experiments");
            return null;
        }

        // we count the number of processed interactions for the current intact entry
        int sumOfInteractions = 0;
        int sumOfNegativeInteractions = 0;

        // iterator of experiments
        Iterator<Experiment> iterator = publication.getExperiments().iterator();

        // the generated publication entries
        SortedSet<PublicationFileEntry> publicationEntries = new TreeSet<PublicationFileEntry>();

        // convert experiments in one to several publication entry(ies)
        while (iterator.hasNext()){
            // the processed experiment
            Experiment exp = iterator.next();

            // number of interactions attached to the processed experiment
            int interactionSize = exp.getInteractionEvidences().size();

            log.info("Process Experiment " + ((IntactExperiment)exp).getShortLabel() + ", interactions : " + interactionSize);

            // we only process experiments having interactions
            if(interactionSize > 0){
                // the experiments does contain negative interactions. Normally in intact, one experiment containing one negative should only contain negative
                if (exp.getInteractionEvidences().iterator().next().isNegative()){
                    log.info("Append experiment to intact entry");

                    if (sumOfNegativeInteractions == 0){
                        startNewIntactEntry(exp, currentNegativeIntactEntry);
                    }
                    else {
                        this.currentNegativeIntactEntry.addAll(exp.getInteractionEvidences());
                    }

                    sumOfNegativeInteractions += interactionSize;
                }
                // we can mix this experiment with others
                else {
                    log.info("Append experiment to intact entry");

                    if (sumOfInteractions == 0){
                        startNewIntactEntry(exp, currentIntactEntry);
                    }
                    else {
                        this.currentIntactEntry.addAll(exp.getInteractionEvidences());
                    }

                    sumOfInteractions += interactionSize;
                }

                // we can flush the current intact entry as the last experiment has been processed
                if (!iterator.hasNext() && !currentIntactEntry.isEmpty()){
                    log.info("Create final chunk file for " + publication.getShortLabel());

                    // we flush the previous currentIntactEntry
                    flushIntactEntry(publicationEntries, publication.getShortLabel(), publication.getCreated(), currentIntactEntry, false);
                }
                if (!iterator.hasNext() && !currentNegativeIntactEntry.isEmpty()){
                    log.info("Create final chunk file for " + publication.getShortLabel());

                    // we flush the previous currentIntactEntry
                    flushIntactEntry(publicationEntries, publication.getShortLabel(), publication.getCreated(), currentNegativeIntactEntry, true);
                }
            }
            else {
                log.info("Skip experiment " + ((IntactExperiment) exp).getShortLabel()+ " because does not contain any interactions");
            }
        }

        return publicationEntries;
    }

    /**
     * Flush the current intact entry
     * @param publicationEntries
     * @param publicationId
     */
    private void flushIntactEntry(Collection<PublicationFileEntry> publicationEntries, String publicationId, Date created,
                                  Collection<InteractionEvidence> intactEntry, boolean isNegative){

        // name of the entry = publicationId_smallChunkNumber
        String publicationName = publicationNameGenerator.createPublicationName(publicationId, null, isNegative);


        // flush the current intact entry and start a new one
        createPublicationEntry(publicationEntries, created, publicationName, intactEntry);
    }

    private void startNewIntactEntry(Experiment exp, Collection<InteractionEvidence> intactEntry){

        // add all the interactions to the currentIntactEntry
        intactEntry.addAll(exp.getInteractionEvidences());
    }

    //@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    private void createPublicationEntry(Collection<PublicationFileEntry> publicationEntries, Date date, String publicationName,
                                        Collection<InteractionEvidence> intactEntry) {
        log.info("create publication entry : " + publicationName);

        // create a publication entry
        PublicationFileEntry publicationEntry = new PublicationFileEntry(date, publicationName, new ArrayList<InteractionEvidence>(intactEntry));
        // add the publication entry to the list of publication entries
        publicationEntries.add(publicationEntry);

        log.info("Finished publication entry : " + publicationName);

        // clear the intact entry
        clearIntactEntry(intactEntry);
    }

    private void clearIntactEntry(Collection<InteractionEvidence> intactEntry) {
        // clear the currentIntactEntry
        intactEntry.clear();
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        clearIntactEntry(currentIntactEntry);
        clearIntactEntry(currentNegativeIntactEntry);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        // nothing to do
    }

    @Override
    public void close() throws ItemStreamException {

        clearIntactEntry(currentIntactEntry);
        clearIntactEntry(currentNegativeIntactEntry);
    }

    public FileNameGenerator getPublicationNameGenerator() {
        return publicationNameGenerator;
    }

    public void setPublicationNameGenerator(FileNameGenerator publicationNameGenerator) {
        this.publicationNameGenerator = publicationNameGenerator;
    }
}
