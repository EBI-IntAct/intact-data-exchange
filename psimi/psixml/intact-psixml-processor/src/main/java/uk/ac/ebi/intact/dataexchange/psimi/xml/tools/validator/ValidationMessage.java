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
package uk.ac.ebi.intact.dataexchange.psimi.xml.tools.validator;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id:ValidationMessage.java 8272 2007-04-25 10:20:12Z baranda $
 */
public class ValidationMessage {

    private String message;
    private MessageType type;
    private Class elementClass;

    public ValidationMessage(Class elementClass, String message, MessageType type) {
        this.elementClass = elementClass;
        this.message = message;
        this.type = type;
    }

    public Class getElementClass() {
        return elementClass;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type).append("] ");
        sb.append(message);

        return sb.toString();
    }
}