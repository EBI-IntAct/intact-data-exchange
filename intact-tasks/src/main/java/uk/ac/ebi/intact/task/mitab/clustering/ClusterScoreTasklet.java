package uk.ac.ebi.intact.task.mitab.clustering;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.repeat.RepeatStatus;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.Encore2Binary;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteractionForScoring;
import uk.ac.ebi.enfin.mi.cluster.score.InteractionClusterScore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Tasklet to cluster and score mitab file
 *
 * @author Rafael Jimenez (rafael@ebi.ac.uk)
 * @version $Id$
 * @since TODO add POM version
 */
public class ClusterScoreTasklet implements Tasklet {
    private String mitabInputFileName;
    private File mitabInputFile;
    private String mitabOutputFileName;
    private File mitabOutputFile;
    private boolean header = true;

    public ClusterScoreTasklet(String mitabInputFolderName, String mitabOutputFolderName) {
        this.mitabInputFileName = mitabInputFolderName;
        this.mitabOutputFileName = mitabOutputFolderName;
    }

    public String getMitabInputFileName() {
        return mitabInputFileName;
    }

    public String getMitabOutputFileName() {
        return mitabOutputFileName;
    }

    public void setMitabInputFileName(String mitabInputFileName) {
        this.mitabInputFileName = mitabInputFileName;
    }

    public void setMitabOutputFileName(String mitabOutputFileName) {
        this.mitabOutputFileName = mitabOutputFileName;
    }

    private void checkInputMitabFile() {
        if (mitabInputFileName == null){
            throw new ItemStreamException("A mitab parent file is needed");
        }

        mitabInputFile = new File (mitabInputFileName);

        if ( !mitabInputFile.exists() ) {
            throw new ItemStreamException( "The mitab file : " + mitabInputFile.getAbsolutePath() + " does not exist and is necessary for this task." );
        }
        else if (!mitabInputFile.canRead()){
            throw new ItemStreamException( "Impossible to read file : " + mitabInputFileName);
        }
    }

    private void checkOutputMitabFile() {
        if (mitabOutputFileName == null){
            throw new ItemStreamException("A mitab output file name is needed");
        }

        mitabOutputFile = new File(mitabOutputFileName);

        if (mitabOutputFile.exists() && !mitabOutputFile.canWrite()) throw new IllegalArgumentException("Cannot write to file: "+mitabOutputFileName);

        if (!mitabOutputFile.getParentFile().exists()){
            mitabOutputFile.getParentFile().mkdir();
        }
    }

    /**
     * Save clustered results including scores in mitab files
     * @param interactionClusterScore
     */
    private void saveMitabOutputFile(InteractionClusterScore interactionClusterScore) throws IOException {
        /* Retrieve results */
        Map<Integer, EncoreInteractionForScoring> interactionMapping = interactionClusterScore.getInteractionMapping();

        PsimiTabWriter writer = new PsimiTabWriter(header);
        Encore2Binary iConverter = new Encore2Binary(interactionClusterScore.getMappingIdDbNames());

        for(Integer mappingId:interactionMapping.keySet()){
            EncoreInteractionForScoring eI = interactionMapping.get(mappingId);
            BinaryInteraction bI = iConverter.getBinaryInteraction(eI);
            writer.writeOrAppend(bI, mitabOutputFile, false);
        }
    }


    /**
     * Execute takslet step
     * @param arg0
     * @param arg1
     * @return
     * @throws Exception
     */
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        // do some checking there
        this.checkOutputMitabFile();
        this.checkInputMitabFile();

        /* Get mitab file */
        PsimiTabReader mitabReader = new PsimiTabReader(header);
        InteractionClusterScore interactionClusterScore = new InteractionClusterScore();

        /* Get binaryInteractions from mitab file */
        List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
        binaryInteractions.addAll(mitabReader.read(mitabInputFile));
        /* Run cluster using list of binary interactions as input */
        interactionClusterScore.setBinaryInteractionIterator(binaryInteractions.iterator());
        interactionClusterScore.setMappingIdDbNames("uniprotkb,irefindex,ddbj/embl/genbank,refseq,chebi");
        interactionClusterScore.runService();

        /* Save mitab clustered data in files */
        saveMitabOutputFile(interactionClusterScore);

        /* task finsihed */
        return RepeatStatus.FINISHED;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }
}
