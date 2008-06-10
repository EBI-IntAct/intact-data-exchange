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
package uk.ac.ebi.intact.dataexchange.cvutils.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obo.datamodel.*;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.util.AliasUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Contains methods to build the CvObject from OBOSession object. Basically
 * iterates through all the terms in the OBO file and converts all the valid
 * terms into CvObject
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class CvObjectOntologyBuilder {


    private static final Log log = LogFactory.getLog( CvObjectOntologyBuilder.class );

    public static Map<String, Class> mi2Class;
    private Map<String, CvObject> processed;


    private static final String MI_ROOT_IDENTIFIER = "MI:0000";
    private static final String ALIAS_IDENTIFIER = "PSI-MI-alternate";
    private static final String SHORTLABEL_IDENTIFIER = "PSI-MI-short";

    private static OBOSession oboSession = null;


    private List<CvDagObject> allValidCvs;

    private CvDatabase nonMiCvDatabase;


    static {
        mi2Class = new HashMap<String, Class>();
        // DAG objects:  A Hashmap of MI and the CV(Type).class
        mi2Class.put( "MI:0001", CvInteraction.class );
        mi2Class.put( "MI:0190", CvInteractionType.class );
        mi2Class.put( "MI:0002", CvIdentification.class );
        mi2Class.put( "MI:0003", CvFeatureIdentification.class );
        mi2Class.put( "MI:0116", CvFeatureType.class );
        mi2Class.put( "MI:0313", CvInteractorType.class );
        mi2Class.put( "MI:0346", CvExperimentalPreparation.class );
        mi2Class.put( "MI:0333", CvFuzzyType.class );
        mi2Class.put( "MI:0353", CvXrefQualifier.class );
        mi2Class.put( "MI:0444", CvDatabase.class );
        mi2Class.put( "MI:0495", CvExperimentalRole.class );
        mi2Class.put( "MI:0500", CvBiologicalRole.class );
        mi2Class.put( "MI:0300", CvAliasType.class );
        mi2Class.put( "MI:0590", CvTopic.class );
        mi2Class.put( "MI:0640", CvParameterType.class );
        mi2Class.put( "MI:0647", CvParameterUnit.class );
    }


   
    public CvObjectOntologyBuilder() {

    }//end constructor


    public CvObjectOntologyBuilder( OBOSession oboSession_ ) {
        oboSession = oboSession_;
        this.nonMiCvDatabase = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(),
                                                             CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT );
        this.processed = new HashMap<String, CvObject>();

    }

    /*
    *   The main method which converts the OBOOBject
    *   toCVObject.
    *
    * */
    public <T extends CvObject> T toCvObject( OBOObject oboObj ) {
        T cvObject;
        try {
            if ( log.isDebugEnabled() ) log.debug( "ID    ->" + oboObj.getID() );
            if ( log.isDebugEnabled() ) log.debug( "Name  ->" + oboObj.getName() );

            //find the CvClass for any given MI identifier
            Class<T> cvClass = findCvClassforMI( oboObj.getID() );

            //if CvClass is null then CvTopic.class is taken as default
            if ( cvClass == null ) {
                cvClass = ( Class<T> ) CvTopic.class;

            }
            if ( log.isDebugEnabled() ) log.debug( "cvClass ->" + cvClass.getName() );

            //Checks if the given object is already processed. If so, returns the CvObject
            String processedKey = cvKey( cvClass, oboObj.getID() );


            if ( processed.containsKey( processedKey ) ) {
                return ( T ) processed.get( processedKey );
            }

            final Institution institution = IntactContext.getCurrentInstance().getInstitution();

            //Short label look for EXACT PSI-MI-short  in synonym tag OBO 1.2
            String shortLabel = calculateShortLabel( oboObj );
            cvObject = CvObjectUtils.createCvObject( institution, cvClass, null, shortLabel );
            if ( log.isDebugEnabled() ) log.debug( "shortLabel     ->" + shortLabel );

            cvObject.addXref( createIdentityXref( cvObject, oboObj.getID() ) );
            cvObject.setFullName( oboObj.getName() );

            /********************************
             *Database and Qualifier Cv
             ********************************/
            Set<Dbxref> defDbXrefSet = oboObj.getDefDbxrefs();
            Object[] dbxrefArray = defDbXrefSet.toArray();

            //check if Unique Resid
            boolean uniqueResid;
            uniqueResid = checkIfUniqueResid( dbxrefArray );

            if ( dbxrefArray != null ) {
                CvObjectXref xref;
                //more than one dbxreference
                //add the first one
                boolean firstDatabasexref = false;
                int firstPubmedIndex = getPubmedIndex( dbxrefArray );

                //add  xrefs
                for ( int i = 0; i < dbxrefArray.length; i++ ) {

                    if ( i == firstPubmedIndex ) {
                        firstDatabasexref = true;
                    }
                    Dbxref defDbxref = ( Dbxref ) dbxrefArray[i];
                    CvTermXref cvtermXref = addQualifiersForOtherDbXreferences( defDbxref, firstDatabasexref, uniqueResid );
                    xref = toXref( cvObject, cvtermXref.getId(), cvtermXref.getQualifier(), cvtermXref.getDatabase() );
                    if ( xref != null ) {
                        cvObject.addXref( xref );
                    }//end inner if

                }//end for
            }  //end elseif
            else {
                log.debug( "No dbxreference" );
            }

            /********************************
             *Definitions
             ********************************/

            // definition
            if ( oboObj.getDefinition() != null ) {

                String definition = oboObj.getDefinition();

                if ( definition.contains( "\n" ) ) {
                    String[] defArray = definition.split( "\n" );

                    if ( defArray.length == 2 ) {
                        String prefixString = defArray[0];
                        String suffixString = defArray[1];

                        if ( suffixString.startsWith( "OBSOLETE" ) || oboObj.isObsolete() ) {


                            Annotation annot = toAnnotation( CvTopic.OBSOLETE, suffixString );
                            if ( annot != null ) {
                                cvObject.addAnnotation( annot );
                            }
                            CvTopic definitionTopicDef = CvObjectUtils.createCvObject( institution, CvTopic.class, null, CvTopic.DEFINITION );
                            cvObject.addAnnotation( new Annotation( institution, definitionTopicDef, prefixString ) );
                        } else if ( suffixString.startsWith( "http" ) ) {

                            Annotation annot = toAnnotation( CvTopic.URL, suffixString );
                            if ( annot != null ) {
                                cvObject.addAnnotation( annot );
                            }
                            CvTopic definitionTopicDef = CvObjectUtils.createCvObject( institution, CvTopic.class, null, CvTopic.DEFINITION );
                            cvObject.addAnnotation( new Annotation( institution, definitionTopicDef, prefixString ) );

                        } else {
                            if ( log.isDebugEnabled() ) log.debug( " New format " + suffixString );
                        }
                    } else {
                        if ( log.isDebugEnabled() ) log.debug( "-----something wrong here check------" );
                    }

                }//end outer if
                else {

                    CvTopic definitionTopic = CvObjectUtils.createCvObject( institution, CvTopic.class, null, CvTopic.DEFINITION );
                    cvObject.addAnnotation( new Annotation( institution, definitionTopic, oboObj.getDefinition() ) );
                }
            }  //end of definition

            /********************************
             *XREF ANNOTATIONS
             ********************************/
            Set<Dbxref> dbxrefSet = oboObj.getDbxrefs();
            for ( Dbxref dbxref : dbxrefSet ) {

                String xref = dbxref.toString();
                if ( xref.contains( CvTopic.XREF_VALIDATION_REGEXP ) ) {

                    Annotation annot = toAnnotation( CvTopic.XREF_VALIDATION_REGEXP, xref.split( ":" )[1] );
                    if ( annot != null ) {
                        cvObject.addAnnotation( annot );
                    }
                }//end if
                if ( xref.contains( CvTopic.SEARCH_URL ) ) {

                    Annotation annot = toAnnotation( CvTopic.SEARCH_URL, dbxref.getDesc() );
                    if ( annot != null ) {
                        cvObject.addAnnotation( annot );
                    }
                } //end if

            }  //end for

            /********************************
             *comment
             ********************************/
            if ( oboObj.getComment() != null && oboObj.getComment().length() > 0 ) {

                Annotation annot = toAnnotation( CvTopic.COMMENT, oboObj.getComment() );
                if ( annot != null ) {
                    cvObject.addAnnotation( annot );
                }
            } //end comment

            /********************************
             *Alias
             ********************************/
            Set<Synonym> syn = oboObj.getSynonyms();
            CvObjectAlias alias_;
            for ( Synonym aSyn : syn ) {
                String aliasName;

                SynonymCategory synCat = aSyn.getSynonymCategory();


                if ( synCat.getID() != null && synCat.getID().equalsIgnoreCase( CvObjectOntologyBuilder.ALIAS_IDENTIFIER ) ) {
                    aliasName = aSyn.getText();

                    alias_ = ( CvObjectAlias ) toAlias( cvObject, aliasName );
                    cvObject.addAlias( alias_ );
                } //end if
            } //end for


            processed.put( processedKey, cvObject );
            log.debug( "--Processed size " + processed.size() );


            if ( cvObject instanceof CvDagObject ) {
                Collection<Link> childLinks = oboObj.getChildren();

                for ( Link childLink1 : childLinks ) {


                    Pattern p = Pattern.compile( "(MI:\\d+)-OBO_REL:is_a->(MI:\\d+)" );
                    Matcher m = p.matcher( childLink1.getID() );
                    if ( m.matches() ) {

                        if ( m.group( 2 ).equalsIgnoreCase( oboObj.getID() ) ) {
                            CvDagObject dagObject = ( CvDagObject ) cvObject;
                            OBOObject childObj = ( OBOObject ) oboSession.getObject( m.group( 1 ) );
                            dagObject.addChild( ( CvDagObject ) toCvObject( childObj ) );
                        }//end if
                    }//end matches
                } //end for
            }//end if

        } catch ( Exception ex ) {
            ex.printStackTrace();
            throw new IntactException( "Exception converting to CvObject from OBOObject: " + oboObj.getID(), ex );
        }

        return cvObject;
    }    //end method

    private int getPubmedIndex( Object[] dbxrefArray ) {
        int index = 0;
        for ( int i = 0; i < dbxrefArray.length; i++ ) {
            Dbxref defDbxref = ( Dbxref ) dbxrefArray[i];

            if ( defDbxref.getDatabase().equalsIgnoreCase( "PMID" ) ) {

                return i;
            }

        }//end for
        return index;
    } //end method

    private boolean checkIfUniqueResid( Object[] dbxrefArray ) {
        int countResid = 0;
        for ( Object aDbxrefArray : dbxrefArray ) {
            Dbxref defDbxref = ( Dbxref ) aDbxrefArray;
            if ( defDbxref.getDatabase().equalsIgnoreCase( "RESID" ) ) {
                countResid++;
            }//end if

        }//end for
        return countResid == 1;

    }//end method

    private <T extends CvObject> String cvKey( Class<T> cvClass, String primaryId ) {
        return cvClass.getSimpleName() + ":" + primaryId;
    }

    /*
    * A recursive method that traverse the tree up to fetch the parent Class
    * If the Class is not found incase of Obsolete entries where is_a relationship
    * is not given, then null is returned
    * */
    protected <T extends CvObject> Class<T> findCvClassforMI( String id ) {


        Class<T> cvClass;
        cvClass = mi2Class.get( id );
        if ( cvClass != null ) {
            //then it is one of rootCv
            return cvClass;
        } else {
            OBOObject oboObj = ( OBOObject ) oboSession.getObject( id );
            Collection<Link> parentLinks = oboObj.getParents();
            for ( Link parentLink : parentLinks ) {


                String miIdentifierRight = parseToGetRightMI( parentLink.getID() );//eg: MI:0436-OBO_REL:is_a->MI:0659
                String miIdentifierLeft = parseToGetLeftMI( parentLink.getID() );//eg: MI:0436-OBO_REL:is_a->MI:0659


                if ( miIdentifierLeft != null && miIdentifierRight != null && miIdentifierLeft.equalsIgnoreCase( oboObj.getID() ) ) {

                    cvClass = mi2Class.get( miIdentifierRight );
                    if ( cvClass != null ) {
                        //then it is one of rootCv
                        return cvClass;
                    }//end if
                    else {
                        return findCvClassforMI( miIdentifierRight );
                    }
                }//end if

            }  //end for

        }
        return cvClass;
    } //end method

    /*
    *  Parses the given String and returns the MI identifier in
    * the left side of is_a
    *
    */
    private String parseToGetLeftMI( String relationString ) {
        Pattern p = Pattern.compile( "(MI:\\d+)-OBO_REL:is_a->(MI:\\d+)" );
        Matcher m = p.matcher( relationString );
        if ( m.matches() ) {
            return m.group( 1 );
        }//end matches
        return null;
    }//end method

    /*
    *  Parses the given String and returns the MI identifier in
    * the right side of is_a
    *
    */
    private String parseToGetRightMI( String relationString ) {
        Pattern p = Pattern.compile( "(MI:\\d+)-OBO_REL:is_a->(MI:\\d+)" );
        Matcher m = p.matcher( relationString );
        if ( m.matches() ) {
            return m.group( 2 );
        }//end matches
        return null;
    } //end method


    /*
   * This method is called if the Def: line has more than one db xreference.
   * The qualifiers are assigned according to the rules set
   *
   * */
    protected CvTermXref addQualifiersForOtherDbXreferences( Dbxref defDbxref, boolean firstDbxref, boolean uniqResid ) {

        String database, qualifier, identifier;

        if ( defDbxref == null ) {
            throw new NullPointerException( "defDbxref is null" );
        }

        if ( log.isDebugEnabled() )
            log.debug( "defDbxref: " + defDbxref.getDatabase() + "defDbxref ID: " + defDbxref.getDatabaseID() );

        if ( defDbxref.getDatabase().equalsIgnoreCase( "PMID" ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.PUBMED;

            if ( firstDbxref ) {
                qualifier = CvXrefQualifier.PRIMARY_REFERENCE;
            } else {
                qualifier = CvXrefQualifier.METHOD_REFERENCE;
            }
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( "PMID for application instance" ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.PUBMED;
            qualifier = CvXrefQualifier.SEE_ALSO;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( CvDatabase.GO ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.GO;
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( CvDatabase.RESID ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.RESID;

            if ( uniqResid ) {
                qualifier = CvXrefQualifier.IDENTITY;
            } else {
                qualifier = CvXrefQualifier.SEE_ALSO;
            }
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( CvDatabase.SO ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.SO;
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( "MOD" ) ) {
            identifier = defDbxref.getDatabaseID();
            database = "MOD";
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( "UNIMOD" ) ) {
            identifier = defDbxref.getDatabaseID();
            database = "UNIMOD";
            qualifier = CvXrefQualifier.IDENTITY;    //check later
        } else {
            throw new IllegalArgumentException( "Unknown database: " + defDbxref.getDatabaseID() + " (" + defDbxref.getDatabase() + ")" );
        }


        if ( log.isDebugEnabled() )
            log.debug( "Returning identifier:  " + identifier + "  " + "  database: " + database + "  qualifier:  " + qualifier );
        return new CvTermXref( identifier, database, qualifier );

    }   //end method

    /*
   * This method returns the shortLabel for the term
   * If the name length is less than 20 characters, name is returned
   * If it is more than 20 characters, then the synonyms with EXACT PSI-MI-short is taken
   *
   * */

    private String calculateShortLabel( OBOObject oboObj ) {
        String shortLabel = null;
        Set<Synonym> syn = oboObj.getSynonyms();
        for ( Synonym synonym : syn ) {
            SynonymCategory synCat = synonym.getSynonymCategory();

            if ( synCat.getID() != null && synCat.getID().equalsIgnoreCase( CvObjectOntologyBuilder.SHORTLABEL_IDENTIFIER ) ) {
                shortLabel = synonym.getText();
                //another check just to reduce the length to 20 characters--rarely happens
                if ( shortLabel != null && shortLabel.length() > 20 ) {

                    shortLabel = shortLabel.substring( 0, 20 );
                }//end if
            }//end for
        } //end for


        if ( shortLabel == null ) {
            if ( oboObj.getName() != null && oboObj.getName().length() <= 20 ) {
                return oboObj.getName();
            } else if ( oboObj.getName() != null && oboObj.getName().length() > 20 ) {
                if ( log.isDebugEnabled() ) log.debug( "No shortLabel for " + oboObj.getName() );
                return oboObj.getName().substring( 0, 20 );
            }
        }
        return shortLabel;
    }  //end method


    protected CvObjectXref createIdentityXref( CvObject parent, String id ) {
        CvObjectXref idXref;


        if ( id != null && id.startsWith( "MI" ) ) {
            idXref = XrefUtils.createIdentityXrefPsiMi( parent, id );
            idXref.prepareParentMi();
        } else {
            idXref = XrefUtils.createIdentityXref( parent, id, nonMiCvDatabase );
        }

        return idXref;
    } //end method


    public Collection<IdentifiedObject> getAllMIOBOObjects() {
        int obsoleteCounter = 0;

        ArrayList<IdentifiedObject> allMIObjects = new ArrayList<IdentifiedObject>();
        Collection<IdentifiedObject> allOBOObjects = oboSession.getObjects();

        for ( IdentifiedObject identifiedObject : allOBOObjects ) {
            if ( identifiedObject.getID().equalsIgnoreCase( MI_ROOT_IDENTIFIER ) ) {
                continue;
            }
            if ( identifiedObject.getID().startsWith( "MI:" ) ) {
                if ( identifiedObject instanceof OBOObject ) {
                    OBOObject obj = ( OBOObject ) identifiedObject;
                    if ( obj.isObsolete() )
                        obsoleteCounter++;

                    allMIObjects.add( identifiedObject );
                }//end if
            }  //end if
        }//end for

        log.info( "obsoleteCounter  " + obsoleteCounter );
        return allMIObjects;
    }//end method

    public Collection<IdentifiedObject> getAllOBOObjects() {
        Collection<IdentifiedObject> allOBOObjects = oboSession.getObjects();
        Collection<IdentifiedObject> allOBOObjectsCol = new ArrayList<IdentifiedObject>();

        for ( IdentifiedObject identifiedObject : allOBOObjects ) {
            if ( identifiedObject instanceof OBOObject ) {
                allOBOObjectsCol.add( identifiedObject );
            } else {
                log.debug( "----Not Oboobject---" + identifiedObject.getID() );
            }
        }
        return allOBOObjectsCol;
    } //end method

    public Collection<IdentifiedObject> getObsoleteOBOObjects() {
        Collection<IdentifiedObject> ObsoloteOboObjects = new ArrayList<IdentifiedObject>();
        Collection<IdentifiedObject> allOBOObjects = oboSession.getObjects();

        for ( IdentifiedObject identifiedObject : allOBOObjects ) {
            if ( identifiedObject.getID().startsWith( "MI:" ) ) {
                if ( identifiedObject instanceof OBOObject ) {
                    OBOObject obj = ( OBOObject ) identifiedObject;
                    String defText = obj.getDefinition();
                    if ( obj.isObsolete() || defText.contains( "\nOBSOLETE" ) ) {
                        ObsoloteOboObjects.add( identifiedObject );
                    } //end if
                } //end if
            } //end if
        } //end for
        return ObsoloteOboObjects;
    }//end of method

    public Collection<IdentifiedObject> getInvalidOBOObjects() {
        Collection<IdentifiedObject> invalidOboObjects = new ArrayList<IdentifiedObject>();
        Collection<IdentifiedObject> allOBOObjects = oboSession.getObjects();
        for ( IdentifiedObject identifiedObject : allOBOObjects ) {

            if ( identifiedObject instanceof OBOObject ) {
                if ( !identifiedObject.getID().startsWith( "MI:" ) ) {
                    invalidOboObjects.add( identifiedObject );
                    if ( log.isDebugEnabled() )
                        log.debug( "invalidCv## " + identifiedObject.getID() + "  " + identifiedObject.getName() );
                }

            } //end if

        } //end for
        return invalidOboObjects;
    }//end of method


    public Collection<IdentifiedObject> getOrphanOBOObjects() {
        Collection<IdentifiedObject> orphanOboObjects = new ArrayList<IdentifiedObject>();
        Collection<IdentifiedObject> allOBOObjects = oboSession.getObjects();

        for ( IdentifiedObject identifiedObject : allOBOObjects ) {
            if ( identifiedObject.getID().startsWith( "MI:" ) ) {
                if ( identifiedObject instanceof OBOObject ) {
                    OBOObject obj = ( OBOObject ) identifiedObject;
                    Class<? extends CvObject> cvClass = findCvClassforMI( obj.getID() );
                    //if CvClass is null then no parent
                    if ( cvClass == null ) {
                        if ( !identifiedObject.getID().startsWith( MI_ROOT_IDENTIFIER ) ) {
                            orphanOboObjects.add( identifiedObject );
                        }

                    } //end if

                } //end if
            } //end if
        } //end for
        return orphanOboObjects;
    }//end of method

    public Collection<IdentifiedObject> getRootOBOObjects() {
        ArrayList<IdentifiedObject> rootOboObjects = new ArrayList<IdentifiedObject>();

        OBOObject rootObj = ( OBOObject ) oboSession.getObject( MI_ROOT_IDENTIFIER );
        Collection<Link> childLinks = rootObj.getChildren();

        for ( Link childLink : childLinks ) {


            Pattern p = Pattern.compile( "(MI:\\d+)-part_of->(MI:\\d+)" );
            Matcher m = p.matcher( childLink.getID() );
            if ( m.matches() ) {

                if ( m.group( 2 ).equalsIgnoreCase( MI_ROOT_IDENTIFIER ) ) {
                    rootOboObjects.add( oboSession.getObject( m.group( 1 ) ) );
                }

            }//end matches

        } //end for
        log.debug( "oboObjects " + rootOboObjects.size() );
        return rootOboObjects;
    }//end method


    private CvObjectXref toXref( CvObject cvObj, String identifier, String qualifier, String database ) {
        log.debug( "from toXref " + identifier + "   " + qualifier + "   " + database );

        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        if ( identifier == null ) {
            throw new IllegalArgumentException( "To create a xref, the identifier cannot be null" );
        }

        CvXrefQualifier qualifierCv = null;

        if ( qualifier != null ) {
            qualifierCv = getCvObjectByLabel( CvXrefQualifier.class, qualifier );
        } else {
            if ( log.isWarnEnabled() ) log.warn( "No qualifier label found for: " + identifier );
        }

        CvDatabase databaseCv = getCvObjectByLabel( CvDatabase.class, database );

        if ( log.isDebugEnabled() ) log.debug( "qualifierCv  " + qualifierCv + "   databaseCv  " + databaseCv );


        if ( qualifierCv == null || databaseCv == null ) {
            if ( CvDatabase.PUBMED.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED );
            } else if ( CvDatabase.GO.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO );
                identifier = "GO:"+identifier;
            } else if ( CvDatabase.RESID.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.SEE_ALSO_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.RESID_MI_REF, CvDatabase.RESID );
            } else if ( CvDatabase.SO.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.SO_MI_REF, CvDatabase.SO );
            } else if ( "MOD".equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, "MI:0897", "MOD" );
            } else {
                log.error( "Unexpected combination qualifier-database found on xref: " + qualifier + " - " + database );
                return null;
            }

        }//end if

        if ( log.isDebugEnabled() )
            log.debug( "Returning from toXref: identifier_: " + identifier + "  qualifierCv: " + qualifierCv + " databaseCv  " + databaseCv );
        return XrefUtils.createIdentityXref( cvObj, identifier, qualifierCv, databaseCv );
    } //end method


    protected Alias toAlias( CvObject cvobj, String aliasName ) {
        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        CvAliasType alias = getCvObjectByLabel( CvAliasType.class, CvAliasType.GO_SYNONYM );
        if ( alias == null ) {
            if ( log.isDebugEnabled() ) log.debug( "alias ==null creating new" );
            alias = CvObjectUtils.createCvObject( owner, CvAliasType.class, CvAliasType.GO_SYNONYM_MI_REF, CvAliasType.GO_SYNONYM );
        }

        return AliasUtils.createAlias( cvobj, aliasName, alias );

    } //end alias

    protected Annotation toAnnotation( String cvTopic, String annotation ) {
        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        CvTopic topic = getCvObjectByLabel( CvTopic.class, cvTopic );

        if ( topic == null ) {
            if ( CvTopic.URL.equalsIgnoreCase( cvTopic ) ) {
                topic = CvObjectUtils.createCvObject( owner, CvTopic.class, CvTopic.URL_MI_REF, CvTopic.URL );
            } else if ( CvTopic.SEARCH_URL.equalsIgnoreCase( cvTopic ) ) {
                topic = CvObjectUtils.createCvObject( owner, CvTopic.class, CvTopic.SEARCH_URL_MI_REF, CvTopic.SEARCH_URL );
            } else if ( CvTopic.XREF_VALIDATION_REGEXP.equalsIgnoreCase( cvTopic ) ) {
                topic = CvObjectUtils.createCvObject( owner, CvTopic.class, CvTopic.XREF_VALIDATION_REGEXP_MI_REF, CvTopic.XREF_VALIDATION_REGEXP );
            } else if ( CvTopic.COMMENT.equalsIgnoreCase( cvTopic ) ) {
                topic = CvObjectUtils.createCvObject( owner, CvTopic.class, CvTopic.COMMENT_MI_REF, CvTopic.COMMENT );
            } else if ( CvTopic.OBSOLETE.equalsIgnoreCase( cvTopic ) ) {
                topic = CvObjectUtils.createCvObject( owner, CvTopic.class, CvTopic.OBSOLETE_MI_REF, CvTopic.OBSOLETE );
                topic.setFullName( CvTopic.OBSOLETE );
            } else {
                log.error( "Unexpected topic found on annotation: " + cvTopic );
                return null;
            }
        }

        log.debug( "Returning from toAnnotation: owner: " + owner + "  topic: " + topic + " annotation  " + annotation );
        return new Annotation( owner, topic, annotation );
    }//end method


    protected <T extends CvObject> T getCvObjectByLabel( Class<T> cvObjectClass, String label ) {

        if ( label == null ) {
            throw new NullPointerException( "label is null" );
        }

        if (log.isDebugEnabled()) log.debug( "Processed values size: " + processed.size() );

        for ( CvObject cvObject : processed.values() ) {


            if ( cvObjectClass.isAssignableFrom( cvObject.getClass() ) && label.equals( cvObject.getShortLabel() ) ) {

                return ( T ) cvObject;
            }
        }

        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao( cvObjectClass ).getByShortLabel( cvObjectClass, label );
    }  //end method


    public Collection<CvDagObject> getAllValidCvs() {


        List<CvObject> validCvs = new ArrayList<CvObject>();
        Collection<IdentifiedObject> rootOboObjects = getRootOBOObjects();

        for ( IdentifiedObject rootOboObject : rootOboObjects ) {
            OBOObject rootObject = ( OBOObject ) rootOboObject;
            CvObject cvObjectRoot = toCvObject( rootObject );
            validCvs.add( cvObjectRoot );

        }//end for

        if ( log.isDebugEnabled() ) log.debug( "validCvs size :" + validCvs.size() );

        allValidCvs = new ArrayList<CvDagObject>();
        for ( CvObject validCv : validCvs ) {
            allValidCvs.addAll( itselfAndChildrenAsList( ( CvDagObject ) validCv ) );
        }

        return new HashSet<CvDagObject>( allValidCvs );


    } //end of method


    public List<CvDagObject> getAllCvs() {

        List<CvDagObject> allCvs = new ArrayList<CvDagObject>();
        //until here
        List<CvObject> rootsAndChildren = new ArrayList<CvObject>();
        Collection<IdentifiedObject> rootOboObjects = getRootOBOObjects();

        for ( IdentifiedObject rootOboObject : rootOboObjects ) {
            OBOObject rootObject = ( OBOObject ) rootOboObject;
            CvObject cvObjectRoot = toCvObject( rootObject );
            rootsAndChildren.add( cvObjectRoot );

        }//end for

        if (log.isDebugEnabled()) log.debug( "Roots and children size :" + rootsAndChildren.size() );

        for ( CvObject validCv : rootsAndChildren ) {
            allCvs.addAll( itselfAndChildrenAsList( ( CvDagObject ) validCv ) );
        }

        for ( IdentifiedObject orphanObo : getOrphanOBOObjects() ) {
            if ( orphanObo instanceof OBOObject ) {
                OBOObject orphanObj = ( OBOObject ) orphanObo;
                CvObject cvOrphan = toCvObject( orphanObj );
                allCvs.addAll( itselfAndChildrenAsList( ( CvDagObject ) cvOrphan ) );

            }


        }//end for


        allCvs = new ArrayList<CvDagObject>( new HashSet<CvDagObject>( allCvs ) );

        if (log.isDebugEnabled()) log.debug( "Size of the collection with all CVs: " + allCvs.size() );


        // put identity in the first position, to avoid recursivity problems
        // put identity on top
        LinkedList<CvDagObject> orderedList = new LinkedList<CvDagObject>();
        for (CvDagObject cv : allCvs) {
            if (CvXrefQualifier.IDENTITY_MI_REF.equals(cv.getMiIdentifier())) {
                orderedList.addFirst(cv);
            } else {
                orderedList.add(cv);
            }
        }

        //until here
        return orderedList;


    } //end of method

    private List<CvDagObject> itselfAndChildrenAsList( CvDagObject cv ) {
        List<CvDagObject> itselfAndChildren = new ArrayList<CvDagObject>();
        itselfAndChildren.add( cv );

        for ( CvDagObject child : cv.getChildren() ) {
            itselfAndChildren.addAll( itselfAndChildrenAsList( child ) );
        }

        return itselfAndChildren;
    }  //end method


    public int getInvalidTermCount() {
        //Invalid Terms
        int invalidCounter = 0;
        for ( IdentifiedObject invalidObo : getInvalidOBOObjects() ) {
            if ( invalidObo instanceof OBOObject ) {
                invalidCounter++;
            }
        } //

        return invalidCounter;
    }  //end method


    public List<CvObject> getOrphanCvObjects() {

        List<CvObject> orphanList = new ArrayList<CvObject>();
        for ( IdentifiedObject orphanObo : getOrphanOBOObjects() ) {
            if ( orphanObo instanceof OBOObject ) {
                OBOObject orphanObj = ( OBOObject ) orphanObo;
                CvObject cvOrphan = toCvObject( orphanObj );
                orphanList.add( cvOrphan );
            }
        }//end for
        //until here
        return orphanList;

    } //end method


}//end class
