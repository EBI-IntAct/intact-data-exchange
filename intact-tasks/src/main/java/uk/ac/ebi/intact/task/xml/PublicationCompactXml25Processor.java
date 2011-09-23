package uk.ac.ebi.intact.task.xml;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.transaction.annotation.Transactional;
import psidev.psi.mi.xml.PsimiXmlForm;
import psidev.psi.mi.xml.converter.ConverterContext;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.InteractionUtils;

import java.util.*;

/**
 * Processor which will convert a publication into a psi-xml25 entrySet
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/09/11</pre>
 */

public class PublicationCompactXml25Processor implements ItemProcessor<Publication, SortedSet<PublicationEntry>> {

    /**
     * Maximum limit of interactions per entry
     */
    private int largeScale;

    /**
     * The entry converter
     */
    private EntryConverter entryConverter;

    /**
     * Maximum limit of interactions per experiments for one entry
     */
    private int smallScale;

    // an currentIntactEntry for generating xml
    private IntactEntry currentIntactEntry;

    /**
     * The intactEntry object to use and flush at once
     */
    private IntactEntry independentIntactEntry;

    /**
     * separator for the entry name
     */
    private String separator;

    /**
     * Negative tag
     */
    private String negativeTag;

    public PublicationCompactXml25Processor(){
        this.entryConverter = new EntryConverter();
        currentIntactEntry = new IntactEntry();
        independentIntactEntry = new IntactEntry();
    }

    @Override
    @Transactional(readOnly = true)
    public SortedSet<PublicationEntry> process(Publication item) throws Exception {
        // we export compact xml only
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().setGenerateExpandedXml(false);
        ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_COMPACT);

        // we count the number of processed interactions for the current intact entry
        int sumOfInteractions = 0;
        // we count the number of intact entries we are processing
        int numberEntries = 1;

        // iterator of experiments
        Iterator<Experiment> iterator = item.getExperiments().iterator();

        // the generated publication entries
        SortedSet<PublicationEntry> publicationEntries = new TreeSet<PublicationEntry>();

        // the current date of release
        Date releasedDate = new Date(System.currentTimeMillis());

        // convert experiments in one to several publication entry(ies)
        while (iterator.hasNext()){
            // the processed experiment
            Experiment exp = iterator.next();

            // number of interactions attached to the processed experiment
            int interactionSize = exp.getInteractions().size();

            // the experiments does contain negative interactions. Normally in intact, one experiment containing one negative should only contain negative
            if (interactionSize > 0 && InteractionUtils.isNegative(exp.getInteractions().iterator().next())){
                // create a publication entry and reinitialize the independent intact entry
                processNegativeExperiments(publicationEntries, item.getOwner(), item.getShortLabel(), item.getCreated(), releasedDate, exp);
            }
            // one large scale experiment will be splitted and need to be processed separately in the independent intact entry
            else if (interactionSize >= largeScale){
                processLargeScaleExperiments(publicationEntries, item.getOwner(), item.getShortLabel(), item.getCreated(), releasedDate, exp);
            }
            // the next experiment cannot be added to the current intact entry so the current intact entry will be flushed and the processed experiment
            // will be in the next intact entry
            else if (interactionSize >= smallScale){
                // we append an index number to the entry name if we have several experiments. This experiment will be alone in a file so the other expiments
                // will go in another file
                boolean appendChunkIndex = item.getExperiments().size() > 1;

                // we create a publication entry for this experiment and flush it using the independent intact entry
                processSmallScaleExperiments(publicationEntries, item.getOwner(), item.getShortLabel(), item.getCreated(), releasedDate, appendChunkIndex, exp, numberEntries);

                // we processed one 'small scale' entry
                numberEntries ++;
            }
            // we cannot append a new experiment otherwise the number of interactions will be too big
            else if (sumOfInteractions + interactionSize > largeScale){
                // the current experiment will be in the next intact entry so an index is necessary
                boolean appendChunkIndex = true;

                // we flush the previous currentIntactEntry
                flushCurrentIntactEntry(publicationEntries, item.getShortLabel(), item.getCreated(), numberEntries, appendChunkIndex);
                // we processed one 'small scale entry'
                numberEntries ++;
                sumOfInteractions = 0;

                // we prepare a new currentIntactEntry
                startNewIntactEntry(item.getOwner(), item.getCreated(), exp);
                sumOfInteractions = interactionSize;
            }
            // we can mix this experiment with others
            else {
                sumOfInteractions += interactionSize;
                appendExperimentsToIntactEntry(exp);
            }

            // we can flush the current intact entry as the last experiment has been processed
            if (!iterator.hasNext() && !currentIntactEntry.getInteractions().isEmpty()){
                boolean appendChunkIndex = numberEntries > 1;

                // we flush the previous currentIntactEntry
                flushCurrentIntactEntry(publicationEntries, item.getShortLabel(), item.getCreated(), numberEntries, appendChunkIndex);
            }
        }

        return publicationEntries;
    }

    /**
     * Each experiment is split into several chunks of interactions which will be one xmlEntry
     * @param publicationEntries
     * @param institution
     * @param publicationId
     */
    private void processLargeScaleExperiments(Collection<PublicationEntry> publicationEntries, Institution institution, String publicationId, Date created, Date released, Experiment exp){

        // number of interactions already processed
        int interactionProcessed = 0;
        // total number of interactions for this experiment
        int totalSize = exp.getInteractions().size();
        // number of interaction chunks (and so interaction files)
        int numberOfChunk = 0;

        // iterator of the interactions
        Iterator<Interaction> iterator = exp.getInteractions().iterator();

        while (interactionProcessed < totalSize){
            // number of interactions processed for a specific chunk
            int interactionChunk = 0;

            // each chunk = a new xml entry
            // set institution
            independentIntactEntry.setInstitution(institution);
            // set release date
            independentIntactEntry.setReleasedDate(released);

            while (interactionChunk < largeScale && interactionChunk < totalSize){

                Interaction interaction = iterator.next();
                independentIntactEntry.getInteractions().add(interaction);

                interactionChunk++;
            }

            numberOfChunk++;

            // name of the entry = publicationId_experimentLabel_chunkNumber
            String publicationName = publicationId + separator + exp.getShortLabel() + separator + numberOfChunk;
            // flush the current intact entry and start a new one
            createChunkPublicationEntry(publicationEntries, created, publicationName, independentIntactEntry);

            interactionProcessed += interactionChunk;
        }
    }

    /**
     * Each experiment will be one xmlEntry
     * @param publicationEntries
     * @param institution
     * @param publicationId
     */
    private void processSmallScaleExperiments(Collection<PublicationEntry> publicationEntries, Institution institution, String publicationId, Date created, Date released, boolean appendSmallChunk, Experiment exp, int index){

        // set institution
        independentIntactEntry.setInstitution(institution);
        // set release date
        independentIntactEntry.setReleasedDate(released);
        // add all the interactions to the currentIntactEntry
        independentIntactEntry.getInteractions().addAll(exp.getInteractions());

        // name of the entry = publicationId_smallChunkNumber
        String publicationName = publicationId;

        if (appendSmallChunk || index > 1){
            publicationName = publicationId + separator + index;
        }

        // flush the current intact entry and start a new one
        createChunkPublicationEntry(publicationEntries, created, publicationName, independentIntactEntry);
    }

    /**
     * Each experiment will be appended in a unique xmlEntry
     * @param publicationEntries
     * @param institution
     * @param publicationId
     */
    private void processNegativeExperiments(Collection<PublicationEntry> publicationEntries, Institution institution, String publicationId, Date created, Date released, Experiment exp){

        // set institution
        independentIntactEntry.setInstitution(institution);
        // set release date
        independentIntactEntry.setReleasedDate(released);

        // add all the interactions to the currentIntactEntry
        independentIntactEntry.getInteractions().addAll(exp.getInteractions());

        // name of the entry = publicationId_smallChunkNumber
        String publicationName = publicationId + separator + negativeTag;
        // flush the current intact entry and start a new one
        createChunkPublicationEntry(publicationEntries, created, publicationName, independentIntactEntry);
    }

    /**
     * Append all the experiments up to a large scale threshold value of interactions
     */
    private void appendExperimentsToIntactEntry(Experiment exp){

        // add all the interactions to the currentIntactEntry
        currentIntactEntry.getInteractions().addAll(exp.getInteractions());

    }

    /**
     * Flush the current intact entry
     * @param publicationEntries
     * @param publicationId
     * @param index
     * @param appendChunkIndex
     */
    private void flushCurrentIntactEntry(Collection<PublicationEntry> publicationEntries, String publicationId, Date created, int index, boolean appendChunkIndex){

        // name of the entry = publicationId_smallChunkNumber
        String publicationName = publicationId;

        if (appendChunkIndex || index > 1){
            publicationName = publicationId + separator + index;
        }

        // flush the current intact entry and start a new one
        createChunkPublicationEntry(publicationEntries, created, publicationName, currentIntactEntry);

    }

    private void startNewIntactEntry(Institution institution, Date date, Experiment exp){

        // set institution
        currentIntactEntry.setInstitution(institution);
        // set release date
        currentIntactEntry.setReleasedDate(date);
        // add all the interactions to the currentIntactEntry
        currentIntactEntry.getInteractions().addAll(exp.getInteractions());
    }

    private void createChunkPublicationEntry(Collection<PublicationEntry> publicationEntries, Date date, String publicationName, IntactEntry intactEntry) {
        // generate the xml entry for this chunk of interactions
        Entry xmlEntry = entryConverter.intactToPsi(intactEntry);

        EntrySet entrySet = new EntrySet(Arrays.asList(xmlEntry), 2, 5, 4);

        // create a publication entry
        PublicationEntry publicationEntry = new PublicationEntry(date, publicationName, entrySet);
        // add the publication entry to the list of publication entries
        publicationEntries.add(publicationEntry);
        // clear the intact entry
        clearIntactEntry(intactEntry);
    }

    private void clearIntactEntry(IntactEntry intactEntry) {
        // clear the currentIntactEntry
        intactEntry.getInteractions().clear();

        if(intactEntry.getExperiments() != null){
            intactEntry.getExperiments().clear();
        }

        if(intactEntry.getInteractors() != null){
            intactEntry.getInteractors().clear();
        }

        if(intactEntry.getAnnotations() != null){
            intactEntry.getAnnotations().clear();
        }
        intactEntry.setInstitution(null);
        intactEntry.setReleasedDate(null);
    }

    public int getLargeScale() {
        return largeScale;
    }

    public void setLargeScale(int largeScale) {
        this.largeScale = largeScale;
    }

    public int getSmallScale() {
        return smallScale;
    }

    public void setSmallScale(int smallScale) {
        this.smallScale = smallScale;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getNegativeTag() {
        return negativeTag;
    }

    public void setNegativeTag(String negativeTag) {
        this.negativeTag = negativeTag;
    }
}
