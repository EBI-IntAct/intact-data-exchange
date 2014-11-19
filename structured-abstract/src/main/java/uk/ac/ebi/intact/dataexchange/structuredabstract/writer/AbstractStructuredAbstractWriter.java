package uk.ac.ebi.intact.dataexchange.structuredabstract.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.exception.MIIOException;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.model.Participant;
import psidev.psi.mi.jami.model.Xref;
import uk.ac.ebi.intact.dataexchange.structuredabstract.model.Sentence;
import uk.ac.ebi.intact.dataexchange.structuredabstract.model.SentenceProperty;
import uk.ac.ebi.intact.dataexchange.structuredabstract.model.SimpleInteractor;
import uk.ac.ebi.intact.jami.model.IntactPrimaryObject;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;

import java.io.*;
import java.util.*;

/**
 * Abstract writer for structured abstract
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public abstract class AbstractStructuredAbstractWriter<I extends Interaction> implements InteractionWriter<I>{
    private static final Log log = LogFactory.getLog(AbstractStructuredAbstractWriter.class);

    /**
     * 	 TARGET List of biological roles
     */
    private Set<String> targetMi;

    /**
     * 	 ENZYME List  of biological roles
     */
    private Set<String> enzymeMi;

    /**
     * 	 BAIT List of experimental roles
     */
    private Set<String> baitMi;

    /**
     *  PREY List of experimental roles
     */
    private Set<String> preyMi;

    /**
     * Map containing all sentences
     */
    private Map<Integer, Sentence> sentenceMap;

    /**
     * The writer
     */
    private Writer writer;

    /**
     * The properties for building sentences
     */
    private String sentencesPropertiesPath;

    /**
     * The map of sentence properties depending on the interaction type
     */
    private Map<String, SentenceProperty> sentencePropertiesMap;

    /**
     * IntAct link to interaction details
     */
    public static final String INTACT_LINK = "http://www.ebi.ac.uk/intact/interaction/";
    /**
     * IntAct link to complex details
     */
    public static final String COMPLEX_LINK = "http://www.ebi.ac.uk/intact/complex/details";

    /**
     * The stringBuilder used to build strings
     */
    private StringBuilder stringBuilder;

    /**
     * Interactor acs to write
     */
    private TreeSet<String> interactorAcs;

    private Map<String, String> cvTermUrls;
    private boolean isInitialised = false;

    public AbstractStructuredAbstractWriter() {
        isInitialised = false;
        initialiseBaits();
        initialiseEnzymes();
        initialisePreys();
        initialiseTargets();
        sentenceMap = new HashMap<Integer, Sentence>();
        this.sentencePropertiesMap = new HashMap<String, SentenceProperty>();
        this.stringBuilder = new StringBuilder(1024);
        this.interactorAcs = new TreeSet<String>();
        this.cvTermUrls = new HashMap<String, String>();
    }

    public AbstractStructuredAbstractWriter(Writer writer) {
        initialiseWriter(writer);
        initialiseBaits();
        initialiseEnzymes();
        initialisePreys();
        initialiseTargets();
        sentenceMap = new HashMap<Integer, Sentence>();
        this.sentencePropertiesMap = new HashMap<String, SentenceProperty>();
        this.stringBuilder = new StringBuilder(1024);
        this.interactorAcs = new TreeSet<String>();
        this.cvTermUrls = new HashMap<String, String>();

        isInitialised = true;
    }

    public AbstractStructuredAbstractWriter(OutputStream stream) {
        initialiseOutputStream(stream);
        initialiseBaits();
        initialiseEnzymes();
        initialisePreys();
        initialiseTargets();
        sentenceMap = new HashMap<Integer, Sentence>();
        this.sentencePropertiesMap = new HashMap<String, SentenceProperty>();
        this.stringBuilder = new StringBuilder(1024);
        this.interactorAcs = new TreeSet<String>();
        this.cvTermUrls = new HashMap<String, String>();

        isInitialised = true;
    }

    public AbstractStructuredAbstractWriter(File file) throws IOException {
        initialiseFile(file);

        initialiseBaits();
        initialiseEnzymes();
        initialisePreys();
        initialiseTargets();
        sentenceMap = new HashMap<Integer, Sentence>();
        this.sentencePropertiesMap = new HashMap<String, SentenceProperty>();
        this.stringBuilder = new StringBuilder(1024);
        this.interactorAcs = new TreeSet<String>();
        this.cvTermUrls = new HashMap<String, String>();

        isInitialised = true;
    }

    @Override
    public void initialiseContext(Map<String, Object> options) {
        if (options == null && !isInitialised){
            throw new IllegalArgumentException("The options for the abstract writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY
                    + " to know where to write the interactions.");
        }
        else if (options == null){
            return;
        }
        else if (options.containsKey(InteractionWriterOptions.OUTPUT_OPTION_KEY)){
            Object output = options.get(InteractionWriterOptions.OUTPUT_OPTION_KEY);

            if (output instanceof File){
                try {
                    initialiseFile((File) output);
                } catch (IOException e) {
                    throw new IllegalArgumentException("Impossible to open and write in output file " + ((File)output).getName(), e);
                }
            }
            else if (output instanceof OutputStream){
                initialiseOutputStream((OutputStream) output);
            }
            else if (output instanceof Writer){
                initialiseWriter((Writer) output);
            }
            // suspect a file path
            else if (output instanceof String){
                try {
                    initialiseFile(new File((String)output));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Impossible to open and write in output file " + output, e);
                }
            }
            else {
                throw new IllegalArgumentException("Impossible to write in the provided output "+output.getClass().getName() + ", a File, OuputStream, Writer or file path was expected.");
            }
        }
        else if (!isInitialised){
            throw new IllegalArgumentException("The options for the abstract writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write the interactions.");
        }

        isInitialised = true;
    }

    @Override
    public void start() throws MIIOException {
        if (!isInitialised){
            throw new IllegalStateException("The abstract writer has not been initialised. " +
                    "The options for the abstract writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write " +
                    "the interactions.");
        }
        clear();
    }

    @Override
    public void end() throws MIIOException {
        if (!isInitialised){
            throw new IllegalStateException("The abstract writer has not been initialised. " +
                    "The options for the abstract writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write " +
                    "the interactions.");
        }
        clear();
    }

    @Override
    public void write(I interaction) throws MIIOException {
        if (!isInitialised){
            throw new IllegalStateException("The abstract writer has not been initialised. " +
                    "The options for the abstract writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write " +
                    "the interactions.");
        }

        try {
            writeStructuredAbstract(interaction);
        } catch (IOException e) {
            throw new MIIOException("Cannot write interaction "+interaction, e);
        }
    }

    @Override
    public void write(Collection<? extends I> interactions) throws MIIOException {
        if (!isInitialised){
            throw new IllegalStateException("The abstract writer has not been initialised. " +
                    "The options for the abstract writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write " +
                    "the interactions.");
        }

        try {
            for (I interaction : interactions){
                writeStructuredAbstract(interaction);
            }
        } catch (IOException e) {
            throw new MIIOException("Cannot write interactions ", e);
        }
    }

    @Override
    public void write(Iterator<? extends I> interactions) throws MIIOException {
        if (!isInitialised){
            throw new IllegalStateException("The abstract writer has not been initialised. " +
                    "The options for the abstract writer should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write " +
                    "the interactions.");
        }

        try {
            while (interactions.hasNext()){
                writeStructuredAbstract(interactions.next());
            }
        } catch (IOException e) {
            throw new MIIOException("Cannot write interactions ", e);
        }
    }

    @Override
    public void flush() throws MIIOException {
        if (this.writer != null){
            try {
                this.writer.flush();
            } catch (IOException e) {
                throw new MIIOException("Cannot flush the writer", e);
            }
        }
    }

    @Override
    public void reset() throws MIIOException {
        if (isInitialised){

            try {
                flush();
            }
            finally {
                isInitialised = false;
                writer = null;
                clear();
            }
        }
    }

    public void close() throws MIIOException {
        if (isInitialised){

            try {
                flush();
            }
            finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new MIIOException("Impossible to close the Abstract writer", e);
                }
            }
            isInitialised = false;
            writer = null;
            clear();
        }
    }

    public void clear() {
        this.sentenceMap.clear();
        this.sentencePropertiesMap.clear();
        this.stringBuilder.setLength(0);
        this.interactorAcs.clear();
        this.cvTermUrls.clear();
    }

    public String getSentencesPropertiesPath() {
        return sentencesPropertiesPath;
    }

    public void setSentencesPropertiesPath(String sentencesPropertiesPath) {
        this.sentencesPropertiesPath = sentencesPropertiesPath;
    }

    protected void writeSentences() throws IOException {
        Iterator<Sentence> sentenceIterator = this.sentenceMap.values().iterator();
        while (sentenceIterator.hasNext()) {
            writeSentence(sentenceIterator.next());
            if (sentenceIterator.hasNext()){
                writeLineSeparator();
            }
        }
    }

    protected abstract void writeLineSeparator() throws IOException;


    /**
     * Write structured abstract for interaction
     * @param interaction
     * @throws IOException
     */
    protected void writeStructuredAbstract(I interaction) throws IOException {
        if (interaction == null){
            throw new IllegalArgumentException("The publication cannot be null");
        }

        // clear
        clear();

        // read and collect abstract for interaction
        collectStructuredAbstractFrom(interaction);

        // write all collected sentences
        writeSentences();
    }

    protected void collectStructuredAbstractFrom(I interaction){

        // build a key
        buildInteractionkey(interaction);

        Sentence sentence = null;
        int key = this.stringBuilder.toString().hashCode();

        // 1. in HashMap
        if (this.sentenceMap.containsKey(key)) {
            sentence = this.sentenceMap.get(key);
        }
        // 2. else create it and put it in HashMap
        else if (interaction.getInteractionType() != null){
            sentence = new Sentence(interaction.getInteractionType(), extractInteractionDetectionMethodFrom(interaction));

            for (Object obj : interaction.getParticipants()) {
                Participant component = (Participant)obj;
                if (targetMi.contains(component.getBiologicalRole()
                        .getMIIdentifier())) {
                    sentence.addInteractorObject(component);
                } else if (enzymeMi.contains(component
                        .getBiologicalRole().getMIIdentifier())) {
                    sentence.addInteractorSubject(component);
                }
                else if (isParticipantPrey(component)) {
                    sentence.addInteractorObject(component);
                } else if (isParticipantBait(component)) {
                    sentence.addInteractorSubject(component);
                } else
                {
                    sentence.addInteractorSubject(component);
                }

            }
            this.sentenceMap.put(key, sentence);
        }
        else {
            throw new IllegalArgumentException("The interaction must hav a non null interaction type and a non null interaction detection method");
        }
        // add interactionAc to sentence
        sentence.addInteractionAc(interaction instanceof IntactPrimaryObject ? ((IntactPrimaryObject)interaction).getAc() : interaction.getShortName());
    }

    protected abstract boolean isParticipantBait(Participant component);

    protected abstract boolean isParticipantPrey(Participant component);

    protected abstract CvTerm extractInteractionDetectionMethodFrom(I interaction);

    protected void buildInteractionkey(I interaction) {
        this.stringBuilder.setLength(0);
        this.interactorAcs.clear();

        // read ordered set of interactor acs
        for (Object obj : interaction.getParticipants()) {
            Participant component = (Participant)obj;
            interactorAcs.add(component.getInteractor() instanceof IntactInteractor ?
                    ((IntactInteractor) component.getInteractor()).getAc() : component.getInteractor().getShortName());
        }

        for (String prL : interactorAcs) {
            stringBuilder.append("    ");
            stringBuilder.append(prL);
        }

        // interaction type
        stringBuilder.append(interaction.getInteractionType().getMIIdentifier());

        // experiment
        CvTerm method = extractInteractionDetectionMethodFrom(interaction);
        if (method != null) {
            stringBuilder.append("    ");
            stringBuilder.append(method.getMIIdentifier());
        }
    }

    protected void writeSentence(Sentence sentence) throws IOException {

        // PROTEIN SUBJECT------------------------------------------
        buildInteractorNamesFrom(sentence.getInteractorsSubject());

        // add proteins Subject if there is only this participant
        if ((sentence.getInteractorsObject().size() == 0) && (sentence.getInteractorsSubject().size() == 1)) {
            String subject = this.stringBuilder.toString();
            this.writer.write(subject);
            this.writer.write(" and ");
            this.writer.write(subject);
            this.writer.write(" ");
        }
        else {
            this.writer.write(this.stringBuilder.toString());
        }

        // load sentence properties if not done yet
        if (this.sentencePropertiesMap.isEmpty()){
            loadSentenceProperties();
        }

        // write interaction type
        writeInteractionType(sentence);

        // PROTEIN OBJECT------------------------------------------
        buildInteractorNamesFrom(sentence.getInteractorsObject());

        this.writer.write(this.stringBuilder.toString());
        this.writer.write(" by ");

        // write interaction detection method
        writeMIOutput(sentence.getDetMethod().getMIIdentifier(), sentence.getDetMethod().getFullName() != null ?
                sentence.getDetMethod().getFullName() : sentence.getDetMethod().getShortName());

        // mintAcs linking as view interaction
        this.writer.write(" (");
        if (sentence.getInteractionAcs().size() == 1) {
            writeInteractionAc(sentence.getInteractionAcs().iterator().next(), 0);

        } else {
            Iterator<String> interactionAcIterator = sentence.getInteractionAcs().iterator();
            int count = 1;
            while (interactionAcIterator.hasNext()){
                String ac = interactionAcIterator.next();
                writeInteractionAc(ac, count);
                if (interactionAcIterator.hasNext()){
                    writer.write(", ");
                }
                count++;
            }
        }
        this.writer.write(")");
        this.writer.flush();
    }

    protected void buildInteractorNamesFrom(List<SimpleInteractor> interactors) {
        this.stringBuilder.setLength(0);

        int subjectSize = interactors.size();
        Iterator<SimpleInteractor> interactorIterator = interactors.iterator();
        boolean isLast;
        boolean isFirst = true;
        while (interactorIterator.hasNext()){
            SimpleInteractor simple = interactorIterator.next();

            isLast = !interactorIterator.hasNext();
            // last element
            if (isLast && subjectSize > 1){
                this.stringBuilder.append(" and ");
            }
            // other element
            else if (!isFirst && subjectSize > 1){
                this.stringBuilder.append(", ");
            }

            String interactorName;
            if (simple.getAuthorAssignedName() != null) {
                interactorName = simple.getAuthorAssignedName();
            } else {
                interactorName = simple.getShortName();
            }

            buildXrefOutput(simple.getXref(), interactorName, simple.getAc());
            isFirst = false;
        }
    }

    protected abstract void buildXrefOutput(Xref xref, String proteinName, String ac);

    protected abstract void writeInteractionAc(String mintAC, int num_int) throws IOException;

    protected void writeInteractionType(Sentence sentence) throws IOException {
        this.writer.write(" ");
        if (this.sentencePropertiesMap.containsKey(sentence.getInteractionTypeMI())){
            SentenceProperty sentenceProperty = this.sentencePropertiesMap.get(sentence.getInteractionTypeMI());
            if (sentence.getInteractorsObject().size() == 0) {
                writeMIOutput(sentence.getInteractionTypeMI(), sentenceProperty.getPluralVerb());

            }// if there is ONE subject, use
            else if (sentence.getInteractorsSubject().size() == 1) {
                writeMIOutput(sentence.getInteractionTypeMI(), sentenceProperty.getSingularVerb());
            } else {
                writeMIOutput(sentence.getInteractionTypeMI(), sentenceProperty.getPluralVerb());
            }

            // add conjunction only if there are object proteins
            if (sentence.getInteractorsObject().size() > 0) {
                this.writer.write(" ");
                this.writer.write(sentenceProperty.getConjunction());
                this.writer.write(" ");
            }
        }
        else{
            if (sentence.getInteractorsObject().size() == 0) {
                writeMIOutput(sentence.getInteractionTypeMI(), "interact with");

            }// if there is ONE subject, use
            else if (sentence.getInteractorsSubject().size() == 1) {
                writeMIOutput(sentence.getInteractionTypeMI(), "interacts with");
            } else {
                writeMIOutput(sentence.getInteractionTypeMI(), "interact with");
            }

            // add conjunction only if there are object proteins
            if (sentence.getInteractorsObject().size() > 0) {
                this.writer.write("  ");
            }
        }
    }

    protected abstract void writeMIOutput(String MIcode, String verb) throws IOException;

    protected void loadSentenceProperties(){
        this.sentencePropertiesMap.clear();
        Properties prop = new Properties();

        if (this.sentencesPropertiesPath == null){
            try {
                //load a properties file
                prop.load(AbstractStructuredAbstractWriter.class.getResourceAsStream("/sentences.properties"));
                loadProperties(prop);

            } catch (IOException ex) {
                log.error("Impossible to load sentence properties.", ex);
            }
        }
        else {
            // load a file
            try {
                prop.load(new FileInputStream(this.sentencesPropertiesPath));
                loadProperties(prop);
            } catch (IOException e) {
                log.error("Impossible to load sentence properties from " + this.sentencesPropertiesPath, e);
            }
        }
    }

    /**
     * Loads the properties for the sentence
     * @param prop
     */
    protected void loadProperties(Properties prop) {
        for (Map.Entry<Object, Object> entry : prop.entrySet()){
            String[] values = extractValues((String) entry.getValue());
            if (values.length == 3){
                this.sentencePropertiesMap.put((String)entry.getKey(), new SentenceProperty(values[0], values[1], values[2]));
            }
            else if (values.length == 2){
                this.sentencePropertiesMap.put((String)entry.getKey(), new SentenceProperty(values[0], values[1], null));
            }
            else {
                this.sentencePropertiesMap.put((String)entry.getKey(), new SentenceProperty(values[0], values[0], null));
            }
        }
    }

    /**
     * Reads the cv term from the properties file
     * @param value
     * @return
     */
    protected String[] extractValues(String value){
        if (value.contains("|")){
            return value.split("\\|");
        }
        else{
            return new String[]{value};
        }
    }

    protected void initialiseTargets(){
        targetMi = new HashSet<String>(3);
        targetMi.add(Participant.ENZYME_TARGET_ROLE_MI);
        targetMi.add(Participant.ELECTRON_ACCEPTOR_ROLE_MI);
        targetMi.add(Participant.FLUORESCENCE_ACCEPTOR_ROLE_MI);
    }
    protected void initialiseEnzymes() {
        enzymeMi = new HashSet<String>(3);
        enzymeMi.add(Participant.ENZYME_ROLE_MI);
        enzymeMi.add(Participant.ELECTRON_DONOR_ROLE_MI);
        enzymeMi.add(Participant.FLUORESCENCE_DONOR_ROLE_MI);
    }

    protected void initialiseBaits() {
        baitMi = new HashSet<String>(5);
        baitMi.add(Participant.BAIT_ROLE_MI);
        baitMi.add(Participant.INHIBITED_MI);
        baitMi.add(Participant.ELECTRON_ACCEPTOR_ROLE_MI);
        baitMi.add(Participant.FLUORESCENCE_ACCEPTOR_ROLE_MI);
        baitMi.add(Participant.ENZYME_TARGET_ROLE_MI);
    }

    protected void initialisePreys() {
        preyMi = new HashSet<String>(5);
        preyMi.add(Participant.PREY_MI);
        preyMi.add(Participant.INHIBITOR_MI);
        preyMi.add(Participant.ELECTRON_DONOR_ROLE_MI);
        preyMi.add(Participant.FLUORESCENCE_DONOR_ROLE_MI);
        preyMi.add(Participant.ENZYME_ROLE_MI);
    }

    protected StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    protected Writer getWriter() {
        return writer;
    }

    protected Map<String, String> getCvTermUrls() {
        return cvTermUrls;
    }

    protected Set<String> getTargetMi() {
        return targetMi;
    }

    protected Set<String> getEnzymeMi() {
        return enzymeMi;
    }

    protected Set<String> getBaitMi() {
        return baitMi;
    }

    protected Set<String> getPreyMi() {
        return preyMi;
    }

    protected Map<Integer, Sentence> getSentenceMap() {
        return sentenceMap;
    }

    protected void initialiseWriter(Writer writer) {
        if (writer == null){
            throw new IllegalArgumentException("The writer cannot be null");
        }
        this.writer = writer;
    }

    protected void initialiseOutputStream(OutputStream stream) {
        if (stream == null){
            throw new IllegalArgumentException("The outputStream cannot be null");
        }
        this.writer = new OutputStreamWriter(stream);
    }

    protected void initialiseFile(File file) throws IOException {
        if (file == null){
            throw new IllegalArgumentException("The file cannot be null");
        }
        this.writer = new FileWriter(file);
    }
}
