/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv.model;

import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.ook.model.implementation.TermBean;

import java.io.PrintStream;
import java.util.*;

/**
 * Container for the IntAct Controlled Vocabulary terms.<br> Allow to perform simple queries that will help during the
 * update process.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30-Sep-2005</pre>
 */
public class IntactOntology {

    ///////////////////////////////
    // Static variable

    private static Map mi2name = new HashMap();

    /**
     * Maps which IntAct CV maps to which CV root (by one to many MI reference) Contains: Class -> String[]
     */
    private static Map class2mi = new HashMap();

    static {

        // Initialising the mapping of IntAct CV Class to CvTerm IDs.

        // DAG
        class2mi.put( CvInteraction.class, new String[]{ "MI:0001" } );
        class2mi.put( CvInteractionType.class, new String[]{ "MI:0190" } );
        class2mi.put( CvIdentification.class, new String[]{ "MI:0002" } );
        class2mi.put( CvFeatureIdentification.class, new String[]{ "MI:0003" } );
        class2mi.put( CvFeatureType.class, new String[]{ "MI:0116" } );
        class2mi.put( CvInteractorType.class, new String[]{ "MI:0313" } );

        // Non DAG
        class2mi.put( CvFuzzyType.class, new String[]{ "MI:0333" } );
        class2mi.put( CvXrefQualifier.class, new String[]{ "MI:0353" } );
        class2mi.put( CvDatabase.class, new String[]{ "MI:0444" } );
        class2mi.put( CvExperimentalRole.class, new String[]{ "MI:0495" } );
        class2mi.put( CvBiologicalRole.class, new String[]{ "MI:0500" } );
        class2mi.put( CvAliasType.class, new String[]{ "MI:0300" } );
        class2mi.put( CvTopic.class, new String[]{ "MI:0590" } );

        // we map here all CVs that are not supported in PSI-MI
        // we use the range IA:0001 to IA:0050 for these
        class2mi.put( CvCellType.class, new String[]{ "IA:0001" } );
        class2mi.put( CvTissue.class, new String[]{ "IA:0002" } );

        // mapping of the non DAG term to their shortlabel
        mi2name.put( "MI:0300", "alias type" );
        mi2name.put( "MI:0333", "feature range status" );
        mi2name.put( "MI:0353", "xref type" );
        mi2name.put( "MI:0444", "database citation" );
        mi2name.put( "MI:0495", "experimental role" );
        mi2name.put( "MI:0500", "biological role" );
        mi2name.put( "MI:0590", "attribute name" );

        mi2name.put( "IA:0001", "cell type" );
        mi2name.put( "IA:0002", "tissue" );
    }

    ///////////////////////////////
    // Instance variables

    private String definition;

    /**
     * Pool of all term contained in that ontology (contains CvTerm)
     */
    private Collection<CvTerm> cvTerms = new ArrayList<CvTerm>( 1024 );

    /**
     * Collection of term having no parent or children.
     */
    private Collection<CvTerm> orphanTerms = new ArrayList<CvTerm>( 64 );

    /**
     * Mapping of all CvTerm by their ID (String -> CvTerm).
     */
    private Map mi2cvTerm = new HashMap( 1024 );

    /**
     * Maps IntAct CV Class to Ontology Terms. node One IntAct CV can have multiple roots.
     */
    private Map intact2psi = new HashMap();

    private Collection<TermBean> invalidTerms = new ArrayList<TermBean>();



    //////////////////////////////
    // Private methods

    /**
     * Convert a Object Array into a Collection.
     *
     * @param ids the Array
     *
     * @return a non null Collection
     */
    private Collection createCollection( Object[] ids ) {
        Collection c = new ArrayList( ids.length );
        for ( int i = 0; i < ids.length; i++ ) {
            Object o = ids[ i ];
            if ( o != null ) {
                c.add( o );
            }
        }
        return c;
    }

    /////////////////////////////
    // Public methods

    /**
     * CvObject --> Collection( "MI:xxx" )
     *
     * @return a copy of the Mapping IntAct CV to MI roots
     */
    public static Map getTypeMapping() {
        return new HashMap( class2mi );
    }

    /**
     * CvObject --> Collection( "MI:xxx" ). If includeDags is false, we take out all concrete class of CvDagObject.
     *
     * @return a copy of the Mapping IntAct CV to MI roots
     */
    public static Map getTypeMapping( boolean includeDags ) {
        Map map = getTypeMapping();

        // if requested, filter out CvDagObject
        if ( ! includeDags ) {
            for ( Iterator iterator = map.keySet().iterator(); iterator.hasNext(); ) {
                Class clazzKey = (Class) iterator.next();
                if ( CvDagObject.class.isAssignableFrom( clazzKey ) ) {
                    iterator.remove();
                }
            }
        }

        return map;
    }

    public static Map getNameMapping() {
        return new HashMap( mi2name );
    }

    /**
     * Add a new Term in the pool. It will be indexed by its ID.
     *
     * @param term
     */
    public void addTerm( CvTerm term ) {

        cvTerms.add( term );
        String id = term.getId();
        if ( mi2cvTerm.containsKey( id ) ) {
            CvTerm old = (CvTerm) mi2cvTerm.get( id );
            System.err.println( "WARNING: 2 Objects have the same ID (" + id + "), the old one is being replaced." );
            System.err.println( "         old: '" + old.getShortName() + "'" );
            System.err.println( "         new: '" + term.getShortName() + "'" );
        }
        mi2cvTerm.put( id, term );
    }

    /**
     * Create a relashionship parent to child between two CvTerm.
     *
     * @param parentId The parent term.
     * @param childId  The child term.
     */
    public void addLink( String parentId, String childId ) {

        CvTerm child = (CvTerm) mi2cvTerm.get( childId );

        if ( child == null ) {
            throw new IllegalArgumentException( "Cannot find child term: " + childId );
        }

        CvTerm parent = (CvTerm) mi2cvTerm.get( parentId );

        if ( parent == null ) {
            throw new IllegalArgumentException( "Cannot find parent term: " + parentId );
        }

        child.addParent( parent );
        parent.addChild( child );
    }

    public void setDefinition( String definition ) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

    /**
     * Answer the question: 'Has that ontology any term loaded ?'.
     *
     * @return true is there are any terms loaded, false otherwise.
     */
    public boolean hasTerms() {
        return cvTerms.isEmpty();
    }

    /**
     * Return all IntAct suppoorted CvObject implementation.
     *
     * @return a Collection of Class. never null.
     */
    public Collection getTypes() {
        Collection types = new ArrayList();

        for ( Iterator iterator = class2mi.keySet().iterator(); iterator.hasNext(); ) {
            Class clazz = (Class) iterator.next();
            types.add( clazz );
        }

        return types;
    }

    /**
     * Uses the Mapping( CV class -> Array(MI) ) to create an other mapping CV class -> Collection( CvTerm root ). That
     * method should only be called once all CvTerm have been added.
     */
    public void updateMapping() {

        if ( ! intact2psi.isEmpty() ) {
            System.out.println( "WARNING: UpdateMapping requested, clearing existing mapping." );
            intact2psi.clear();
        }

        for ( Iterator iterator = class2mi.keySet().iterator(); iterator.hasNext(); ) {
            Class clazz = (Class) iterator.next();

            Object[] mis = (Object[]) class2mi.get( clazz );
            if ( mis.length == 1 ) {

                String mi = (String) mis[ 0 ];
                intact2psi.put( clazz, createCollection( new Object[]{ mi2cvTerm.get( mi ) } ) );

            } else {

                // collecting all the roots from the given set of MI references
                Collection cvTerms = new ArrayList( mis.length );
                for ( int i = 0; i < mis.length; i++ ) {
                    String mi = (String) mis[ i ];

                    cvTerms.add( mi2cvTerm.get( mi ) );
                }

                // add mapping entry
                intact2psi.put( clazz, createCollection( cvTerms.toArray() ) );
            }
        }

        // update the list of orphan terms
        for ( Iterator iterator = cvTerms.iterator(); iterator.hasNext(); ) {
            CvTerm cvTerm = (CvTerm) iterator.next();
            if ( cvTerm.getChildren().isEmpty() && cvTerm.getParents().isEmpty() ) {
                System.out.println( "Term " + cvTerm.getId() + " (" + cvTerm.getShortName() + ") is orphan." );
                orphanTerms.add( cvTerm );
            }
        }
    }

    /**
     * Search for a CvTerm by its ID.
     *
     * @param id
     *
     * @return a CvTerm or null if not found.
     */
    public CvTerm search( String id ) {
        return (CvTerm) mi2cvTerm.get( id );
    }

    /**
     * Get the Root terms of a specific IntAct CV Class.
     *
     * @param cvObjectClass
     *
     * @return Root terms
     */
    public Collection getRoots( Class cvObjectClass ) {
        return (Collection) intact2psi.get( cvObjectClass );
    }

    /**
     * Get all CvTerms of a specific IntAct CV Class.
     *
     * @param cvObjectClass
     *
     * @return All CvTerms
     */
    public Set getCvTerms( Class cvObjectClass ) {
        Collection roots = (Collection) intact2psi.get( cvObjectClass );

        Set terms = new HashSet();

        for ( Iterator iterator = roots.iterator(); iterator.hasNext(); ) {
            CvTerm root = (CvTerm) iterator.next();
            if ( root == null ) {
                throw new IllegalStateException();
            }
            terms.addAll( root.getAllChildren() );
        }

        return terms;
    }

    /**
     * Get all CvTerm.
     *
     * @return An unmodifiable collection of all CvTerms
     */
    public Collection getCvTerms() {
        return Collections.unmodifiableCollection( cvTerms );
    }

    /**
     * Returns all CvTerm that are not linked to an IntAct type.
     *
     * @return all CvTerms not linked to an IntAct type
     */
    public Collection getOrphanTerms() {

        return orphanTerms;
    }


    public Collection<TermBean> getInvalidTerms()
    {
        return invalidTerms;
    }

    public void setInvalidTerms(Collection<TermBean> invalidTerms)
    {
        this.invalidTerms = invalidTerms;
    }

    /**
     * Go through the list of all CV Term and select those that are obsolete.
     *
     * @return a non null Collection of obsolete term.
     */
    public Collection getObsoleteTerms() {

        Collection obsoleteTerms = new ArrayList();

        for ( Iterator iterator = getCvTerms().iterator(); iterator.hasNext(); ) {
            CvTerm cvTerm = (CvTerm) iterator.next();

            if ( cvTerm.isObsolete() ) {
                obsoleteTerms.add( cvTerm );
            }
        }

        return obsoleteTerms;
    }

    public Collection getIdentities() {
        return new ArrayList( mi2cvTerm.keySet() );
    }

    /////////////////////////////////
    // Utility - Display methods

    public void print(PrintStream ps) {

        ps.println( cvTerms.size() + " terms to display." );
        ps.println( intact2psi.size() + " CV types." );

        for ( Iterator iterator = intact2psi.keySet().iterator(); iterator.hasNext(); ) {
            Class aClass = (Class) iterator.next();
            Collection root = (Collection) intact2psi.get( aClass );

            int count = 0;
            if ( ! root.isEmpty() ) {
                for ( Iterator iterator1 = root.iterator(); iterator1.hasNext(); ) {
                    CvTerm cvTerm = (CvTerm) iterator1.next();
                    count += cvTerm.getAllChildren().size();
                }
            }

            ps.println( aClass + " ( " + count + " )" );
            for ( Iterator iterator1 = root.iterator(); iterator1.hasNext(); ) {
                CvTerm cvTerm = (CvTerm) iterator1.next();
                print( cvTerm, ps);
            }
            ps.println( "" );
        }
    }

    private void print( CvTerm term, String indent, PrintStream ps ) {

        ps.println( indent + term.getId() + "   " + term.getShortName() + " (" + term.getFullName() + ")" );
        for ( Iterator iterator = term.getChildren().iterator(); iterator.hasNext(); ) {
            CvTerm cvTerm = (CvTerm) iterator.next();
            print( cvTerm, indent + "  ", ps );
        }
    }

    public void print( CvTerm term, PrintStream ps) {
        print( term, "", ps );
    }

    public void print( Class clazz, PrintStream ps ) {

        System.out.println( "------------------------------------------------" );
        System.out.println( clazz.getName() );
        System.out.println( "------------------------------------------------" );
        Collection roots = (Collection) this.getRoots( clazz );
        if ( roots != null ) {
            for ( Iterator iterator = roots.iterator(); iterator.hasNext(); ) {
                CvTerm cvTerm = (CvTerm) iterator.next();

                print( cvTerm, ps );
            }
        } else {
            System.err.println( "Could not find a mapping for " + clazz.getName() );
        }
    }
}