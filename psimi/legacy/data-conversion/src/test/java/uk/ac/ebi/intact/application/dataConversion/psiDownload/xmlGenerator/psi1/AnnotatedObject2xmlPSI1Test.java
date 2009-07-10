// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.model.TestableProtein;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.AbstractAnnotatedObject2xml;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2.AnnotatedObject2xmlPSI2;
import uk.ac.ebi.intact.model.*;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:AnnotatedObject2xmlPSI1Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 */
public class AnnotatedObject2xmlPSI1Test extends PsiDownloadTest {

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( AnnotatedObject2xmlPSI1Test.class );
    }

    ////////////////////////
    // Utility method

    private Protein createProtein() {

        // create the IntAct object
        String sequence = "MSEPEVPFKVVAQFPYKSDYEDDLNFEKDQEIIVTSVEDAEWYFGEYQDSNGDVIEGIFP" +
                          "KSFVAVQGSEVGKEAESSPNTGSTEQRTIQPEVEQKDLPEPISPETKKETLSGPVPVPAA" +
                          "TVPVPAATVPVPAATAVSAQVQHDSSSGNGERKVPMDSPKLKARLSMFNQDITEQVPLPK" +
                          "STHLDLENIPVKKTIVADAPKYYVPPGIPTNDTSNLERKKSLKENEKKIVPEPINRAQVE" +
                          "SGRIETENDQLKKDLPQMSLKERIALLQEQQRLQAAREEELLRKKAKLEQEHERSAVNKN" +
                          "EPYTETEEAEENEKTEPKPEFTPETEHNEEPQMELLAHKEITKTSREADEGTNDIEKEQF" +
                          "LDEYTKENQKVEESQADEARGENVAEESEIGYGHEDREGDNDEEKEEEDSEENRRAALRE" +
                          "RMAKLSGASRFGAPVGFNPFGMASGVGNKPSEEPKKKQHKEKEEEEPEQLQELPRAIPVM" +
                          "PFVDPSSNPFFRKSNLSEKNQPTETKTLDPHATTEHEQKQEHGTHAYHNLAAVDNAHPEY" +
                          "SDHDSDEDTDDHEFEDANDGLRKHSMVEQAFQIGNNESENVNSGEKIYPQEPPISHRTAE" +
                          "VSHDIENSSQNTTGNVLPVSSPQTRVARNGSINSLTKSISGENRRKSINEYHDTVSTNSS" +
                          "ALTETAQDISMAAPAAPVLSKVSHPEDKVPPHPVPSAPSAPPVPSAPSVPSAPPVPPAPP" +
                          "ALSAPSVPPVPPVPPVSSAPPALSAPSIPPVPPTPPAPPAPPAPLALPKHNEVEEHVKSS" +
                          "APLPPVSEEYHPMPNTAPPLPRAPPVPPATFEFDSEPTATHSHTAPSPPPHQNVTASTPS" +
                          "MMSTQQRVPTSVLSGAEKESRTLPPHVPSLTNRPVDSFHESDTTPKVASIRRSTTHDVGE" +
                          "ISNNVKIEFNAQERWWINKSAPPAISNLKLNFLMEIDDHFISKRLHQKWVVRDFYFLFEN" +
                          "YSQLRFSLTFNSTSPEKTVTTLQERFPSPVETQSARILDEYAQRFNAKVVEKSHSLINSH" +
                          "IGAKNFVSQIVSEFKDEVIQPIGARTFGATILSYKPEEGIEQLMKSLQKIKPGDILVIRK" +
                          "AKFEAHKKIGKNEIINVGMDSAAPYSSVVTDYDFTKNKFRVIENHEGKIIQNSYKLSHMK" +
                          "SGKLKVFRIVARGYVGW";

        Protein protein = new TestableProtein( "EBI-333333", owner, yeast, "bbc1_yeast", proteinType, sequence );
        protein.setFullName( "Myosin tail region-interacting protein MTI1" );

        protein.addXref( new InteractorXref( owner, uniprot, "P47068", null, null, identity ) );
        protein.addXref( new InteractorXref( owner, uniprot, "P47067", null, null, secondaryAc ) );
        protein.addXref( new InteractorXref( owner, uniprot, "Q8X1F4", null, null, secondaryAc ) );
        protein.addXref( new InteractorXref( owner, sgd, "S000003557", "BBC1", null, secondaryAc ) );
        // NOTE: the Xref.secondaryId are truncated to 30 characters
        protein.addXref( new InteractorXref( owner, go, "GO:0030479", "C:actin cortical patch (sensu Fungi)", null, null ) );
        protein.addXref( new InteractorXref( owner, go, "GO:0017024", "F:myosin I binding", null, null ) );
        protein.addXref( new InteractorXref( owner, go, "GO:0030036", "P:actin cytoskeleton organization and biogenesis", null, null ) );
        protein.addXref( new InteractorXref( owner, go, "GO:0007010", "P:cytoskeleton organization and biogenesis", null, null ) );

        protein.addAlias( new InteractorAlias( owner, protein, geneName, "BBC1" ) );
        protein.addAlias( new InteractorAlias( owner, protein, geneNameSynonym, "MTI1" ) );
        protein.addAlias( new InteractorAlias( owner, protein, locusName, "YJL020C/YJL021C" ) );
        protein.addAlias( new InteractorAlias( owner, protein, orfName, "J1305/J1286" ) );

        protein.addAnnotation( new Annotation( owner, comment, "an interresting comment." ) );
        protein.addAnnotation( new Annotation( owner, remark, "an interresting remark." ) );

        return protein;
    }

    ////////////////////////
    // Tests

    private void testBuildNames_nullArguments( PsiVersion version ) {

        UserSessionDownload session = new UserSessionDownload( version );

        // create a container
        Element parentElement = session.createElement( "proteinInteractor" );

        // call the method we are testing
        Element namesElement = null;

        AbstractAnnotatedObject2xml aao = null;

        if ( version.equals( PsiVersion.VERSION_1 ) ) {
            aao = new AnnotatedObject2xmlPSI1();
        } else if ( version.equals( PsiVersion.VERSION_2 ) ) {
            aao = new AnnotatedObject2xmlPSI2();
        } else {
            fail( "Unsupported version of PSI" );
        }


        try {
            namesElement = aao.createNames( session, parentElement, null );
            fail( "giving a null AnnotatedObject should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( namesElement );

        // create the IntAct object
        Protein protein = createProtein();

        try {
            namesElement = aao.createNames( null, parentElement, protein );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( namesElement );

        try {
            namesElement = aao.createNames( session, null, protein );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( namesElement );
    }

    public void testBuildNames_nullArguments_PSI1() {
        testBuildNames_nullArguments( PsiVersion.VERSION_1 );
    }

    public void testBuildNames_protein_ok_PSI1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );
        AbstractAnnotatedObject2xml aao = new AnnotatedObject2xmlPSI1();

        // create a container
        Element parentElement = session.createElement( "proteinInteractor" );

        // create the IntAct object
        Protein protein = createProtein();
        protein.setFullName( "fullname of the protein." );

        // call the method we are testing
        Element namesElement = aao.createNames( session, parentElement, protein );

        assertNotNull( namesElement );

        // check that we have a primaryRef attached to the given parent tag
        assertEquals( 1, parentElement.getChildNodes().getLength() );
        Element _primaryRef = (Element) parentElement.getChildNodes().item( 0 );
        assertEquals( namesElement, _primaryRef );

        // check content of the tag
        assertEquals( "names", namesElement.getNodeName() );
        assertEquals( 2, namesElement.getChildNodes().getLength() );

        assertHasShortlabel( namesElement, "bbc1_yeast" );

//        assertEquals( 1, namesElement.getElementsByTagName( "shortLabel" ).getLength() );
//        Element shortlabel = (Element) namesElement.getElementsByTagName( "shortlabel" ).item( 0 );
//        String text = getTextFromElement( shortlabel );
//        assertNotNull( text );
//        assertEquals( "bbc1_yeast", text );
        assertHasFullname( namesElement, "fullname of the protein." );

//        assertEquals( 1, namesElement.getElementsByTagName( "fullname" ).getLength() );
//        Element fullname = (Element) namesElement.getElementsByTagName( "fullname" ).item( 0 );
//        text = getTextFromElement( fullname );
//        assertNotNull( text );
//        assertEquals( "fullname of the protein.", text );
    }

    public void testBuildNamesNoFullname_protein_ok_PSI1() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.VERSION_1 );

        // create a container
        Element parentElement = session.createElement( "proteinInteractor" );

        // create the IntAct object
        Protein protein = createProtein();
        protein.setFullName( null );

        AbstractAnnotatedObject2xml aao = new AnnotatedObject2xmlPSI1();

        // call the method we are testing
        Element namesElement = aao.createNames( session, parentElement, protein );
        assertNotNull( namesElement );

        // check that we have a primaryRef attached to the given parent tag
        assertEquals( 1, parentElement.getChildNodes().getLength() );
        Element _primaryRef = (Element) parentElement.getChildNodes().item( 0 );
        assertEquals( namesElement, _primaryRef );

        // check content of the tag
        assertEquals( "names", namesElement.getNodeName() );
        assertEquals( 1, namesElement.getChildNodes().getLength() );

        assertHasShortlabel( namesElement, "bbc1_yeast" );

        // no full name
        assertEquals( 0, namesElement.getElementsByTagName( "fullname" ).getLength() );
    }
}