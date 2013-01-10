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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.location;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class DisabledLocationTree extends LocationTree {

    private LocationItem locationItem;

    public DisabledLocationTree() {
        locationItem = new LocationItem("0", Object.class);
    }

    @Override
    public void newChild(LocationItem locationItem) {
        // nothing
    }

    @Override
    public void resetPosition() {
        // nothing
    }

    @Override
    public LocationItem getCurrentLocation() {
        return locationItem;
    }

    @Override
    public LocationItem getParent() {
        return locationItem;
    }

    @Override
    public LocationItem getRoot() {
        return locationItem;
    }
}
