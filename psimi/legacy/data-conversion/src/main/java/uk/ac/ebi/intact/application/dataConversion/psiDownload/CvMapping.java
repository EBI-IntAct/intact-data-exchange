/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiDownload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Xref;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

/**
 * Holds a Controlled Vocabulary Mapping.
 * <p/>
 * The mapping is loaded from a file and the CvMapping is then queried in order to get a remapped CV.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25-Jun-2005</pre>
 */
public class CvMapping {

    private static final Log log = LogFactory.getLog(CvMapping.class);

    public static final String TABULATION = "\t";
    public static final String STAR = "*";

    private Map map = new HashMap();

    /**
     * Construct a CvMapping object.
     */
    public CvMapping() {
    }

    /**
     * Extract from the given CV term its PSI-MI id (if any).
     *
     * @param cv the CV term.
     *
     * @return a psi id or the shortlabel of the CV if not found.
     */
    private String getPsiReference( CvObject cv ) {

        if ( cv == null ) {
            throw new IllegalArgumentException( "the given CvObject must not be null." );
        }

        for ( Iterator iterator = cv.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( CvDatabase.PSI_MI.equals( xref.getCvDatabase().getShortLabel() ) ) {
                return xref.getPrimaryId();
            }
        }

        return cv.getShortLabel();
    }

    /**
     * Get the simple name of a Class Object.
     *
     * @param clazz the class
     *
     * @return the simple name of the given class (ie. without the package)
     */
    private String getSimpleName( Class clazz ) {

        String name = clazz.getName();

        int idx = name.lastIndexOf( "." );
        if ( idx != -1 ) {
            name = name.substring( idx + 1, name.length() );
        }

        return name;
    }

    /**
     * Add an association into the map.
     * <p/>
     * It takes care of : <ul> <li> circular mapping (a term to itself)</li> <li> mapping to uncompatible type (ie. both
     * from and to have to have the same type)</li> <li> overwritting existing mapping.</li> </ul>
     *
     * @param map
     * @param from
     * @param to
     */
    private void addMapping( Map map, CvObject from, CvObject to ) {

        String mapping = getPsiReference( from ) + " --> " + getPsiReference( to );

        if ( from.equals( to ) ) {
            log.warn( "skip unuseful mapping, " + mapping );
            return;
        }

        if ( ! from.getClass().equals( to.getClass() ) ) {
            String fromCls = getSimpleName( from.getClass() );
            String toCls = getSimpleName( to.getClass() );
            String msg = fromCls + "->" + toCls;

            log.error( "skip mapping involving incompatible class type (" + msg + "), " + mapping );
            return;
        }

        if ( map.containsKey( from ) ) {
            String existing = getPsiReference( from ) + " --> " + getPsiReference( (CvObject) map.get( from ) );
            System.err.println( "WARNING: mapping " + mapping + " is conflicting with " + existing );
            System.err.println( "Skip it." );
            return;
        }

        log.debug( "ADD: " + mapping );
        map.put( from, to );
    }

    //////////////////////////////////////
    // Map serialization

    /**
     * Load mapping from a serialized file. The format is as follow:
     * <pre>
     * &lt;Integer&gt; count of associations
     * &lt;CvObject&gt; from
     * &lt;CvObject&gt; to
     * &lt;CvObject&gt; from
     * &lt;CvObject&gt; to
     * &lt;CvObject&gt; from
     * &lt;CvObject&gt; to
     * ...
     * </pre>
     *
     * @param serializedFile
     *
     * @return
     */
    private Map loadSerializedMap( File serializedFile ) {

        Map aMap = null;

        try {
            if ( serializedFile.exists() && serializedFile.canRead() ) {

                log.debug( "Loading mapping from serialized cache (" + serializedFile.getAbsolutePath() + ")" );

                FileInputStream in = new FileInputStream( serializedFile );
                ObjectInputStream ois = new ObjectInputStream( in );

                Integer count = (Integer) ois.readObject();

                if ( count.intValue() > 0 ) {
                    aMap = new HashMap( count.intValue() ); // initialise with the exact size.
                }

                for ( int i = 0; i < count.intValue(); i++ ) {

                    CvObject from = (CvObject) ois.readObject();
                    CvObject to = (CvObject) ois.readObject();

                    addMapping( aMap, from, to );
                }

                ois.close();

                log.debug( map.size() + " association loaded." );
            }
        } catch ( Exception e ) {
            aMap = null;
            e.printStackTrace();
        }

        return aMap;
    }

    /**
     * Serialize the CV mapping into a file.
     * <pre>
     * &lt;Integer&gt; count of associations
     * &lt;CvObject&gt; from
     * &lt;CvObject&gt; to
     * &lt;CvObject&gt; from
     * &lt;CvObject&gt; to
     * &lt;CvObject&gt; from
     * &lt;CvObject&gt; to
     * ...
     * </pre>
     *
     * @param aMap           the aMap to serialize.
     * @param serializedFile the file in which we want to serialize the aMap.
     *
     * @see #loadSerializedMap(java.io.File)
     */
    private void serializeMapping( Map aMap, File serializedFile ) {

        FileOutputStream out = null;
        try {
            if ( ! aMap.isEmpty() ) {
                log.debug( "Caching " + aMap.size() + " associations into " + serializedFile.getAbsolutePath() );
                out = new FileOutputStream( serializedFile );
                ObjectOutputStream oos = new ObjectOutputStream( out );

                // write the count of associations
                oos.writeObject( new Integer( aMap.size() ) );

                for ( Iterator iterator = aMap.keySet().iterator(); iterator.hasNext(); ) {
                    CvObject from = (CvObject) iterator.next();
                    CvObject to = (CvObject) aMap.get( from );

                    oos.writeObject( from );
                    oos.writeObject( to );
                }
                oos.flush();
                oos.close();
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////
    // Public methods - CV Loading

    /**
     * Load a tabulation separated flat file having the following format:
     * <pre>
     *         &lt;MI:1&gt;   &lt;shortalbel1&gt;   &lt;MI:2&gt; &lt;shortalbel2&gt;
     * <p/>
     * &lt;MI:1&gt;         : PSI MI reference of the PSI2 term
     * &lt;shortalbel1&gt;  : shortlabel of the PSI2 term (only for clarity)
     * &lt;MI:2&gt;         : PSI MI reference of the PSI1 term
     * &lt;shortalbel2&gt;  : shortlabel of the PSI1 term (only for clarity)
     * <p/>
     * notes:
     *         - all blank lines are skipped
     *         - all lines starting with # are considered as comments (hence skipped)
     *         - if &lt;MI:1&gt; is a star ('*') we load all the terms, picking the type based on &lt;MI:2&gt;'s
     *         - if &lt;MI:2&gt; is a star ('*') we load all the terms, picking the type based on &lt;MI:1&gt;'s
     * </pre>
     *
     * @param file
     *
     * @return
     */
    public Map loadFile(File file) {

        if ( map == null ) {
            // initialise it
            map = new HashMap();
        } else {
            // clear any existing data
            map.clear();
        }

        File serializedFile = new File( file.getName() + ".ser" );
        Map loadedMap = loadSerializedMap( serializedFile );

        if ( loadedMap != null ) {
            map.putAll( loadedMap );

            // we stop loading here
            return map;
        }

        try {
            BufferedReader in = new BufferedReader( new FileReader( file ) );
            String str;
            int lineCount = 0;
            while ( ( str = in.readLine() ) != null ) {

                lineCount++;

                str = str.trim();

                if ( "".equals( str ) || str.startsWith( "#" ) ) {
                    continue; // skip white line and comments (#)
                }

                StringTokenizer st = new StringTokenizer( str, TABULATION, true );

                String fromMI = null;
                String fromLabel = null;
                String toMI = null;
                String toLabel = null;

                if ( st.hasMoreTokens() ) {
                    fromMI = st.nextToken();
                    if ( fromMI.equals( TABULATION ) ) {
                        fromMI = null;
                    } else {
                        st.nextToken(); // skip next TABULATION
                    }
                    if ( st.hasMoreTokens() ) {
                        fromLabel = st.nextToken();
                        if ( fromLabel.equals( TABULATION ) ) {
                            fromLabel = null;
                        } else {
                            st.nextToken(); // skip next TABULATION
                        }
                        if ( st.hasMoreTokens() ) {
                            toMI = st.nextToken();
                            if ( toMI.equals( TABULATION ) ) {
                                toMI = null;
                            } else {
                                st.nextToken(); // skip next TABULATION
                            }
                            if ( st.hasMoreTokens() ) {
                                toLabel = st.nextToken();
                            }
                        }
                    }
                }

                try {

                    if ( fromMI != null && toMI != null ) {

                        if ( fromMI.equals( toMI ) ) {
                            // self mapping, skip quietly
                            log.debug( "SKIP self mapping of " + fromMI );
                            continue;
                        }

                        // either they are both MI, or one of them is MI and the orher a STAR.
                        if ( ( fromMI.startsWith( "MI:" ) || fromMI.equals( STAR ) )
                             && ( toMI.startsWith( "MI:" ) || toMI.equals( STAR ) )
                             && ( !( fromMI.equals( STAR ) && toMI.equals( STAR ) ) ) ) {

                            CvObjectDao<CvObject> cvObjectDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvObject.class);

                            CvObject fromCvObject = null;
                            if ( fromMI.startsWith( "MI:" ) ) {
                                Collection<CvObject> c = cvObjectDao.getByXrefLike(fromMI );

                                if ( c.size() == 1 ) {
                                    fromCvObject =  c.iterator().next();
                                } else if ( c.size() > 1 ) {

                                    // get the class from the other MI ref, and filter using it instead of CvObject
                                    c = cvObjectDao.getByXrefLike(toMI);
                                    if ( c.size() == 1 ) {
                                        // get the class
                                        CvObject cv = c.iterator().next();
                                        Class clazz = cv.getClass();

                                        // there should be only one.
                                        fromCvObject = IntactContext.getCurrentInstance().getCvContext().getByMiRef(clazz, fromMI );
                                    }
                                }

                                if ( fromCvObject == null ) {
                                    System.err.println( "Line " + lineCount + ": Warning, " + fromMI + " could not be found in the database." );
                                    continue; // skip it
                                }
                            }

                            CvObject toCvObject = null;
                            if ( toMI.startsWith( "MI:" ) ) {
                                toCvObject = cvObjectDao.getByXref(toMI );

                                if ( toCvObject == null ) {
                                    System.err.println( "Line " + lineCount + ": Warning, " + toMI + " could not be found in the database." );
                                    continue; // skip it
                                }
                            }

                            Collection fromCollection = null;
                            if ( fromCvObject == null ) {
                                // it was a star, hence check our the type of toCvObject
                                fromCollection = cvObjectDao.getAll(); // load them all

                                // remove evenutual cycle
                                fromCollection.remove( toCvObject );
                            }

                            Collection toCollection = null;
                            if ( toCvObject == null ) {
                                // it was a star, hence check our the type of fromCvObject
                                toCollection = cvObjectDao.getAll(); // load them all

                                // remove evenutual cycle
                                toCollection.remove( fromCvObject );
                            }

                            if ( fromCollection != null ) {

                                for ( Iterator iterator = fromCollection.iterator(); iterator.hasNext(); ) {
                                    CvObject _fromCvObject = (CvObject) iterator.next();

                                    addMapping( map, _fromCvObject, toCvObject );
                                }

                            } else if ( toCollection != null ) {

                                for ( Iterator iterator = toCollection.iterator(); iterator.hasNext(); ) {
                                    CvObject _toCvObject = (CvObject) iterator.next();

                                    addMapping( map, fromCvObject, _toCvObject );
                                }

                            } else {

                                addMapping( map, fromCvObject, toCvObject );
                            }

                        } else {

                            System.err.println( "Line " + lineCount + " from (" + fromMI +
                                                ") and to (" + toMI +
                                                ") format is erroneous. Skip it." );
                        }
                    } else {
                        System.err.println( "Line " + lineCount + ": Mapping wrong. Skip it." );
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }

            in.close();

        } catch ( IOException e ) {
            e.printStackTrace();
        }

        // try to cache the map on disk for later use.
        serializeMapping( map, serializedFile );

        return map;
    }


    /**
     * If there is a mapping available, replace the given CV by an other one.
     *
     * @param fromCvObject CvObject to remap.
     *
     * @return a CvObject, it can be different if specified in the mapping.
     */
    public CvObject getPSI2toPSI1( CvObject fromCvObject ) {
        CvObject toCvObject = (CvObject) map.get( fromCvObject );
        return ( toCvObject == null ? fromCvObject : toCvObject );
    }

    /////////////////////////////
    // M A I N

    public static void main( String[] args ) throws IntactException {

        CvMapping mapping = new CvMapping();

        try
        {
            log.debug( "Database: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName() );
            long start = System.currentTimeMillis();
            mapping.loadFile( new File( args[ 0 ] ));
            long stop = System.currentTimeMillis();

            log.debug( "Loading time: " + ( stop - start ) + "ms" );
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }
}