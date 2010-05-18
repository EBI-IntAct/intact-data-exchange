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
import uk.ac.ebi.intact.core.IntactException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.util.AliasUtils;
import uk.ac.ebi.intact.model.util.CvObjectUtils;
import uk.ac.ebi.intact.model.util.XrefUtils;
import uk.ac.ebi.intact.model.visitor.BaseIntactVisitor;
import uk.ac.ebi.intact.model.visitor.DefaultTraverser;
import uk.ac.ebi.intact.model.visitor.IntactObjectTraverser;
import uk.ac.ebi.intact.model.visitor.IntactVisitor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;


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
    public static Map<String, String> class2mi;

    /**
     * classname+MI -> CvObject
     */
    private Map<String, CvObject> processed;


    private static final String MI_ROOT_IDENTIFIER = "MI:0000";
    private static final String ALIAS_IDENTIFIER = "PSI-MI-alternate";
    private static final String SHORTLABEL_IDENTIFIER = "PSI-MI-short";

    private static OBOSession oboSession;


    private CvDatabase nonMiCvDatabase;
    private CvDatabase psimod;

    private Map<String, Set<String>> map4misWithMoreParent;


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
        mi2Class.put( "IAX:0001", CvTissue.class );
        mi2Class.put( "IAX:0002", CvCellType.class );

        //
        class2mi = new HashMap<String, String>();
        // DAG objects:  A Hashmap of MI and the CV(Type).class
        class2mi.put( "CvInteraction", "MI:0001" );
        class2mi.put( "CvInteractionType", "MI:0190" );
        class2mi.put( "CvIdentification", "MI:0002" );
        class2mi.put( "CvFeatureIdentification", "MI:0003" );
        class2mi.put( "CvFeatureType", "MI:0116" );
        class2mi.put( "CvInteractorType", "MI:0313" );
        class2mi.put( "CvExperimentalPreparation", "MI:0346" );
        class2mi.put( "CvFuzzyType", "MI:0333" );
        class2mi.put( "CvXrefQualifier", "MI:0353" );
        class2mi.put( "CvDatabase", "MI:0444" );
        class2mi.put( "CvExperimentalRole", "MI:0495" );
        class2mi.put( "CvBiologicalRole", "MI:0500" );
        class2mi.put( "CvAliasType", "MI:0300" );
        class2mi.put( "CvTopic", "MI:0590" );
        class2mi.put( "CvParameterType", "MI:0640" );
        class2mi.put( "CvParameterUnit", "MI:0647" );
        class2mi.put( CvTissue.class.getSimpleName(), "IAX:0001");
        class2mi.put( CvCellType.class.getSimpleName(), "IAX:0002");
    }


    public CvObjectOntologyBuilder() {
    }

    public CvObjectOntologyBuilder( OBOSession oboSession_ ) {
        oboSession = oboSession_;
        this.nonMiCvDatabase = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(),
                                                             CvDatabase.class, CvDatabase.INTACT_MI_REF, CvDatabase.INTACT );
        this.psimod = CvObjectUtils.createCvObject( IntactContext.getCurrentInstance().getInstitution(),
                                                             CvDatabase.class, "MI:0897", "psi-mod" );
        this.processed = Maps.newHashMap();
        this.map4misWithMoreParent = Maps.newHashMap();
        map4misWithMoreParent = initializeMisWithMoreParents();

        if ( log.isDebugEnabled() ) {
            log.debug( "map4misWithMoreParent size " + map4misWithMoreParent.size() );
        }
    }

    /**
     * Sometimes Cvterms with more than one parent will
     * have children but as it would be difficult to assign
     * to the right root parent class, we also create
     * children for each of the parent root class
     *
     * @param oboObj
     * @return String[] with MIs of all childresn
     */
    private String[] getChildren4MisWithMoreParent( OBOObject oboObj ) {

        Collection<Link> childLinks = oboObj.getChildren();
        String[] mis = new String[childLinks.size()];

        int i = -1;
        for ( Link childLink1 : childLinks ) {

            Pattern p = Pattern.compile( "(MI:\\d+)-OBO_REL:is_a->(MI:\\d+)" );
            Matcher m = p.matcher( childLink1.getID() );

            if ( m.matches() ) {

                if ( m.group( 2 ).equalsIgnoreCase( oboObj.getID() ) ) {

                    OBOObject childObj = ( OBOObject ) oboSession.getObject( m.group( 1 ) );
                    mis[++i] = childObj.getID();
                }
            }
        } 

        return mis;
    }   


    protected boolean isHavingMoreThanOneParent( String id ) {
        if ( id == null ) {
            throw new NullPointerException( "You must give a non null id" );
        }

        return map4misWithMoreParent.get( id ) != null;
    }


    private <T extends CvObject> Class<T> getRootClass( String className ) {
        Class<T> cvclass = null;
        if ( class2mi.get( className ) != null && mi2Class.get( class2mi.get( className ) ) != null ) {
            cvclass = mi2Class.get( class2mi.get( className ) );
        }
        return cvclass;
    }

  /**
   *   The main method which converts the OBOOBject
   *   toCVObject.
   *
   */
    public <T extends CvObject> T toCvObject( OBOObject oboObj, OboCategory... categories ) {
        T cvObject;
        Class<T> cvClass = null;
        try {

            if ( log.isTraceEnabled() ) log.trace( "ID    ->" + oboObj.getID() + "   Name ->" + oboObj.getName() );

            /* first check if it has more than one parents
             * it true, get all the children
             * and add to the map4misWithMoreParent but with a new
             * LinkedHashset same as that of the parent
             */

            if ( this.isHavingMoreThanOneParent( oboObj.getID() ) ) {

                String[] children = getChildren4MisWithMoreParent( oboObj );

                for ( String child : children ) {
                    Set<String> linkedSet4child = map4misWithMoreParent.get( oboObj.getID() );

                    LinkedHashSet<String> newLinkedSet4child = new LinkedHashSet<String>();
                    newLinkedSet4child.addAll( linkedSet4child );

                    map4misWithMoreParent.put( child, newLinkedSet4child );
                }

                /*
               * map4misWithMoreParent stores the mi id and a
               * LinkedHashSet that contains all the root parent class
               * as we have to create cvterms for each root, for each
               * iteration take one parent root assign to cv class
               * and remove it from the set...otherwise we don't know
               * which parent class was assigned when
               *
               * */
                String rootclass = null;
                Set<String> linkedSet = map4misWithMoreParent.get( oboObj.getID() );
                if ( linkedSet != null && linkedSet.size() > 0 ) {
                    rootclass = ( String ) linkedSet.toArray()[0];
                    cvClass = getRootClass( rootclass );
                }

                if ( rootclass != null ) {
                    linkedSet.remove( rootclass );
                }

                if ( log.isTraceEnabled() ) {
                    log.trace( "More than One Parent True " + oboObj.getID() );
                }
                /*
             *   if more than one parent is true and cvClass is still null means
                 it has parents that are from same root class eg: 3 parents 2 root class
                 so when it iterates third time and as the set contains one two classes, it returns null
               * */
                if ( cvClass == null ) {
                    cvClass = findCvClassforMI( oboObj.getID() );
                    String processedKey_ = createCvKey( cvClass, oboObj.getID() );

                    if ( processed.containsKey( processedKey_ ) ) {
                        return ( T ) processed.get( processedKey_ );
                    }
                }

            } else {
                //find the CvClass for any given MI identifier
                cvClass = findCvClassforMI( oboObj.getID() );
            }

            //if CvClass is null then CvTopic.class is taken as default
            if ( cvClass == null ) {
                cvClass = ( Class<T> ) CvTopic.class;
            }
            if ( log.isTraceEnabled() ) log.trace( "cvClass ->" + cvClass.getName() );

            //Checks if the given object is already mi2cv. If so, returns the CvObject
            String processedKey = createCvKey( cvClass, oboObj.getID() );

            if ( processed.containsKey( processedKey ) ) {
                return ( T ) processed.get( processedKey );
            }

            final Institution institution = IntactContext.getCurrentInstance().getInstitution();

            //Short label look for EXACT PSI-MI-short  in synonym tag OBO 1.2
            String shortLabel = calculateShortLabel( oboObj );
            cvObject = CvObjectUtils.createCvObject( institution, cvClass, null, shortLabel );
            if ( log.isTraceEnabled() ) log.trace( "shortLabel     ->" + shortLabel );

            //Identity xref is added to all the cvs
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
                    }
                }
            } else {
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
                    String prefixString = "";
                    String suffixString = "";

                    if ( defArray.length == 2 ) {
                        prefixString = defArray[0];
                        suffixString = defArray[1];
                    } else if ( defArray.length > 2 ) {
                        prefixString = defArray[0];

                        for ( int i = 1; i < defArray.length; i++ ) {
                            if ( i == 1 ) {
                                suffixString = defArray[i];
                            } else {
                                suffixString = suffixString + "\n" + defArray[i];
                            }
                        }
                    }

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
                       if ( log.isDebugEnabled() ) log.debug( " Line Break in Definition--special case  MI: " +oboObj.getID()+"  Defintion:  " +oboObj.getDefinition() );
                       CvTopic definitionTopic = CvObjectUtils.createCvObject( institution, CvTopic.class, null, CvTopic.DEFINITION );
                       cvObject.addAnnotation( new Annotation( institution, definitionTopic, oboObj.getDefinition() ) );
                    }


                } else {

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
                    int firstIndex = xref.indexOf( '"' );
                    int lastIndex = xref.lastIndexOf( '"' );

                    String annotationText = xref.substring( firstIndex + 1, lastIndex );
                    Annotation annot = toAnnotation( CvTopic.XREF_VALIDATION_REGEXP, annotationText );
                    if ( annot != null ) {
                        cvObject.addAnnotation( annot );
                    }
                } else if ( xref.contains( CvTopic.SEARCH_URL ) ) {
                    Annotation annot = toAnnotation( CvTopic.SEARCH_URL, dbxref.getDesc() );
                    if ( annot != null ) {
                        cvObject.addAnnotation( annot );
                    }
                } 
            }  

            /********************************
             * comment
             ********************************/
            if ( oboObj.getComment() != null && oboObj.getComment().length() > 0 ) {

                Annotation annot = toAnnotation( CvTopic.COMMENT, oboObj.getComment() );
                if ( annot != null ) {
                    cvObject.addAnnotation( annot );
                }
            } //end comment

            /********************************
             * Alias
             ********************************/
            Set<Synonym> syn = oboObj.getSynonyms();
            CvObjectAlias alias_;
            for ( Synonym aSyn : syn ) {
                SynonymCategory synCat = aSyn.getSynonymCategory();
                if ( synCat != null && synCat.getID() != null && synCat.getID().equalsIgnoreCase( CvObjectOntologyBuilder.ALIAS_IDENTIFIER ) ) {
                    String aliasName = aSyn.getText();
                    alias_ = ( CvObjectAlias ) toAlias( cvObject, aliasName );
                    cvObject.addAlias( alias_ );
                }
            }

            processed.put( processedKey, cvObject );

            if ( log.isTraceEnabled() ) log.trace( "--Processed size " + processed.size() );


            if ( cvObject instanceof CvDagObject ) {
                Collection<Link> childLinks = oboObj.getChildren();

                for ( Link childLink1 : childLinks ) {

                    CvDagObject dagObject = (CvDagObject) cvObject;

                    OBOObject childObj = (OBOObject) childLink1.getChild();

                    //check for subset
                    if (categories == null || categories.length == 0) {
                        dagObject.addChild((CvDagObject) toCvObject(childObj, categories));

                    } else {
                        for (OboCategory category : categories) {
                            for (TermCategory oboCat : childObj.getCategories()) {
                                if (category.getName().equalsIgnoreCase(oboCat.getName())) {
                                    if (log.isTraceEnabled()) {
                                        log.trace("Adding child after subset check: " + childObj.getID() + "   " + childObj.getName());
                                    }

                                    dagObject.addChild((CvDagObject) toCvObject(childObj, categories));
                                } 
                            } 
                        }
                    } 
                } 
            }

        } catch ( Exception ex ) {
            throw new IntactException( "Exception converting to CvObject from OBOObject: " + oboObj.getID(), ex );
        }

        return cvObject;
    }    

    private int getPubmedIndex( Object[] dbxrefArray ) {
        int index = 0;
        for ( int i = 0; i < dbxrefArray.length; i++ ) {
            Dbxref defDbxref = ( Dbxref ) dbxrefArray[i];

            if ( defDbxref.getDatabase().equalsIgnoreCase( "PMID" ) ) {
                return i;
            }
        }
        return index;
    } 

    private boolean checkIfUniqueResid( Object[] dbxrefArray ) {
        int countResid = 0;
        for ( Object aDbxrefArray : dbxrefArray ) {
            Dbxref defDbxref = ( Dbxref ) aDbxrefArray;
            if ( defDbxref.getDatabase().equalsIgnoreCase( "RESID" ) ) {
                countResid++;
            }
        }
        return countResid == 1;

    }

    private <T extends CvObject> String createCvKey( Class<T> cvClass, String primaryId ) {
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

                String parentId = parentLink.getParent().getID();

                return findCvClassforMI( parentId );
            }  
        }
        return cvClass;
    } 

    protected <T extends CvObject> Map<String, Set<String>> initializeMisWithMoreParents() {

        for ( IdentifiedObject identifiedObject : oboSession.getObjects() ) {

            if ( identifiedObject instanceof OBOObject ) {
                OBOObject oboObj = ( OBOObject ) identifiedObject;

                Class<T> cvClass;
                //cvClass = mi2Class.get( id );

                // OBOObject oboObj = ( OBOObject ) oboSession.getObject( obo.getID() );
                Collection<Link> parentLinks = oboObj.getParents();

                if ( parentLinks != null && parentLinks.size() > 1 ) {

                    Set<String> hashSet = new LinkedHashSet<String>();

                    for ( Link parentLink : parentLinks ) {

                        final String parentId = parentLink.getParent().getID();
                        cvClass = findCvClassforMI(parentId);

                        if (cvClass == null) {
                            throw new RuntimeException("Couldn't find class type for identifier: " + parentId);
                        }

                        hashSet.add(cvClass.getSimpleName());
                        //parentMap.put(miIdentifierRight+":"+cvClass.getSimpleName(),id);
                        map4misWithMoreParent.put(oboObj.getID(), hashSet);
                    }  
                }
            }
        }
        
        int counter = 0;
        List<String> falseList = new ArrayList<String>();
        for ( String key : map4misWithMoreParent.keySet() ) {

            if ( map4misWithMoreParent.get( key ).size() > 1 ) {
                counter++;

            } else {
                falseList.add( key );
            }
        } 

        for ( String falseKey : falseList ) {
            map4misWithMoreParent.remove( falseKey );
        }
        log.debug( "More than two parents count " + counter );

        return map4misWithMoreParent;
    } 


    /**
   * This method is called if the Def: line has more than one db xreference.
   * The qualifiers are assigned according to the rules set
   *
   * */
    protected CvTermXref addQualifiersForOtherDbXreferences( Dbxref defDbxref, boolean firstDbxref, boolean uniqResid ) {

        String database, qualifier, identifier;

        if ( defDbxref == null ) {
            throw new NullPointerException( "defDbxref is null" );
        }

        if ( log.isTraceEnabled() )
            log.trace( "defDbxref: " + defDbxref.getDatabase() + "defDbxref ID: " + defDbxref.getDatabaseID() );

        if ( defDbxref.getDatabase().equalsIgnoreCase( "PMID" ) ||
             defDbxref.getDatabase().equalsIgnoreCase( "pubmed" ) ) {
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
            identifier = defDbxref.getDatabase() + ":" + defDbxref.getDatabaseID();
            database = CvDatabase.GO;
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( CvDatabase.INTACT ) ) {
            identifier = defDbxref.getDatabase() + ":" + defDbxref.getDatabaseID();
            database = CvDatabase.INTACT;
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
            identifier = defDbxref.getDatabase() + ":" + defDbxref.getDatabaseID();
            database = CvDatabase.SO;
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( "MOD" ) || defDbxref.getDatabase().equalsIgnoreCase("PSI-MOD") ) {
            identifier = defDbxref.getDatabase() + ":" + defDbxref.getDatabaseID();
            database = "PSI-MOD";
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( "UNIMOD" ) ) {
            identifier = defDbxref.getDatabaseID();
            database = "UNIMOD";
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( "TISSUE LIST" ) ) {
            identifier = defDbxref.getDatabaseID();
            database = "tissue list";
            qualifier = CvXrefQualifier.PRIMARY_REFERENCE;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( CvDatabase.CABRI ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.CABRI;
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( CvDatabase.NEWT ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.NEWT;
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( "DeltaMass" ) ) {
            identifier = defDbxref.getDatabaseID();
            database = "DeltaMass";
            qualifier = CvXrefQualifier.IDENTITY;
        } else if ( defDbxref.getDatabase().equalsIgnoreCase( CvDatabase.CHEBI ) ) {
            identifier = defDbxref.getDatabaseID();
            database = CvDatabase.CHEBI;
            qualifier = CvXrefQualifier.IDENTITY;
        } else {
            throw new IllegalArgumentException( "Unknown database: " + defDbxref.getDatabaseID() + " (" + defDbxref.getDatabase() + ")" );
        }

        if ( log.isTraceEnabled() )
            log.trace( "Returning identifier:  " + identifier + "  " + "  database: " + database + "  qualifier:  " + qualifier );
        return new CvTermXref( identifier, database, qualifier );
    }   

  /**
   * This method returns the shortLabel for the term
   * If the name length is less than <code>AnnotatedObject.MAX_SHORT_LABEL_LEN</code> characters, name is returned
   * If it is more than <code>AnnotatedObject.MAX_SHORT_LABEL_LEN</code> characters, then the synonyms with EXACT PSI-MI-short is taken
   */
    private String calculateShortLabel( OBOObject oboObj ) {
        String shortLabel = null;

        for ( Synonym synonym : oboObj.getSynonyms() ) {
            if( synonym != null ) {
                SynonymCategory synCat = synonym.getSynonymCategory();

                if ( synCat != null &&
                     synCat.getID() != null &&
                     synCat.getID().equalsIgnoreCase( CvObjectOntologyBuilder.SHORTLABEL_IDENTIFIER ) ) {
                    shortLabel = synonym.getText();
                    //another check just to reduce the length to 256 characters--rarely happens
                    if ( shortLabel != null && shortLabel.length() > AnnotatedObject.MAX_SHORT_LABEL_LEN) {
                        shortLabel = shortLabel.substring( 0, AnnotatedObject.MAX_SHORT_LABEL_LEN );
                    }
                }
            }
        }

        if ( shortLabel == null ) {
            if ( oboObj.getName() != null && oboObj.getName().length() <= AnnotatedObject.MAX_SHORT_LABEL_LEN ) {
                return oboObj.getName();
            } else if ( oboObj.getName() != null && oboObj.getName().length() > AnnotatedObject.MAX_SHORT_LABEL_LEN ) {
                if ( log.isDebugEnabled() ) log.debug( "No shortLabel for " + oboObj.getName() );
                return oboObj.getName().substring( 0, AnnotatedObject.MAX_SHORT_LABEL_LEN );
            }
        }
      
        return shortLabel;
    }


    protected CvObjectXref createIdentityXref( CvObject parent, String id ) {
        CvObjectXref idXref = null;

        if (id != null) {
            if (id.startsWith("MI")) {
                idXref = XrefUtils.createIdentityXrefPsiMi(parent, id);
                idXref.prepareParentMi();
            } else if (id.startsWith("IA")) {
                idXref = XrefUtils.createIdentityXref(parent, id, nonMiCvDatabase);
            } else if (id.startsWith("MOD")) {
                idXref = XrefUtils.createIdentityXref(parent, id, psimod);
            } else {
                if (log.isWarnEnabled()) log.warn("Uknown prefix for id: "+id+". Will store as a cross reference to database: "+nonMiCvDatabase.getShortLabel());
                idXref = XrefUtils.createIdentityXref(parent, id, nonMiCvDatabase);
            }
        }

        return idXref;
    } 


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
                }
            }  
        }

        log.info( "obsoleteCounter  " + obsoleteCounter );
        return allMIObjects;
    }

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
    } 

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
                    } 
                } 
            } 
        } 
        return ObsoloteOboObjects;
    }

    public Collection<IdentifiedObject> getInvalidOBOObjects() {
        Collection<IdentifiedObject> invalidOboObjects = new ArrayList<IdentifiedObject>();
        Collection<IdentifiedObject> allOBOObjects = oboSession.getObjects();
        for ( IdentifiedObject identifiedObject : allOBOObjects ) {

            if ( identifiedObject instanceof OBOObject ) {
                if ( !identifiedObject.getID().startsWith( "MI:" ) ) {
                    invalidOboObjects.add( identifiedObject );
                    if ( log.isTraceEnabled() )
                        log.trace( "invalidCv## " + identifiedObject.getID() + "  " + identifiedObject.getName() );
                }
            } 
        } 
        return invalidOboObjects;
    }

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
                    } 
                } 
            } 
        } 
        return orphanOboObjects;
    }

    public Collection<IdentifiedObject> getRootOBOObjects( OboCategory... categories ) {
        ArrayList<IdentifiedObject> rootOboObjects = new ArrayList<IdentifiedObject>();

        OBOObject rootObj = ( OBOObject ) oboSession.getObject( MI_ROOT_IDENTIFIER );
        Collection<Link> childLinks = rootObj.getChildren();

        for ( Link childLink : childLinks ) {

            if (MI_ROOT_IDENTIFIER.equals(childLink.getParent().getID())) {
                OBOObject immediateChildOfRoot = (OBOObject) childLink.getChild();

                if ( checkIfCategorySubset( immediateChildOfRoot, categories ) ) {
                    rootOboObjects.add( immediateChildOfRoot );
                }
            }
        } 
        log.debug( "oboObjects " + rootOboObjects.size() );
        return rootOboObjects;
    }


    private CvObjectXref toXref( CvObject cvObj, String identifier, String qualifier, String database ) {
        if ( log.isTraceEnabled() ) log.trace( "from toXref " + identifier + "   " + qualifier + "   " + database );

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

        if ( log.isTraceEnabled() ) log.trace( "qualifierCv  " + qualifierCv + "   databaseCv  " + databaseCv );

        if ( qualifierCv == null || databaseCv == null ) {
            if ( CvDatabase.PUBMED.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.PRIMARY_REFERENCE_MI_REF, qualifier );
                qualifierCv.setFullName( "created by IntAct --- should have been removed !!!" );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.PUBMED_MI_REF, CvDatabase.PUBMED );
            } else if ( CvDatabase.GO.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.GO_MI_REF, CvDatabase.GO );
            } else if ( CvDatabase.RESID.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.SEE_ALSO_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.RESID_MI_REF, CvDatabase.RESID );
            } else if ( CvDatabase.SO.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.SO_MI_REF, CvDatabase.SO );
            } else if ( "psi-mod".equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, "MI:0897", "psi-mod" );
            } else if ( CvDatabase.NEWT.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.NEWT_MI_REF, CvDatabase.NEWT );
            } else if ( CvDatabase.CABRI.equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, CvDatabase.CABRI_MI_REF, CvDatabase.CABRI );
            } else if ( "tissue list".equalsIgnoreCase( database ) ) {
                qualifierCv = CvObjectUtils.createCvObject( owner, CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF, qualifier );
                databaseCv = CvObjectUtils.createCvObject( owner, CvDatabase.class, "MI:0830", "tissue list" );
            } else {
                log.error( "Unexpected combination qualifier-database found on xref: " + qualifier + " - " + database );
                return null;
            }
        }

        if ( log.isTraceEnabled() )
            log.trace( "Returning from toXref: identifier_: " + identifier + "  qualifierCv: " + qualifierCv + " databaseCv  " + databaseCv );
        return XrefUtils.createIdentityXref( cvObj, identifier, qualifierCv, databaseCv );
    } 


    protected Alias toAlias( CvObject cvobj, String aliasName ) {
        Institution owner = IntactContext.getCurrentInstance().getInstitution();

        CvAliasType alias = getCvObjectByLabel( CvAliasType.class, CvAliasType.GO_SYNONYM );
        if ( alias == null ) {
            if ( log.isTraceEnabled() ) log.trace( "alias ==null creating new" );
            alias = CvObjectUtils.createCvObject( owner, CvAliasType.class, CvAliasType.GO_SYNONYM_MI_REF, CvAliasType.GO_SYNONYM );
        }

        return AliasUtils.createAlias( cvobj, aliasName, alias );
    }

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

        if ( log.isTraceEnabled() )
            log.debug( "Returning from toAnnotation: owner: " + owner + "  topic: " + topic + " annotation  " + annotation );

        return new Annotation( owner, topic, annotation );
    }


    protected <T extends CvObject> T getCvObjectByLabel( Class<T> cvObjectClass, String label ) {

        if ( label == null ) {
            throw new NullPointerException( "label is null" );
        }

        if ( log.isTraceEnabled() ) log.trace( "Processed values size: " + processed.size() );

        for ( CvObject cvObject : processed.values() ) {

            if ( cvObjectClass.isAssignableFrom( cvObject.getClass() ) && label.equals( cvObject.getShortLabel() ) ) {
                return ( T ) cvObject;
            }
        }

        return IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getCvObjectDao( cvObjectClass ).getByShortLabel( cvObjectClass, label );
    }  


    /**
     * Used only for test purposes
     *
     * @return Collection of CvDagObjects
     */
    public Collection<CvDagObject> getAllValidCvs() {

        List<CvObject> validCvs = new ArrayList<CvObject>();
        Collection<IdentifiedObject> rootOboObjects = getRootOBOObjects();

        for ( IdentifiedObject rootOboObject : rootOboObjects ) {
            OBOObject rootObject = ( OBOObject ) rootOboObject;
            CvObject cvObjectRoot = toCvObject( rootObject );
            validCvs.add( cvObjectRoot );
        }

        if ( log.isDebugEnabled() ) log.debug( "validCvs size :" + validCvs.size() );

        List<CvDagObject> allValidCvs = new ArrayList<CvDagObject>();
        for ( CvObject validCv : validCvs ) {
            allValidCvs.addAll( itselfAndChildrenAsList( ( CvDagObject ) validCv ) );
        }

        return new HashSet<CvDagObject>( allValidCvs );
    } 

    /**
     * @param oboObject  The current OBOObject instance
     * @param categories OboCategory could be PSI-MI slim, Drugable, etc.,
     * @return true if belongs to the subset
     */

    protected boolean checkIfCategorySubset( OBOObject oboObject, OboCategory... categories ) {

        if ( categories == null || categories.length == 0 )
            return true;

        for ( OboCategory category : categories ) {
            for ( TermCategory oboCat : oboObject.getCategories() ) {
                if ( category.getName().equalsIgnoreCase( oboCat.getName() ) ) {
                    return true;
                } 
            } 
        }
        return false;
    }

    /**
     * @param categories OboCategory could be PSI-MI slim, Drugable, etc.,
     * @return A subset of CvDagObjects if a category is passed, if not returns all
     */
    public List<CvDagObject> getAllCvs( OboCategory... categories ) {

        List<CvDagObject> allCvs = new ArrayList<CvDagObject>();
        //until here
        List<CvObject> rootsAndChildren = new ArrayList<CvObject>();
        Collection<IdentifiedObject> rootOboObjects = getRootOBOObjects( categories );

        for ( IdentifiedObject rootOboObject : rootOboObjects ) {
            OBOObject rootObject = ( OBOObject ) rootOboObject;

            if ( log.isTraceEnabled() ) log.trace( "Adding Parent Object " + rootObject.getID() );

            CvObject cvObjectRoot = toCvObject( rootObject, categories );

            rootsAndChildren.add( cvObjectRoot );
        }

        // handle the cvobjects with more than one parent here
        if ( log.isDebugEnabled() ) log.debug( "Roots and children size :" + rootsAndChildren.size() );

        for ( CvObject validCv : rootsAndChildren ) {
            allCvs.addAll( itselfAndChildrenAsList( ( CvDagObject ) validCv ) );
        }

        for ( IdentifiedObject orphanObo : getOrphanOBOObjects() ) {
            if ( orphanObo instanceof OBOObject ) {
                OBOObject orphanObj = ( OBOObject ) orphanObo;

                if ( checkIfCategorySubset( orphanObj, categories ) ) {
                    CvObject cvOrphan = toCvObject( orphanObj, categories );
                    allCvs.addAll( itselfAndChildrenAsList( ( CvDagObject ) cvOrphan ) );
                }
            }
        }

        allCvs = new ArrayList<CvDagObject>( new HashSet<CvDagObject>( allCvs ) );

        if ( log.isDebugEnabled() ) log.debug( "Size of the collection with all CVs: " + allCvs.size() );

        // put identity in the first position, to avoid recursivity problems
        // put identity on top
        LinkedList<CvDagObject> orderedList = new LinkedList<CvDagObject>();
        for ( CvDagObject cv : allCvs ) {
            if ( CvXrefQualifier.IDENTITY_MI_REF.equals( cv.getMiIdentifier() ) ) {
                orderedList.addFirst( cv );
            } else {
                orderedList.add( cv );
            }
        }

        //Order by Putting CvXrefs first and CvDatabases next followed by all other topics
        return getAllOrderedCvs( orderedList );
    }


    public List<CvDagObject> getAllOrderedCvs( List<CvDagObject> allCvs ) {
        List<CvDagObject> orderedList = new LinkedList<CvDagObject>();
        List<CvDagObject> xrefList = new LinkedList<CvDagObject>();
        List<CvDagObject> databaseList = new LinkedList<CvDagObject>();
        List<CvDagObject> otherList = new LinkedList<CvDagObject>();

        for ( CvDagObject cvDag : allCvs ) {

            if ( cvDag.getClass().toString().equals( uk.ac.ebi.intact.model.CvXrefQualifier.class.toString() ) ) {
                xrefList.add( cvDag );
            } else if ( cvDag.getClass().toString().equals( CvDatabase.class.toString() ) ) {
                databaseList.add( cvDag );
            } else {
                otherList.add( cvDag );
            }
        }
        //add in an order
        orderedList.addAll( xrefList );
        orderedList.addAll( databaseList );
        orderedList.addAll( otherList );

        // resolve CVs on Annotations, Xrefs and Aliases
        IntactObjectTraverser traverser = new DefaultTraverser() {
            @Override
            public void traverse(IntactObject intactObject, IntactVisitor... visitors) {
                if (intactObject instanceof Institution) {
                    return;
                }
                super.traverse(intactObject, visitors);
            }

            @Override
            protected void traverseInstitution( Institution institution, IntactVisitor... visitors ) {
                return;
            }
        };
        IntactVisitor visitor = new CvResolverVisitor( processed );
        for ( CvObject cvObject : processed.values() ) {
            traverser.traverse( cvObject, visitor);
        }

        return orderedList;
    }

    private List<CvDagObject> itselfAndChildrenAsList( CvDagObject cv ) {
        List<CvDagObject> itselfAndChildren = new ArrayList<CvDagObject>();
        itselfAndChildren.add( cv );

        for ( CvDagObject child : cv.getChildren() ) {
            itselfAndChildren.addAll( itselfAndChildrenAsList( child ) );
        }

        return itselfAndChildren;
    }  

    public int getInvalidTermCount() {
        //Invalid Terms
        int invalidCounter = 0;
        for ( IdentifiedObject invalidObo : getInvalidOBOObjects() ) {
            if ( invalidObo instanceof OBOObject ) {
                invalidCounter++;
            }
        }

        return invalidCounter;
    }  

    public List<CvObject> getOrphanCvObjects() {

        List<CvObject> orphanList = new ArrayList<CvObject>();
        for ( IdentifiedObject orphanObo : getOrphanOBOObjects() ) {
            if ( orphanObo instanceof OBOObject ) {
                OBOObject orphanObj = ( OBOObject ) orphanObo;
                CvObject cvOrphan = toCvObject( orphanObj );
                orphanList.add( cvOrphan );
            }
        }
        //until here
        return orphanList;
    }

    private class CvResolverVisitor extends BaseIntactVisitor {

        final Map<String, CvObject> mi2cv;

        private CvResolverVisitor( Map<String, CvObject> mi2cv ) {
            this.mi2cv = mi2cv;
        }

        @Override
        public void visitAlias( Alias alias ) {

            final CvAliasType type = alias.getCvAliasType();
            if( type != null ) {
                final String mi = type.getIdentifier();
                if ( mi != null ) {
                    final String key = createCvKey( type.getClass(), mi );
                    final CvAliasType oboTerm = ( CvAliasType ) mi2cv.get( key );
                    if ( oboTerm == null ) {
                        log.warn( "Could not find alias " + type.getClass().getSimpleName() + "( " + type.getShortLabel() + " ) by MI: " + mi );
                    } else {
                        alias.setCvAliasType( oboTerm );
                    }
                }
            }
        }

        @Override
        public void visitXref( Xref xref ) {

            final CvDatabase db = xref.getCvDatabase();
            String mi = db.getIdentifier();
            if ( mi != null ) {
                String key = createCvKey( db.getClass(), mi );
                final CvDatabase oboTerm = ( CvDatabase ) mi2cv.get( key );
                if ( oboTerm == null ) {
                    log.warn( "Could not find xref " + db.getClass().getSimpleName() + "( " + db.getShortLabel() + " ) by MI: " + mi );
                } else {
                    xref.setCvDatabase( oboTerm );
                }
            }

            final CvXrefQualifier qualifier = xref.getCvXrefQualifier();
            if( qualifier != null ) {
                mi = qualifier.getIdentifier();

                if ( mi != null ) {
                    final String key = createCvKey( qualifier.getClass(), mi );
                    final CvXrefQualifier oboQualifier = ( CvXrefQualifier ) mi2cv.get( key );
                    if ( oboQualifier == null ) {
                        log.warn( "Could not find qualifier " + qualifier.getClass().getSimpleName() + "( " + qualifier.getShortLabel() + " ) by MI: " + mi );
                    } else {
                        xref.setCvXrefQualifier( oboQualifier );
                    }
                }
            }
        }

        @Override
        public void visitAnnotation( Annotation annotation ) {

            final CvTopic topic = annotation.getCvTopic();
            final String mi = topic.getIdentifier();
            if ( mi != null ) {
                final CvTopic oboTerm = ( CvTopic ) mi2cv.get( createCvKey( topic.getClass(), mi ) );
                if ( oboTerm == null ) {
                    log.warn( "Could not find topic " + topic.getClass().getSimpleName() + "( " + topic.getShortLabel() + " ) by MI: " + mi );
                } else {
                    annotation.setCvTopic( oboTerm );
                }
            }
        }
    }
}
