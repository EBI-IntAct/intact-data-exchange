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
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvConfiguration {

    /**
     * OBO File with CVs
     *
     * @parameter
     * @required
     */
    private File oboFile;

    /**
     * Additional CVs in CSV format
     *
     * @parameter
     * @required
     */
    private File additionalFile;

    /**
     * File with additional annotations
     *
     * @parameter
     * @required
     */
    private File additionalAnnotationsFile;

    public CvConfiguration() {
    }

    public File getAdditionalAnnotationsFile() {
        return additionalAnnotationsFile;
    }

    public void setAdditionalAnnotationsFile(File additionalAnnotationsFile) {
        this.additionalAnnotationsFile = additionalAnnotationsFile;
    }

    public File getAdditionalFile() {
        return additionalFile;
    }

    public void setAdditionalFile(File additionalFile) {
        this.additionalFile = additionalFile;
    }

    public File getOboFile() {
        return oboFile;
    }

    public void setOboFile(File oboFile) {
        this.oboFile = oboFile;
    }
}