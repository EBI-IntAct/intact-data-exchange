// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.PsiDownloadTest;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.model.TestableProtein;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id:Interactor2xmlPSI25Test.java 5298 2006-07-07 09:35:05 +0000 (Fri, 07 Jul 2006) baranda $
 */
public class Interactor2xmlPSI25Test extends PsiDownloadTest {

    protected void setUp() throws Exception
    {
        super.setUp();
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        IntactContext.getCurrentInstance().getDataContext().commitAllActiveTransactions();
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

        protein.addXref( new InteractorXref ( owner, uniprot, "P47068", null, null, identity ) );
        protein.addXref( new InteractorXref ( owner, uniprot, "P47067", null, null, secondaryAc ) );
        protein.addXref( new InteractorXref ( owner, uniprot, "Q8X1F4", null, null, secondaryAc ) );
        protein.addXref( new InteractorXref ( owner, sgd, "S000003557", "BBC1", null, secondaryAc ) );
        // NOTE: the Xref.secondaryId are truncated to 30 characters
        protein.addXref( new InteractorXref ( owner, go, "GO:0030479", "C:actin cortical patch (sensu Fungi)", null, null ) );
        protein.addXref( new InteractorXref ( owner, go, "GO:0017024", "F:myosin I binding", null, null ) );
        protein.addXref( new InteractorXref ( owner, go, "GO:0030036", "P:actin cytoskeleton organization and biogenesis", null, null ) );
        protein.addXref( new InteractorXref ( owner, go, "GO:0007010", "P:cytoskeleton organization and biogenesis", null, null ) );

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

    public void testBuildProtein_nullArguments( PsiVersion version ) {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion25() );

        // create a container
        Element parent = session.createElement( "interactorList" );

        // call the method we are testing
        Element element = null;

        Interactor2xmlPSI25 generator = Interactor2xmlPSI25.getInstance();

        try {
            element = generator.create( session, parent, null );
            fail( "giving a null Protein should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        // create the IntAct object
        Protein protein = new ProteinImpl( owner, yeast, "bbc1_yeast", proteinType );

        try {
            Element wrongParent = session.createElement( "foobar" );
            element = generator.create( session, wrongParent, protein );
            fail( "Creation of a protein from a parent diferent from interactorList or proteinParticipant should not be allowed." );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        try {
            element = generator.create( null, parent, protein );
            fail( "giving a null session should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );

        try {
            element = generator.create( session, null, protein );
            fail( "giving a null parent Element should throw an exception" );
        } catch ( IllegalArgumentException e ) {
            // ok
        }

        assertNull( element );
    }

    public void testBuildProtein_full_ok() {

        UserSessionDownload session = new UserSessionDownload( PsiVersion.getVersion25() );
        session.addAnnotationFilter( remark );

        // create a container
        Element parent = session.createElement( "interactorList" );

        Protein protein = createProtein();

        Interactor2xmlPSI25 generator = Interactor2xmlPSI25.getInstance();
        // Protein2xmlI generator = Protein2xmlFactory.getInstance( session );

        // generating the PSI element...
        Element element = generator.create( session, parent, protein );

        // names, xref, organism, sequence, attributeList
        assertEquals( 6, element.getChildNodes().getLength() );

        // starting the checks...
        assertNotNull( element );
        assertEquals( "" + session.getInteractorIdentifier( protein ), element.getAttribute( "id" ) );

        // Checking names...
        // TODO write a method that returns an Element by name coming from the direct level
        Element names = (Element) element.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 6, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "bbc1_yeast" );
        assertHasFullname( names, "Myosin tail region-interacting protein MTI1" );
        // Checking Aliases
        assertHasAlias( names, "BBC1", "gene name", "MI:0301" );
        assertHasAlias( names, "MTI1", "gene name-synonym", "MI:0302" );
        assertHasAlias( names, "YJL020C/YJL021C", "locus name", "MI:0305" );
        assertHasAlias( names, "J1305/J1286", "orf name", "MI:0306" );

        // Checking xref...
        Element xref = (Element) element.getElementsByTagName( "xref" ).item( 0 );
        assertNotNull( xref );

        assertEquals( 9, xref.getChildNodes().getLength() );
        assertHasPrimaryRef( xref, "P47068", "uniprotkb", null, null );
        assertHasSecondaryRef( xref, "P47067", "uniprotkb", null, null );
        assertHasSecondaryRef( xref, "Q8X1F4", "uniprotkb", null, null );
        assertHasSecondaryRef( xref, "S000003557", "sgd", "BBC1", null );
        // NOTE: the Xref.secondaryId are truncated to 30 characters
        assertHasSecondaryRef( xref, "GO:0030479", "go", "C:actin cortical patch (sensu ", null );
        assertHasSecondaryRef( xref, "GO:0017024", "go", "F:myosin I binding", null );
        assertHasSecondaryRef( xref, "GO:0030036", "go", "P:actin cytoskeleton organizat", null );
        assertHasSecondaryRef( xref, "GO:0007010", "go", "P:cytoskeleton organization an", null );
        assertHasSecondaryRef( xref, "EBI-333333", "intact", "bbc1_yeast", null );

        // Checking organism...
        Element hostOrganism = (Element) element.getElementsByTagName( "organism" ).item( 0 );
        assertNotNull( hostOrganism );
        assertEquals( "4932", hostOrganism.getAttribute( "ncbiTaxId" ) );
        // check names
        assertEquals( 1, hostOrganism.getElementsByTagName( "names" ).getLength() );
        names = (Element) hostOrganism.getElementsByTagName( "names" ).item( 0 );
        assertNotNull( names );
        assertEquals( 3, names.getChildNodes().getLength() );
        assertHasShortlabel( names, "yeast" );
        // check aliases
        assertHasAlias( names, "sacharomices" );
        assertHasAlias( names, "Baker Yeast" );

        // Checking sequence...
        Element sequenceElement = (Element) element.getElementsByTagName( "sequence" ).item( 0 );
        assertNotNull( sequenceElement );
        // the remark should have been filtered out.
        assertEquals( 1, sequenceElement.getChildNodes().getLength() );
        assertEquals( protein.getSequence(), getTextFromElement( sequenceElement ) );

        // Checking annotations
        Element attributeListElement = (Element) element.getElementsByTagName( "attributeList" ).item( 0 );
        assertHasAttribute( attributeListElement, "comment", "an interresting comment." );
    }
}