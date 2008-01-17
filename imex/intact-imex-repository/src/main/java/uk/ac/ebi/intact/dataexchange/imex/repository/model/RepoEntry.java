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

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

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
@NamedQueries(value = {
    @NamedQuery(name = "repoEntryByPmid", query="select re from RepoEntry re where re.pmid = :pmid"),
    @NamedQuery(name = "repoEntryImportable", query="select re from RepoEntry re where re.valid = true"),
    @NamedQuery(name = "repoEntryCountAll", query="select count(re) from RepoEntry re"),
    @NamedQuery(name = "repoEntryModifiedAfter", query="select re from RepoEntry re where re.updated  > :date and re.importable = true")
})
public class RepoEntry extends RepoEntity {

    @ManyToOne
    private RepoEntrySet repoEntrySet;

    @Column (unique = true)
    private String pmid;

    private boolean enriched;

    private boolean valid;

    private boolean importable = true;

    private boolean autoFixed;

    @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
    private DateTime releaseDate;

    @OneToMany (mappedBy = "repoEntry", cascade = CascadeType.ALL)
    private List<Message> messages;

    public RepoEntry() {
    }

    public RepoEntry(String pmid, DateTime releaseDate) {
        this.pmid = pmid;
        this.releaseDate = releaseDate;
    }

    /////////////////////////////
    // Getters and Setters

    public RepoEntrySet getRepoEntrySet() {
        return repoEntrySet;
    }

    public void setRepoEntrySet(RepoEntrySet repoEntrySet) {
        this.repoEntrySet = repoEntrySet;
    }

    public String getPmid()
    {
        return pmid;
    }

    public void setPmid(String pmid)
    {
        this.pmid = pmid;
    }

    public boolean isEnriched()
    {
        return enriched;
    }

    public void setEnriched(boolean enriched)
    {
        this.enriched = enriched;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;

        if (!valid) importable = false;
    }

    public List<Message> getMessages() {
        if (messages == null) {
           messages = new ArrayList<Message>();
        }
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        getMessages().add(message);
        message.setRepoEntry(this);

        if (message instanceof ValidationMessage && message.getLevel() == MessageLevel.ERROR) {
            valid = false;
        }

        if (message.getLevel() == MessageLevel.ERROR) {
            importable = false;
        }
    }

    public boolean isImportable() {
        return importable;
    }

    public void setImportable(boolean importable) {
        this.importable = importable;
    }

    public DateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(DateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isAutoFixed() {
        return autoFixed;
    }

    public void setAutoFixed(boolean autoFixed) {
        this.autoFixed = autoFixed;
    }
}