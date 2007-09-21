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

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Entity
@NamedQueries (value = {
    @NamedQuery(name = "repoEntrySetByName", query="select es from RepoEntrySet es where es.name = :name")
    })
public class RepoEntrySet extends RepoEntity {

    private String name;

    @ManyToOne 
    private Provider provider;

    @OneToMany (mappedBy = "repoEntrySet", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<RepoEntry> repoEntries;

    public RepoEntrySet() {
    }

    public RepoEntrySet(Provider provider, String name) {
        this.provider = provider;
        this.name = name;
    }

    /////////////////////////
    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RepoEntry> getRepoEntries() {
        if (repoEntries == null) {
            repoEntries = new ArrayList<RepoEntry>();
        }
        return repoEntries;
    }

    public void setRepoEntries(List<RepoEntry> repoEntries) {
        this.repoEntries = repoEntries;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}