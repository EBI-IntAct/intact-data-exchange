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

import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location.LocationItem;

import java.io.Serializable;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ConverterMessage implements Serializable {

    private MessageLevel level;

    private String text;

    private LocationItem location;

    public ConverterMessage() {
    }

    public ConverterMessage(MessageLevel level, String text, LocationItem location) {
        this.level = level;
        this.text = text;
        this.location = location;
    }

    public MessageLevel getLevel() {
        return level;
    }

    public void setLevel(MessageLevel level) {
        this.level = level;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocationItem getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "["+level+"] "+text+" [ Location: "+location.pathFromRootAsString()+" ]";
    }
}