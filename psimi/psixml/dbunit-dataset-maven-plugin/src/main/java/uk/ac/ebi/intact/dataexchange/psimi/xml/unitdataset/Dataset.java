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
package uk.ac.ebi.intact.dataexchange.psimi.xml.unitdataset;

import java.io.File;

/**
 * Represents a dataset
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Dataset {

    /**
     * Id of the dataset
     *
     * @parameter
     * @required
     */
    private String id;

    /**
     * Format of the dataset
     *
     * @parameter default-value="xml"
     */
    private String type = "xml";

    /**
     * Whether the dataset will contain all the available CVs (from the OBO file)
     * @parameter default-value="false"
     */
    private boolean containsAllCVs = false;

    /**
     * PSI MI 2.5 Xml files to import
     */
    private File[] files;

    public Dataset() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public File[] getFiles() {
        return files;
    }

    public void setFiles(File[] files) {
        this.files = files;
    }

    public boolean isContainsAllCVs()
    {
        return containsAllCVs;
    }

    public void setContainsAllCVs(boolean containsAllCVs)
    {
        this.containsAllCVs = containsAllCVs;
    }

    public String toString()
    {
        return id;
    }
}