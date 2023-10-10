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
package uk.ac.ebi.intact.task.mitab;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.core.io.FileSystemResource;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.File;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MitabItemWriter extends FlatFileItemWriter<BinaryInteraction> implements BinaryInteractionItemWriter, ItemStream{

    private PsimiTabVersion mitabVersion = PsimiTabVersion.v2_7;
    private static final String RESTART_DATA_NAME = "current.count";
    private String fileName;

    @Override
    public void afterPropertiesSet() throws Exception {
        // important to override this method or the writer will complain
        setLineAggregator(new MitabLineAggregator(mitabVersion, false));
        super.afterPropertiesSet();

        setTransactional(false);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

        if (executionContext.containsKey(RESTART_DATA_NAME)) {
            setLineAggregator(new MitabLineAggregator(mitabVersion, true));
        }
        else {
            setLineAggregator(new MitabLineAggregator(mitabVersion, false));
        }

        super.open(executionContext);
    }

    public PsimiTabVersion getMitabVersion() {
        return mitabVersion;
    }

    public void setMitabVersion(PsimiTabVersion mitabVersion) {
        this.mitabVersion = mitabVersion;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;

        File file = new File(fileName);
        String parent = file.getParent();
        File parentFile = new File(parent);

        if (parentFile == null){
            parentFile.mkdirs();
        }

        setResource(new FileSystemResource(file));
    }
}
    