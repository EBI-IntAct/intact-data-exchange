/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.task.mitab.index;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import psidev.psi.mi.search.index.PsimiIndexWriter;
import psidev.psi.mi.search.index.impl.BinaryInteractionIndexWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.search.IntactPsimiTabIndexWriter;
import uk.ac.ebi.intact.task.mitab.BinaryInteractionItemWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LuceneItemWriter implements BinaryInteractionItemWriter, ItemStream {

    private File directory;

    private IndexWriter indexWriter;

    public void write(List<? extends BinaryInteraction> items) throws Exception {
        if (items.isEmpty()) {
            return;
        }

        PsimiIndexWriter psimiIndexWriter;

        BinaryInteraction first = items.iterator().next();

        if (first instanceof IntactBinaryInteraction) {
            psimiIndexWriter = new IntactPsimiTabIndexWriter();
        } else {
            psimiIndexWriter = new BinaryInteractionIndexWriter();
        }

        for (BinaryInteraction binaryInteraction : items) {
            psimiIndexWriter.addBinaryInteractionToIndex(indexWriter, binaryInteraction);
        }

        indexWriter.flush();
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public void open(ExecutionContext executionContext) throws ItemStreamException {
         if (directory == null) {
            throw new NullPointerException("No directory set");
        }
        try {
            Directory luceneDirectory = FSDirectory.getDirectory(directory);
            indexWriter = new IndexWriter(luceneDirectory, new StandardAnalyzer());
        } catch (Throwable e) {
            throw new ItemStreamException("Problem creating lucene index writer for directory: "+directory, e);
        }
    }

    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    public void close() throws ItemStreamException {
        if (indexWriter != null) {
            try {
                indexWriter.flush();
                indexWriter.optimize();
                indexWriter.close();
            } catch (IOException e) {
                throw new ItemStreamException("Problem optimizing and closing index writer");
            }
        }
    }
}
