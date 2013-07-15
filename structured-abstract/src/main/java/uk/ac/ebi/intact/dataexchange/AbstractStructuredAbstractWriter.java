package uk.ac.ebi.intact.dataexchange;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.*;

import java.io.*;
import java.util.*;

/**
 * Abstract writer for structured abstract
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>15/07/13</pre>
 */

public class AbstractStructuredAbstractWriter {
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
     * The stringBuilder used to build strings
     */
    private StringBuilder stringBuilder;

    /**
     * Interactor acs to write
     */
    private TreeSet<String> interactorAcs;

    private Map<String, String> cvTermUrls;

    public AbstractStructuredAbstractWriter(Writer writer) {
        if (writer == null){
            throw new IllegalArgumentException("The writer cannot be null");
        }
        initialiseBaits();
        initialiseEnzymes();
        initialisePreys();
        initialiseTargets();
        sentenceMap = new HashMap<Integer, Sentence>();
        this.writer = writer;
        this.sentencePropertiesMap = new HashMap<String, SentenceProperty>();
        this.stringBuilder = new StringBuilder(1024);
        this.interactorAcs = new TreeSet<String>();
        this.cvTermUrls = new HashMap<String, String>();
    }

    public AbstractStructuredAbstractWriter(OutputStream stream) {
        if (stream == null){
            throw new IllegalArgumentException("The outputStream cannot be null");
        }
        initialiseBaits();
        initialiseEnzymes();
        initialisePreys();
        initialiseTargets();
        sentenceMap = new HashMap<Integer, Sentence>();
        this.writer = new OutputStreamWriter(stream);
        this.sentencePropertiesMap = new HashMap<String, SentenceProperty>();
        this.stringBuilder = new StringBuilder(1024);
        this.interactorAcs = new TreeSet<String>();
        this.cvTermUrls = new HashMap<String, String>();
    }

    public AbstractStructuredAbstractWriter(File file) throws IOException {
        if (file == null){
            throw new IllegalArgumentException("The file cannot be null");
        }
        initialiseBaits();
        initialiseEnzymes();
        initialisePreys();
        initialiseTargets();
        sentenceMap = new HashMap<Integer, Sentence>();
        this.writer = new FileWriter(file);
        this.sentencePropertiesMap = new HashMap<String, SentenceProperty>();
        this.stringBuilder = new StringBuilder(1024);
        this.interactorAcs = new TreeSet<String>();
        this.cvTermUrls = new HashMap<String, String>();
    }

    public void close() throws IOException {
        clear();
        this.writer.close();
    }

    public void clear() throws IOException {
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

    public void writeStructuredAbstract(Publication publication) throws IOException {
        if (publication == null){
            throw new IllegalArgumentException("The publication cannot be null");
        }

        // clear
        clear();

        //get all experiments
        Collection<Experiment> experiments = publication.getExperiments();
        for (Experiment exp : experiments) {
            // read and collect abstract for each interaction
            for (Interaction in : exp.getInteractions()) {
                collectStructuredAbstractFrom(in);
            }
        }

        // write all collected sentences
        for (Sentence sentence : this.sentenceMap.values()) {
            writeSentence(sentence);
        }
    }

    protected void writeSentence(Sentence sentence) throws IOException {
        this.stringBuilder.setLength(0);

        // PROTEIN SUBJECT------------------------------------------
        writeInteractorNames(sentence.getInteractorsSubject());

        // add proteins Subject if there is only this participant
        if ((sentence.getInteractorsObject().size() == 0) && (sentence.getInteractorsSubject().size() == 1)) {
            String subject = this.stringBuilder.toString();
            this.stringBuilder.append(" and ");
            this.stringBuilder.append(subject);
            this.stringBuilder.append(" ");
        }

        this.writer.write(this.stringBuilder.toString());
        this.stringBuilder.setLength(0);

        // load sentence properties if not done yet
        if (this.sentencePropertiesMap.isEmpty()){
           loadSentenceProperties();
        }

        // write interaction type
        writeInteractionType(sentence);

        // PROTEIN OBJECT------------------------------------------
        writeInteractorNames(sentence.getInteractorsSubject());

        this.writer.write(this.stringBuilder.toString());
        this.writer.write(" by ");

        // write interaction detection method
        writeMIOutput(sentence.getDetMethod().getIdentifier(), sentence.getDetMethod().getFullName());

        int count = 1;// counter for control number of mintAc present in list
        // mintAcs linking as view interaction
        if (sentence.getInteractionAcs().size() == 1) {
            interaction_link += getMintAcOutput(2, mintAcs.get(0), 0);

        } else {
            interaction_link += "View Interaction: ";
            for (String mintAc : mintAcs) {

                // // COMMA insert for multiple MINTACs
                if (count >= 2)
                    interaction_link += ", ";
                interaction_link += getMintAcOutput(2, mintAc, count);
                count++;
            }

        }

        sentence += " (" + interaction_link + ")";
        return sentence;
    }

    private void writeInteractionType(Sentence sentence) throws IOException {
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
            writeMIOutput(sentence.getInteractionTypeMI(), "");
            // add conjunction only if there are object proteins
            if (sentence.getInteractorsObject().size() > 0) {
                this.writer.write("  ");
            }
        }
    }

    protected void writeMIOutput(String MIcode, String verb) throws IOException {
        this.writer.write("<a href=\"http://www.ebi.ac.uk/ontology-lookup/?termId=");
        this.writer.write(MIcode);
        this.writer.write(" \" style=\"text-decoration:none; \">");
        this.writer.write(verb);
        this.writer.write("</a>");
    }

    protected void writeInteractorNames(List<SimpleInteractor> interactors) {
        int countSubj = 1;
        int subjectSize = interactors.size();
        for (SimpleInteractor component : interactors) {

            if (countSubj > 1) {
                if (countSubj == subjectSize)// last protein
                {
                    this.stringBuilder.append(" and ");
                } else {
                    this.stringBuilder.append(", ");
                }
            }
            countSubj++;

            String interactorName = "";
            if (component.getAuthorAssignedName() != null) {
                interactorName = component.getAuthorAssignedName();
            } else {
                interactorName = component.getShortName();
            }

            writeXrefOutput(component.getXref(), interactorName);
        }
    }

    protected void writeXrefOutput(Xref xref, String proteinName) {
        this.stringBuilder.append("<a href=\"");
        this.stringBuilder.append(XrefLinkUtils.getPrimaryIdLink(xref, this.cvTermUrls));
        this.stringBuilder.append(" \" style=\"text-decoration:none; \"><b>");
        this.stringBuilder.append(proteinName);
        this.stringBuilder.append("</b></a>");
    }

    protected void collectStructuredAbstractFrom(Interaction interaction){

        // build a key
        buildInteractionkey(interaction);

        Sentence sentence = null;
        int key = this.stringBuilder.toString().hashCode();

        // 1. in HashMap
        if (this.sentenceMap.containsKey(key)) {
            sentence = this.sentenceMap.get(key);
        }
        // 2. else create it and put it in HashMap
        else {
            sentence = new Sentence(interaction.getCvInteractionType(), interaction.getExperiments().iterator().next().getCvInteraction());

            if (interaction.getExperiments().size() > 1) {
                log.warn("more than one experiment associated to interaction "+ interaction.getAc());}

            for (Component component : interaction.getComponents()) {

                if (targetMi.contains(component.getCvBiologicalRole()
                        .getIdentifier())) {
                    sentence.addInteractorObject(component);
                } else if (enzymeMi.contains(component
                        .getCvBiologicalRole().getIdentifier())) {
                    sentence.addInteractorSubject(component);
                }
                else if (false == component.getExperimentalRoles().isEmpty() && preyMi.contains(component.getExperimentalRoles().iterator().next()
                        .getIdentifier())) {
                    sentence.addInteractorObject(component);
                } else if (false == component.getExperimentalRoles().isEmpty() && baitMi.contains(component.getExperimentalRoles().iterator().next()
                        .getIdentifier())) {
                    sentence.addInteractorSubject(component);
                } else
                {
                    sentence.addInteractorSubject(component);
                }

            }
            this.sentenceMap.put(key, sentence);
        }
        // add interactionAc to sentence
        sentence.addInteractionAc(interaction.getAc());
    }

    protected void buildInteractionkey(Interaction interaction) {
        this.stringBuilder.setLength(0);
        this.interactorAcs.clear();

        // read ordered set of interactor acs
        for (Component component : interaction.getComponents()) {
            interactorAcs.add(component.getInteractor().getAc());
        }

        for (String prL : interactorAcs) {
            stringBuilder.append("    ");
            stringBuilder.append(prL);
        }

        // interaction type
        stringBuilder.append(interaction.getCvInteractionType().getIdentifier());

        // experiment
        if (!interaction.getExperiments().isEmpty()) {
            stringBuilder.append("    ");
            stringBuilder.append(interaction.getExperiments().iterator().next().getCvInteraction().getIdentifier());
        }
    }

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
        targetMi.add(CvBiologicalRole.ENZYME_TARGET_PSI_REF);
        targetMi.add(CvBiologicalRole.ELECTRON_ACCEPTOR_MI_REF);
        targetMi.add(CvBiologicalRole.FLUROPHORE_ACCEPTOR_MI_REF);
    }
    protected void initialiseEnzymes() {
        enzymeMi = new HashSet<String>(3);
        enzymeMi.add(CvBiologicalRole.ENZYME_PSI_REF);
        enzymeMi.add(CvBiologicalRole.ELECTRON_DONOR_MI_REF);
        enzymeMi.add(CvBiologicalRole.FLUROPHORE_DONOR_MI_REF);
    }

    protected void initialiseBaits() {
        baitMi = new HashSet<String>(5);
        baitMi.add(CvExperimentalRole.BAIT_PSI_REF);
        baitMi.add(CvExperimentalRole.INHIBITED_PSI_REF);
        baitMi.add(CvExperimentalRole.ELECTRON_ACCEPTOR_MI_REF);
        baitMi.add(CvExperimentalRole.FLUROPHORE_ACCEPTOR_MI_REF);
        baitMi.add(CvExperimentalRole.ENZYME_TARGET_PSI_REF);
    }

    protected void initialisePreys() {
        preyMi = new HashSet<String>(5);
        preyMi.add(CvExperimentalRole.PREY_PSI_REF);
        preyMi.add(CvExperimentalRole.INHIBITOR_PSI_REF);
        preyMi.add(CvExperimentalRole.ELECTRON_DONOR_MI_REF);
        preyMi.add(CvExperimentalRole.FLUROPHORE_DONOR_MI_REF);
        preyMi.add(CvExperimentalRole.ENZYME_PSI_REF);
    }
}
