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

import org.hibernate.annotations.Index;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.NamedQuery;
import javax.persistence.Column;
import java.util.Collection;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Entity
@NamedQuery(name = "providerByName", query="select p from Provider p where p.name = :name")
public class Provider extends RepoEntity {

    @Index(name="provider_name_idx")
    @Column(unique = true)
    public String name;

    @OneToMany (mappedBy = "provider")
    private Collection<ProviderProperty> properties;

    public Provider() {
    }

    public Provider(String name) {
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

    public Collection<ProviderProperty> getProperties() {
        return properties;
    }

    public void setProperties(Collection<ProviderProperty> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Provider{name="+getName()+", created="+getCreated()+"}";
    }
}