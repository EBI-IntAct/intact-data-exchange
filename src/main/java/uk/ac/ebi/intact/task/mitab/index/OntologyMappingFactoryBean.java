/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.task.mitab.index;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import uk.ac.ebi.intact.bridges.ontologies.OntologyMapping;

/**
 * TODO write description of the class.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class OntologyMappingFactoryBean implements FactoryBean{

    private String name;
    private Resource resource;

    public Object getObject() throws Exception {
        return new OntologyMapping(name, resource.getURL());
    }

    public Class getObjectType() {
        return OntologyMapping.class;
    }

    public boolean isSingleton() {
        return false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
}
