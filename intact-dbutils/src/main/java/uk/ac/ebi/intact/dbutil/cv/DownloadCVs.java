/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.dbutil.cv.model.IntactOntology;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29-Oct-2005</pre>
 */
public class DownloadCVs {

    private static final Log log = LogFactory.getLog(DownloadCVs.class);

    public static final String VERSION = "0.3";

    public static final String ONTOLOGY_NAME = "intact";

    private static final String NEW_LINE = System.getProperty( "line.separator" );

    private static final String TIME;

    public CvDatabase psi = null;
    public CvDatabase intact = null;
    public CvXrefQualifier identity = null;
    public CvTopic definitionTopic = null;
    public CvTopic obsolete = null;

    public DownloadCVs( ) throws IntactException {

        // Initialises required vocabularies...
        psi = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvDatabase.class).getByXref( CvDatabase.PSI_MI_MI_REF );
        if ( psi == null ) {
            throw new IllegalArgumentException( "Could not find PSI via MI reference: " + CvDatabase.PSI_MI_MI_REF );
        }

        intact = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvDatabase.class).getByXref( CvDatabase.INTACT_MI_REF );
        if ( intact == null ) {
            throw new IllegalArgumentException( "Could not find IntAct via MI reference: " + CvDatabase.INTACT_MI_REF );
        }

        identity = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByXref( CvXrefQualifier.IDENTITY_MI_REF );
        if ( identity == null ) {
            throw new IllegalArgumentException( "Could not find identity via MI reference: " + CvXrefQualifier.IDENTITY_MI_REF );
        }

        definitionTopic = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvTopic.class).getByShortLabel(CvTopic.DEFINITION );
        if ( definitionTopic == null ) {
            throw new IllegalArgumentException( "Could not find definition by its name: " + CvTopic.DEFINITION );
        }

        obsolete = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvTopic.class).getByXref( CvTopic.OBSOLETE_MI_REF );
        if ( obsolete == null ) {
            throw new IllegalArgumentException( "Could not find definition via MI reference: " + CvTopic.OBSOLETE_MI_REF );
        }
    }

    static {
        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd@HH_mm" );
        TIME = formatter.format( new Date() );
        formatter = null;
    }

    /**
     * Implementation of a basic search replace.
     *
     * @param buffer the buffer containing the text on which we will apply the search-replace.
     * @param from   text to replace.
     * @param to     replacement text.
     *
     * @return the updated StringBuffer.
     */
    private static StringBuffer replace( StringBuffer buffer, String from, String to ) {

        int index = buffer.indexOf( from );

        while ( index != -1 ) {
            int start = index;
            int stop = start + from.length();
            buffer.replace( start, stop, to );

            // we don't search what before where we just inserted. That also prevent infinite loops
            int fromIndex = start + to.length();
            index = buffer.indexOf( from, fromIndex );
        }

        return buffer;
    }

    /**
     * Escape special characters according to: http://www.geneontology.org/GO.format.shtml
     *
     * @param text the text to escape
     *
     * @return the excaped text
     */
    public static String escapeCharacter( String text ) {

        if ( text == null ) {
            return "";
        }
        /*
        \n  	 newline      -
        \W 	whitespace
        \t 	tab               -
        \: 	colon             -
        \, 	comma             -
        \" 	double quote      -
        \\ 	backslash         -
        \(  \) 	parentheses    -
        \[  \] 	brackets       -
        \{  \} 	curly brackets -
        \<newline> 	<no value> -
        */

        // we would insert 1 new character per replacement, 15 if not likely to be ever reached.
        StringBuffer sb = new StringBuffer( text.length() + 15 );
        sb.append( text );

        replace( sb, "\\", "\\\\" );

        // http://en.wikipedia.org/wiki/Newline
        replace( sb, "\r\n", " " ); // Windows  style ... has to be done before Unix Style
        replace( sb, "\n", " " );   // Unix/Mac style

//        replace( sb, "\r\n", "\\n" ); // Windows  style ... has to be done before Unix Style
//        replace( sb, "\n", "\\n" );   // Unix/Mac style
//
        replace( sb, "\t", "\\t" );
        replace( sb, ":", "\\:" );
        replace( sb, ",", "\\," );
        replace( sb, "\"", "\\\"" );

        // This is adviced on the OBO web site but not respected by oboedit.
//        replace( sb, "(", "\\(" );
//        replace( sb, ")", "\\)" );

        replace( sb, "[", "\\[" );
        replace( sb, "]", "\\]" );
        replace( sb, "{", "\\{" );
        replace( sb, "}", "\\}" );

        replace( sb, "  ", " " );


        return sb.toString();
    }

    /**
     * Keep the last largest buffer length allocated.
     */
    private int maxBufferLength = 1024;

    /**
     * Format a CvObject into an OBO record.
     *
     * @param cvObject
     *
     * @return
     */
    private String formatToObo( CvObject cvObject, CvObject root ) throws IntactException {
        StringBuffer sb = new StringBuffer( maxBufferLength );

        String id = getIdentifier( cvObject );

        log.info( "Processing " + cvObject.getShortLabel() + " (" + id + ")" );
        if ( id == null ) {
            log.warn( "That term (" + cvObject.getShortLabel() + ") doesn't have an id, skip it." );
            return null;
        }

        sb.append( "[Term]" ).append( NEW_LINE );

        /////////////////////
        // 1. ID handling
        sb.append( "id: " ).append( id ).append( NEW_LINE );

        //////////////////////
        // 2. Name Handling
        String name = cvObject.getFullName();
        if ( name == null || name.trim().length() == 0 ) {
            name = cvObject.getShortLabel();
        }
        sb.append( "name: " ).append( escapeCharacter( name ) ).append( NEW_LINE );

        //////////////////////////////
        // 2. Xrefs handling
        StringBuffer allXrefs = new StringBuffer( 200 );
        List sortedXrefs = new ArrayList( cvObject.getXrefs() );

        // filter out all psi / intact Xref
        for ( Iterator iterator = sortedXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();
            CvDatabase db = xref.getCvDatabase();
            if ( intact.equals( db ) || psi.equals( db ) ) {
                iterator.remove();
            }
        }

        // Sort remaining Xrefs
        Collections.sort( sortedXrefs, new Comparator() {

            // Order Xrefs by CvDatabase and primaryId
            public int compare( Object o1, Object o2 ) {
                Xref x1 = (Xref) o1;
                Xref x2 = (Xref) o2;

                CvDatabase db1 = x1.getCvDatabase();
                CvDatabase db2 = x2.getCvDatabase();

                if ( db1.equals( db2 ) ) {
                    // sort by primaryId
                    return x1.getPrimaryId().compareTo( x2.getPrimaryId() );
                } else {
                    return db1.getShortLabel().compareTo( db2.getShortLabel() );
                }
            }
        } );

        for ( Iterator iterator = sortedXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            String db = xref.getCvDatabase().getShortLabel();

            // Skip IntAct and PSI

            if ( psi.equals( xref.getCvDatabase() ) ) {
                continue;
            } else if ( intact.equals( xref.getCvDatabase() ) ) {
                continue;
            }

            allXrefs.append( db ).append( ":" ).append( xref.getPrimaryId() );

            allXrefs.append( " \"" );
            if ( xref.getCvXrefQualifier() != null ) {
                allXrefs.append( xref.getCvXrefQualifier().getShortLabel() );
            }
            allXrefs.append( "\"" );

            if ( iterator.hasNext() ) {
                allXrefs.append( ", " );
            }
        }

        /////////////////////////
        // Definition handling
        Annotation definition = getDefinition( cvObject );

        sb.append( "def: " ).append( '"' );

        if ( definition != null ) {
            sb.append( escapeCharacter( definition.getAnnotationText() ) );
        }

        sb.append( '"' );
        if ( allXrefs != null ) {
            sb.append( ' ' ).append( '[' ).append( allXrefs.toString() ).append( ']' );
        }
        sb.append( NEW_LINE );

        ////////////////////////////
        // exact_synonym handling
        sb.append( "exact_synonym: \"" ).append( cvObject.getShortLabel() ).append( "\" []" ).append( NEW_LINE );

        ////////////////////////////
        // 3. Aliases handling
        List synonyms = new ArrayList( cvObject.getAliases() );
        Collections.sort( synonyms, new Comparator() {

            // compare by
            public int compare( Object o1, Object o2 ) {
                Alias a1 = (Alias) o1;
                Alias a2 = (Alias) o2;

                String name1 = ( a1.getName() == null ? "" : a1.getName() );
                String name2 = ( a2.getName() == null ? "" : a2.getName() );

                CvAliasType t1 = a1.getCvAliasType();
                CvAliasType t2 = a2.getCvAliasType();

                if ( t1 != null && t2 != null ) {
                    if ( t1.equals( t2 ) ) {
                        return name1.compareTo( name2 );
                    } else {
                        return t1.getShortLabel().compareTo( t2.getShortLabel() );
                    }
                } else {
                    return name1.compareTo( name2 );
                }
            }
        } );
        for ( Iterator iterator = synonyms.iterator(); iterator.hasNext(); ) {
            Alias alias = (Alias) iterator.next();
            sb.append( "xref_analog: " );
            if ( alias.getCvAliasType() != null ) {
                sb.append( alias.getCvAliasType().getShortLabel() );
            } else {
                log.info( "WARNING: Term " + id + " has an Alias without a CvAliasType" );
                sb.append( "type_not_specified" );
            }
            sb.append( ":" );
            sb.append( escapeCharacter( alias.getName() ) );
            sb.append( " \"" );
            sb.append( "ALIAS" );
            sb.append( "\"" );
            sb.append( NEW_LINE );
        }

        /////////////////////////////
        // 4. Annotation handling
        List annotations = new ArrayList( cvObject.getAnnotations() );
        Collections.sort( annotations, new Comparator() {

            // compare by
            public int compare( Object o1, Object o2 ) {
                Annotation a1 = (Annotation) o1;
                Annotation a2 = (Annotation) o2;

                String annot1 = ( a1.getAnnotationText() == null ? "" : a1.getAnnotationText() );
                String annot2 = ( a2.getAnnotationText() == null ? "" : a2.getAnnotationText() );

                CvTopic t1 = a1.getCvTopic();
                CvTopic t2 = a2.getCvTopic();

                if ( t1 != null && t2 != null ) {
                    if ( t1.equals( t2 ) ) {
                        return annot1.compareTo( annot2 );
                    } else {
                        return t1.getShortLabel().compareTo( t2.getShortLabel() );
                    }
                } else {
                    return annot1.compareTo( annot2 );
                }
            }
        } );
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext(); ) {
            Annotation annot = (Annotation) iterator.next();

            // filter out definition as it has been already exported!
            if ( annot.equals( definition ) ) {
                continue;
            }

            if ( definitionTopic.equals( annot.getCvTopic() ) ) {
                log.info( "WARNING - more than one definition available in the current CV Term." );
                log.info( "          1) \"" + definition.getAnnotationText() + "\"" );
                log.info( "          2) \"" + annot.getAnnotationText() + "\"" );
            }

            sb.append( "xref_analog: " );
            if ( annot.getCvTopic() != null ) {
                sb.append( annot.getCvTopic().getShortLabel() );
            } else {
                log.info( "WARNING: Term " + id + " has an Annotation without a CvTopic" );
                sb.append( "type_not_specified" );
            }
            sb.append( ":" );

            sb.append( escapeCharacter( annot.getAnnotationText() ) );
            sb.append( " \"" );
            sb.append( "ANNOTATION" );
            sb.append( "\"" );
            sb.append( NEW_LINE );
        }

        // 5. obsolete handling
        Annotation obsolete = getObsolete( cvObject );
        if ( obsolete != null ) {
            // the comment has already been exported in the xref_analog
            sb.append( "is_obsolete: true" ).append( NEW_LINE );
        }

        // 6. DAG handling
        // Note: one OBO term can be actually mapped to multiple IntAct CV, in which case these terms would have the
        //       same MI ref but different concrete classes.

        // this is not a DAG, generate a root if there is one given as param
        if ( root != null ) {
            String rootMi = getIdentifier( root );
            String childMi = getIdentifier( cvObject );

            // when root is MI:0000 the way to describe the relationship is: relationship: part_of MI:0000 ! ...
            // otherwise is_a: MI:xxxx ! ...

            if ( ! rootMi.equals( childMi ) ) {

                if ( rootMi.equals( "MI:0000" ) ) {
                    sb.append( "relationship: part_of " );
                } else {
                    sb.append( "is_a: " );
                }

                sb.append( rootMi ).append( ' ' ).append( '!' ).append( ' ' );
                sb.append( escapeCharacter( root.getShortLabel() ) );
                sb.append( NEW_LINE );
            }
        }

        // process parents if the object is a dag element
        if ( CvDagObject.class.isAssignableFrom( cvObject.getClass() ) ) {
            // this is a DAG object, may have parents to mention here
            Collection otherTypes = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvDagObject.class)
                    .getByXrefLike(id);
            Set alreadyExported = new HashSet( 4 );

            // keeps a mapping MI -> CvObject to allow later ordering my MI reference.
            Map mi2parents = new HashMap();

            // add the remaining parents to the current definition
            for ( Iterator iterator = otherTypes.iterator(); iterator.hasNext(); ) {
                CvDagObject dag = (CvDagObject) iterator.next();

                if ( ! dag.getParents().isEmpty() ) {

                    for ( Iterator iterator2 = dag.getParents().iterator(); iterator2.hasNext(); ) {
                        CvDagObject parent = (CvDagObject) iterator2.next();
                        String mi = getIdentifier( parent );

                        if ( alreadyExported.contains( mi ) ) {
                            // avoid the same term from appearing multiple times
                            continue;
                        }

                        alreadyExported.add( mi );

                        mi2parents.put( mi, parent );
                    }
                }
            } // loop otherType

            List parents = new ArrayList( mi2parents.keySet() );
            Collections.sort( parents ); // sort MI references by alphabetical order

            // print out all sorted parents
            for ( Iterator iterator = parents.iterator(); iterator.hasNext(); ) {
                String mi = (String) iterator.next();
                CvDagObject parent = (CvDagObject) mi2parents.get( mi );

                sb.append( "is_a: " ).append( mi ).append( ' ' ).append( '!' ).append( ' ' );
                sb.append( escapeCharacter( parent.getShortLabel() ) );
                if ( parent.getFullName() != null
                     &&
                     ! parent.getShortLabel().equals( parent.getFullName() ) ) {
                    sb.append( ':' ).append( ' ' ).append( escapeCharacter( parent.getFullName() ) );
                }

                sb.append( NEW_LINE );
            }

        } // end of CvDagObject processing

        if ( sb.length() > maxBufferLength ) {
            maxBufferLength = sb.length();
        }

        return sb.toString();
    }

    /**
     * Return a definition for the given CvObject.
     *
     * @param cvObject the CvObject that we will introspect.
     *
     * @return a String that can be null.
     */
    private Annotation getDefinition( CvObject cvObject ) {
        for ( Iterator iterator = cvObject.getAnnotations().iterator(); iterator.hasNext(); ) {
            Annotation annotation = (Annotation) iterator.next();
            if ( definitionTopic.equals( annotation.getCvTopic() ) ) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * If any, get the unique Obsolete Annotation of the given object.
     *
     * @param cvObject the CvObject that we will introspect.
     *
     * @return an Annotation object if it can be found, otherwise null.
     */
    private Annotation getObsolete( CvObject cvObject ) {
        for ( Iterator iterator = cvObject.getAnnotations().iterator(); iterator.hasNext(); ) {
            Annotation annotation = (Annotation) iterator.next();
            if ( obsolete.equals( annotation.getCvTopic() ) ) {
                return annotation;
            }
        }
        return null;
    }

    private boolean isObsolete( CvObject cvObject ) {
        Annotation obsolete = getObsolete( cvObject );
        if ( obsolete != null ) {
            return true;
        }
        return false;
    }

    /**
     * Selects psi-mi reference (MI:xxxx) from the given CvObject Xrefs or otherwise an IntAct reference (IA:xxxx).
     *
     * @param cvObject
     *
     * @return an mi reference or an intact reference or null if none is found.
     */
    private String getIdentifier( CvObject cvObject ) {
        String mi = null;
        String ia = null;

        // TODO check that only one has identity !!

        for ( Iterator iterator = cvObject.getXrefs().iterator(); iterator.hasNext() && mi == null; ) {
            Xref xref = (Xref) iterator.next();
            if ( identity.equals( xref.getCvXrefQualifier() ) ) {
                if ( psi.equals( xref.getCvDatabase() ) ) {
                    mi = xref.getPrimaryId();
                } else if ( intact.equals( xref.getCvDatabase() ) ) {
                    ia = xref.getPrimaryId();
                    if ( ! ia.startsWith( "IA:" ) ) {
                        log.info( "WARNING: CV Term '" + cvObject.getShortLabel() + "' has an intact identity malformed: " + ia );
                    }
                }
            }
        }

        return ( mi != null ? mi : ia );
    }

    private String getMiIdentifier( CvObject cvObject ) {
        String mi = null;

        for ( Iterator iterator = cvObject.getXrefs().iterator(); iterator.hasNext() && mi == null; ) {
            Xref xref = (Xref) iterator.next();
            if ( psi.equals( xref.getCvDatabase() ) ) {
                mi = xref.getPrimaryId();
            }
        }

        // return psi if available, otherwise intact.
        return mi;
    }

    private boolean hasReference( CvObject cvObject, String identifier ) {

        if ( identifier == null ) {
            throw new IllegalArgumentException( "The MI given must be not null." );
        }

        String cvid = getIdentifier( cvObject );
        if ( identifier.equals( cvid ) ) {
            return true;
        }
        return false;
    }

    /**
     * Generate the Header of the OBO file.
     *
     * @return the content of the header.
     */
    public String generateHeader() {
        //format-version: 1.0
        //date: 25:10:2005 15:55
        //saved-by: luisa
        //auto-generated-by: DAG-Edit 1.419 rev 3
        //default-namespace: psi-mi25.dag

        StringBuffer sb = new StringBuffer( 128 );

        sb.append( "format-version: 1.0" ).append( NEW_LINE );

        SimpleDateFormat formatter = new SimpleDateFormat( "dd:MM:yyyy HH:mm" );
        String date = formatter.format( new Date() );
        sb.append( "date: " ).append( date ).append( NEW_LINE );

        sb.append( "saved-by: samuel" ).append( NEW_LINE );

        sb.append( "auto-generated-by: IntAct - " ).append( getClass().getName() );
        sb.append( " - v" ).append( VERSION ).append( NEW_LINE );

        return sb.toString();
    }

    public String generateFooter() {

        StringBuffer sb = new StringBuffer( 128 );

        sb.append( "[Typedef]" ).append( NEW_LINE );
        sb.append( "id: part_of" ).append( NEW_LINE );
        sb.append( "name: part of" ).append( NEW_LINE );
        sb.append( "is_transitive: true" ).append( NEW_LINE );

        return sb.toString();
    }

    private class VirtualCvRoot extends CvObject {
        public VirtualCvRoot( Institution owner, String shortLabel ) {
            super( owner, shortLabel );
        }

        public VirtualCvRoot( Institution owner, String shortLabel, String mi ) {
            super( owner, shortLabel );

            if ( mi == null ) {
                throw new IllegalArgumentException( "mi must not be null" );
            }

            // add PSI Xref
            addXref( new CvObjectXref( owner, psi, mi, null, null, identity ) );
        }
    }

    public void download( BufferedWriter out ) throws IOException, IntactException {
        download(out, false);
    }

    public void download( BufferedWriter out, boolean isDryRun ) throws IOException, IntactException {

        // 1. Get all CvObject
        log.info( "Loading all IntAct CVs ... " );
        Collection cvObjects = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao().getAll();
        log.info( cvObjects.size() + " found." );

        // creating the root of all CVs
        final VirtualCvRoot superRoot = new VirtualCvRoot( new Institution( "tmp" ), "molecular interaction", "MI:0000" );
        superRoot.setFullName( "Controlled vocabularies originally created for protein protein interactions, extended to other molecules interactions." );

        // collecting all available types of CVs
        Set allCvClasses = new HashSet();
        for ( Iterator iterator = cvObjects.iterator(); iterator.hasNext(); ) {
            CvObject cvObject = (CvObject) iterator.next();
            allCvClasses.add( CgLibUtil.removeCglibEnhanced(cvObject.getClass()) );
        }

        Map typeMapping = IntactOntology.getTypeMapping( true ); // incl. DAGs and non DAGs
        Map mi2name = IntactOntology.getNameMapping();
        Map cvClass2root = new HashMap( allCvClasses.size() );
        HashSet rootsOfType = new HashSet();

        // 2. Add potentially missing non Dag root. These missing roots are materialized as VirtualCvRoot
        //    If a PSI root term is missing in IntAct we simulate it with a virtual node, so the exported file
        //    Still complies to the original PSI one.

        // Check that every IntAct CV type has a root in the database.
        // The IntactOntology should contain all necessary mappings.
        // if a root term is missing, we create it.
        // along the way, we create a mapping CvObject Class --> CvObject (the root)
        for ( Iterator iterator = allCvClasses.iterator(); iterator.hasNext(); ) {
            Class aCvClass = (Class) iterator.next();

            if ( typeMapping.containsKey( aCvClass ) ) {
                String[] miRefs = (String[]) typeMapping.get( aCvClass );
                log.info( aCvClass + " maps to " );
                for ( int i = 0; i < miRefs.length; i++ ) {
                    String miRef = miRefs[ i ];
                    log.info( miRef );

                    // look up in the database
                    CvObject root = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao().getByXref(miRef);

                    if ( root == null && !isDryRun) {
                        // doesn't exist yet, then create it
                        try {
                            Constructor constructor = aCvClass.getDeclaredConstructor( new Class[]{ Institution.class, String.class } );
                            if ( constructor != null ) {
                                String name = (String) mi2name.get( miRef );
                                root = (CvObject) constructor.newInstance( new Object[]{ IntactContext.getCurrentInstance().getInstitution(), name } );

                                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao().persist( root );

                                // add Xref
                                String database = null;
                                if ( miRef.startsWith( "MI:" ) ) {
                                    database = CvDatabase.PSI_MI_MI_REF;
                                } else if ( miRef.startsWith( "IA:" ) ) {
                                    database = CvDatabase.INTACT_MI_REF;
                                } else {
                                    throw new IllegalArgumentException();
                                }
                                // TODO [ intact | psi | identity ] need to exist before hand.
                                CvDatabase db = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvDatabase.class).getByXref( database );
                                CvXrefQualifier q = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvXrefQualifier.class).getByXref(CvXrefQualifier.IDENTITY_MI_REF);

                                CvObjectXref xref = new CvObjectXref( IntactContext.getCurrentInstance().getInstitution(), db, miRef, null, null, q );
                                root.addXref( xref );
                                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( xref );

                                // add it to the list of all CVs so it gets processed later on.
                                cvObjects.add( root );

                                log.info( " ( Node created [" + db.getShortLabel() + ", " + miRef + "] )" );
                            }
                        } catch ( Exception e ) {
                            throw new IntactException( "Failed to create CV term (" + miRef + "), cf. nested errors.", e );
                        }
                    } // root was not found
                    else
                    {
                         if (log.isDebugEnabled())
                            log.debug("DRY RUN: root CV would have been created");
                    }

                    // if not done yet, add mapping CV class to the specific root
                    if ( cvClass2root.containsKey( aCvClass ) ) {
                        CvObject cv = (CvObject) cvClass2root.get( aCvClass );
                        log.info( "\nWARNING: Trying to overwrite root ( " + cv.getShortLabel() + " ) with " + root.getShortLabel() );
                        log.info( "         Skipping " + cv.getShortLabel() + "." );
                    } else {
                        cvClass2root.put( aCvClass, root );
                    }

                    if ( ( i + 1 ) < miRefs.length ) {
                        log.info( ", " );
                    }

                    // keep track of all root of CVs
                    if ( root != null ) {
                        rootsOfType.add( root );
                    }

                } // CV IDs
                log.info( "." );

            } else {

                String msg = "WARNING: " + aCvClass + " is not mapped, please update the mapping in IntactOntology.";
                throw new IllegalStateException( msg );
            }
        } // allCvClasses

        // now that the classification is done, we add the super root to the list (VirtualCvRoot cannot be classified)
        cvObjects.add( superRoot );
        log.info( "Identifier of Super Root is: " + getMiIdentifier( superRoot ) );
        log.info( "Adding Super Root into the collection of all CV Terms: " + cvObjects.contains( superRoot ) );

        // 3. Sort terms by MI reference
        Map mi2cvObject = new HashMap( cvObjects.size() );
        Collection noMiTerms = new ArrayList();
        for ( Iterator iterator = cvObjects.iterator(); iterator.hasNext(); ) {
            CvObject cvObject = (CvObject) iterator.next();

            String mi = getMiIdentifier( cvObject );
            if ( mi != null ) {
                mi2cvObject.put( mi, cvObject );
            } else {
                // store it under a special flag
                noMiTerms.add( cvObject );
            }
        }
        log.info( "Found " + noMiTerms.size() + " terms without MI reference" );
        List allMiReferences = new ArrayList( mi2cvObject.keySet() );
        Collections.sort( allMiReferences );

        // 4. include header
        out.write( generateHeader() );
        out.write( NEW_LINE );

        // 5. Process term that have an MI reference in order
        for ( Iterator iterator = allMiReferences.iterator(); iterator.hasNext(); ) {
            String ref = (String) iterator.next();
            CvObject cvObject = (CvObject) mi2cvObject.get( ref );

            CvObject root = null;


            log.info( "------------------------------" );
            log.info( "Term: " + ref );

            // if super root, give no root
            // if root of type, give super root
            // else give classified root.
            if ( cvObject == superRoot ) {
                log.info( "Super root, give no root" );
                root = null;
            } else if ( rootsOfType.contains( cvObject ) ) {
                log.info( "Simple root, give MI:0000 as root" );
                root = superRoot;
            } else if ( CvDagObject.class.isAssignableFrom( cvObject.getClass() ) ) {

                log.info( "Simple term (assignable from CvDagObject), give root: " );
                // if the term doesn't have parents, we need to attach it to a root, otherwise we won't know its type.
                CvDagObject cvDagObject = (CvDagObject) cvObject;
                if ( cvDagObject.getParents().isEmpty() ) {
                    log.info( "Current CvDagObject doesn't have any parent" );
                    root = (CvObject) cvClass2root.get( cvObject.getClass() );
                } else {
                    log.info( "Current CvDagObject has " + cvDagObject.getParents().size() + " parent(s), we DO NOT give root." );
                }
            } else {
                log.info( "Simple CV Term (CvObject, not a DAG), give root according to class type (" + cvObject.getClass() + ")." );
                // if not a CvDagObject, there is not hierarchy, so we need to give a parent
                root = (CvObject) cvClass2root.get( cvObject.getClass() );
            }

            String rootRef = null;
            if ( root != null ) {
                rootRef = getIdentifier( root );
                if ( rootRef == null ) {
                    log.info( "ERROR - could not find an identifier for CV Term: " + root.getShortLabel() + " (" + root.getAc() + ")" );
                }
            }

            String term = formatToObo( cvObject, root );
            if ( term != null ) {
                out.write( term );
                out.write( NEW_LINE );
                out.flush();
            }
        }

        // 6. Process terms that don't have an MI reference
        log.info( "--------------------------------------------------" );
        log.info( "Processing term having no MI reference." );

        // update all terms having no IA:xxxx so they get one.
        for ( Iterator iterator = noMiTerms.iterator(); iterator.hasNext(); ) {
            CvObject cvObject = (CvObject) iterator.next();

            String id = getIdentifier( cvObject );

            if ( id == null) {
                // neither an IA:xxxx or MI:xxxx available ...
                try {
                    String localId;

                    if (!isDryRun)
                    {
                        localId = SequenceManager.getNextId( );
                    }
                    else
                    {
                        localId = "IA-DR:"+System.currentTimeMillis();
                    }

                    CvObjectXref xref = new CvObjectXref( IntactContext.getCurrentInstance().getInstitution(), intact, localId, null, null, identity );

                    if (!isDryRun)
                    {
                        cvObject.addXref( xref );
                        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( xref );

                    }
                    else
                    {
                        log.info("A new xref would have been added to: "+cvObject.getShortLabel()+" ("+cvObject.getAc()+")");
                    }

                    id = localId;
                    log.debug( "Added new Xref to '" + cvObject.getShortLabel() + "': " + id );

                } catch ( IntactException e ) {
                    e.printStackTrace();
                }
            }
        }

        // Sort terms by identity
        List sortedCVs = new ArrayList(noMiTerms);

        if (!isDryRun)
        {
            Collections.sort(sortedCVs, new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    CvObject cv1 = (CvObject) o1;
                    CvObject cv2 = (CvObject) o2;

                    String id1 = getIdentifier(cv1);
                    String id2 = getIdentifier(cv2);

                    return id1.compareTo(id2);
                }
            });

        }
        else
        {
            log.debug("DRY RUN: Terms would have been sorted by its identifier");
        }

        // export terms
        for ( Iterator iterator = sortedCVs.iterator(); iterator.hasNext(); ) {
            CvObject cvObject = (CvObject) iterator.next();

            log.info( "------------------------------" );
            log.info( "Term: " + getIdentifier( cvObject ) );

            CvObject root = null;

            if ( cvObject == superRoot ) {
                root = null;
            } else if ( rootsOfType.contains( cvObject ) ) {
                root = superRoot;
            } else if ( CvDagObject.class.isAssignableFrom( cvObject.getClass() ) ) {

                // if the term doesn't have parents, we need to attach it to a root, otherwise we won't know its type.
                CvDagObject cvDagObject = (CvDagObject) cvObject;
                if ( cvDagObject.getParents().isEmpty() ) {
                    root = (CvObject) cvClass2root.get( cvObject.getClass() );
                }
            } else {
                // if not a CvDagObject, there is not hierarchy, so we need to give a parent
                root = (CvObject) cvClass2root.get( cvObject.getClass() );
            }

            String term = formatToObo( cvObject, root );
            if ( term != null ) {
                out.write( term );
                out.write( NEW_LINE );
                out.flush();
            }
        }

        // 7. Generate the Footer
        out.write( generateFooter() );
    }

    /**
     * @param args
     *
     * @throws IntactException
     */
    public static void main( String[] args ) throws IntactException {

        String outputFilename = null;
        if ( args.length > 0 ) {
            outputFilename = args[ 0 ];
        } else {
            // assign default filename
            outputFilename = "CvDownload-" + TIME + ".obo";
        }

        try {
            BufferedWriter out = new BufferedWriter( new FileWriter( outputFilename ) );

            if (log.isInfoEnabled())
            {
                try
                {
                    log.info( "Database: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName() );
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }

                DownloadCVs downloadCVs = new DownloadCVs();
                downloadCVs.download( out );


            out.flush();
            out.close();
            log.info( "Closing " + outputFilename );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}