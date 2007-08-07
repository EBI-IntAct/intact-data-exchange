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
package uk.ac.ebi.intact.dataexchange.imex.repository.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Entity
@NamedQuery(name = "repoEntryByName", query="select re from RepoEntrySet re where re.name = :name")
public class RepoEntry extends RepoEntity {

    @ManyToOne
    private RepoEntrySet repoEntrySet;

    private String name;

    private boolean enriched;

    public RepoEntry() {
    }

    /////////////////////////////
    // Getters and Setters

    public RepoEntrySet getRepoEntrySet() {
        return repoEntrySet;
    }

    public void setRepoEntrySet(RepoEntrySet repoEntrySet) {
        this.repoEntrySet = repoEntrySet;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isEnriched()
    {
        return enriched;
    }

    public void setEnriched(boolean enriched)
    {
        this.enriched = enriched;
    }
}