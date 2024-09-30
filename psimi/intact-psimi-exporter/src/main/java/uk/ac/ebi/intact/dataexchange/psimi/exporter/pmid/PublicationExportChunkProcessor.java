package uk.ac.ebi.intact.dataexchange.psimi.exporter.pmid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.InteractionEvidence;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;
import uk.ac.ebi.intact.jami.model.extension.IntactPublication;

import javax.annotation.Resource;
import java.util.*;

/**
 * Processor which will convert a publication loaded from IntAct database into a sorted set of PublicationFileEntry with a limitation in the number of interactions
 * per publicationFileEntry.
 *
 * If the experiment contains a negative interaction, the file will be split into two different files : one for positive interactions, one for negative interactions
 * It will give a unique id for each processed object for the all step .
 *
 * If the number of interactions is superior to largescale property, the processor will split into chunk publicationFileEntry
 *
 * Some properties can be customized :
 * - fileName generator which generates the proper file name (initialized by default)
 * - largescale which controls the size of the batch per publicationFileEntry
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/09/11</pre>
 */

public class PublicationExportChunkProcessor implements ItemProcessor<IntactPublication, SortedSet<PublicationFileEntry>>, ItemStream {

    /**
     * Maximum limit of interactions per entry
     */
    private int largeScale = 2000;

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

    /**
     * The intactEntry object to use and flush at once
     */
    private Collection<InteractionEvidence> independentIntactEntry;

    private static final Log log = LogFactory.getLog(PublicationExportChunkProcessor.class);

    @Resource(name ="intactDao")
    private IntactDao intactDao;

    public PublicationExportChunkProcessor(){
        currentIntactEntry = new ArrayList<InteractionEvidence>();
        independentIntactEntry = new ArrayList<InteractionEvidence>();
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
        // we count the number of intact entries we are processing
        int numberEntries = 1;
        int numberNegativeEntries = 1;

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
                    if (interactionSize > largeScale){
                        processLargeScaleExperiments(
                                publicationEntries, publication.getShortLabel(), publication.getCreated(), exp, true, publication.getPublicationDate());
                    }
                    // we cannot append a new experiment otherwise the number of interactions will be too big
                    else if (sumOfNegativeInteractions + interactionSize > largeScale){
                        log.info("create chunk files");

                        // the current experiment will be in the next intact entry so an index is necessary
                        boolean appendChunkIndex = true;

                        // we flush the previous currentIntactEntry
                        flushIntactEntry(
                                publicationEntries, publication.getShortLabel(), publication.getCreated(), numberNegativeEntries, appendChunkIndex, this.currentNegativeIntactEntry, true, publication.getPublicationDate());
                        // we processed one 'small scale entry'
                        numberNegativeEntries ++;
                        sumOfNegativeInteractions = 0;

                        // we prepare a new currentIntactEntry
                        startNewIntactEntry(exp, currentNegativeIntactEntry);
                        sumOfNegativeInteractions = interactionSize;
                    }
                    // we can mix this experiment with others
                    else {
                        log.info("Append experiment to intact entry");

                        if (sumOfNegativeInteractions == 0){
                            startNewIntactEntry(exp, currentNegativeIntactEntry);
                        }
                        else {
                            this.currentNegativeIntactEntry.addAll(exp.getInteractionEvidences());
                        }

                        sumOfNegativeInteractions += interactionSize;
                    }
                }
                // one large scale experiment will be splitted and need to be processed separately in the independent intact entry
                else if (interactionSize > largeScale){
                    processLargeScaleExperiments(
                            publicationEntries, publication.getShortLabel(), publication.getCreated(), exp, false, publication.getPublicationDate());
                }
                // we cannot append a new experiment otherwise the number of interactions will be too big
                else if (sumOfInteractions + interactionSize > largeScale){
                    log.info("create chunk files");

                    // the current experiment will be in the next intact entry so an index is necessary
                    boolean appendChunkIndex = true;

                    // we flush the previous currentIntactEntry
                    flushIntactEntry(
                            publicationEntries, publication.getShortLabel(), publication.getCreated(), numberEntries, appendChunkIndex, currentIntactEntry, false, publication.getPublicationDate());
                    // we processed one 'small scale entry'
                    numberEntries ++;
                    sumOfInteractions = 0;

                    // we prepare a new currentIntactEntry
                    startNewIntactEntry(exp, currentIntactEntry);
                    sumOfInteractions = interactionSize;
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
                    boolean appendChunkIndex = numberEntries > 1;

                    // we flush the previous currentIntactEntry
                    flushIntactEntry(
                            publicationEntries, publication.getShortLabel(), publication.getCreated(), numberEntries, appendChunkIndex, currentIntactEntry, false, publication.getPublicationDate());
                }
                if (!iterator.hasNext() && !currentNegativeIntactEntry.isEmpty()){
                    log.info("Create final chunk file for " + publication.getShortLabel());
                    boolean appendChunkIndex = numberNegativeEntries > 1;

                    // we flush the previous currentIntactEntry
                    flushIntactEntry(
                            publicationEntries, publication.getShortLabel(), publication.getCreated(), numberNegativeEntries, appendChunkIndex, currentNegativeIntactEntry, true, publication.getPublicationDate());
                }
            }
            else {
                log.info("Skip experiment " + ((IntactExperiment) exp).getShortLabel()+ " because does not contain any interactions");
            }
        }

        return publicationEntries;
    }

    /**
     * Each experiment is split into several chunks of interactions which will be one xmlEntry
     * @param publicationEntries
     * @param publicationId
     */
    private void processLargeScaleExperiments(Collection<PublicationFileEntry> publicationEntries, String publicationId,
                                              Date created, Experiment exp, boolean isNegative, Date publicationDate){
        log.info("Create large scale experiment " + ((IntactExperiment)exp).getShortLabel());
        // number of interactions already processed
        int interactionProcessed = 0;
        // total number of interactions for this experiment
        int totalSize = exp.getInteractionEvidences().size();
        // number of interaction chunks (and so interaction files)
        int numberOfChunk = 0;

        // iterator of the interactions
        Iterator<InteractionEvidence> iterator = exp.getInteractionEvidences().iterator();

        Set<String> interactorAcs = new HashSet<String>(exp.getInteractionEvidences().size() * 2);

        while (interactionProcessed < totalSize){
            // number of interactions processed for a specific chunk
            int interactionChunk = 0;

            // each chunk = a new xml entry

            while (interactionChunk < largeScale && interactionChunk < totalSize && iterator.hasNext()){

                InteractionEvidence interaction = iterator.next();
                independentIntactEntry.add(interaction);

                interactionChunk++;
            }

            numberOfChunk++;

            // name of the entry = publicationId_experimentLabel_chunkNumber
            String publicationName = publicationNameGenerator.createPublicationName(publicationId, ((IntactExperiment)exp).getShortLabel(), numberOfChunk, isNegative);
            // flush the current intact entry and start a new one
            createChunkPublicationEntry(publicationEntries, created, publicationName, independentIntactEntry, publicationDate);

            interactionProcessed += interactionChunk;

            // clear the interactors acs as we finished the current chunk
            interactorAcs.clear();
        }
    }

    /**
     * Flush the current intact entry
     * @param publicationEntries
     * @param publicationId
     * @param index
     * @param appendChunkIndex
     */
    private void flushIntactEntry(Collection<PublicationFileEntry> publicationEntries, String publicationId, Date created, int index, boolean appendChunkIndex,
                                  Collection<InteractionEvidence> intactEntry, boolean isNegative, Date publicationDate){

        // name of the entry = publicationId_smallChunkNumber
        String publicationName;

        if (appendChunkIndex || index > 1){
            publicationName = publicationNameGenerator.createPublicationName(publicationId, index, isNegative);
        }
        else {
            publicationName = publicationNameGenerator.createPublicationName(publicationId, null, isNegative);
        }

        // flush the current intact entry and start a new one
        createChunkPublicationEntry(publicationEntries, created, publicationName, intactEntry, publicationDate);

    }

    private void startNewIntactEntry(Experiment exp, Collection<InteractionEvidence> intactEntry){

        // add all the interactions to the currentIntactEntry
        intactEntry.addAll(exp.getInteractionEvidences());
    }

    //@Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    private void createChunkPublicationEntry(Collection<PublicationFileEntry> publicationEntries, Date date, String publicationName,
                                             Collection<InteractionEvidence> intactEntry, Date publicationDate) {
        log.info("create chunk publication entry : " + publicationName);

        // create a publication entry
        PublicationFileEntry publicationEntry = new PublicationFileEntry(
                date, publicationName, new ArrayList<InteractionEvidence>(intactEntry), publicationDate);
        // add the publication entry to the list of publication entries
        publicationEntries.add(publicationEntry);

        log.info("Finished chunk publication entry : " + publicationName);

        // clear the intact entry
        clearIntactEntry(intactEntry);
    }

    private void clearIntactEntry(Collection<InteractionEvidence> intactEntry) {
        // clear the currentIntactEntry
        intactEntry.clear();
    }

    public int getLargeScale() {
        return largeScale;
    }

    public void setLargeScale(int largeScale) {
        this.largeScale = largeScale;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        clearIntactEntry(currentIntactEntry);
        clearIntactEntry(currentNegativeIntactEntry);
        clearIntactEntry(independentIntactEntry);
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
        clearIntactEntry(independentIntactEntry);
    }

    public FileNameGenerator getPublicationNameGenerator() {
        return publicationNameGenerator;
    }

    public void setPublicationNameGenerator(FileNameGenerator publicationNameGenerator) {
        this.publicationNameGenerator = publicationNameGenerator;
    }
}
