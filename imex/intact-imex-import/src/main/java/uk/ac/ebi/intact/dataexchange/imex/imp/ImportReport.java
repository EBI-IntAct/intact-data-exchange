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
package uk.ac.ebi.intact.dataexchange.imex.imp;

import uk.ac.ebi.intact.model.meta.ImexObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Summarises an import
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImportReport {

    private List<String> pubmedsNotFoundInRepo;
    private List<ImexObject> failedImexObjects;
    private List<ImexObject> successfullImexObjects;

    public ImportReport() {
        this.pubmedsNotFoundInRepo = new ArrayList<String>();
        this.failedImexObjects = new ArrayList<ImexObject>();
        this.successfullImexObjects = new ArrayList<ImexObject>();
    }

    public List<ImexObject> getFailedImexObjects() {
        return failedImexObjects;
    }

    public List<String> getPubmedsNotFoundInRepo() {
        return pubmedsNotFoundInRepo;
    }

    public List<ImexObject> getSuccessfullImexObjects() {
        return successfullImexObjects;
    }
}