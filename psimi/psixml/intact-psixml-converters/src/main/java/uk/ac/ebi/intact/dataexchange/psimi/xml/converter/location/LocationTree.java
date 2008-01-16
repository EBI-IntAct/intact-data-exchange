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

import java.util.LinkedList;
import java.util.Iterator;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LocationTree {

    private LocationItem root;
    private LocationItem parent;
    private LocationItem currentLocation;

    public LocationTree() {
    }

    public void newChild(LocationItem locationItem) {
        if (currentLocation != null) {
            currentLocation.getChildren().add(locationItem);
            parent = currentLocation;
            locationItem.setParent(parent);
        } else {
            root = locationItem;
            parent = locationItem;
        }
        currentLocation = locationItem;
    }

    public void resetPosition() {
        currentLocation = parent;
        if (currentLocation.getParent() != root) {
            parent = currentLocation.getParent();
        } else {
            parent = currentLocation;
        }
    }

    public LocationItem getCurrentLocation() {
        return currentLocation;
    }

    public LocationItem getParent() {
        return parent;
    }

    public LocationItem getRoot() {
        return root;
    }

}