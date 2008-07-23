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
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.core.unit.IntactUnit;

import java.util.List;
import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUtilsTest extends IntactBasicTestCase {




    private static List<CvDagObject> ontology;


    @BeforeClass
    public static void beforeClass() throws Exception {
        ontology = new CvObjectOntologyBuilder( OboUtils.createOBOSessionFromDefault( "1.51" ) ).getAllCvs();
    }

    @Test
    public void findLowerCommonAncestor() throws Exception {
        Assert.assertEquals( "MI:0116", CvUtils.findLowestCommonAncestor( ontology, "MI:0252", "MI:0505" ) );
        Assert.assertEquals( "MI:0505", CvUtils.findLowestCommonAncestor( ontology, "MI:0253", "MI:0505" ) );
        Assert.assertNull( CvUtils.findLowestCommonAncestor( ontology, "MI:0500", "MI:0116" ) );
        Assert.assertEquals( "MI:0495", CvUtils.findLowestCommonAncestor( ontology, "MI:0496", "MI:0498", "MI:0503" ) );
        Assert.assertNull( CvUtils.findLowestCommonAncestor( ontology, "MI:0496", "MI:0498", "MI:0503", "MI:0501" ) );
    }


    @Test
    public void getCvsInIntactNotInPsiAndDateTest() throws Exception {
        new IntactUnit().createSchema( true );

        String DATE_FORMAT = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat( DATE_FORMAT );

        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        beginTransaction();

        CvObject twoHybrid = CvObjectUtils.createCvObject( owner, CvInteraction.class, "MI:0018", "two hybrid" );
        twoHybrid.setCreated( sdf.parse( "2008-06-17" ) );
        PersisterHelper.saveOrUpdate( twoHybrid );

        CvObject exp = CvObjectUtils.createCvObject( owner, CvInteraction.class, "MI:0045", "experimental interac" );
        exp.setCreated( sdf.parse( "2008-06-18" ) );
        PersisterHelper.saveOrUpdate( exp );

        CvObject negative = CvObjectUtils.createCvObject( owner, CvTopic.class, null, "negative" );
        negative.setCreated( sdf.parse( "2008-06-19" ) );
        PersisterHelper.saveOrUpdate( negative );

        CvObject positive = CvObjectUtils.createCvObject( owner, CvTopic.class, null, "positive" );
        positive.setCreated( sdf.parse( "2008-06-20" ) );
        PersisterHelper.saveOrUpdate( positive );

        CvObject hippocampus = CvObjectUtils.createCvObject( owner, CvTissue.class, null, "hippocampus" );
        hippocampus.setCreated( sdf.parse( "2008-06-21" ) );
        PersisterHelper.saveOrUpdate( hippocampus );

        CvObject pc12 = CvObjectUtils.createCvObject( owner, CvCellType.class, null, "pc12" );
        pc12.setCreated( sdf.parse( "2008-06-22" ) );
        PersisterHelper.saveOrUpdate( pc12 );

        Collection<String> exclusionList = new ArrayList<String>();
        exclusionList.add( "uk.ac.ebi.intact.model.CvCellType" );
        exclusionList.add( "uk.ac.ebi.intact.model.CvTissue" );


        List<CvObject> notInPsiCvs = CvUtils.getCvsInIntactNotInPsi(exclusionList);
       
        //6 terms are added out of 4 have null MI_Identifier and after excluding CvTissue and CvCellType it should be 2
        Assert.assertEquals( 2, notInPsiCvs.size() );

        Date cutoffDate = sdf.parse( "2008-06-19" );

        List<CvObject> cvsbefore = CvUtils.getCVsAddedBefore( cutoffDate,null );
        Assert.assertEquals( 2, cvsbefore.size() );

        // it should be 3+3 terms(intact+identity+psi-mi)
        List<CvObject> cvsafter = CvUtils.getCvsAddedAfter( cutoffDate,null );
        Assert.assertEquals( 6, cvsafter.size() );

        //with exclusion list
        List<CvObject> cvsafterWithExclusion = CvUtils.getCvsAddedAfter( cutoffDate,exclusionList );
        Assert.assertEquals( 4, cvsafterWithExclusion.size() );

        //one term which is added on the date provided (2008-06-19) is left out
        commitTransaction();
    
    }


}