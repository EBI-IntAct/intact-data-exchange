// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.model.TestableBioSource;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25.AnnotatedObject2xmlPSI25;
import uk.ac.ebi.intact.model.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class PsiDownloadTest extends TestCase {

    /**
     * default constructor
     */
    public PsiDownloadTest() {
    }

    /**
     * Constructs a instance of a test with a specified name.
     *
     * @param name the name of the test.
     */
    public PsiDownloadTest( final String name ) {
        super( name );
    }

    /**
     * Returns this test suite. Reflection is used here to add all the testXXX() methods to the suite.
     */
    public static Test suite() {
        return new TestSuite( PsiDownloadTest.class );
    }

    //////////////////////////////
    // Instance variables

    protected static Institution owner = new Institution( "EBI" );

    // the CvDatabase are required to build the BioSource.
    protected static CvDatabase uniprot = new CvDatabase( owner, CvDatabase.UNIPROT );
    protected static CvDatabase intact = new CvDatabase( owner, CvDatabase.INTACT );
    protected static CvDatabase psi = new CvDatabase( owner, CvDatabase.PSI_MI );
    protected static CvDatabase cabri = new CvDatabase( owner, CvDatabase.CABRI );
    protected static CvDatabase interpro = new CvDatabase( owner, CvDatabase.INTERPRO );
    protected static CvDatabase newt = new CvDatabase( owner, CvDatabase.NEWT );
    protected static CvDatabase go = new CvDatabase( owner, CvDatabase.GO );
    protected static CvDatabase pubmed = new CvDatabase( owner, CvDatabase.PUBMED );
    protected static CvDatabase sgd = new CvDatabase( owner, CvDatabase.SGD );

    protected static BioSource yeast = createBioSource( owner, "yeast", "4932",
                                                        new String[]{ "sacharomices", "Baker Yeast" } );
    protected static BioSource human = createBioSource( owner, "human", "9606",
                                                        new String[]{ "homo sapiens", "homme" } );

    protected static CvXrefQualifier identity = new CvXrefQualifier( owner, CvXrefQualifier.IDENTITY );
    protected static CvXrefQualifier secondaryAc = new CvXrefQualifier( owner, CvXrefQualifier.SECONDARY_AC );
    protected static CvXrefQualifier primaryReference = new CvXrefQualifier( owner, CvXrefQualifier.PRIMARY_REFERENCE );
    protected static CvXrefQualifier seeAlso = new CvXrefQualifier( owner, CvXrefQualifier.SEE_ALSO );

    protected static CvTopic comment = new CvTopic( owner, "comment" );
    protected static CvTopic remark = new CvTopic( owner, CvTopic.INTERNAL_REMARK );
    protected static CvTopic confidence_mapping = new CvTopic( owner, CvTopic.CONFIDENCE_MAPPING );
    protected static CvTopic authorConfidence = new CvTopic( owner, CvTopic.AUTHOR_CONFIDENCE );

    protected static CvAliasType geneName = new CvAliasType( owner, CvAliasType.GENE_NAME );
    protected static CvAliasType geneNameSynonym = new CvAliasType( owner, CvAliasType.GENE_NAME_SYNONYM );
    protected static CvAliasType locusName = new CvAliasType( owner, CvAliasType.LOCUS_NAME );
    protected static CvAliasType orfName = new CvAliasType( owner, CvAliasType.ORF_NAME );

    protected static CvFeatureType formylation = new CvFeatureType( owner, "formylation" );
    protected static CvFeatureType hydroxylation = new CvFeatureType( owner, "hydroxylation" );

    protected static CvExperimentalRole bait = new CvExperimentalRole( owner, CvExperimentalRole.BAIT );
    protected static CvExperimentalRole prey = new CvExperimentalRole( owner, CvExperimentalRole.PREY );
    protected static CvExperimentalRole neutral = new CvExperimentalRole( owner, CvExperimentalRole.NEUTRAL );

    protected static CvBiologicalRole unspecified = new CvBiologicalRole( owner, CvBiologicalRole.UNSPECIFIED );

    protected static CvInteractionType aggregation = new CvInteractionType( owner, "aggregation" );
    protected static CvInteractionType cleavage = new CvInteractionType( owner, "cleavage" );

    protected static CvInteractorType proteinType = new CvInteractorType( owner, "protein" );
    protected static CvInteractorType interactionType = new CvInteractorType( owner, "interaction" );
    protected static CvInteractorType nucleicAcidType = new CvInteractorType( owner, "nucleic acid" );

    static {
        addPsiReference( bait, "MI:0496" );
        addPsiReference( prey, "MI:0498" );
        addPsiReference( neutral, "MI:0498" );

        addPsiReference( aggregation, "MI:0407" );

        addPsiReference( uniprot, CvDatabase.UNIPROT_MI_REF );
        addPsiReference( intact, CvDatabase.INTACT_MI_REF );
        addPsiReference( psi, CvDatabase.PSI_MI_MI_REF );
        addPsiReference( cabri, CvDatabase.CABRI_MI_REF );
        addPsiReference( interpro, CvDatabase.INTERPRO_MI_REF );
        addPsiReference( newt, CvDatabase.NEWT_MI_REF );
        addPsiReference( go, CvDatabase.GO_MI_REF );
        addPsiReference( pubmed, CvDatabase.PUBMED_MI_REF );
        addPsiReference( sgd, CvDatabase.SGD_MI_REF );

        addPsiReference( identity, CvXrefQualifier.IDENTITY_MI_REF );
        addPsiReference( secondaryAc, CvXrefQualifier.SECONDARY_AC_MI_REF );
        addPsiReference( seeAlso, CvXrefQualifier.SEE_ALSO_MI_REF );
        addPsiReference( primaryReference, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF );

        addPsiReference( geneName, CvAliasType.GENE_NAME_MI_REF );
        addPsiReference( geneNameSynonym, CvAliasType.GENE_NAME_SYNONYM_MI_REF );
        addPsiReference( locusName, CvAliasType.LOCUS_NAME_MI_REF );
        addPsiReference( orfName, CvAliasType.ORF_NAME_MI_REF );

        addPsiReference( formylation, "MI:0207" );
        addPsiReference( hydroxylation, "MI:0210" );

        addPsiReference( proteinType, CvInteractorType.getProteinMI() );
        addPsiReference( interactionType, CvInteractorType.getInteractionMI() );
        addPsiReference( interactionType, (String) CvInteractorType.getNucleicAcidMIs().get( 0 ) );
    }


    private static BioSource createBioSource( Institution owner, String shortlabel, String taxid, String[] aliases ) {

        BioSource bioSource = new TestableBioSource( "EBI-1234567", owner, shortlabel, taxid );
        bioSource.addXref( new BioSourceXref( owner, newt, taxid, null, null, identity ) );
        for ( int i = 0; i < aliases.length; i++ ) {
            String alias = aliases[ i ];
            // TODO: update the way the Alias is created in order to mimic the Xref.
            bioSource.addAlias( new BioSourceAlias( owner, bioSource, null, alias ) );
        }

        return bioSource;
    }

    //////////////////////////////
    // Testing Utility methods

    /**
     * Checks if the parent tag xrefs contains at least one secondaryRef having the given attributes.
     *
     * @param xrefs     the xref container.
     * @param id        the id of the secondaryRef we are looking for.
     * @param db        the database name of the secondaryRef we are looking for.
     * @param secondary the secondary id of the secondaryRef we are looking for. <code>null</code> for none specified.
     * @param version   the version of the secondaryRef we are looking for. <code>null</code> for none specified.
     */
    protected void assertHasSecondaryRef( Element xrefs, String id, String db, String secondary, String version ) {

        if ( xrefs == null ) {
            throw new IllegalArgumentException( "You must give a non null xref tag." );
        }

        if ( !"xref".equals( xrefs.getNodeName() ) && !"bibRef".equals( xrefs.getNodeName() ) ) {
            fail( "You must give a tag <xref>, not <" + xrefs.getNodeName() + ">." );
        }

        NodeList list = xrefs.getChildNodes();
        for ( int i = 0; i < list.getLength(); i++ ) {
            Element xref = (Element) list.item( i );

            if ( "secondaryRef".equals( xref.getNodeName() ) ) {

                if ( hasXrefAttributes( xref, id, db, secondary, version ) ) {
                    return;
                }
            }
        }

        StringBuffer sb = new StringBuffer( 128 );
        sb.append( "Could not find a secondary Xref having the following attributes: " );
        sb.append( "id=" ).append( id ).append( ", " );
        sb.append( "db=" ).append( db ).append( ", " );
        sb.append( "secondary=" ).append( secondary ).append( ", " );
        sb.append( "version=" ).append( version );

        fail( sb.toString() );
    }

    /**
     * Checks if the parent tag xrefs contains at least one primaryRef having the given attributes.
     *
     * @param xrefs     the xref container.
     * @param id        the id of the primaryRef we are looking for.
     * @param db        the database name of the primaryRef we are looking for.
     * @param secondary the secondary id of the primaryRef we are looking for. <code>null</code> for none specified.
     * @param version   the version of the secondaryprimaryRefRef we are looking for. <code>null</code> for none
     *                  specified.
     */
    protected void assertHasPrimaryRef( Element xrefs, String id, String db, String secondary, String version ) {

        if ( xrefs == null ) {
            throw new IllegalArgumentException( "You must give a non null xref tag." );
        }

        if ( !"xref".equals( xrefs.getNodeName() ) && !"bibRef".equals( xrefs.getNodeName() ) ) {
            fail( "You must give a tag <xref>, not <" + xrefs.getNodeName() + ">." );
        }

        NodeList list = xrefs.getChildNodes();
        for ( int i = 0; i < list.getLength(); i++ ) {
            Element xref = (Element) list.item( i );

            if ( "primaryRef".equals( xref.getNodeName() ) ) {

                if ( hasXrefAttributes( xref, id, db, secondary, version ) ) {
                    return;
                }
            }
        }

        StringBuffer sb = new StringBuffer( 128 );
        sb.append( "Could not find a secondary Xref having the following attributes: " );
        sb.append( "id=" ).append( id ).append( ", " );
        sb.append( "db=" ).append( db ).append( ", " );
        sb.append( "secondary=" ).append( secondary ).append( ", " );
        sb.append( "version=" ).append( version );

        fail( sb.toString() );
    }

    /**
     * Checks if the given xref has the given attributes.
     *
     * @param xref      the xref element (could be either a primaryRef or a seconaryRef).
     * @param id        the id of the xref we are looking for.
     * @param db        the database name of the xref we are looking for.
     * @param secondary the secondary id of the xref we are looking for. <code>null</code> for none specified.
     * @param version   the version of the xref we are looking for. <code>null</code> for none specified.
     *
     * @return true if that xref has all requested attributes, false otherwise.
     */
    protected boolean hasXrefAttributes( Element xref, String id, String db, String secondary, String version ) {

        // The Attr value as a string, or the empty string if that attribute does not
        // have a specified or default value.

        if ( id != null ) {
            if ( !id.equals( xref.getAttribute( "id" ) ) ) {
                return false;
            }
        } else {
            if ( !"".equals( xref.getAttribute( "id" ) ) ) {
                return false;
            }
        }

        if ( db != null ) {
            if ( !db.equals( xref.getAttribute( "db" ) ) ) {
                return false;
            }
        } else {
            if ( !"".equals( xref.getAttribute( "db" ) ) ) {
                return false;
            }
        }

        if ( secondary != null ) {
            if ( !secondary.equals( xref.getAttribute( "secondary" ) ) ) {
                return false;
            }
        } else {
            if ( !"".equals( xref.getAttribute( "secondary" ) ) ) {
                return false;
            }
        }

        if ( version != null ) {
            if ( !version.equals( xref.getAttribute( "version" ) ) ) {
                return false;
            }
        } else {
            if ( !"".equals( xref.getAttribute( "version" ) ) ) {
                return false;
            }
        }

        return true; // all 4 fields are equals
    }

    protected String getTextFromElement( Element element ) {

        String text = null;

        final NodeList children = element.getChildNodes();
        for ( int i = 0; i < children.getLength(); i++ ) {
            final Node child = children.item( i );
            if ( child instanceof Text ) {
                if ( text != null ) {
                    fail( "We should have here only one Text Node." );
                }
                text = child.getNodeValue();
            }
        }

        return text;
    }

    protected void assertHasShortlabel( Element parent, String shortlabel ) {

        assertNotNull( parent );
        assertNotNull( shortlabel );
        assertSubTextNodeUnique( parent, "shortLabel", shortlabel );
    }

    protected void assertHasFullname( Element parent, String fullname ) {

        assertNotNull( parent );
        assertSubTextNodeUnique( parent, "fullName", fullname );
    }

    protected void assertHasAlias( Element parent, String alias ) {

        assertNotNull( parent );
        assertSubTextNodeMultiple( parent, "alias", alias );
    }

    protected void assertHasAlias( Element parent, String alias, String type, String typeAc ) {

        assertNotNull( parent );
        assertSubTextNodeMultiple( parent, "alias", alias );
        NodeList list = parent.getElementsByTagName( "alias" );
        assertTrue( "no Alias found.", list.getLength() > 0 );

        boolean found = false;
        for ( int i = 0; i < list.getLength() && ! found; i++ ) {
            Element aliasElement = (Element) list.item( i );

            String _type = aliasElement.getAttribute( AnnotatedObject2xmlPSI25.TYPE_ATTRIBUTE_NAME );
            String _typeAc = aliasElement.getAttribute( AnnotatedObject2xmlPSI25.TYPE_AC_ATTRIBUTE_NAME );

            if ( type != null ) {
                if ( false == _type.equals( type ) ) {
                    continue;
                }
            } else {
                // check that there is none
                if ( false == _type.equals( "" ) ) {
                    continue;
                }
            }

            // type and _type are equals

            if ( typeAc != null ) {
                if ( false == typeAc.equals( _typeAc ) ) {
                    continue;
                } else {
                    found = true;
                }
            } else {
                // check that there is none
                if ( false == _typeAc.equals( "" ) ) {
                    continue;
                } else {
                    found = true;
                }
            }
        }

        if ( found == false ) {
            fail( "Could not find that alias( '" + alias + "', '" + type + "', '" + typeAc + "' )." );
        }
    }

    /**
     * Given an attribute list, checks that it contains the given attribute description.
     *
     * @param parent the attribute list Element.
     * @param name   the name of the attribute.
     * @param text   the text of the attribute.
     */
    protected void assertHasAttribute( Element parent, String name, String text ) {

        assertNotNull( parent );
        assertNotNull( name );
        assertNotNull( text );

        if ( !"attributeList".equals( parent.getNodeName() ) ) {
            fail( "You must give a tag <attributeList>, not <" + parent.getNodeName() + ">." );
        }

        NodeList list = parent.getChildNodes();
        for ( int i = 0; i < list.getLength(); i++ ) {
            Element attribute = (Element) list.item( i );

            if ( "attribute".equals( attribute.getNodeName() ) ) {

                if ( name.equals( attribute.getAttribute( "name" ) ) ) {
                    if ( text.equals( getTextFromElement( attribute ) ) ) {

                        // found it
                        return;
                    }
                }

            } else {

                fail( "We should not find other tags than <attribute> in here: " + attribute.getNodeName() );
            }
        }

        StringBuffer sb = new StringBuffer( 128 );
        sb.append( "Could not find an attribute having the following attributes: " );
        sb.append( "name=" ).append( name ).append( ", " );
        sb.append( "text=" ).append( text );

        fail( sb.toString() );
    }

    private void assertSubTextNodeUnique( Element parent, String nodeName, String text ) {

        assertNotNull( parent );
        assertNotNull( nodeName );
        assertNotNull( text );

        NodeList list = parent.getElementsByTagName( nodeName );
        assertNotNull( list );
        assertEquals( 1, list.getLength() );

        Element element = (Element) list.item( 0 );
        assertNotNull( element );
        assertEquals( text, getTextFromElement( element ) );
    }

    private void assertSubTextNodeMultiple( Element parent, String nodeName, String text ) {

        assertNotNull( parent );
        assertNotNull( nodeName );
        assertNotNull( text );

        NodeList list = parent.getElementsByTagName( nodeName );
        assertNotNull( list );

        for ( int i = 0; i < list.getLength(); i++ ) {

            Element element = (Element) list.item( i );
            assertNotNull( element );

            if ( text.equals( getTextFromElement( element ) ) ) {
                return;
            }
        }

        if ( list.getLength() > 0 ) {
            fail( "Could not find the Node: " + nodeName + " having the value: '" + text + "'." );
        } else {
            fail( "Could not find a node " + nodeName + " under " + parent.getNodeName() );
        }
    }

    //////////////////////////////
    // IntAct Utility methods

    protected static void addPsiReference( CvObject cvObject, String psiId ) {

        cvObject.addXref( new CvObjectXref( owner, psi, psiId, null, null, identity ) );
    }

    protected static CvObject createCvObject( Class clazz, String shortlabel, String fullname, String psiID ) {

        CvObject cvObject = null;

        Class[] intArgsClass = new Class[]{ Institution.class, String.class };
        Object[] intArgs = new Object[]{ owner, shortlabel };
        Constructor intArgsConstructor;

        try {

            intArgsConstructor = clazz.getConstructor( intArgsClass );
            cvObject = (CvObject) intArgsConstructor.newInstance( intArgs );

            if ( fullname != null && !"".equals( fullname.trim() ) ) {
                cvObject.setFullName( fullname );
            }

            if ( psiID != null && !"".equals( psiID.trim() ) ) {
                cvObject.addXref( new CvObjectXref( owner, psi, psiID, null, null, identity ) );
            }

        } catch ( NoSuchMethodException e ) {
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            e.printStackTrace();
        } catch ( InvocationTargetException e ) {
            e.printStackTrace();
        } catch ( InstantiationException e ) {
            e.printStackTrace();
        }

        return cvObject;
    }

    //////////////////////////////////////////////////////
    // test of the methods used in the test methods...

    private static boolean alreadyDone = false;

    public void testCreateCvObject() {

        // TODO isolate that test that it is done only once.
        // currently all classes extending it are running that test too !

        if ( !alreadyDone ) {
            CvCellType cellType = (CvCellType) createCvObject( CvCellType.class, "ct", "cell type", "MI:0001" );
            assertNotNull( cellType );
            assertEquals( "ct", cellType.getShortLabel() );
            assertEquals( "cell type", cellType.getFullName() );
            assertEquals( 1, cellType.getXrefs().size() );

            assertEquals( 1, cellType.getXrefs().size() );
            Xref xref = (Xref) cellType.getXrefs().iterator().next();
            assertNotNull( xref );
            assertEquals( psi, xref.getCvDatabase() );
            assertEquals( "MI:0001", xref.getPrimaryId() );

            alreadyDone = true;
        }
    }
}