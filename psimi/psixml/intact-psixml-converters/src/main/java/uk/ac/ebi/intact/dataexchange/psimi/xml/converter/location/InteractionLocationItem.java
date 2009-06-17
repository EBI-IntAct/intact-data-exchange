/*
 * Copyright 2001-2008 The European Bioinformatics Institute.
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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionLocationItem extends LocationItem{

    private String imexId;

    public InteractionLocationItem(String id, Class<?> type, String imexId) {
        super(id, type);
        this.imexId = imexId;
    }

    public String getImexId() {
        return imexId;
    }

    public void setImexId(String imexId) {
        this.imexId = imexId;
    }

    @Override
    public String toString() {
        return getType().getSimpleName()+"[imexId="+imexId+", id="+getId()+"]";
    }
}