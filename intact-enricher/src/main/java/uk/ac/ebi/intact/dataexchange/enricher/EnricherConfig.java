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
package uk.ac.ebi.intact.dataexchange.enricher;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class EnricherConfig {

    private boolean updateOrganisms = true;
    private boolean updateProteins = true;
    private String oboUrl = "http://intact.svn.sourceforge.net/viewvc/*checkout*/intact/repo/utils/data/controlledVocab/psi-mi25-4intact.obo";

    public EnricherConfig() {

    }

    public boolean isUpdateOrganisms() {
        return updateOrganisms;
    }

    public void setUpdateOrganisms(boolean updateOrganisms) {
        this.updateOrganisms = updateOrganisms;
    }

    public boolean isUpdateProteins() {
        return updateProteins;
    }

    public void setUpdateProteins(boolean updateProteins) {
        this.updateProteins = updateProteins;
    }

    public String getOboUrl() {
        return oboUrl;
    }

    public void setOboUrl(String oboUrl) {
        this.oboUrl = oboUrl;
    }
}