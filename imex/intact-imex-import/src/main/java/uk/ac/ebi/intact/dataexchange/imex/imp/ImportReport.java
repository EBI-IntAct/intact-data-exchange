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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summarises an import
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImportReport {

    private List<String> pmidsNotFoundInRepo;
    private Map<String,Throwable> failedPmids;
    private List<String> sucessfullPmids;
    private List<String> newPmisImported;

    public ImportReport() {
        this.pmidsNotFoundInRepo = new ArrayList<String>();
        this.failedPmids = new HashMap<String,Throwable>();
        this.sucessfullPmids = new ArrayList<String>();
        this.newPmisImported = new ArrayList<String>();
    }

    public Map<String,Throwable> getFailedPmids() {
        return failedPmids;
    }

    public List<String> getPmidsNotFoundInRepo() {
        return pmidsNotFoundInRepo;
    }

    public List<String> getSucessfullPmids() {
        return sucessfullPmids;
    }

    public List<String> getNewPmisImported() {
        return newPmisImported;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Import Report:\n");
        sb.append("\tPmids successfully imported (").append(sucessfullPmids.size()).append("): ").append(sucessfullPmids).append("\n");
        sb.append("\t\tNew Pmids (").append(newPmisImported.size()).append("): ").append(newPmisImported).append("\n");
        sb.append("\tPmids failed (").append(failedPmids.size()).append("): ").append(failedPmids.keySet()).append("\n");
        sb.append("\tPmids not found in repo (").append(pmidsNotFoundInRepo.size()).append("): ").append(pmidsNotFoundInRepo);

        return sb.toString();
    }
}