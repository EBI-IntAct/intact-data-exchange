package uk.ac.ebi.intact.task.mitab.clustering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.xml.converter.ConverterException;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This processor retrieves cluster score for a binary interactions.
 *
 * It needs a MITAB file containing unique identifiers for both first and second interactors and a valid numerical score
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/02/12</pre>
 */

public class MitabClusterScoreItemProcessor implements ItemProcessor<BinaryInteraction, BinaryInteraction>, ItemStream {

    private static final Log log = LogFactory.getLog(MitabClusterScoreItemProcessor.class);

    private File clusteredMitabFile;
    private Map<BinaryPair, Double> scores;
    private String miScoreLabel;
    private boolean hasHeader = false;
    
    private String[] databasesForUniqIdentifier;

    public MitabClusterScoreItemProcessor(){
        scores = new TreeMap<BinaryPair, Double>();
    }

    @Override
    public BinaryInteraction process(BinaryInteraction item) throws Exception {
        
        if (item == null){
            return null; 
        }
        
        Interactor interactorA = item.getInteractorA();
        Interactor interactorB = item.getInteractorB();
        
        String firstInteractor = extractInteractorIdentifier(interactorA);
        String secondInteractor = extractInteractorIdentifier(interactorB);
        
        BinaryPair currentPair = new BinaryPair(firstInteractor, secondInteractor);
        
        if (scores.containsKey(currentPair)){
            Double score = scores.get(currentPair);

            Confidence conf = new ConfidenceImpl(miScoreLabel, Double.toString(score));
            item.getConfidenceValues().add(conf);
        }
        
        return item;
    }

    public File getClusteredMitabFile() {
        return clusteredMitabFile;
    }

    public void setClusteredMitabFile(File clusteredMitabFile) {
        this.clusteredMitabFile = clusteredMitabFile;
    }

    public String getMiScoreLabel() {
        return miScoreLabel;
    }

    public void setMiScoreLabel(String miScoreLabel) {
        this.miScoreLabel = miScoreLabel;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    /**
     *
     * @param interactor
     * @return the first identifier of an interactor, null otherwise
     */
    private String extractInteractorIdentifier(Interactor interactor){

        if (interactor == null){
            return null;
        }

        if (!interactor.getIdentifiers().isEmpty() && databasesForUniqIdentifier == null){
            if (interactor.getIdentifiers().size() > 1){
                log.warn("Interactor with more than one identifiers : " + interactor.toString() + ". Only the first identifier is taken into account");
            }

            return interactor.getIdentifiers().iterator().next().getIdentifier();
        }
        else if (!interactor.getIdentifiers().isEmpty()){
            
            for (String db : databasesForUniqIdentifier){

                for (CrossReference ref : interactor.getIdentifiers()){
                    if (ref.getDatabase() != null && ref.getDatabase().equalsIgnoreCase(db)){
                        return ref.getIdentifier();
                    }
                }
            }

            log.warn("The identifiers of interactor " + interactor.toString() + " are not recognized so only the first identifier will be taken into account");

            return interactor.getIdentifiers().iterator().next().getIdentifier();
        }
        else {
            log.warn("Interactor without identifiers : " + interactor.toString() + ". This interactor will be ignored");
        }

        return null;
    }

    private Double extractClusterScoreFrom(List<Confidence> confidences){

        for (Confidence conf : confidences){
            if (conf.getType() != null && conf.getType().equalsIgnoreCase(miScoreLabel)){
                try {
                    Double score = Double.valueOf(conf.getValue());

                    return score;
                }
                catch (NumberFormatException e){
                    log.error("The mi score " + conf.getValue() + " is not a valid mi score and will be ignored.");
                }
            }
        }

        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

        if (clusteredMitabFile == null){
            throw new ItemStreamException("The Mitab cluster score item processor needs a clustered mitab file containing the scores to be able to process the non clustered MITAB file");
        }

        PsimiTabReader mitabReader = new PsimiTabReader(hasHeader);

        try {
            Iterator<BinaryInteraction> binaryInteractionIterator = mitabReader.iterate(clusteredMitabFile);

            while (binaryInteractionIterator.hasNext()){
                BinaryInteraction binary = binaryInteractionIterator.next();

                Interactor interactorA = binary.getInteractorA();
                Interactor interactorB = binary.getInteractorB();
                List<Confidence> confidences = binary.getConfidenceValues();

                String firstInteractor = extractInteractorIdentifier(interactorA);
                String secondInteractor = extractInteractorIdentifier(interactorB);
                Double score = extractClusterScoreFrom(confidences);

                if (score != null){
                    scores.put(new BinaryPair(firstInteractor, secondInteractor), score);
                }
            }

        } catch (IOException e) {
            throw new ItemStreamException("Impossible to read the clustered mitab file " + clusteredMitabFile.getAbsolutePath(), e);
        } catch (ConverterException e) {
            throw new ItemStreamException("Conversion problem while reading clustered mitab file " + clusteredMitabFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // Nothing to update
    }

    @Override
    public void close() throws ItemStreamException {

        scores.clear();
    }

    public String[] getDatabasesForUniqIdentifier() {
        
        return databasesForUniqIdentifier;
    }

    public void setDatabasesForUniqIdentifier(String[] databasesForUniqIdentifier) {
        this.databasesForUniqIdentifier = databasesForUniqIdentifier;
    }
}
