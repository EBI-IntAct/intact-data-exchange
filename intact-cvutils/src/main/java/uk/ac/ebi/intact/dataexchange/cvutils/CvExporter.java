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
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.datamodel.*;
import org.obo.datamodel.impl.*;
import uk.ac.ebi.intact.dataexchange.cvutils.model.CvObjectOntologyBuilder;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import static java.util.Collections.sort;


/**
 * Contains methods to recreate the OBOSession object from a list of CVObjects
 * The CVObject is stripped and a OBOObject is created which is then added
 * to the OBOSession and finally written to an OBO 1.2 file using a DataAdapter
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class CvExporter {

    //initialize logger
    protected final static Logger log = Logger.getLogger( CvExporter.class );

    private static final String ALIAS_IDENTIFIER = "PSI-MI-alternate";
    private static final String SHORTLABEL_IDENTIFIER = "PSI-MI-short";

    public CvDatabase psi = null;
    public CvDatabase intact = null;
    public CvXrefQualifier identity = null;
    public CvTopic definitionTopic = null;
    public CvTopic obsolete = null;


    private static OBOSession oboSession;

    public CvExporter() {

    } //end constructor

    static {
        ObjectFactory objFactory;
        objFactory = new DefaultObjectFactory();
        oboSession = new OBOSessionImpl( objFactory );
    }

    /**
     * Converts a list of Cvs to list of OBOObjects and add it to the OBOSession
     *
     * @param allCvs List of all Cvs
     * @return OBOSession objects with all Cvs converted to OBOObject and added to the OBOsession
     */

    public OBOSession convertCvList2OBOSession( List<CvDagObject> allCvs ) {

        // List<CvDagObject> allUniqCvs = removeCvsDuplicated( allCvs );
        List<CvDagObject> allUniqCvs;
        allUniqCvs = allCvs;

        sort( allUniqCvs, new Comparator<CvDagObject>() {
            public int compare( CvDagObject o1, CvDagObject o2 ) {

                String id1 = CvObjectUtils.getIdentity( o1 );
                String id2 = CvObjectUtils.getIdentity( o2 );

                return id1.compareTo( id2 );
            }
        } );
        int counter = 1;
        for ( CvDagObject cvDagObj : allUniqCvs ) {

            if ( CvObjectUtils.getIdentity( cvDagObj ) == null ) {
                throw new NullPointerException( "No Identifier for the cvObject " + cvDagObj );
            }
            if(log.isTraceEnabled())log.trace( counter + "  " + CvObjectUtils.getIdentity( cvDagObj ) );

            oboSession.addObject( getRootObject() );
            OBOClass oboObj = convertCv2OBO( cvDagObj );
            oboSession.addObject( oboObj );

            counter++;
        }  //end of for

        addHeaderInfo();
        addFooterInfo();


        return oboSession;
    }//end method


    public void addObject( OBOClass oboObj ) {
        oboSession.addObject( oboObj );
    } //end method


    /**
     * The OBOFileAdapter writes the OBOSession object in to the given file specified
     *
     * @param oboSession The OBOsession object with all OBOClass instances added to it
     * @param outFile    The OBO file
     * @throws DataAdapterException refer org.bbop.dataadapter.DataAdapterException
     * @throws IOException          refer java.io.IOException
     */
    public void writeOBOFile( OBOSession oboSession, File outFile ) throws DataAdapterException, IOException {

        final OBOFileAdapter.OBOAdapterConfiguration config = new OBOFileAdapter.OBOAdapterConfiguration();
        config.setWritePath( outFile.getAbsolutePath() );
        OBOFileAdapter adapter = new OBOFileAdapter();
        adapter.doOperation( OBOFileAdapter.WRITE_ONTOLOGY, config, oboSession );

    }//end method


    private void addHeaderInfo() {


        oboSession.setDefaultNamespace( new Namespace( "PSI-MI" ) );
        //extend it more when u find the right methods

    }

    private void addFooterInfo() {
        //todo if necessary

    }

    /**
     * The List contains duplicates as the method itselfAndChildrenAsList adds
     * itself and the children and again the child gets added.
     * This method removes the dubplicates from the list
     *
     * @param allCvs List of all Cvs with duplicates
     * @return Lists of Uniq Cvs
     */

    public List<CvDagObject> removeCvsDuplicated( List<CvDagObject> allCvs ) {

        HashMap<String, CvDagObject> cvHash = new HashMap<String, CvDagObject>();
        List<CvDagObject> allUniqCvs = new ArrayList<CvDagObject>();
        for ( CvDagObject cvObj : allCvs ) {
            cvHash.put( CvObjectUtils.getIdentity( cvObj ), cvObj );
        }


        for ( String s : cvHash.keySet() ) {
            CvDagObject cvDagObject = cvHash.get( s );
            allUniqCvs.add( cvDagObject );
        }

        return allUniqCvs;
    }//end of method

    /**
     * Converts cvobject to OBOobject
     *
     * @param cvObj CvObject that needs to be converted to OBOOBject
     * @return a OBOClass instance
     */

    public OBOClass convertCv2OBO( CvObject cvObj ) {

        OBOClass oboObj = null;

        if ( cvObj instanceof CvDagObject ) {
            CvDagObject dagObj = ( CvDagObject ) cvObj;
            if ( CvObjectUtils.getIdentity( dagObj ) == null ) {
                throw new NullPointerException( "Identifier is null" );
            }

            oboObj = new OBOClassImpl( dagObj.getFullName(), CvObjectUtils.getIdentity( dagObj ) );
            //assign short label

            if ( dagObj.getShortLabel() != null ) {
                Synonym syn = createSynonym( dagObj.getShortLabel() );
                oboObj.addSynonym( syn );
            }

            //assign Xrefs
            Collection<CvObjectXref> xrefs = dagObj.getXrefs();


            for ( CvObjectXref xref : xrefs ) {
                boolean isIdentity = false;
                CvXrefQualifier qualifier = xref.getCvXrefQualifier();
                CvDatabase database = xref.getCvDatabase();
                String qualMi;
                String dbMi;

                if ( qualifier != null && database != null &&
                     ( qualMi = CvObjectUtils.getIdentity( qualifier ) ) != null &&
                     ( dbMi = CvObjectUtils.getIdentity( database ) ) != null &&
                     qualMi.equals( CvXrefQualifier.IDENTITY_MI_REF ) &&
                     dbMi.equals( CvDatabase.PSI_MI_MI_REF ) ) {
                    isIdentity = true;
                }//end if

                if ( !isIdentity ) {

                    String dbx = "";

                    //check for pubmed

                    if ( database != null && database.getShortLabel() != null ) {
                        if ( database.getShortLabel().equals( CvDatabase.PUBMED ) ) {

                            dbx = "PMID";
                        } else {

                            dbx = database.getShortLabel().toUpperCase();
                        }
                    }

                    Dbxref dbxref = new DbxrefImpl( dbx, xref.getPrimaryId() );
                    dbxref.setType( Dbxref.DEFINITION );

                    oboObj.addDefDbxref( dbxref );
                }//end if

            } //end for

            //assign def   from Annotations
            Collection<Annotation> annotations = dagObj.getAnnotations();

            String definitionPrefix = "";
            String definitionSuffix = "";
            for ( Annotation annotation : annotations ) {

                if ( annotation.getCvTopic() != null && annotation.getCvTopic().getShortLabel() != null ) {
                    CvTopic cvTopic = annotation.getCvTopic();

                    if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.DEFINITION ) ) {
                        definitionPrefix = annotation.getAnnotationText();
                    } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.URL ) ) {
                        definitionSuffix = "\n" + annotation.getAnnotationText();
                    } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.SEARCH_URL ) ) {
                        String annotationText = annotation.getAnnotationText();
                       if (log.isTraceEnabled())  log.trace( "annotationText before " + annotationText );
                        annotationText = annotationText.replaceAll( "\\\\", "" );

                        annotationText = " \"" + annotationText + "\"";
                       if (log.isTraceEnabled())  log.trace( "annotationText after " + annotationText );
                        Dbxref dbxref = new DbxrefImpl( CvTopic.SEARCH_URL, annotationText );

                        oboObj.addDbxref( dbxref );
                    } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.XREF_VALIDATION_REGEXP ) ) {
                        Dbxref dbxref = new DbxrefImpl( CvTopic.XREF_VALIDATION_REGEXP, annotation.getAnnotationText() );
                        oboObj.addDbxref( dbxref );
                    } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.COMMENT ) ) {
                        oboObj.setComment( annotation.getAnnotationText() );
                    } else if ( cvTopic.getShortLabel().equalsIgnoreCase( CvTopic.OBSOLETE ) ) {
                        oboObj.setObsolete( true );
                        definitionSuffix = "\n" + annotation.getAnnotationText();
                    } else {
                       if (log.isDebugEnabled())  log.debug( "Annotation don't fit anywhere-----" );
                    }
                } //end if
            }//end for
            oboObj.setDefinition( definitionPrefix + definitionSuffix );
            //assign alias

            for ( CvObjectAlias cvAlias : dagObj.getAliases() ) {
                Synonym altSyn = createAlias( cvAlias );
                oboObj.addSynonym( altSyn );

            }

            //add children and parents
            //check if root

            if ( checkIfRootMI( CvObjectUtils.getIdentity( dagObj ) ) ) {
               
                OBOClass rootObject = getRootObject();
                Link linkToRoot = new OBORestrictionImpl( oboObj );
                OBOProperty oboProp = new OBOPropertyImpl( "part_of" );
                linkToRoot.setType( oboProp );
                rootObject.addChild( linkToRoot );
            }

            List<CvDagObject> cvParents = ( List ) dagObj.getParents();

            sort( cvParents, new Comparator<CvDagObject>() {
                public int compare( CvDagObject o1, CvDagObject o2 ) {

                    String id1 = CvObjectUtils.getIdentity( o1 );
                    String id2 = CvObjectUtils.getIdentity( o2 );

                    return id1.compareTo( id2 );
                }
            } );

            for ( CvDagObject cvParentObj : cvParents ) {

                OBOClass isA = convertCv2OBO( cvParentObj );
                Link linkToIsA = new OBORestrictionImpl( oboObj );
                linkToIsA.setType( OBOProperty.IS_A );
                isA.addChild( linkToIsA );
            }//end for


        }//outermost if

        return oboObj;
    }//end method

    private OBOClass getRootObject() {
        /*
          [Term]
          id: MI:0000
          name: molecular interaction
          def: "Controlled vocabularies originally created for protein protein interactions, extended to other molecules interactions." [PMID:14755292]
          subset: Drugable
          subset: PSI-MI slim
          synonym: "mi" EXACT PSI-MI-short []
        */

        OBOClass rootObj = new OBOClassImpl( "molecular interaction", "MI:0000" );
        rootObj.setDefinition( "Controlled vocabularies originally created for protein protein interactions, extended to other molecules interactions." );
        //[PMID:14755292]"
        Dbxref dbxref = new DbxrefImpl( "PMID", "14755292" );
        dbxref.setType( Dbxref.DEFINITION );
        rootObj.addDefDbxref( dbxref );
        Synonym syn = new SynonymImpl();
        syn.setText( "mi" );
        SynonymCategory synCat = new SynonymCategoryImpl();
        synCat.setID( SHORTLABEL_IDENTIFIER );
        syn.setSynonymCategory( synCat );
        syn.setScope( 1 );
        rootObj.addSynonym( syn );

        return rootObj;
    }//end of method


    private boolean checkIfRootMI( String mi ) {
        for ( String s : CvObjectOntologyBuilder.mi2Class.keySet() ) {
            if ( mi.equalsIgnoreCase( s ) ) {
                return true;
            } //end if
        }//end for
        return false;
    }//end method

    private Synonym createAlias( CvObjectAlias cvAlias ) {
        Synonym syn = new SynonymImpl();
        syn.setText( cvAlias.getName() );
        SynonymCategory synCat = new SynonymCategoryImpl();
        synCat.setID( ALIAS_IDENTIFIER );
        syn.setSynonymCategory( synCat );
        syn.setScope( 1 );
        return syn;
    } //end method

    private Synonym createSynonym( String shortLabel ) {
        Synonym syn = new SynonymImpl();
        syn.setText( shortLabel );
        SynonymCategory synCat = new SynonymCategoryImpl();
        synCat.setID( SHORTLABEL_IDENTIFIER );
        syn.setSynonymCategory( synCat );
        syn.setScope( 1 );
        return syn;
    } //end method

    public static OBOSession getOboSession() {
        return oboSession;
    } //end method


} //end class
