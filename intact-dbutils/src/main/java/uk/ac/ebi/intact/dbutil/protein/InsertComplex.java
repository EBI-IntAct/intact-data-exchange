/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.dbutil.protein;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.ExperimentDao;
import uk.ac.ebi.intact.util.HttpProxyManager;
import uk.ac.ebi.intact.util.NewtServerProxy;
import uk.ac.ebi.intact.util.protein.UpdateProteins;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Insert complex data for Ho and Gavin publications. Data is read from an input text file.
 * <p/>
 * Input file format: Line records, elements are space-delimited: Interaction Bait   Preys         Experiment number 12
 * Q05524 P00330 Q05524 gavin
 *
 * @author Henning Hermjakob, hhe@ebi.ac.uk
 */
public final class InsertComplex {

    public static final String NEW_LINE = System.getProperty( "line.separator" );
    private final static org.apache.log4j.Logger logger = Logger.getLogger( "InsertComplex" );

    private UpdateProteins proteinFactory;

    /**
     * All proteins which have been created for the current complex.
     */
    private HashMap<String, Collection<Protein>> createdProteins = null;

    /**
     * Newt Server class - used to get a valid BioSource given a taxID
     */
    private NewtServerProxy newtServer;

    /**
     * The owner (EBI) of the DB - kept here to avoid lots of unnecessary DB calls. Needs to be set for everything
     * anyway
     */
    private Institution owner;

    /**
     * The BioSource used for everything - kept here to avoid lots of unnecessary DB calls. Needs to be set for
     * everything anyway
     */
    private BioSource bioSource;

    /**
     * basic constructor - sets up intact helper and protein factory
     */
    public InsertComplex() throws Exception {
        try {
            System.out.println( "Database user:     " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbUserName() );
            System.out.println( "Database instance: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName() );

            proteinFactory = new UpdateProteins( );

            // Transactions are controlled by this class, not by UpdateProteins.
            // Set local transaction control to false.
            proteinFactory.setLocalTransactionControl( false );

            //set up newt access...
            URL newtUrl = new URL( "http://www.ebi.ac.uk/newt/display" );
            newtServer = new NewtServerProxy( newtUrl );
        } catch ( IntactException ie ) {

            //something failed with type map or datasource...
            String msg = "unable to create intact helper class";
            System.err.println( msg );
            ie.printStackTrace();
        } catch ( Exception e ) {
            //something failed with type map or datasource...
            String msg = "unable to create protein factory";
            logger.error( msg );
            e.printStackTrace();
        }
    }

    private boolean hasIdentity( Protein protein, String spAc ) {

        // as long as the BioSource of the protein is checked, we don't need to
        // filter on 'identity' Xref.
        for ( Xref xref : protein.getXrefs() ) {
//            if( "uniprot".equals( xref.getCvDatabase().getShortLabel() ) &&
//                    "identity".equals( xref.getCvXrefQualifier().getShortLabel() )&&
//                    spAc.equals(xref.getPrimaryId() )
//            ) {
            if ( CvDatabase.UNIPROT.equals( xref.getCvDatabase().getShortLabel() ) &&
                 spAc.equals( xref.getPrimaryId() )
                    ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Insert a Component object linking an Interactor to an Interaction.
     *
     * @param act  The interaction to add the Interactor to
     * @param spAc Swiss-Prot accession number of the Protein to add. If the protein does not yet exist, it will be
     *             created.
     * @param role Role of the protein in the interaction.
     *
     * @throws IntactException
     */
    public final void insertComponent( Interaction act,
                                       String spAc,
                                       CvExperimentalRole role,
                                       CvBiologicalRole bioRole ) throws IntactException {

        Collection<Protein> proteins;

        // The relevant proteins might already have been created for the current complex.
        if ( createdProteins.containsKey( spAc ) ) {

            proteins = createdProteins.get( spAc );

        } else {

            proteins = new ArrayList<Protein>(IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByXrefLike(spAc));

            if ( 0 == proteins.size() ) {
                // * If the protein does not exist, create it
                System.err.print( "P" );

                // if it is an sptr protein, create a full protein object
                proteins.addAll( proteinFactory.insertSPTrProteins( spAc ) );

                // if it looks like an sgd protein, create it with an xref to sgd
                if ( ( 0 == proteins.size() ) && ( spAc.substring( 0, 1 ).equals( "S" ) ) ) {
                    CvDatabase sgd = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvDatabase.class).getByShortLabel(CvDatabase.SGD);
                    Protein protein = proteinFactory.insertSimpleProtein( spAc, sgd, bioSource.getTaxId() );
                    proteins.add( protein );
                }
            } else {
                System.err.print( "p" );
            }

            // Save the created proteins for further use
            createdProteins.put( spAc, proteins );
        }
        Protein targetProtein = null;

        // Filter for the correct protein - the filter is done on taxid and uniprot identity (needed to distinguish protein from splice variant)
        Set<Protein> uniqProteins = new HashSet<Protein>( proteins );

        for ( Protein tmp : uniqProteins ) {
            // hasIdentity checks that the protein has a Uniprot Xref with spAc (primary or secondary)
            if ( tmp.getBioSource().getTaxId().equals( bioSource.getTaxId() ) && hasIdentity( tmp, spAc ) ) {
                if ( null == targetProtein ) {
                    targetProtein = tmp;
                } else {
                    System.err.println( tmp.getAc() + " and " + targetProtein.getAc() + " were found !!!!!!!!!!" );
                    throw new IntactException( "More than one target protein found for: " + spAc );
                }
            }
        }

        if ( null == targetProtein ) {
            throw new IntactException( "No target protein found for: " + spAc );
        }

        //now build the Component.....
        Component comp = new Component( owner, act, targetProtein, role, bioRole );
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getComponentDao().persist( comp );
    }

    /**
     * Utility method to get an Experiment. If one cannot be found then an new one will be created using the owner and
     * BioSource objects already built.
     *
     * @param experimentLabel Label to search the DB for the Experiment
     *
     * @return either an exisitng Experiment or a (valid) new one.
     *
     * @throws IntactException thrown if there was a problem searching the DB
     */
    private Experiment getExperiment( String experimentLabel ) throws IntactException {

        ExperimentDao expDao = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getExperimentDao();

        // Get experiment from the local node
        logger.debug( "looking for an Experiment in the DB with label " + experimentLabel + " ...." );
        Experiment ex = expDao.getByShortLabel(experimentLabel);

        if ( null == ex ) {
            // create it - NB under the new model the Experiment needs
            //a non-null owner, shortLabel and BioSource....
            logger.debug( "no Experiment found with label " + experimentLabel + ": creating a new one..." );
            ex = new Experiment( owner, experimentLabel, bioSource );
            //NB as this is already in a TX scope the Experiment will not be persisted yet!!

            expDao.persist( ex );
            logger.debug( "checking it was persisted....." );
            Experiment dummy = expDao.getByShortLabel( experimentLabel );
            if ( dummy == null ) {
                logger.debug( "Error: new Experiment not created; may be nested TX problem..." );
                throw new IllegalStateException( "Failed to save experiment(" + experimentLabel + "). Please check the persistence layer." );
            }
        }

        boolean needUpdate = false;
        if ( null == ex.getCvInteraction() ) {
            needUpdate = true;

            logger.debug( "Oops, an Experiment in the DB does not have a CvInteraction!" );
            logger.debug( "Setting one for it now...." );
            // Give that experiment a default CvInteraction
            Collection result = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvInteraction.class).getByShortLabelLike(CvInteraction.EXPERIMENTAL_INTERACTION);

            if ( result.size() == 1 ) {
                CvInteraction cvInteraction = (CvInteraction) result.iterator().next();
                ex.setCvInteraction( cvInteraction );
            } else {
                logger.debug( "ERROR! Found " + result.size() + "  CvInteraction by shortlabel: " + CvInteraction.EXPERIMENTAL_INTERACTION );
                throw new IntactException( "failed to add CvInteraction to Experiment - " +
                                           "multiple object found by shortlabel: " + CvInteraction.EXPERIMENTAL_INTERACTION + "." );
            }
        }

        if ( null == ex.getCvIdentification() ) {
            needUpdate = true;

            logger.debug( "Oops, an Experiment in the DB does not have a CvIdentification!" );
            logger.debug( "Setting one for it now...." );
            // Give that experiment a default CvIdentification
            Collection result = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvIdentification.class).getByShortLabelLike(CvIdentification.WESTERN_BLOT);
            if ( result.size() == 1 ) {
                CvIdentification cvIdentification = (CvIdentification) result.iterator().next();
                ex.setCvIdentification( cvIdentification );
            } else {
                logger.debug( "ERROR! Found " + result.size() + " CvIdentification by shortlabel: " + CvIdentification.WESTERN_BLOT );
                throw new IntactException( "failed to add CvIdentification to Experiment - " +
                                           "multiple object found by shortlabel: " + CvIdentification.WESTERN_BLOT + "." );
            }

        }

        //Experiments in the DB SHOULD have a BioSource - but just in case
        //they don't.....
        if ( ex.getBioSource() == null ) {
            logger.debug( "Oops, an Experiment in the DB does not have a BioSource!" );
            logger.debug( "Setting one for it now...." );
            if ( bioSource != null ) {
                ex.setBioSource( bioSource );
                logger.debug( "BioSource must already be a valid persistent one -" );
                logger.debug( "Using details as follows.." );
                logger.debug( "TaxId: " + bioSource.getTaxId() );
                logger.debug( "AC: " + bioSource.getAc() );
            } else {
                logger.debug( "ERROR! Don't have a valid BioSource to set in an Experiment" );
                throw new IntactException( "failed to add BioSource to Experiment - no bioSource!" );
            }


        }
        logger.debug( "Experiment: " + ex );

        if ( needUpdate == true ) {
            //persist the change (assumes TX started outside this method..)
            expDao.saveOrUpdate(ex);

            logger.debug( "needUpdate called on Experiment - BioSource added (but not yet persistent).." );
        }
        return ex;
    }

    /**
     * Private method to get the CV info for an Interaction type. As this is not mandatory if the type cannot be found
     * it can safely be ignored.
     *
     * @param typeLabel The label used to find the Interaction type (may be null)
     *
     * @return CvInteractionType the CV data if found, null otherwise
     */
    private CvInteractionType getInteractionType( String typeLabel ) {

        CvInteractionType cvType = null;
        if ( typeLabel != null ) {
            try {
                cvType = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvInteractionType.class).getByShortLabel(typeLabel);

                if ( cvType == null ) {
                    // TODO: Problem if type is unknown - do what??
                    logger.debug( typeLabel + " is not known as a CvInteractionType shortLabel." );
                }
            } catch ( IntactException ie ) {
                // this is not mandatory, skip it.
            }
        }
        return cvType;
    }


    /**
     * Inserts a complex into the database If the complex already exists, it is skipped!
     *
     * @param interactionNumber The number of the interaction in the publication. Used for the shortLabel.
     * @param bait              Swiss-Prot accession number of the bait protein.
     * @param preys             Swiss-Prot accession numbers of the prey proteins.
     * @param actLabel          The short label to be used for the Interaction
     * @param experiment        The Experiment that the Complex belongs to
     *
     * @throws Exception
     */
    public final void insertComplex( String interactionNumber,
                                     String bait,
                                     Vector preys,
                                     String actLabel,
                                     Experiment experiment,
                                     String interactionTypeLabel ) throws Exception {

        // Get Interaction
        // The label is the first two letters of the experiment label plus the interaction number
        //String actLabel = experimentLabel.substring(0, 2) + "-" + interactionNumber;
        //Interaction interaction = (Interaction) helper.getObjectByLabel(Interaction.class, actLabel);
        //if (null == interaction) {

        Collection<Experiment> experiments = new ArrayList<Experiment>();
        //Experiment experiment = getExperiment(experimentLabel);
        experiments.add( experiment );

        // if requested, try to set the CvInteractionType.
        CvInteractionType cvInteractionType = getInteractionType( interactionTypeLabel );

        // Get the default interactor type for an interaction.
        CvInteractorType cvInteractorType = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvInteractorType.class)
                .getByXref(CvInteractorType.getInteractionMI());

        //got our data - now build the new Interaction (with an empty component Collection)
        //get the info needed to create a new Interaction and build one...

        InteractionImpl interaction = new InteractionImpl(
                experiments, cvInteractionType, cvInteractorType, actLabel, owner );
        interaction.setBioSource( experiment.getBioSource() );

        IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().persist( interaction );

        // Initialise list of proteins created
        createdProteins = new HashMap<String, Collection<Protein>>();

        // add bait
        CvExperimentalRole cvBait =  IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvExperimentalRole.class).getByPsiMiRef(CvExperimentalRole.BAIT_PSI_REF);
        CvExperimentalRole cvPrey = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvExperimentalRole.class).getByPsiMiRef(CvExperimentalRole.PREY_PSI_REF);
        CvBiologicalRole cvUnspecified = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvBiologicalRole.class).getByPsiMiRef(CvBiologicalRole.UNSPECIFIED_PSI_REF);

        insertComponent( interaction, bait, cvBait, cvUnspecified );

        // add preys
        for ( int i = 0; i < preys.size(); i++ ) {
            String prey = (String) preys.elementAt( i );
            insertComponent( interaction, prey, cvPrey, cvUnspecified );
        }

        // link interaction to experiment
        experiment.addInteraction( interaction );

        // No need to do an update here because we have created a new Interaction.
        // In fact, it is an error to do so because you can only update objects that
        // are already in the DB.
//            helper.update(interaction);
        System.err.print( "C" );
        //}
        //else {
        //System.err.print("c");
        //}
        // Only update if the object exists in the database. Since
        // the transaction is outside this method, do nothing for creation as it
        // is handled upon committing the transaction.
        // NOTE: This update causes problem with the postgres driver. It seems to be OK
        // with the oracle though. The indirection table is properly updated irrespective
        // of this statement (probably by adding interaction to the experiment).
//        if (helper.isPersistent(experiment)) {
//            helper.update(experiment);
//        }
    }


    /**
     * @param filename        the filename to parse
     * @param taxId           the taxId
     * @param interactionType the CvInteractionType shortlabel which will allow to retreive the right object from the
     *                        database and then to link it to the created interactions.
     *
     * @throws Exception
     */
    public final void insert( String filename, String taxId, String interactionType ) throws Exception {

        //makes sense to get the Institution here - avoids a call
        //to the DB for every line processed...
        owner = IntactContext.getCurrentInstance().getInstitution();

        // TODO: this block below could be replaced by the BioSourceFactory !!!!!!!

        //now get a valid BioSource - has to be done via newt,
        //and we don't want to go to get one every time a line
        //is processed so do it here....
        NewtServerProxy.NewtResponse response;
        try {
            System.out.println( "Attempting to get BioSource info from Newt server....." );
            response = newtServer.query( Integer.parseInt( taxId ) );
        } catch ( NewtServerProxy.TaxIdNotFoundException txe ) {
            logger.debug( "Error - failed to find BioSource with Tax ID " + taxId );
            throw new Exception( "failed to get BioSource - cannot proceed with data loading!" );
        } catch ( IOException ioe ) {
            logger.debug( "IO Error - failed to access newt server to obtain BioSource" );
            throw new Exception( "IO Error trying to get BioSource - cannot proceed with data loading!" );
        }
        String bioLabel;
        if ( response == null ) {
            throw new Exception( "No response from Newt server!!" );
        }
        if ( response.hasShortLabel() ) {
            bioLabel = response.getShortLabel();
        } else {
            throw new Exception( "Error - need a BioSource with a short label: tax ID "
                                 + taxId + " does not have one!" );
        }
        String bioName = response.getFullName();

        //check the DB to see if it's already there (and a consistent one!)-
        //if not it will need persisting....
        bioSource = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBioSourceDao().getByTaxonIdUnique(taxId);

        if ( ( bioSource == null ) || ( bioSource.getOwner() == null ) ||
             ( bioSource.getTaxId() == null ) ) {

            logger.debug( "No BioSource with TaxId "
                          + taxId + " exists yet - creating a new one.." );
            try {
                //have either no object or an invalid state - make a new one
                //(provided the TaxId hasn't already been used - it must be
                //unique..)
                bioSource = new BioSource( owner, bioLabel, taxId );
                logger.debug( "new BioSource created, with shortLabel "
                              + bioSource.getShortLabel()
                              + " and taxId " + bioSource.getTaxId() );
                bioSource.setFullName( bioName );   //set for good measure...

                //first make sure there is a BioSource that is persistent....
                //NB this must be done in a seperate TX as it is needed later...
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBioSourceDao().persist( bioSource );

            } catch ( Exception ie ) {
                ie.printStackTrace();
                System.err.println();
                System.err.println( "Error persisting the BioSource for the data! (maybe taxId exists?)" );
                System.err.println( ie.getMessage() );
            }
        }
        logger.debug( "BioSource Details:" );
        logger.debug( "Short Label: " + bioSource.getShortLabel() );
        logger.debug( "Tax Id: " + bioSource.getTaxId() );
        logger.debug( "AC: " + bioSource.getAc() );

        // Parse input file line by line

        FileReader fr = null;
        BufferedReader file = null;
        try {
            fr = new FileReader( filename );
            file = new BufferedReader( fr );
            String line;
            int lineCount = 0;

            System.out.print( "Lines processed: " );

            while ( null != ( line = file.readLine() ) ) {

                // Tokenize lines
                StringTokenizer st = new StringTokenizer( line );
                String interactionNumber = st.nextToken();
                String bait = st.nextToken();
                Vector<String> preys = new Vector<String>();

                while ( st.hasMoreTokens() ) {
                    preys.add( st.nextToken() );
                }

                // remove last element from preys vector, it is the experiment identifier.
                String experimentLabel = preys.lastElement();
                preys.removeElement( preys.lastElement() );

                //Process the Complex data...

                //first see if we need to create an Interaction at all - if so
                //then we need some transactions but if not, just print the
                //legend to say the Complex already exists.....
                String actLabel = experimentLabel.substring( 0, 2 ) + "-" + interactionNumber;
                Interaction interaction = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getInteractionDao().getByShortLabel( actLabel );
                if ( interaction == null ) {

                    //Doesn't already exist - so need to do some Transactions...
                    Experiment experiment;
                    try {
                        //must persist the Experiment first (if necessary) as the Complexes
                        //need it to be 'fully' defined before they can be persisted
                        //(due to the Experiment now needing to have a BioSource defined..)
                        experiment = getExperiment( experimentLabel );

                        //now do the Complexes....
                        insertComplex( interactionNumber, bait, preys, actLabel, experiment, interactionType );

                    } catch ( Exception ie ) {
                        ie.printStackTrace();
                        if ( ie.getCause() != null ) {
                            ie.getCause().printStackTrace();
                        }
                        System.err.println();
                        System.err.println( "Error while processing input line: " );
                        System.err.println( line );
                        System.err.println( ie.getMessage() );
                    }
                } else {
                    System.err.print( "c" );
                }

                // Progress report
                if ( ( ++lineCount % 1 ) == 0 ) {
                    System.out.print( lineCount + " " );
                } else {
                    System.out.println( "." );
                }
            }
            System.out.println( NEW_LINE );
        } finally {
            // close opened streams.
            if ( file != null ) {
                try {
                    file.close();
                } catch ( IOException ioe ) {
                    ioe.printStackTrace();
                }
            }
            if ( fr != null ) {
                try {
                    fr.close();
                } catch ( IOException ioe ) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    private static void displayUsage( Options options ) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "InsertComplex -file <filename> " +
                             "-taxId <biosource.taxId> " +
                             "[-interactionType <CvInteractionType.shortLabel>]",
                             options );
    }

    private static void displayLegend() {
        System.out.println( "Legend:" );
        System.out.println( "C: new Complex created." );
        System.out.println( "c: Complex already existing." );
        System.out.println( "P: new Protein created." );
        System.out.println( "p: Protein already existing." );
    }


    /**
     * Read complex data from flat file and insert it into the database.
     *
     * @param args the command line arguments. The first argument is the InputFileName and the second argument is the
     *             the tax id of the target proteins.
     *
     * @throws Exception for any errors.
     */
    public static void main( String[] args ) throws Exception {

        /* Usage: InsertComplex -file <filename>
        *                      -taxid <biosource.taxid>
        *                      [-interactionType <CvInteractionType.shortLabel>]
        */

        // create Option objects
        Option helpOpt = new Option( "help", "print this message" );

        Option filenameOpt = OptionBuilder.withArgName( "filename" )
                .hasArg()
                .withDescription( "use given buildfile" )
                .create( "file" );
        filenameOpt.setRequired( true );

        Option taxidOpt = OptionBuilder.withArgName( "biosource.taxid" )
                .hasArg()
                .withDescription( "taxId of the BioSource to link to that Complex" )
                .create( "taxId" );
        taxidOpt.setRequired( true );

        Option interactionTypeOpt = OptionBuilder.withArgName( "CvInteractionType.shortLabel" )
                .hasArg()
                .withDescription( "Shortlabel of the existing " +
                                  "CvInteractionType to link to that Complex" )
                .create( "interactionType" );
        // Not mandatory.
        // interactionTypeOpt.setRequired( true );

        Options options = new Options();

        options.addOption( helpOpt );
        options.addOption( filenameOpt );
        options.addOption( taxidOpt );
        options.addOption( interactionTypeOpt );

        // create the parser
        CommandLineParser parser = new BasicParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args, true );

            if ( line.hasOption( "help" ) ) {
                displayUsage( options );
                System.exit( 0 );
            }

            // These argument are mandatory.
            String filename = line.getOptionValue( "file" );
            String taxid = line.getOptionValue( "taxId" );
            String interactionType = line.getOptionValue( "interactionType" );

            try {
                HttpProxyManager.setup();
            } catch ( HttpProxyManager.ProxyConfigurationNotFound proxyConfigurationNotFound ) {
                proxyConfigurationNotFound.printStackTrace();
            }

            displayLegend();

            InsertComplex tool = new InsertComplex();
            tool.insert( filename, taxid, interactionType );
            System.exit( 0 );
        } catch ( ParseException exp ) {
            // Oops, something went wrong

            displayUsage( options );

            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            System.exit( 1 );
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }
    }
}
