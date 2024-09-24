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


package uk.ac.ebi.intact.dataexchange.cvutils;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.DataAdapterException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.*;
import org.obo.datamodel.impl.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ebi.intact.core.unit.IntactMockBuilder;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.CvDagObject;
import uk.ac.ebi.intact.model.CvInteraction;
import uk.ac.ebi.intact.model.CvObject;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Test the DownloadCvsExtended class that contains methods to recreate the OBOSession object from a list of CVObjects
 * The CVObject is stripped and a OBOObject is created which is then added
 * to the OBOSession and finally written to an OBO 1.2 file using a DataAdapter
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations="classpath:/retry.properties")
@ContextConfiguration(locations = {
        "classpath*:/META-INF/intact.spring.xml",
        "classpath*:/META-INF/standalone/*-standalone.spring.xml"
})
public class CvExporterTest {

    //initialize logger
    protected final static Logger log = Logger.getLogger( CvExporterTest.class );

    private List<CvDagObject> allCvs;

    @Before
    public void prepareCvs() throws OBOParseException, IOException, PsiLoaderException {

        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.51" );
        OBOSession oboSession = OboUtils.createOBOSession( CvExporterTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        log.debug( oboSession.getObjects().size() );
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        List<CvDagObject> allCvs_;
        allCvs_ = ontologyBuilder.getAllCvs();

        this.allCvs = allCvs_;
        if ( log.isDebugEnabled() ) log.debug( "allCvs size " + allCvs.size() );

    }//end method


    @Test
    public void testCv2OBORoundTrip() throws OBOParseException, IOException {

        /**
         * id: MI:0244
         name: reactome complex
         def: "Collection of functional complexes within Reactome - a knowledgebase of biological processes.\nhttp://www.reactome.org/" [PMID:14755292]
         subset: PSI-MI slim
         xref: id-validation-regexp:\"REACT_[0-9\]\{1\,4}\\.[0-9\]\{1\,3}|[0-9\]+\"
         xref: search-url: "http://www.reactome.org/cgi-bin/eventbrowser?ID=${ac}"
         is_a: MI:0467 ! reactome

         */

        //OBOSession oboSession = OboUtils.createOBOSessionFromDefault( "1.51" );
        OBOSession oboSession = OboUtils.createOBOSession( CvExporterTest.class.getResource("/ontologies/psi-mi25-1_51.obo" ));
        CvObjectOntologyBuilder ontologyBuilder = new CvObjectOntologyBuilder( oboSession );

        OBOObject readOBOObj = ( OBOObject ) oboSession.getObject( "MI:0244" );
        CvObject cvObject = ontologyBuilder.toCvObject( readOBOObj );

        CvExporter downloadCv = new CvExporter();

        Map<String, HashSet<CvDagObject>> cvMapWithParents = downloadCv.groupByMis( allCvs );

        OBOObject createdOBOObj = downloadCv.convertCv2OBO( ( CvDagObject ) cvObject, cvMapWithParents );

        Assert.assertEquals( readOBOObj.getID(), createdOBOObj.getID() );
        Assert.assertEquals( readOBOObj.getDefinition(), createdOBOObj.getDefinition() );
        Assert.assertEquals( readOBOObj.getName(), createdOBOObj.getName() );
        Assert.assertEquals( readOBOObj.getDefDbxrefs().size(), createdOBOObj.getDefDbxrefs().size() );

        // TODO check the following assertion
        //Assert.assertEquals( readOBOObj.getDbxrefs().size(), createdOBOObj.getDbxrefs().size() );


    }//end method


    @Test
    public void testAllCvs() throws DataAdapterException, IOException {
        CvExporter downloadCv = new CvExporter();

        if ( log.isDebugEnabled() ) log.debug( "From Test all : " + allCvs.size() );

        OBOSession oboSession = downloadCv.convertToOBOSession( allCvs );

        File outFile = new File( "target", "test"+System.currentTimeMillis()+".obo" );
        downloadCv.writeOBOFile( oboSession, outFile );
    }//end method


    // @Test
    public void testSimpleCv() throws DataAdapterException, IOException {

        CvExporter downloadCv = new CvExporter();
        OBOClass obj1 = new OBOClassImpl( "molecular interaction", "MI:000" );
        obj1.setDefinition( "Controlled vocabularies originally created for protein protein interactions, extended to other molecules interactions [PMID:14755292]" );

        Synonym syn = new SynonymImpl();
        syn.setText( "mi" );
        SynonymType synCat = new SynonymTypeImpl();
        synCat.setID( "PSI-MI-short" );

        syn.setSynonymType( synCat );
        syn.setScope( 1 );
        obj1.addSynonym( syn );

        CvObject cvObject = new IntactMockBuilder().createCvObject( CvInteraction.class, "MI:0001", "interaction detect" );
        cvObject.setFullName( "interaction detection method" );

        Map<String, HashSet<CvDagObject>> cvMapWithParents = downloadCv.groupByMis( allCvs );
        OBOClass obj2 = downloadCv.convertCv2OBO( ( CvDagObject ) cvObject, cvMapWithParents );

        Link linkToObj2 = new OBORestrictionImpl( obj2 );
        OBOProperty oboProp = new OBOPropertyImpl( "part_of" );
        linkToObj2.setType( oboProp );

        obj1.addChild( linkToObj2 );

        CvExporter cvExporter = new CvExporter();
        
        cvExporter.getOboSession().addObject( obj1 );
        cvExporter.getOboSession().addObject( obj2 );

        File tempDir = new File( "temp" );
        boolean success = tempDir.mkdir();
        if(success){
        File outFile = File.createTempFile( "test", ".obo" );
        outFile.deleteOnExit();
        downloadCv.writeOBOFile( cvExporter.getOboSession(), outFile );
        }   


    }//end method


}//end class