package uk.ac.ebi.intact.task.mitab.clustering;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Tasklet to cluster and score mitab files
 *
 * @author Rafael Jimenez (rafael@ebi.ac.uk)
 * @version $Id$
 * @since TODO add POM version
 */
public class ClusterScoreTasklet implements Tasklet {
    private String mitabInputFolderName;
    private File mitabInputFolder;
    private String mitabOutputFolderName;
    private File mitabOutputFolder;
    private int fileLineLimit = 500;

    public ClusterScoreTasklet(String mitabInputFolderName, String mitabOutputFolderName) {
        this.mitabInputFolderName = mitabInputFolderName;
        this.mitabOutputFolderName = mitabOutputFolderName;
        this.setMitabOutputFolder();
        this.setMitabInputFolder();
    }

    public String getMitabInputFolderName() {
        return mitabInputFolderName;
    }

    public String getMitabOutputFolderName() {
        return mitabOutputFolderName;
    }

    public int getFileLineLimit() {
        return fileLineLimit;
    }

    public void setFileLineLimit(int fileLineLimit) {
        this.fileLineLimit = fileLineLimit;
    }

    public File getMitabInputFolder() {
        return mitabInputFolder;
    }

    private void setMitabInputFolder() {
        if (mitabInputFolderName == null){
            throw new ItemStreamException("A mitab parent folder is needed");
        }

        mitabInputFolder = new File (mitabInputFolderName);

        if ( !mitabInputFolder.exists() ) {
            throw new ItemStreamException( "The mitab parent folder : " + mitabInputFolder.getAbsolutePath() + " does not exist and is necessary for this task." );
        }
        else if (!mitabInputFolder.isDirectory()){
            throw new ItemStreamException( mitabInputFolderName + " is not a directory." );
        }
        else if (!mitabInputFolder.canRead()){
            throw new ItemStreamException( "Impossible to read files : " + mitabInputFolderName );
        }
    }

    public File getMitabOutputFolder() {
        return mitabOutputFolder;
    }

    private void setMitabOutputFolder() {
        if (mitabOutputFolderName == null){
            throw new ItemStreamException("A mitab parent folder is needed");
        }
        this.mitabOutputFolder = new File (mitabOutputFolderName);

        if ( !mitabOutputFolder.exists() ) {
            throw new ItemStreamException( "The mitab parent folder : " + mitabOutputFolder.getAbsolutePath() + " does not exist and is necessary for this task." );
        }
        else if (!mitabOutputFolder.isDirectory()){
            throw new ItemStreamException( mitabOutputFolderName + " is not a directory." );
        }
        else if (!mitabOutputFolder.canRead()){
            throw new ItemStreamException( "Impossible to read files : " + mitabOutputFolderName );
        }
    }

    /**
     * Get mitab files from a resource location
     * @return
     */
    private Collection<File> getListOfMitabFiles(){
        /* Get a list of files */
        Collection<File> files = FileUtils.listFiles(mitabInputFolder, new String[] {"txt"}, false);

        /* Make sure there are files in the provided location */
        if (files == null) {
            throw new UnexpectedJobExecutionException("Could not find mitab files in " + mitabInputFolderName);
        }
        return files;
    }

    /**
     * Save clustered results including scores in mitab files
     * @param interactionClusterScore
     */
    private void saveMitabOutputFiles(InteractionClusterScore interactionClusterScore){
        /* Retrieve results */
        Map<Integer, EncoreInteractionForScoring> interactionMapping = interactionClusterScore.getInteractionMapping();

        PsimiTabWriter writer = new PsimiTabWriter();
        Encore2Binary iConverter = new Encore2Binary(interactionClusterScore.getMappingIdDbNames());

        int fileCount=1;
        int lineCount=1;
        File file = new File(this.mitabOutputFolderName, "test-" + fileCount + ".txt");
        for(Integer mappingId:interactionMapping.keySet()){
            EncoreInteractionForScoring eI = interactionMapping.get(mappingId);
            BinaryInteraction bI = iConverter.getBinaryInteraction(eI);
            try {
                writer.writeOrAppend(bI, file, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(lineCount == fileLineLimit){
                lineCount = 0;
                fileCount++;
                file = new File(this.mitabOutputFolderName, "test-" + fileCount + ".txt");
                if(file.exists()){
                    file.delete();
                    file = new File(this.mitabOutputFolderName, "test-" + fileCount + ".txt");
                }
            }
            lineCount++;
        }
    }

    /**
     * Get clustered results including scores
     * @param binaryInteractions
     * @return
     */
    private InteractionClusterScore getInteractionClusterScore(List<BinaryInteraction> binaryInteractions) {
        InteractionClusterScore iC = new InteractionClusterScore();
        iC.setBinaryInteractionIterator(binaryInteractions.iterator());
        iC.setMappingIdDbNames("uniprotkb,irefindex,ddbj/embl/genbank,refseq,chebi");
        iC.runService();
        return iC;
    }


    /**
     * Execute takslet step
     * @param arg0
     * @param arg1
     * @return
     * @throws Exception
     */
    public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
        /* Get mitab files */
        Collection<File> files = getListOfMitabFiles();
        PsimiTabReader mitabReader = new PsimiTabReader(false);
        InteractionClusterScore interactionClusterScore = new InteractionClusterScore();
        for (File file : files) {
            /* Get binaryInteractions from mitab file */
            List<BinaryInteraction> binaryInteractions = new ArrayList<BinaryInteraction>();
            binaryInteractions.addAll(mitabReader.read(file));
            /* Run cluster using list of binary interactions as input */
            interactionClusterScore.setBinaryInteractionIterator(binaryInteractions.iterator());
            interactionClusterScore.setMappingIdDbNames("uniprotkb,irefindex,ddbj/embl/genbank,refseq,chebi");
            interactionClusterScore.runService();
        }
        /* Save mitab clustered data in files */
        saveMitabOutputFiles(interactionClusterScore);

        /* task finsihed */
        return RepeatStatus.FINISHED;
    }



}
