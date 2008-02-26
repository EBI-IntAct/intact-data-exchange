/*
 * Copyright 2001-2008 The European Bioinformatics Institute.
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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores some information about the conversion
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ConverterReport {

    private List<ConverterMessage> messages;

    protected ConverterReport() {
        this.messages = new ArrayList<ConverterMessage>();
    }

    public List<ConverterMessage> getMessages() {
        return messages;
    }

    public void addMessage(ConverterMessage message) {
        this.messages.add(message);
    }

    public List<ConverterMessage> getMessagesByLevel(MessageLevel level) {
        List<ConverterMessage> messagesByLevel = new ArrayList<ConverterMessage>(messages.size());

        for (ConverterMessage message : messages) {
            if (level == message.getLevel()) {
                messagesByLevel.add(message);
            }
        }

        return messagesByLevel;
    }
}