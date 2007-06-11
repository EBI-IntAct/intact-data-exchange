/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.model.CvTopic;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.dbutil.cv.model.*;
import uk.ac.ebi.ook.loader.impl.AbstractLoader;
import uk.ac.ebi.ook.loader.parser.OBOFormatParser;
import uk.ac.ebi.ook.model.implementation.AnnotationBean;
import uk.ac.ebi.ook.model.implementation.DbXrefBean;
import uk.ac.ebi.ook.model.implementation.TermBean;
import uk.ac.ebi.ook.model.implementation.TermSynonymBean;
import uk.ac.ebi.ook.model.interfaces.DbXref;
import uk.ac.ebi.ook.model.interfaces.TermRelationship;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wrapper class that hides the way OLS handles OBO files.
 *
 * @author Samuel Kerrien
 * @version $Id$
 * @since <pre>30-Sep-2005</pre>
 */
public class PSILoader extends AbstractLoader {

    private PrintStream output;

    public PSILoader()
    {
        this(System.out);
    }

    public PSILoader(PrintStream output)
    {
        this.output = output;
    }

    /////////////////////////////
    // AbstractLoader's methods

    protected void configure() {
        /**
         * ensure we get the right logger
         */
//        logger = Logger.getLogger( PSILoader.class );

//        logger = LoggerFactory.getLogger( PSILoader.class,
//                                          CommonUtilities.getResource( Constants.LOG4J_CONFIG_FILE, Constants.OLS_JAR ) );
        logger = Logger.getLogger( PSILoader.class );

        parser = new OBOFormatParser();
        ONTOLOGY_DEFINITION = "PSI MI";
        FULL_NAME = "PSI Molecular Interactions";
        SHORT_NAME = "PSI-MI";
        FQCN = PSILoader.class.getName();
    }

    protected void parse( Object params ) {
        try {
            Vector v = new Vector();
            v.add( (String) params );
            ( (OBOFormatParser) parser ).configure( v );
            parser.parseFile();

        } catch ( Exception e ) {
            logger.fatal( "Parse failed: " + e.getMessage(), e );
        }
    }

    protected void printUsage() {
        // done to comply to AbstractLoader requirements
    }

    //////////////////////////////
    // User's methods

    private List getRelatedSynonyms( TermBean term ) throws PsiLoaderException {

        if ( term == null ) {
            throw new IllegalArgumentException( "You must give a non null TermBean." );
        }

        List synonyms = new ArrayList( 4 ); // most of the time it will be less.

        for ( Iterator itSyn = term.getSynonyms().iterator(); itSyn.hasNext(); ) {
            TermSynonymBean syn = (TermSynonymBean) itSyn.next();
            if ( syn.getSynonymType() != null ) {
                if ( syn.getSynonymType().getName().equals( "related" ) ) {
                    synonyms.add( syn.getSynonym() );
                }
            }
        }

        if ( synonyms.isEmpty() ) {
            synonyms = Collections.EMPTY_LIST;
        }

        return synonyms;
    }

    /**
     * Returns an exact synonym if any available.
     *
     * @param term the term from which we want to get an exact_synonym.
     *
     * @return an exact synonym or null if none found.
     *
     * @throws PsiLoaderException if more than one exact synonym are available.
     */
    private String getExactSynonym( TermBean term ) throws PsiLoaderException {

        if ( term == null ) {
            throw new IllegalArgumentException( "You must give a non null TermBean." );
        }

        if ( term.getSynonyms() == null ) {
            return null;
        }

        String synonym = null;

        for ( Iterator it_syn = term.getSynonyms().iterator(); it_syn.hasNext(); ) {
            TermSynonymBean syn = (TermSynonymBean) it_syn.next();
            if ( syn.getSynonymType() != null ) {
                if ( syn.getSynonymType().getName().equals( "exact" ) ) {
                    if ( synonym != null ) {
                        throw new PsiLoaderException( "CV Term '" + term.getIdentifier() + "' has more than one " +
                                                      "exact_synonym found: '" + synonym + "' and '" +
                                                      syn.getSynonym() + "'" );
                    } else {
                        synonym = syn.getSynonym();
                    }
                }
            }
        }

        return synonym;
    }

    private static final Pattern ID_PATTERN = Pattern.compile( "(MI|IA):\\d+" ); // Matches MI:0835, IA:0075

    private boolean isIdentifierValid( TermBean term ) {
        Matcher matcher = ID_PATTERN.matcher( term.getIdentifier() );
        return matcher.matches();
    }

    /**
     * XML character escaper.
     *
     * @param input String that can be null.
     *
     * @return the escaped string or null if non was given.
     */
    private String escapeXMLTags( final String input ) {
        if ( input == null ) {
            return null;
        }
        return StringEscapeUtils.unescapeXml( input );
    }

    private IntactOntology buildIntactOntology(PrintStream output) throws PsiLoaderException {

        IntactOntology ontology = new IntactOntology();

        // 1. convert and index all terms (note: at this stage we don't handle the hierarchy)
        for ( Iterator it_term = ontBean.getTerms().iterator(); it_term.hasNext(); ) {
            TermBean term = (TermBean) it_term.next();

            String id = escapeXMLTags( term.getIdentifier() );

            if ( ! isIdentifierValid( term ) ) {
                // Skip any term that is not MI:xxxx of IA:xxxx
                ontology.getInvalidTerms().add(term);
                continue;
            }

            // try to split the name into short and long name
            String shortlabel = escapeXMLTags( getExactSynonym( term ) );
            if ( shortlabel == null ) {
                shortlabel = escapeXMLTags( term.getName() );
                if ( shortlabel != null && shortlabel.length() > 20 ) {
                    output.println( "NOTE: term " + id +
                                        " has its name longer than 20 chars. it should have an exact_synonym." );
                }
            }

            // create a CvTerm
            CvTerm cvTerm = new CvTerm( id, shortlabel );

            if ( term.getName() != null && ! "".equals( term.getName().trim() ) ) {
                cvTerm.setFullName( escapeXMLTags( term.getName() ) );
            } else {
                cvTerm.setFullName( shortlabel );
            }

            // only save definition if there a valid one.
            String definition = escapeXMLTags( term.getDefinition() );
            if ( definition != null && ! "".equals( definition.trim() ) ) {
                cvTerm.setDefinition( definition );
            }

            output.println("Term: "+id+" ("+shortlabel+")");

            // check for Xrefs: pubmed, resid...
            if ( term.getXrefs() != null ) {

                // Store in these 2 sets database using the qualifier identity or primary-reference
                // There should be only one per term.
                Set identity = new HashSet();
                Set primaryReference = new HashSet();

                for ( Iterator itXref = term.getXrefs().iterator(); itXref.hasNext(); ) {
                    DbXrefBean dbXref = (DbXrefBean) itXref.next();

                    // format of xref_analog: type:accession "description"

                    String type = escapeXMLTags( dbXref.getDbName() );
                    String accession = escapeXMLTags( dbXref.getAccession() );
                    String desc = escapeXMLTags( dbXref.getDescription() );

                    output.println("\tType: "+type+"; Accession: "+accession+"; Desc: "+desc+"; Xref type: "+dbXref.getXrefType());

                    switch ( dbXref.getXrefType() ) {

                        case DbXref.OBO_DBXREF_ANALOG :

                            if ( "ANNOTATION".equalsIgnoreCase( desc ) ) {

                                if ( type.equals( "id-validation-regexp" ) ) {
                                    // remove leading and trailing " so the dag edit parser is happy
                                    if ( accession.startsWith( "\"" ) ) {
                                        accession = accession.substring( 1, accession.length() );
                                    }

                                    if ( accession.endsWith( "\"" ) ) {
                                        accession = accession.substring( 0, accession.length() - 1 );
                                    }

                                    accession = accession.trim();
                                }

                                CvTermAnnotation annotation = new CvTermAnnotation( type, accession );
                                cvTerm.addAnnotation( annotation );

                            } else if ( "ALIAS".equalsIgnoreCase( desc ) ) {

                                CvTermSynonym synonym = new CvTermSynonym( type, accession );
                                cvTerm.addSynonym( synonym );

                            } else {
                                if (desc == null || desc.trim().equals(""))
                                {
                                    throw new InvalidOboFormatException("Description 'ALIAS' or 'ANNOTATION' missing for type "+type+" in term: "+term.getName());
                                }
                                throw new InvalidOboFormatException( "Unsupported Xref Type " + type +" for term: "+term.getName());
                            }
                            break;

                        case DbXref.OBO_DBXREF_DEFINITION :

                            // make sure that we have a unique Xref( identity, database ) and Xref( primary-reference, database )
                            if ( CvXrefQualifier.IDENTITY.equals( desc ) ) {
                                if ( identity.contains( type ) ) {
                                    throw new PsiLoaderException( "CV Term '" + id + "' has more than one " +
                                                                  "Xref(identity, " + type + ")" );
                                } else {
                                    identity.add( type );
                                }
                            } else if ( CvXrefQualifier.IDENTITY.equals( desc ) ) {
                                if ( primaryReference.contains( type ) ) {
                                    throw new PsiLoaderException( "CV Term '" + id + "' has more than one " +
                                                                  "Xref(primary-reference, " + type + "): " + accession );
                                } else {
                                    primaryReference.add( type );
                                }
                            }

                            CvTermXref xref = new CvTermXref( accession, type, desc );
                            cvTerm.addXref( xref );
                            break;

                        default:
                    }
                }
            }

            // Check for synonyms
            if ( term.getSynonyms() != null ) {
                for ( Iterator itSyn = getRelatedSynonyms( term ).iterator(); itSyn.hasNext(); ) {
                    String syn = (String) itSyn.next();

                    CvTermSynonym synonym = new CvTermSynonym( escapeXMLTags( syn ) );
                    cvTerm.addSynonym( synonym );
                }
            }

            // Check for annotation
            if ( term.getAnnotations() != null ) {
                for ( Iterator itAnnot = term.getAnnotations().iterator(); itAnnot.hasNext(); ) {
                    AnnotationBean annot = (AnnotationBean) itAnnot.next();

                    CvTermAnnotation annotation = new CvTermAnnotation( escapeXMLTags( annot.getAnnotationName() ),
                                                                        escapeXMLTags( annot.getAnnotationValue() ) );
                    cvTerm.addAnnotation( annotation );
                }
            }

            // the message related to the obsolete flag should be there as an ANNOTATION.
            cvTerm.setObsolete( term.isObsolete() );

            // try to set the obsolete message
            String message = null;
            for ( Iterator iterator = cvTerm.getAnnotations().iterator(); iterator.hasNext() && message == null; ) {
                CvTermAnnotation annotation = (CvTermAnnotation) iterator.next();
                if ( CvTopic.OBSOLETE.equals( annotation.getTopic() ) ) {
                    message = annotation.getAnnotation();
                }
            }
            if ( message != null ) {
                cvTerm.setObsoleteMessage( message );
            }

            ontology.addTerm( cvTerm );
        }

        // 2. build hierarchy based on the relations of the Terms
        for ( Iterator iterator = ontBean.getTerms().iterator(); iterator.hasNext(); ) {
            TermBean term = (TermBean) iterator.next();

            if ( term.getRelationships() != null ) {
                for ( Iterator itRelationship = term.getRelationships().iterator(); itRelationship.hasNext(); ) {
                    TermRelationship relation = (TermRelationship) itRelationship.next();

                    ontology.addLink( relation.getObjectTerm().getIdentifier(),
                                      relation.getSubjectTerm().getIdentifier() );
                }
            }
        }

        // 3. Organise a mapping between IntAct CV classes and PSI-MI Terms.
        ontology.updateMapping();

        return ontology;
    }

    public static final String NEW_LINE = System.getProperty( "line.separator" );

    public static final String SOURCEFORGE_URL = "http://psidev.sourceforge.net/mi/rel25/data/psi-mi25.obo";

    /**
     * Load the latest PSI MI definition from sourceforge.
     *
     * @return
     *
     * @throws PsiLoaderException
     */
    public IntactOntology loadLatestPsiMiFromSourceforge() throws PsiLoaderException {
        try {
            return parseOboFile( new URL( SOURCEFORGE_URL ) );
        } catch ( MalformedURLException e ) {
            throw new PsiLoaderException( "Error loading the latest definition of PSI-MI 2.5 from: " + SOURCEFORGE_URL );
        }
    }

    /**
     * Load an OBO file from an URL.
     *
     * @param url               the URL to load (must not be null)
     * @param keepTemporaryFile if set to false, the temp file containing the dwonloaded data is deleted.
     *
     * @return an ontology
     *
     * @throws PsiLoaderException
     * @see #parseOboFile( File file )
     */
    public IntactOntology parseOboFile( URL url, boolean keepTemporaryFile ) throws PsiLoaderException {

        if ( url == null ) {
            throw new IllegalArgumentException( "Please give a non null URL." );
        }

        StringBuffer buffer = new StringBuffer( 1024 * 8 );
        try {
            // Read URL content
            System.out.println( "Loading URL: " + url );

            BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ), 1024 );

            String line;
            int lineCount = 0;
            while ( ( line = in.readLine() ) != null ) {
                lineCount++;

                buffer.append( line ).append( NEW_LINE );

                // display progress bar on STDOUT
                if ( ( lineCount % 20 ) == 0 ) {
                    System.out.print( "." );
                    System.out.flush();
                    if ( ( lineCount % 500 ) == 0 ) {
                        System.out.println( "   " + lineCount );
                    }
                }
            }

            in.close();

            // Create a temp file and write URL content in it.
            File tempDirectory = new File( System.getProperty( "java.io.tmpdir", "tmp" ) );
            if ( !tempDirectory.exists() ) {
                if ( !tempDirectory.mkdirs() ) {
                    throw new IOException( "Cannot create temp directory: " + tempDirectory.getAbsolutePath() );
                }
            }
            System.out.println( "Using temp directory: " + tempDirectory.getAbsolutePath() );

            File tempFile = File.createTempFile( "psimi.v25.", ".obo", tempDirectory );
            tempFile.deleteOnExit();
            tempFile.deleteOnExit();

            System.out.println( "The OBO file is temporary store as: " + tempFile.getAbsolutePath() );

            BufferedWriter out = new BufferedWriter( new FileWriter( tempFile ), 1024 );
            out.write( buffer.toString() );
            out.flush();
            out.close();

            // Parse file
            return parseOboFile( tempFile );

        } catch ( IOException e ) {
            throw new PsiLoaderException( "Error while loading URL (" + url + ")", e );
        }
    }

    /**
     * Load an OBO file from an URL.
     *
     * @param url the URL to load (must not be null)
     *
     * @return an ontology
     *
     * @throws PsiLoaderException
     * @see #parseOboFile( File file )
     */
    public IntactOntology parseOboFile( URL url ) throws PsiLoaderException {
        return parseOboFile( url, false );
    }

    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param file the input file. It has to exist and to be readable, otherwise it will break.
     *
     * @return a non null IntactOntology.
     */
    public IntactOntology parseOboFile( File file ) throws PsiLoaderException, IOException {
        return parseOboFile(new FileInputStream(file), System.out);
    }

     /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param file the input file. It has to exist and to be readable, otherwise it will break.
     *
     * @return a non null IntactOntology.
     */
    public IntactOntology parseOboFile( File file, PrintStream output ) throws PsiLoaderException, IOException {

        if ( !file.exists() ) {
            throw new IllegalArgumentException( file.getAbsolutePath() + " doesn't exist." );
        }

        if ( !file.canRead() ) {
            throw new IllegalArgumentException( file.getAbsolutePath() + " could not be read." );
        }

        return parseOboFile(new FileInputStream(file), output);
    }

    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param stream the input file. It has to exist and to be readable, otherwise it will break.
     *
     * @return a non null IntactOntology.
     */
    public IntactOntology parseOboFile( InputStream stream ) throws PsiLoaderException, IOException {
        return parseOboFile(stream, output);
    }

    /**
     * Parse the given OBO file and build a representation of the DAG into an IntactOntology.
     *
     * @param stream the input file. It has to exist and to be readable, otherwise it will break.
     *
     * @return a non null IntactOntology.
     */
    public IntactOntology parseOboFile( InputStream stream, PrintStream output ) throws PsiLoaderException, IOException {

        File file = File.createTempFile("oboFile_", String.valueOf(System.currentTimeMillis()));
        file.deleteOnExit();

        FileWriter writer = new FileWriter(file);

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line;
        while ((line = reader.readLine())!= null)
        {
            writer.write(line+"\n");
        }
        writer.close();

        //setup vars
        configure();

        //parse obo file
        output.println( "Reading " + file.getAbsolutePath() );

        parse( file.getAbsolutePath() );

        //process into relations
        process();

        return buildIntactOntology(output);
    }
}