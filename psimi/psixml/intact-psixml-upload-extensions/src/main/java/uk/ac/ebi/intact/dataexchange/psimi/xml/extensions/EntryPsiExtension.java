/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.extensions;

import psidev.psi.mi.xml.model.Entry;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.Phase;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension.ExtensionContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension.annotation.PsiExtension;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension.annotation.PsiExtensionContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.extension.annotation.PsiExtensionMethod;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.validator.ValidationMessage;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@PsiExtension(forClass = Entry.class)
public class EntryPsiExtension {

    @PsiExtensionContext
    ExtensionContext extensionContext;

    @PsiExtensionMethod(phase = Phase.BEFORE_VALIDATION)
    public void printEntryPreValidation() {
        Entry entry = (Entry) extensionContext.getElement();

        System.out.println("Validating entry with " + entry.getExperiments().size() + " experiments");
    }

    @PsiExtensionMethod(phase = Phase.AFTER_VALIDATION)
    public void printEntryPostValidation() {
        boolean valid = extensionContext.getProcessReport().getValidationReport().isValid();
        System.out.println("\tThis entry is: " + (valid ? "valid" : "invalid"));

        for (ValidationMessage msg : extensionContext.getProcessReport().getValidationReport().getMessages()) {
            System.out.println("\t\t" + msg);
        }
    }

    @PsiExtensionMethod
    public void printEntryProcessInfo() {
        Entry entry = (Entry) extensionContext.getElement();

        System.out.println("Processing entry with " + entry.getExperiments().size() + " experiments");
    }

    @PsiExtensionMethod
    public void executeMyTestExtension() {
        Entry entry = (Entry) extensionContext.getElement();

        System.out.println("\tThis entry contains interactions: " + entry.getInteractions().size() +
                           " - is valid: " + extensionContext.getProcessReport().getValidationReport().isValid());
    }
}