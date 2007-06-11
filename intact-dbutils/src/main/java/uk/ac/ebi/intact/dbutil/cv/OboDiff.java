/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv;

import org.apache.commons.cli.*;
import org.apache.commons.collections.CollectionUtils;
import uk.ac.ebi.intact.model.CvCellType;
import uk.ac.ebi.intact.model.CvTissue;
import uk.ac.ebi.intact.dbutil.cv.model.CvTerm;
import uk.ac.ebi.intact.dbutil.cv.model.IntactOntology;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Script comparing 2 OBO files.
 * <p/>
 * Shows the terms appearing in one OBO file and not in the other (and vice versa).
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01-Mar-2006</pre>
 */
public class OboDiff {

    ///////////////////////////
    // Command line support

    private final static CommandLine setupCommandLine( String[] args ) {
        // if only one argument, then dump the matching experiment classified by specied into a file

        // create Option objects
        Option helpOpt = new Option( "help", "print this message." );

        Option intactOpt = OptionBuilder.withArgName( "intactFile" )
                .hasArg( true )
                .withDescription( "The IntAct OBO file" )
                .create( "intact" );
        intactOpt.setRequired( true );

        Option psiOpt = OptionBuilder.withArgName( "psiFile" )
                .hasArg( true )
                .withDescription( "The PSI-MI OBO file" )
                .create( "psi" );
        psiOpt.setRequired( true );

        Options options = new Options();
        options.addOption( helpOpt );
        options.addOption( intactOpt );
        options.addOption( psiOpt );

        // create the parser
        CommandLineParser parser = new BasicParser();
        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse( options, args, true );
        } catch ( ParseException exp ) {
            // Oops, something went wrong
            displayUsage( options );

            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            System.exit( 1 );
        }

        if ( line.hasOption( "help" ) ) {
            displayUsage( options );
            System.exit( 0 );
        }

        return line;
    }

    private final static void displayUsage( Options options ) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "OboDiff -intact [filename] -psi [filename] ",
                             options );
    }

    ///////////////////////////
    // Private methods

    private static Collection collectIdentities( Collection cvTerms ) {
        Set identities = new HashSet( cvTerms.size() );

        for ( Iterator iterator = cvTerms.iterator(); iterator.hasNext(); ) {
            CvTerm cvTerm = (CvTerm) iterator.next();
            identities.add( cvTerm.getId() );
        }

        return identities;
    }

    private static Collection collectIdentities( IntactOntology ontology1, Class[] filters ) {

        Set filter = new HashSet();
        if ( filters != null ) {
            for ( int i = 0; i < filters.length; i++ ) {
                Class aClass = filters[ i ];

                filter.addAll( collectIdentities( ontology1.getCvTerms( aClass ) ) );
            }
        }

        return CollectionUtils.subtract( ontology1.getIdentities(), filter );
    }

    private static Collection sortAlphabetically( Collection c ) {
        List list = new ArrayList( c );
        Collections.sort( list, new Comparator() {
            public int compare( Object o1, Object o2 ) {
                String s1 = (String) o1;
                String s2 = (String) o2;
                return s1.compareTo( s2 );
            }
        } );
        return list;
    }

    private static void showMissingTerms( IntactOntology ontology1, IntactOntology ontology2 ) {

        // term not to be taken into account.
        Class[] filters = new Class[]{ CvCellType.class, CvTissue.class };

        // ABCD - CD -> AB
        Collection ont1 = collectIdentities( ontology1, filters );
        Collection ont2 = collectIdentities( ontology2, filters );

        Collection minus = sortAlphabetically( CollectionUtils.subtract( ont1, ont2 ) );

        System.out.println( "Term(s) found in " + ontology1.getDefinition() + " but not in " + ontology2.getDefinition() + ":" );
        System.out.println( "---------------------------------------------------------------------------------------------------" );
        for ( Iterator iterator = minus.iterator(); iterator.hasNext(); ) {
            String mi = (String) iterator.next();
            CvTerm cv = ontology1.search( mi );
            System.out.println( mi + " - " + cv.getShortName() );
        }

        System.out.println();
        System.out.println();
        minus = sortAlphabetically( CollectionUtils.subtract( ont2, ont1 ) );
        System.out.println( "Term(s) found in " + ontology2.getDefinition() + " but not in " + ontology1.getDefinition() + ":" );
        System.out.println( "---------------------------------------------------------------------------------------------------" );
        for ( Iterator iterator = minus.iterator(); iterator.hasNext(); ) {
            String mi = (String) iterator.next();
            CvTerm cv = ontology2.search( mi );
            System.out.println( mi + " - " + cv.getShortName() );
        }
    }

    private static void showOutdatedTerms( IntactOntology ontology1, IntactOntology ontology2 ) {

        // ABCD - CD -> CD
        Collection commons = CollectionUtils.intersection( ontology1.getIdentities(), ontology2.getIdentities() );

        System.out.println( "Term(s) showing difference(s) between " + ontology1.getDefinition() + " and " + ontology2.getDefinition() + ":" );
        System.out.println( "------------------------------------------------------------------------------------------------" );
        for ( Iterator iterator = commons.iterator(); iterator.hasNext(); ) {
            String mi = (String) iterator.next();
            CvTerm cv1 = ontology1.search( mi );
            CvTerm cv2 = ontology2.search( mi );
            if ( ! cv1.equals( cv2 ) ) {
                System.out.println( mi + " - " + cv1.getShortName() );
                // show differences
//                if( cv.sho )
            }
        }
    }

    private static void compareOntologyStructure( IntactOntology ontology1, IntactOntology ontology2 ) {

        Collection types = ontology1.getTypes();

        System.out.println( "Found " + types.size() + " types in ontology1" );
        for ( Iterator iterator = types.iterator(); iterator.hasNext(); ) {
            Class aClass = (Class) iterator.next();
            System.out.println();
            System.out.println( "=============================================" );
            System.out.println( "aClass = " + aClass );

            Collection roots1 = ontology1.getRoots( aClass );
            Collection roots2 = ontology2.getRoots( aClass );

            // compare that sub DAG
            Stack stack1 = new Stack();
            Stack stack2 = new Stack();

            pushAll( stack1, roots1,
                     stack2, roots2 );

            while ( ! ( stack1.isEmpty() || stack2.isEmpty() ) ) {
                // if none of the stack is empty.

                CvTerm term = (CvTerm) stack1.pop();

                // ...
            }
        }
    }

    /**
     * Push on stack 1 and 2 respective terms 1 and 2 in the same order.
     *
     * @param stack1
     * @param terms1
     * @param stack2
     * @param terms2
     */
    private static void pushAll( Stack stack1, Collection terms1,
                                 Stack stack2, Collection terms2 ) {

        CollectionUtils.intersection( terms1, terms2 );
        CollectionUtils.subtract( terms1, terms2 );

        for ( Iterator iterator = terms1.iterator(); iterator.hasNext(); ) {
            CvTerm term1 = (CvTerm) iterator.next();
            CvTerm term2 = null;

            term2 = findCvTerm( terms1, term1 );
            if ( term2 == null ) {
                System.out.println( "" );
            }
        }

        // but also report all terms of terms2 that are not found in terms1
        // could use minus here !!
        // intersection for push

    }

    private static CvTerm findCvTerm( Collection terms, CvTerm termToFind ) {
        for ( Iterator iterator = terms.iterator(); iterator.hasNext(); ) {
            CvTerm cvTerm = (CvTerm) iterator.next();
            if ( cvTerm.equals( termToFind ) ) {
                return cvTerm;
            }
        }
        return null;
    }

    //////////////////////
    // M A I N
    public static void main( String[] args ) throws PsiLoaderException, IOException
    {

        CommandLine commandLine = setupCommandLine( args );

        String intactFilename = commandLine.getOptionValue( "intact" );
        String psiFilename = commandLine.getOptionValue( "psi" );

        PSILoader loader1 = new PSILoader();
        PSILoader loader2 = new PSILoader();

        System.out.println( "Loading IntAct ontology: " + intactFilename );
        IntactOntology intactOntology = loader1.parseOboFile( new File( intactFilename ) );
        intactOntology.setDefinition( "IntAct Ontology" );
        System.out.println( "IntAct Ontology loaded (" + intactFilename + ")" );

        System.out.println( "Loading PSI ontology: " + psiFilename );
        IntactOntology psiOntology = loader2.parseOboFile( new File( psiFilename ) );
        psiOntology.setDefinition( "PSI Ontology" );
        System.out.println( "PSI Ontology loaded (" + psiFilename + ")" );

        showMissingTerms( intactOntology, psiOntology );

//        compareOntologyStructure( intactOntology, psiOntology );
    }
}