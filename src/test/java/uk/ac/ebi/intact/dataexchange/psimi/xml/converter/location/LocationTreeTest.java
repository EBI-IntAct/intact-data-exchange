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

import org.junit.Test;
import org.junit.Assert;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class LocationTreeTest {

    @Test
    public void locationTree() throws Exception {
        LocationTree locTree = new LocationTree();

        final LocationItem child11 = new LocationItem("child11", String.class);
        final LocationItem child12 = new LocationItem("child12", String.class);
        final LocationItem child2 = new LocationItem("child2", String.class);

        locTree.newChild(new LocationItem("root", String.class));
        locTree.newChild(new LocationItem("child1", String.class));
        locTree.newChild(child11);
        locTree.resetPosition();
        locTree.newChild(child12);
        locTree.resetPosition();
        locTree.resetPosition();
        locTree.newChild(child2);
                

        Assert.assertEquals("String[id=root] > String[id=child1] > String[id=child11]", child11.pathFromRootAsString());
        Assert.assertEquals("String[id=root] > String[id=child1] > String[id=child12]", child12.pathFromRootAsString());
        Assert.assertEquals("String[id=root] > String[id=child2]", child2.pathFromRootAsString());

    }

}