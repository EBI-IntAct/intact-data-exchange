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

import org.springframework.batch.item.file.LineMapper;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.DocumentDefinition;
import psidev.psi.mi.tab.model.builder.MitabDocumentDefinition;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class MitabLineMapper implements LineMapper<BinaryInteraction>{

    private DocumentDefinition documentDefinition;

    public MitabLineMapper() {
    }

    public BinaryInteraction mapLine(String line, int lineNumber) throws Exception {
        if (documentDefinition == null) {
            documentDefinition = new MitabDocumentDefinition();
        }
        
        try {
            return documentDefinition.interactionFromString(line);
        } catch (Exception e) {
            throw new Exception("Problem converting to binary interaction line "+lineNumber+": "+line);
        }
    }

    public void setDocumentDefinition(DocumentDefinition documentDefinition) {
        this.documentDefinition = documentDefinition;
    }
}
