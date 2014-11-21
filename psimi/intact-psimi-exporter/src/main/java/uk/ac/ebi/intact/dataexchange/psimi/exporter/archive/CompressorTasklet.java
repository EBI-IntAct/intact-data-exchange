/**
 * Copyright 2011 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.util.List;

/**
 * TODO comment this class header.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CompressorTasklet implements Tasklet {

    private Compressor compressor;

    private File outputFile;
    private List<File> filesToCompress;
    private boolean deleteSourceFiles;

    public CompressorTasklet() {
        deleteSourceFiles = true;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        if (compressor == null) {
            compressor = new Compressor();
        }

        if (outputFile == null) {
            throw new IllegalArgumentException("An output file is needed");
        }

        if (filesToCompress == null) {
            throw new IllegalArgumentException("An list of files to compress is needed");
        }

        compressor.compress(outputFile, filesToCompress, deleteSourceFiles);

        return RepeatStatus.FINISHED;
    }

    public Compressor getCompressor() {
        return compressor;
    }

    public void setCompressor(Compressor compressor) {
        this.compressor = compressor;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public List<File> getFilesToCompress() {
        return filesToCompress;
    }

    public void setFilesToCompress(List<File> filesToCompress) {
        this.filesToCompress = filesToCompress;
    }

    public boolean isDeleteSourceFiles() {
        return deleteSourceFiles;
    }

    public void setDeleteSourceFiles(boolean deleteSourceFiles) {
        this.deleteSourceFiles = deleteSourceFiles;
    }
}
