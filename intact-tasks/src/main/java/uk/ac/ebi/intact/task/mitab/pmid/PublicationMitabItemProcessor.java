package uk.ac.ebi.intact.task.mitab.pmid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.OrganismImpl;
import psidev.psi.mi.tab.model.builder.MitabWriterUtils;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.util.InteractionUtils;
import uk.ac.ebi.intact.psimitab.converters.converters.ExperimentConverter;
import uk.ac.ebi.intact.psimitab.converters.converters.PublicationConverter;
import uk.ac.ebi.intact.task.mitab.IntactBinaryInteractionProcessor;
import uk.ac.ebi.intact.task.mitab.InteractionExpansionCompositeProcessor;
import uk.ac.ebi.intact.task.util.FileNameGenerator;

import java.util.*;

/**
 * Processor which converts a publication from intact to mitab file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */

public class PublicationMitabItemProcessor implements ItemProcessor<Publication, SortedSet<PublicationFileEntry>>, ItemStream {

    /**
     * The fileName generator
     */
    private FileNameGenerator publicationNameGenerator;

    private StringBuffer currentStringBuilder;

    private StringBuffer currentNegativeStringBuilder;

    private static final Log log = LogFactory.getLog(PublicationMitabItemProcessor.class);

    private PsimiTabVersion version = PsimiTabVersion.v2_7;

    private IntactBinaryInteractionProcessor compositeProcessor;

    private BinaryInteraction binaryTemplate;
    private PublicationConverter publicationConverter;
    private ExperimentConverter experimentConverter;

    private boolean addPublicationIdentifiers = false;

    public PublicationMitabItemProcessor(){
        currentStringBuilder = new StringBuffer(1064);
        currentNegativeStringBuilder = new StringBuffer(1064);
        publicationNameGenerator = new FileNameGenerator();
        publicationConverter = new PublicationConverter();
        experimentConverter = new ExperimentConverter();
        experimentConverter.setAddPublicationIdentifiers(addPublicationIdentifiers);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SortedSet<PublicationFileEntry> process(Publication item) throws Exception {

        // reattach the publication object to the entity manager because connection may have been closed after reading the object
        Publication publication = IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().merge(item);

        log.info("Start processing publication : " + publication.getShortLabel());
        // if the publication does not have any experiments, we skip it
        if (publication.getExperiments().isEmpty()){
            log.info("Skip publication " + publication.getShortLabel() + " because does not contain any experiments");
            return null;
        }

        // convert the publication
        convertPublicationToMitab(publication);

        // iterator of experiments
        Iterator<Experiment> iterator = publication.getExperiments().iterator();

        // the generated publication entries
        SortedSet<PublicationFileEntry> publicationEntries = new TreeSet<PublicationFileEntry>();

        // clear builders
        clearIntactBuilders();

        // convert experiments in one to several publication entry(ies)
        while (iterator.hasNext()){
            // clear previous experiment details
            clearExperimentDetails();

            // the processed experiment
            Experiment exp = iterator.next();

            // convert experiment details
            convertExperimentToMitab(exp);

            // number of interactions attached to the processed experiment
            int interactionSize = exp.getInteractions().size();

            log.info("Process Experiment " + exp.getShortLabel() + ", interactions : " + interactionSize);

            // we only process experiments having interactions
            if(interactionSize > 0){
                for (Interaction interaction : exp.getInteractions()){
                    // the experiments does contain negative interactions. Normally in intact, one experiment containing one negative should only contain negative
                    if (InteractionUtils.isNegative(interaction)){
                        processIntactInteraction(interaction, this.currentNegativeStringBuilder, exp);
                    }
                    // positive interactions
                    else {
                        processIntactInteraction(interaction, this.currentStringBuilder, exp);
                    }
                }
            }
            else {
                log.info("Skip experiment " + exp.getShortLabel() + " because does not contain any interactions");
            }
        }

        if (this.currentNegativeStringBuilder.length() > 0){
            createPublicationEntry(publicationEntries, publication.getCreated(), publication.getShortLabel(), this.currentNegativeStringBuilder, true);
        }
        if (this.currentStringBuilder.length() > 0){
            createPublicationEntry(publicationEntries, publication.getCreated(), publication.getShortLabel(), this.currentStringBuilder, false);
        }

        IntactContext.getCurrentInstance().getDaoFactory().getEntityManager().clear();

        return publicationEntries;
    }

    private void convertPublicationToMitab(Publication publication) {
        binaryTemplate = new BinaryInteractionImpl();

        if (publication == null){
           return;
        }

        publicationConverter.intactToMitab(publication, binaryTemplate);
    }

    private void convertExperimentToMitab(Experiment experiment) {
        if (this.binaryTemplate == null){
            this.binaryTemplate = new BinaryInteractionImpl();
        }
        if (experiment == null){
            return;
        }

        experimentConverter.intactToMitab(experiment, binaryTemplate, true, false);
    }

    private void processIntactInteraction(Interaction interaction, StringBuffer builder, Experiment exp) throws Exception {
        Collection<? extends BinaryInteraction> binaryInteractions = this.compositeProcessor.process(interaction);
        if (binaryInteractions != null && !binaryInteractions.isEmpty()){
            boolean isFirst = true;

            for (BinaryInteraction binary : binaryInteractions){
                copyPublicationAndExperimentDetailsTo(binary, isFirst, exp);
                builder.append(MitabWriterUtils.buildLine(binary, this.version));

                isFirst = false;
            }
        }
    }

    private void copyPublicationAndExperimentDetailsTo(BinaryInteraction binary, boolean isFirst, Experiment exp){

        if (binary != null && this.binaryTemplate != null){
            // we override all the shared collections of the expanded binary interactions excepted annotations (can come from publication and interactions)
            // experiment details
            binary.setDetectionMethods(binaryTemplate.getDetectionMethods());
            binary.setHostOrganism(binaryTemplate.getHostOrganism());

            if (binary.getInteractorA() != null && binary.getInteractorA().getParticipantIdentificationMethods().isEmpty()){
                 experimentConverter.addParticipantDetectionMethodForInteractor(exp,  binary.getInteractorA());
            }
            if (binary.getInteractorB() != null && binary.getInteractorB().getParticipantIdentificationMethods().isEmpty()){
                experimentConverter.addParticipantDetectionMethodForInteractor(exp,  binary.getInteractorB());
            }

            // publication details
            binary.setPublications(binaryTemplate.getPublications());
            binary.setAuthors(binaryTemplate.getAuthors());
            binary.setSourceDatabases(binaryTemplate.getSourceDatabases());
            binary.setCreationDate(binaryTemplate.getCreationDate());

            // we don't need to update all the interactions as they all share the same annotation collection
            if (isFirst){
               binary.getAnnotations().addAll(binaryTemplate.getAnnotations());
            }
        }
    }

    private void createPublicationEntry(Set<PublicationFileEntry> publicationEntries, Date date, String publicationName, StringBuffer mitab, boolean isNegative) {
        log.info("create publication entry : " + publicationName);

        // create a publication name
        // name of the entry = publicationId_experimentLabel_chunkNumber
        String entryName = publicationNameGenerator.createPublicationName(publicationName, null, isNegative);

        // create a publication entry
        PublicationFileEntry publicationEntry = new PublicationFileEntry(date, entryName, mitab, isNegative);
        // add the publication entry to the list of publication entries
        publicationEntries.add(publicationEntry);

        log.info("Finished chunk publication entry : " + publicationName);
    }

    private void clearIntactBuilders() {
        // clear the currentIntactEntry
        this.currentNegativeStringBuilder.setLength(0);
        this.currentStringBuilder.setLength(0);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");

        if (this.compositeProcessor == null){
            this.compositeProcessor = new InteractionExpansionCompositeProcessor(false, false);
            ((InteractionExpansionCompositeProcessor) this.compositeProcessor).setAddPublicationIdentifiers(this.addPublicationIdentifiers);
        }

        clearIntactBuilders();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        Assert.notNull(executionContext, "ExecutionContext must not be null");
    }

    @Override
    public void close() throws ItemStreamException {
        clearIntactBuilders();
    }

    public FileNameGenerator getPublicationNameGenerator() {
        return publicationNameGenerator;
    }

    public void setPublicationNameGenerator(FileNameGenerator publicationNameGenerator) {
        this.publicationNameGenerator = publicationNameGenerator;
    }

    public PsimiTabVersion getVersion() {
        return version;
    }

    public void setVersion(PsimiTabVersion version) {
        this.version = version;
    }

    public IntactBinaryInteractionProcessor getCompositeProcessor() {
        return compositeProcessor;
    }

    public void setCompositeProcessor(IntactBinaryInteractionProcessor compositeProcessor) {
        this.compositeProcessor = compositeProcessor;
    }

    private void clearExperimentDetails(){
         if (this.binaryTemplate != null){
             this.binaryTemplate.setDetectionMethods(new ArrayList<CrossReference>());
             this.binaryTemplate.setHostOrganism(new OrganismImpl());
         }
    }

    public boolean isAddPublicationIdentifiers() {
        return addPublicationIdentifiers;
    }

    public void setAddPublicationIdentifiers(boolean addPublicationIdentifiers) {
        this.addPublicationIdentifiers = addPublicationIdentifiers;
        this.experimentConverter.setAddPublicationIdentifiers(this.addPublicationIdentifiers);
    }
}
