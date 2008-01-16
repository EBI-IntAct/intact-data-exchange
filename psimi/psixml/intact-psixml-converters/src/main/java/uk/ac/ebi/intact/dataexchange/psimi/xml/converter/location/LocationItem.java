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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LocationItem {

    private String id;
    private Class<?> type;
    private List<LocationItem> children;
    private LocationItem parent;

    public LocationItem(String id, Class<?> type) {
        this.id = id;
        this.type = type;

        this.children = new ArrayList<LocationItem>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public List<LocationItem> getChildren() {
        return children;
    }

    public void setChildren(List<LocationItem> children) {
        this.children = children;
    }

    public LocationItem getParent() {
        return parent;
    }

    public void setParent(LocationItem parent) {
        this.parent = parent;
    }

    public String pathFromRootAsString() {
        StringBuilder sb = new StringBuilder();

        for (Iterator<LocationItem> iterator = pathToParent(this).iterator(); iterator.hasNext();) {
            LocationItem locationItem =  iterator.next();

            sb.append(locationItem);

            if (iterator.hasNext()) {
                sb.append(" > ");
            }
        }

        return sb.toString();
    }

    private LinkedList<LocationItem> pathToParent(LocationItem locationItem) {
        LinkedList<LocationItem> pathToParent = new LinkedList<LocationItem>();
        pathToParent.addFirst(locationItem);

        if (locationItem.getParent() != null) {
            pathToParent.addAll(0, pathToParent(locationItem.getParent()));
        }

        return pathToParent;
    }

    @Override
    public String toString() {
        return type.getSimpleName()+"[id="+id+"]";
    }


}