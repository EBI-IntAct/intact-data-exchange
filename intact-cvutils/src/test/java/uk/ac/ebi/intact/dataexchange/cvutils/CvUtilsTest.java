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
import org.junit.Before;
import org.junit.Test;
import org.obo.datamodel.OBOSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUtilsTest extends IntactBasicTestCase {

    private List<CvDagObject> ontology;

    @Autowired
    private PersisterHelper persisterHelper;

    @Before
    public void before() throws Exception {
        OBOSession oboSession = OboUtils.createOBOSession( CvUtilsTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );
        ontology = ontologyBuilder.getAllCvs();
    }

    @Test
    @DirtiesContext
    public void findLowerCommonAncestor() throws Exception {
        Assert.assertEquals( "MI:0116", CvUtils.findLowestCommonAncestor( ontology, "MI:0252", "MI:0505" ) );
        Assert.assertEquals( "MI:0505", CvUtils.findLowestCommonAncestor( ontology, "MI:0253", "MI:0505" ) );
        Assert.assertNull( CvUtils.findLowestCommonAncestor( ontology, "MI:0500", "MI:0116" ) );
        Assert.assertEquals( "MI:0495", CvUtils.findLowestCommonAncestor( ontology, "MI:0496", "MI:0498", "MI:0503" ) );
        Assert.assertNull( CvUtils.findLowestCommonAncestor( ontology, "MI:0496", "MI:0498", "MI:0503", "MI:0501" ) );
    }


    @Test
    @DirtiesContext
    public void getCvsInIntactNotInPsiAndDateTest() throws Exception {
        String DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );

        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        CvObject twoHybrid = CvObjectUtils.createCvObject( owner, CvInteraction.class, "MI:0018", "two hybrid" );
        twoHybrid.setCreated( sdf.parse( "2008-06-17" ) );
        persisterHelper.save( twoHybrid );

        CvObject exp = CvObjectUtils.createCvObject( owner, CvInteraction.class, "MI:0045", "experimental interac" );
        exp.setCreated( sdf.parse( "2008-06-18" ) );
        persisterHelper.save(exp );

        CvObject negative = CvObjectUtils.createCvObject( owner, CvTopic.class, null, "negative" );
        negative.setCreated( sdf.parse( "2008-06-20" ) );
        persisterHelper.save( negative );

        CvObject positive = CvObjectUtils.createCvObject( owner, CvTopic.class, null, "positive" );
        positive.setCreated( sdf.parse( "2008-06-20" ) );
        persisterHelper.save( positive );

        CvObject hippocampus = CvObjectUtils.createCvObject( owner, CvTissue.class, null, "hippocampus" );
        hippocampus.setCreated( sdf.parse( "2008-06-21" ) );
        persisterHelper.save( hippocampus );

        CvObject pc12 = CvObjectUtils.createCvObject( owner, CvCellType.class, null, "pc12" );
        pc12.setCreated( sdf.parse( "2008-06-22" ) );
        persisterHelper.save( pc12 );

        Collection<String> exclusionList = new ArrayList<String>();
        exclusionList.add( CvCellType.class.getName() );
        exclusionList.add( CvTissue.class.getName() );


        List<CvObject> notInPsiCvs = CvUtils.getCvsInIntactNotInPsi(exclusionList);
       
        //6 terms are added : hidden, used-in-class, on-hold,correction comment, negative, positive
        Assert.assertEquals( 6, notInPsiCvs.size() );

        Date cutoffDate = sdf.parse( "2008-06-19" );

        List<CvObject> cvsbefore = CvUtils.getCVsAddedBefore( cutoffDate,null );
        Assert.assertEquals( 2, cvsbefore.size() );

        List<CvObject> cvsafter = CvUtils.getCvsAddedAfter( cutoffDate,null );
        Assert.assertEquals( 42, cvsafter.size() );

        //with exclusion list
        List<CvObject> cvsafterWithExclusion = CvUtils.getCvsAddedAfter( cutoffDate,exclusionList );
        Assert.assertEquals( 40, cvsafterWithExclusion.size() );
    
    }


}