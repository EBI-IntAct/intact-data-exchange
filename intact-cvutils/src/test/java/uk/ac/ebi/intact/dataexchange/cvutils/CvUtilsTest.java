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
package uk.ac.ebi.intact.dataexchange.cvutils;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.CvDagObject;

import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUtilsTest {

    private static List<CvDagObject> ontology;

    @BeforeClass
    public static void beforeClass() throws Exception {
       ontology = new CvObjectOntologyBuilder(OboUtils.createOBOSessionFromDefault("1.48")).getAllCvs();
    }

    @Test
    public void findLowerCommonAncestor() throws Exception {
        Assert.assertEquals("MI:0116", CvUtils.findLowestCommonAncestor(ontology, "MI:0252", "MI:0505"));
        Assert.assertEquals("MI:0505", CvUtils.findLowestCommonAncestor(ontology, "MI:0253", "MI:0505"));
        Assert.assertNull(CvUtils.findLowestCommonAncestor(ontology, "MI:0500", "MI:0116"));
        Assert.assertEquals("MI:0495", CvUtils.findLowestCommonAncestor(ontology, "MI:0496", "MI:0498", "MI:0503"));
        Assert.assertNull(CvUtils.findLowestCommonAncestor(ontology, "MI:0496", "MI:0498", "MI:0503", "MI:0501"));
    }

}