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

    // collection of large scale experiments : each experiment can be splitted
    private Collection<Experiment> largeScaleExperiments = new ArrayList<Experiment>();
    // collection of small scale experiments : each experiment will be one entry
    private Collection<Experiment> smallScaleExperiments = new ArrayList<Experiment>();
    // collection of mixed experiments : the experiments will be in a single entry
    private Collection<Experiment> mixedExperiments = new ArrayList<Experiment>();

    // collection of negativeInteractions. In IntAct, one negative interaction is in a separate experiment
    private Collection<Experiment> negativeExperiments = new ArrayList<Experiment>();

    // an intactEntry for generating xml
    private IntactEntry intactEntry;

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
        intactEntry = new IntactEntry();
    }

    @Override
    @Transactional(readOnly = true)
    public SortedSet<PublicationEntry> process(Publication item) throws Exception {
        // we export compact xml only
        uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext.getInstance().setGenerateExpandedXml(false);
        ConverterContext.getInstance().getConverterConfig().setXmlForm(PsimiXmlForm.FORM_COMPACT);

        int sumOfInteractions = 0;

        // filter the experiments and interactions of this publication
        for (Experiment exp : item.getExperiments()){
            int interactionSize = exp.getInteractions().size();

            // the experiments does contain negative interactions. Normally in intact, one experiment containing one negative should only contain negative
            if (interactionSize > 0 && InteractionUtils.isNegative(exp.getInteractions().iterator().next())){
                negativeExperiments.add(exp);
            }
            // one large scale experiment will be splitted and need to be processed separately
            else if (interactionSize >= largeScale){
                largeScaleExperiments.add(exp);
            }
            // the experiment is quite big and will form a small scale chunk
            else if (interactionSize >= smallScale){
                smallScaleExperiments.add(exp);
            }
            // we can mix this experiment with others
            else {
                mixedExperiments.add(exp);
            }
        }

        SortedSet<PublicationEntry> publicationEntries = new TreeSet<PublicationEntry>();

        if (!largeScaleExperiments.isEmpty()){
            processLargeScaleExperiments(publicationEntries, item.getOwner(), item.getShortLabel(), item.getCreated());
        }
        if (!smallScaleExperiments.isEmpty()){
            boolean appendChunkIndex = smallScaleExperiments.size() + mixedExperiments.size() > 1;
           processSmallScaleExperiments(publicationEntries, item.getOwner(), item.getShortLabel(), item.getCreated(), appendChunkIndex);
        }
        if (!negativeExperiments.isEmpty()){
           processNegativeExperiments(publicationEntries, item.getOwner(), item.getShortLabel(), item.getCreated());
        }
        if (!mixedExperiments.isEmpty()){
           processMixedExperiments(publicationEntries, item.getOwner(), item.getShortLabel(), item.getCreated(), smallScaleExperiments.size());
        }

        // clean the context before returning the result
        negativeExperiments.clear();
        largeScaleExperiments.clear();
        smallScaleExperiments.clear();
        mixedExperiments.clear();

        return publicationEntries;
    }

    /**
     * Each experiment is split into several chunks of interactions which will be one xmlEntry
     * @param publicationEntries
     * @param institution
     * @param publicationId
     * @param date
     */
    private void processLargeScaleExperiments(Collection<PublicationEntry> publicationEntries, Institution institution, String publicationId, Date date){

        for (Experiment exp : largeScaleExperiments){
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
                intactEntry.setInstitution(institution);
                // set release date
                intactEntry.setReleasedDate(date);

                while (interactionChunk < largeScale && interactionChunk < totalSize){

                    Interaction interaction = iterator.next();
                    intactEntry.getInteractions().add(interaction);

                    interactionChunk++;
                }

                numberOfChunk++;

                // name of the entry = publicationId_experimentLabel_chunkNumber
                String publicationName = publicationId + separator + exp.getShortLabel() + separator + numberOfChunk;
                // flush the current intact entry and start a new one
                createChunkPublicationEntry(publicationEntries, date, publicationName);

                interactionProcessed += interactionChunk;
            }
        }
    }

    /**
     * Each experiment will be one xmlEntry
     * @param publicationEntries
     * @param institution
     * @param publicationId
     * @param date
     */
    private void processSmallScaleExperiments(Collection<PublicationEntry> publicationEntries, Institution institution, String publicationId, Date date, boolean appendSmallChunk){
        int numberOfSmallChunk = 0;

        for (Experiment exp : smallScaleExperiments){
            numberOfSmallChunk++;

            // set institution
            intactEntry.setInstitution(institution);
            // set release date
            intactEntry.setReleasedDate(date);
            // add all the interactions to the intactEntry
            intactEntry.getInteractions().addAll(exp.getInteractions());

            // name of the entry = publicationId_smallChunkNumber
            String publicationName = publicationId;

            if (appendSmallChunk || smallScaleExperiments.size() > 1){
                 publicationName = publicationId + separator + numberOfSmallChunk;
            }

            // flush the current intact entry and start a new one
            createChunkPublicationEntry(publicationEntries, date, publicationName);
        }
    }

    /**
     * Each experiment will be appended in a unique xmlEntry
     * @param publicationEntries
     * @param institution
     * @param publicationId
     * @param date
     */
    private void processNegativeExperiments(Collection<PublicationEntry> publicationEntries, Institution institution, String publicationId, Date date){

        // set institution
        intactEntry.setInstitution(institution);
        // set release date
        intactEntry.setReleasedDate(date);

        for (Experiment exp : negativeExperiments){

            // add all the interactions to the intactEntry
            intactEntry.getInteractions().addAll(exp.getInteractions());
        }

        // name of the entry = publicationId_smallChunkNumber
        String publicationName = publicationId + separator + negativeTag;
        // flush the current intact entry and start a new one
        createChunkPublicationEntry(publicationEntries, date, publicationName);
    }

    /**
     * Append all the experiments up to a large scale threshold value of interactions
     * @param publicationEntries
     * @param institution
     * @param publicationId
     * @param date
     */
    private void processMixedExperiments(Collection<PublicationEntry> publicationEntries, Institution institution, String publicationId, Date date, int start){

        int numberOfSmallChunk = start;
        int numberInteractions = 0;
        int numberExperiment = 0;

        Iterator<Experiment> iterator = mixedExperiments.iterator();
        // set institution
        intactEntry.setInstitution(institution);
        // set release date
        intactEntry.setReleasedDate(date);

        while (numberExperiment < mixedExperiments.size()){
            Experiment exp = iterator.next();
            int sizeInteractions = exp.getInteractions().size();

            // the number of interactions is still inferior to largescale so we can append this experiment to the current entry
            if (numberInteractions + sizeInteractions <= largeScale){
                numberInteractions += sizeInteractions;

                // add all the interactions to the intactEntry
                intactEntry.getInteractions().addAll(exp.getInteractions());
            }
            // the number of interactions is too big for this entry so the experiment will be appended to a new entry after processing the current one
            else {
                // we have a new chunk
                numberOfSmallChunk++;
                // name of the entry = publicationId_smallChunkNumber
                String publicationName = publicationId + separator + numberOfSmallChunk;
                // flush the current intact entry and start a new one
                createChunkPublicationEntry(publicationEntries, date, publicationName);

                // start a new intact entry
                // set institution
                intactEntry.setInstitution(institution);
                // set release date
                intactEntry.setReleasedDate(date);
                // add all the interactions to the intactEntry
                intactEntry.getInteractions().addAll(exp.getInteractions());

                numberInteractions = sizeInteractions;
            }

            // process the last or only one intactEntry
            // name of the entry = publicationId_smallChunkNumber
            String publicationName = publicationId;
            if (numberOfSmallChunk > 1){
                publicationName = publicationId + separator + numberOfSmallChunk;
            }
            // flush the current intact entry and start a new one
            createChunkPublicationEntry(publicationEntries, date, publicationName);
        }
    }

    private void createChunkPublicationEntry(Collection<PublicationEntry> publicationEntries, Date date, String publicationName) {
        // generate the xml entry for this chunk of interactions
        Entry xmlEntry = entryConverter.intactToPsi(intactEntry);

        EntrySet entrySet = new EntrySet(Arrays.asList(xmlEntry), 2, 5, 4);

        // create a publication entry
        PublicationEntry publicationEntry = new PublicationEntry(date, publicationName, entrySet);
        // add the publication entry to the list of publication entries
        publicationEntries.add(publicationEntry);
        // clear the intact entry
        clearIntactEntry();
    }

    private void clearIntactEntry() {
        // clear the intactEntry
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
}
