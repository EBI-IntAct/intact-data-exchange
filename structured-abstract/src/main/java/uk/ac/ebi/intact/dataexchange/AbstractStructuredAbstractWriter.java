package uk.ac.ebi.intact.dataexchange;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.CvBiologicalRole;
import uk.ac.ebi.intact.model.CvExperimentalRole;

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

    public void close() throws IOException {
        this.sentenceMap.clear();
        this.sentencePropertiesMap.clear();
        this.writer.close();
    }

    public void clear() throws IOException {
        this.sentenceMap.clear();
        this.sentencePropertiesMap.clear();
    }

    public String getSentencesPropertiesPath() {
        return sentencesPropertiesPath;
    }

    public void setSentencesPropertiesPath(String sentencesPropertiesPath) {
        this.sentencesPropertiesPath = sentencesPropertiesPath;
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
}
