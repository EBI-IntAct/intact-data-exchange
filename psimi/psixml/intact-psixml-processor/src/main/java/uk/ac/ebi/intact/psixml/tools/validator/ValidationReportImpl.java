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
package uk.ac.ebi.intact.psixml.tools.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:ValidationReportImpl.java 8272 2007-04-25 10:20:12Z baranda $
 */
public class ValidationReportImpl implements ValidationReport {

    private boolean valid;
    private Collection<ValidationMessage> messages;

    public ValidationReportImpl() {
        valid = true;
        this.messages = new ArrayList<ValidationMessage>();
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setMessages(Collection<ValidationMessage> messages) {
        this.messages = messages;
    }

    public Collection<ValidationMessage> getMessages() {
        return messages;
    }

    public Collection<ValidationMessage> getMessages(MessageType type) {
        List<ValidationMessage> subList = new ArrayList<ValidationMessage>();

        for (ValidationMessage message : messages) {
            if (message.getType() == type) {
                subList.add(message);
            }
        }

        return messages;
    }

    public void addMessage(ValidationMessage message) {
        if (messages == null) {
            messages = new ArrayList<ValidationMessage>();
        }

        if (message.getType() == MessageType.ERROR) {
            setValid(false);
        }

        messages.add(message);
    }

    /**
     * Merge with an existing validation report, so all the messages are in one report
     */
    public void mergeWith(ValidationReport report) {
        if (!report.isValid()) {
            setValid(false);
        }

        for (ValidationMessage message : report.getMessages()) {
            addMessage(message);
        }
    }
}