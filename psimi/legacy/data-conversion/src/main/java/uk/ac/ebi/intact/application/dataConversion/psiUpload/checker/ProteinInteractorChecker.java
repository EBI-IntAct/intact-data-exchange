/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.checker;

import org.apache.commons.lang.exception.ExceptionUtils;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.OrganismTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinHolder;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ProteinInteractorTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.CommandLineOptions;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.util.Crc64;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;
import uk.ac.ebi.intact.util.protein.UpdateProteinsI;

import java.util.*;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class ProteinInteractorChecker {

    private static CvInteractorType cvProteinType = null;

    private static final String LINE_SEPARATOR = System.getProperty( "line.separator" );

    private static class AmbiguousBioSourceException extends Exception {

        public AmbiguousBioSourceException( String message ) {
            super( message );
        }
    }

    private final static boolean DEBUG = CommandLineOptions.getInstance().isDebugEnabled();

    /**
     * will avoid to have to search again later !
     * <p/>
     * uniprotId#taxid --> Collection(Protein)
     */
    private static final Map cache = new HashMap();


    public static boolean interatorTypeChecked = false;

    public static void checkCvInteractorType( ) {

        if ( false == interatorTypeChecked ) {

            // Load CvInteractorType( interaction / MI: )
            cvProteinType = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvInteractorType.class, CvInteractorType.getInteractionMI());
            if ( cvProteinType == null ) {
                MessageHolder.getInstance().addCheckerMessage( new Message( "Could not find CvInteractorType( interaction )." ) );
            }
            interatorTypeChecked = true;
        }
    }

    public static CvInteractorType getCvProteinType() {
        return cvProteinType;
    }

    /**
     * Search a protein in the cache
     *
     * @param id
     * @param bioSource
     *
     * @return
     */
    public static ProteinHolder getProtein( String id, String db, BioSource bioSource ) {

        String taxid = null;
        if ( null != bioSource ) {
            taxid = bioSource.getTaxId();
        }

//        System.out.println( "Search cache using: " + buildID( id, db, taxid ) );

        return (ProteinHolder) cache.get( buildID( id, db, taxid ) );
    }

    /**
     * Build an identifier for the cache
     *
     * @param id    uniprot id
     * @param taxid taxid of the biosource (can be null)
     *
     * @return a unique identifier for the given protein and taxid.
     */
    private static String buildID( final String id,
                                   final String db,
                                   final String taxid ) {
        String cacheId = id;

        if ( null != db ) {
            cacheId = cacheId + '#' + db;
        }

        if ( null != taxid ) {
            cacheId = cacheId + '#' + taxid;
        }

        return cacheId;
    }


    /**
     * Answer the question: is that protein a Splice Variant ? One (quick) way is to check if the shortlabel match the
     * pattern: XXXXXX-#
     *
     * @param label the protein label to check
     *
     * @return true is this is a splice variant, otherwise false.
     */
    private static boolean isSpliceVariant( final String label ) {
        return ( label.indexOf( '-' ) != -1 );
    }


    /**
     * Remove from a collection of Protein all those that are not related to the given taxid.
     *
     * @param proteins the collection of protein to filter out.
     * @param taxid    the taxid that the returned protein must have (can be null - in which case there is no
     *                 filtering).
     *
     * @return a new collection of proteins.
     */
    private static Collection filterByTaxid( final Collection proteins,
                                             final String taxid ) {

        if ( taxid == null ) {
            if ( DEBUG ) {
                System.out.println( "No taxid specified, returns identical collection" );
            }

            return proteins;
        }

        Collection filteredProteins = new ArrayList( proteins.size() );

        for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
            final Protein protein = (Protein) iterator.next();
            if ( taxid.equals( protein.getBioSource().getTaxId() ) ) {
                filteredProteins.add( protein );
            }
        }

        return filteredProteins;
    }


    /**
     * Get Protein from IntAct from its ID. If this is ID refers to (1) a protein, we send back a Protein only (2) a
     * splice varaint, we send back the splice variant and its master protein.
     *
     * @param id     the id of the object we are looking for (must not be null)
     * @param taxid  the taxid filter (can be null)
     *
     * @return the objects that holds either [protein, -] or [protein, spliceVariant] or null if not found.
     */
    private static ProteinHolder getIntactProtein( final String id,
                                                   final String taxid,
                                                   final ProteinInteractorTag proteinInteractor )
            throws AmbiguousBioSourceException {

        if ( id == null ) {
            throw new IllegalArgumentException( "the protein ID must not be null" );
        }

        ProteinHolder result = null;

        if ( DEBUG ) {
            System.out.println( "\ngetIntactObject(" + id + ", " + taxid + ")" );
        }

        CvDatabase uniprot = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvDatabase.class, CvDatabase.UNIPROT_MI_REF);
        CvXrefQualifier identity = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF);

        if ( isSpliceVariant( id ) ) {
            if ( DEBUG ) {
                System.out.println( "is splice variant ID" );
            }
            // the ID is a splice variant's one.
            String proteinId = id.substring( 0, id.indexOf( '-' ) );

            if ( DEBUG ) {
                System.out.println( "Protein ID: " + proteinId );
            }

            Protein protein = null;
            try {

                // search all protein having the uniprot Xref for that ID (it doesn't retreive the splice variant).
                // We only look for Xref( uniprot, identity )

                Collection proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByXrefLike(uniprot, identity, proteinId);

                if ( proteins != null ) {

                    if ( null == taxid ) {
                        // no filtering will be possible, so we have to check if there is ambiguity on
                        // which protien to pick up.
                        if ( hasMultipleBioSource( proteins ) ) {
                            StringBuffer sb = new StringBuffer( 64 );
                            sb.append( "The uniprot id: " ).append( id );
                            sb.append( " describes proteins related to multiple Biosources: " );
                            for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
                                Protein protein1 = (Protein) iterator.next();
                                BioSource biosource = protein1.getBioSource();
                                sb.append( biosource.getShortLabel() ).append( '(' );
                                sb.append( biosource.getTaxId() ).append( ')' ).append( ' ' );
                            }
                            sb.append( '.' ).append( "You need to specify a specific taxid." );
                            throw new AmbiguousBioSourceException( sb.toString() );
                        }
                    }

                    Collection filteredProteins = filterByTaxid( proteins, taxid );
                    int count = filteredProteins.size();
                    if ( count == 1 ) {
                        protein = (Protein) filteredProteins.iterator().next();
                    } else if ( count > 1 ) {
                        StringBuffer sb = new StringBuffer( 64 );
                        sb.append( "Search By Xref(" ).append( proteinId ).append( ") returned " );
                        sb.append( proteins.size() ).append( " elements" );
                        sb.append( "After filtering on taxid(" ).append( taxid ).append( "): " );
                        sb.append( filteredProteins.size() ).append( " proteins remaining" );
                        throw new AmbiguousBioSourceException( sb.toString() );
                    }
                }
            } catch ( IntactException e ) {
                e.printStackTrace();
                return null;
            }

            if ( protein == null ) {
                if ( DEBUG ) {
                    System.out.println( "Could not found the master protein (" + proteinId + ")" );
                }
                return null;
            }

            if ( DEBUG ) {
                System.out.println( "found master protein: " + protein );
            }

            // search for splice variant
            Protein spliceVariant = null;
            try {
                if ( DEBUG ) {
                    System.out.println( "search splice variant of master AC: " + protein.getAc() );
                }

                Collection spliceVariants = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByXrefLike(protein.getAc());

                if ( spliceVariants == null || spliceVariants.isEmpty() ) {
                    if ( DEBUG ) {
                        System.out.println( "No splice variant found, abort" );
                    }
                    return null;
                }

                if ( DEBUG ) {
                    System.out.println( spliceVariants.size() + " splice variants found" );
                }

                Collection filtered = filterByTaxid( spliceVariants, taxid );
                for ( Iterator iterator = filtered.iterator(); iterator.hasNext(); ) {
                    final Protein sv = (Protein) iterator.next();

                    // The splice variant shortlabel can be either the id (lowercase) of the id to which we
                    // have concatenated the biosource shortlabel (occurs when we have multiple species for 
                    // a splice variant).
                    if ( sv.getShortLabel().startsWith( id.toLowerCase() ) ) {
                        spliceVariant = sv;
                        break; // exit the loop.
                    }
                }

                if ( spliceVariant == null ) {
                    if ( DEBUG ) {
                        System.out.println( "Didn't find it !" );
                    }
                    return null;
                }

                if ( DEBUG ) {
                    System.out.println( "Selected: " + spliceVariant );
                }

            } catch ( IntactException e ) {
                e.printStackTrace();
                return null;
            }

            if ( DEBUG ) {
                System.out.println( "Create protein Holder with protein and splice variant" );
            }
            result = new ProteinHolder( protein, spliceVariant, proteinInteractor );

        } else {

            // This is not a splice variant but a protein ID.

            if ( DEBUG ) {
                System.out.println( "This is a Protein ID" );
            }

            Protein protein = null;
            try {
                Collection proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByXrefLike(uniprot, identity, id);

                if ( proteins == null || proteins.isEmpty() ) {
                    // If none found, try also with other uniprot Xrefs
                    proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByXrefLike(uniprot, id);
                }

                if ( null == taxid ) {

                    // no filtering will be possible, so we have to check if there is ambiguity on
                    // which protien to pick up.
                    if ( hasMultipleBioSource( proteins ) ) {
                        StringBuffer sb = new StringBuffer( 64 );
                        sb.append( "The uniprot id: " ).append( id );
                        sb.append( " describes proteins related to multiple Biosources: " );
                        for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
                            Protein protein1 = (Protein) iterator.next();
                            BioSource biosource = protein1.getBioSource();
                            sb.append( biosource.getShortLabel() ).append( '(' );
                            sb.append( biosource.getTaxId() ).append( ')' ).append( ' ' );
                        }
                        sb.append( '.' ).append( "You need to specify a specific taxid." );
                        throw new AmbiguousBioSourceException( sb.toString() );
                    }
                }

                Collection filteredProteins = filterByTaxid( proteins, taxid );
                int count = filteredProteins.size();
                if ( count == 1 ) {
                    protein = (Protein) filteredProteins.iterator().next();
                    if ( DEBUG ) {
                        System.out.println( "Found: " + protein );
                    }
                } else if ( count > 1 ) {
                    StringBuffer sb = new StringBuffer( 64 );
                    sb.append( "Search By Xref(" + id + ") returned " + proteins.size() + " elements. " );
                    sb.append( "After filtering on taxid(" + taxid + "): " + filteredProteins.size() +
                               " proteins remaining" );
                    throw new AmbiguousBioSourceException( sb.toString() );
                }

            } catch ( IntactException e ) {
                e.printStackTrace();
                return null;
            }

            if ( protein == null ) {
                if ( DEBUG ) {
                    System.out.println( "Could not find it in intact" );
                }
                return null;
            }

            if ( DEBUG ) {
                System.out.println( "Create protein Holder with only a protein" );
            }
            result = new ProteinHolder( protein, proteinInteractor );
        }

        return result;
    }

    /**
     * Check if the set of proteins is related to more than one biosource.
     *
     * @param proteins
     *
     * @return true if more than one distinct biosource found, else false.
     */
    private static boolean hasMultipleBioSource( final Collection proteins ) {
        Set biosources = new HashSet();

        for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
            Protein protein = (Protein) iterator.next();
            BioSource bioSource = protein.getBioSource();
            if ( null != bioSource ) {
                biosources.add( bioSource );
            }
        }

        boolean answer;

        if ( biosources.size() > 1 ) {
            answer = true;
        } else {
            answer = false;
        }

        return answer;
    }


    /**
     * @param proteinInteractor
     * @param proteinFactory
     * @param bioSourceFactory
     */
    public static void check( final ProteinInteractorTag proteinInteractor,
                              final UpdateProteinsI proteinFactory,
                              final BioSourceFactory bioSourceFactory ) {

        final OrganismTag organism = proteinInteractor.getOrganism();
        String taxId = null;
        if ( organism != null ) {
            taxId = organism.getTaxId();
        }

        final String proteinId = proteinInteractor.getPrimaryXref().getId();
        String db = proteinInteractor.getPrimaryXref().getDb();

        final String cacheId = buildID( proteinId, db, taxId );

        // if it has already been check, skip here.
        if ( cache.containsKey( cacheId ) ) {
            return;
        }

        if ( organism != null ) {
            OrganismChecker.check( organism, bioSourceFactory );
        }

        // check that the CvInteractorType( interaction is available ).
        checkCvInteractorType( );

        /**
         * We have to deal with 3 different cases:
         *
         *  (1) the protein has a UniProt Xref in which case we use the UpdateProteins
         *      to do the job.
         *
         *  (2) The protein doesn't have a UniProt ID and we create it by hand using
         *      the data found in the XML file (Xrefs, Aliases, shorltabel, fullName)
         *
         *  (3) The protein doesn't have any Xrefs, we show an error message.
         */

        if ( ! proteinInteractor.hasUniProtXref() ) {

            if ( DEBUG ) {
                System.out.println( "########## PROTEIN WITH NO UNIPROT ID (" + proteinInteractor.getPrimaryXref().getId() + ") ##########" );
            }

            if ( cache.keySet().contains( cacheId ) ) {

                if ( DEBUG ) {
                    System.out.println( "Found from cache (" + cacheId + ") ... " );
                }

            } else {

                if ( DEBUG ) {
                    System.out.println( "Checking non UniProt protein's Xrefs ... " );
                }

                // check the CvDatabases of the primary and secondary Xrefs
                XrefTag primary = proteinInteractor.getPrimaryXref();
                XrefChecker.check( primary );

                Collection secondaries = proteinInteractor.getSecondaryXrefs();
                for ( Iterator iterator = secondaries.iterator(); iterator.hasNext(); ) {
                    XrefTag secondaryRef = (XrefTag) iterator.next();
                    XrefChecker.check( secondaryRef );
                }

                // TODO search in IntAct by primaryXref and BioSource !!! give Tag only if not found.

                ProteinHolder result = null;
                Protein protein = null;
                if( XrefChecker.getCvDatabase( primary.getDb() ) != null  ) {
                    // only create the Protein is the CvDatabase of the primary Id is valid.
                    protein = getOrCreateNonUniprotProtein( proteinInteractor );
                }

                if ( protein != null ) {
                    // a protein has been foudn in the database or created.
                    result = new ProteinHolder( protein, proteinInteractor );
                    cache.put( cacheId, result );
                }
            }

        } else {

            // it has a UniProt Xref
            final XrefTag uniprotDef = proteinInteractor.getPrimaryXref();
            XrefChecker.check( uniprotDef );

            /**
             * -STATEGY-
             *
             * 2 cases: the user can have requested
             *              (1) to reuse existing protein in which case the UpdateProtein
             *                  is only called if the protein is not found in the IntAct node.
             *              (2) to force update, in which case the UpdateProteins is called
             *                  for every single ID in order to have up-to-date data.
             *
             * BEWARE: the uniprot ID given in the XML file can refer to either a protein or a splice variant
             *         and we link that ID to the relevant objects.
             */

            /**
             * 1. retreive either protein or splice variant from IntAct
             *    getIntactObject( Object[2] prot_and_sv,  )
             *    1a. if something as been found, cache it and finish
             *    1b. if not, use UpdateProteins to get the data and search again.
             *
             * 2. do as in 1.
             *    2a. if found ok
             *    2b. if not, error.
             *
             *
             *
             * 1. details
             * ----------
             *
             * ID could be P12345 (Protein) or Q87264-2 (Splice variant)
             *
             * if (ID is splice variant) {
             *     search by shortlabel (lowercase(ID))
             *     get also the master protein using the xref to its AC.
             * } else {
             *    search by xref
             * }
             *
             */

            if ( DEBUG ) {
                System.out.println( "\nChecking on " + cacheId );
            }

            if ( !cache.keySet().contains( cacheId ) ) {

                // cache:  [uniprotID, taxid] -> [protein, spliceVariant] or [protein, null]

                String source = null;
                ProteinHolder result = null;

                // WARNING:
                // if we ask for reuse of protein, and let's say a Protein A is already in IntAct but the entry
                // in SRS has now 2 proteins (A and B). If no taxid is specified and reuseProtein requested, we might
                // happily take A instead of throwing an error because of the existence of B that make the case ambiguous.
                // Hence, reuse protein must be used only if taxid is not null !
                if ( CommandLineOptions.getInstance().reuseProtein() && taxId != null ) {
                    // check if the proteins are in IntAct
                    source = "IntAct";

                    if ( DEBUG ) {
                        System.out.println( "Searching in Intact..." );
                    }

                    // taxid is not null here so no exception can be thrown.
                    try {
                        result = getIntactProtein( proteinId, taxId, proteinInteractor );
                    } catch ( AmbiguousBioSourceException e ) {
                        // we should never get here ! but just in case ...
                        MessageHolder.getInstance().addCheckerMessage( new Message( e.getMessage() ) );
                        System.out.println( e.getMessage() );
                    }
                }

                // retreived by ID having different taxid

                if ( result == null ) { // always null if taxId == null or no reuseProtein requested.
                    // Update database
                    if ( DEBUG ) {
                        System.out.println( "Protein not found in intact, updating..." );
                    }
                    source = "UpdateProteins";
                    Collection tmp = proteinFactory.insertSPTrProteins( proteinId, taxId, true ); // taxId can be null !

                    Map exceptions = proteinFactory.getParsingExceptions();
                    if ( !exceptions.isEmpty() ) {
                        // there was exception during update, the proteins hasn't been updated.
                        StringBuffer messageBuffer = new StringBuffer( 128 );
                        messageBuffer.append( "Could not update the protein " ).append( proteinId );
                        messageBuffer.append( " using UpdateProteins, a parsing error occured." );

                        // get stacktraces
                        for ( Iterator iterator = exceptions.values().iterator(); iterator.hasNext(); ) {
                            Throwable t = (Throwable) iterator.next();
                            messageBuffer.append( ExceptionUtils.getStackTrace( t ) );
                            messageBuffer.append( LINE_SEPARATOR ).append( "============================================" );
                        }

                        String msg = messageBuffer.toString();
                        MessageHolder.getInstance().addCheckerMessage( new Message( msg ) );
                        System.err.println( msg );
                    } else {

                        if ( DEBUG ) {
                            System.out.println( tmp.size() + " Protein created/updated." );
                        }

                        // search against updated database
                        try {
                            result = getIntactProtein( proteinId, taxId, proteinInteractor );
                        } catch ( AmbiguousBioSourceException e ) {
                            MessageHolder.getInstance().addCheckerMessage( new Message( e.getMessage() ) );
                            System.out.println( e.getMessage() );
                        }
                    }
                }


                if ( result == null ) {
                    // error
                    final String msg = "Could not find Protein for uniprot ID: " + proteinId + " and BioSource " + taxId;
                    MessageHolder.getInstance().addCheckerMessage( new Message( msg ) );
                    System.err.println( msg );

                } else {

                    if ( result.isSpliceVariantExisting() ) {

                        String svLabel = result.getSpliceVariant().getShortLabel();
                        String pLabel = result.getProtein().getShortLabel();
                        System.out.println( "Found 1 splice variant (" + svLabel + ") and its master protein (" + pLabel +
                                            ") for uniprot ID: " + proteinId + " (" + source + ")" );
                    } else {

                        String pLabel = result.getProtein().getShortLabel();
                        System.out.println( "Found 1 protein (" + pLabel + ") for uniprot ID: " +
                                            proteinId + " (" + source + ")" );
                    }
                }

                cache.put( cacheId, result );
            } else {
                if ( DEBUG ) {
                    System.out.println( "Found from cache ... " );
                }
            }
        }
    } // check

    private static void createXref( Protein protein,
                                    XrefTag xrefTag,
                                    boolean identity ) throws IntactException {

        CvXrefQualifier qualifier = null;
        if ( identity ) {
            qualifier = ControlledVocabularyRepository.getIdentityQualifier();
        }

        CvDatabase database = XrefChecker.getCvDatabase( xrefTag.getDb() );
        if ( database == null ) {

            // failed to find the database, skip the Xref creation

        } else {

            InteractorXref xref = new InteractorXref( IntactContext.getCurrentInstance().getInstitution(),
                                  database,
                                  xrefTag.getId(),
                                  xrefTag.getSecondary(),
                                  xrefTag.getVersion(),
                                  qualifier );

            protein.addXref( xref );
            IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getXrefDao().persist( xref );
        }
    }

    /**
     * In case of Non uniprot protein, we check if it already exist in IntAct (based on biosource, primaryid and
     * CvDatabase) and if not we create it.
     *
     * @param proteinInteractor protein attributes.
     *
     * @return the created protein or null if it fails.
     */
    private static Protein getOrCreateNonUniprotProtein( ProteinInteractorTag proteinInteractor ) {

        Protein protein = null;

        String proteinId = proteinInteractor.getPrimaryXref().getId();
        String db = proteinInteractor.getPrimaryXref().getDb();

        try {
            // firstly search by primaryID
            Collection proteins = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().getByXrefLike(proteinId);

//            System.out.println( ">>>>>>>>>>>" + proteins.size() + " protein found by Xref(" + proteinId + ")" );

            if ( ! proteins.isEmpty() ) {
                // check that the Xref has the same CvDatabase.

                for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
                    Protein protein1 = (Protein) iterator.next();

//                    System.out.println( ">>>>>>>>>>> Protein: " + protein1.getAc() );

                    if ( protein1.getBioSource().getTaxId().equals( proteinInteractor.getOrganism().getTaxId() ) ) {

//                        System.out.println( ">>>>>>>>>>> Same BioSource" );

                        boolean xrefFound = false;
                        for ( Iterator iterator2 = protein1.getXrefs().iterator(); iterator2.hasNext() && ! xrefFound; )
                        {
                            Xref xref = (Xref) iterator2.next();

                            if ( xref.getPrimaryId().equals( proteinId ) ) {
//                                System.out.println( ">>>>>>>>>>> Same Primary id" );

                                // check qualifier
                                if ( ControlledVocabularyRepository.getIdentityQualifier().equals( xref.getCvXrefQualifier() ) )
                                {
//                                    System.out.println( ">>>>>>>>>>> Same qualifier(identity)" );

                                    // check database
                                    if ( xref.getCvDatabase().getShortLabel().equals( db ) ) {
//                                        System.out.println( ">>>>>>>>>>> Same CvDatabase" );

                                        xrefFound = true;
                                    }
                                }
                            }
                        }

                        if ( !xrefFound ) {
                            iterator.remove(); // only keep matching protein
                        }

                    } else {
                        // biosource taxid mismatch
                        iterator.remove(); // only keep matching protein
                    }
                }

                // check our findings ...
                switch ( proteins.size() ) {
                    case 0:
                        // do nothing a new one will be created
                        break;
                    case 1:
                        protein = (Protein) proteins.iterator().next();
                        System.out.println( "Found 1 protein (" + protein.getShortLabel() + ") for Primary ID: " +
                                            proteinId + ", Database: " + db + " (IntAct)" );
                        break;
                    default:
                        // more than one is an error that the user has to solve.
                        // that should never happen ... but who knows ...
                        String message = "Could not find a unique instance of the protein having Biosource(" +
                                         proteinInteractor.getOrganism().getTaxId() + ") and Xref: " + db + "(" +
                                         proteinId + "):";

                        // list all ACs
                        for ( Iterator iterator = proteins.iterator(); iterator.hasNext(); ) {
                            Protein protein1 = (Protein) iterator.next();

                            message += protein1.getAc();

                            if ( iterator.hasNext() ) {
                                message += ", ";
                            }
                        }

                        // we should never get here ! but just in case ...
                        MessageHolder.getInstance().addCheckerMessage( new Message( message ) );
                        System.out.println( message );

                        return null; // break here
                }
            }


            if ( protein == null ) {

                // create non uniprot proteins
                CvInteractorType proteinType = IntactContext.getCurrentInstance().getCvContext().getByMiRef(CvInteractorType.class, CvInteractorType.getProteinMI());

                if ( proteinType == null ) {

                    String message = "Could not find a CvInteractorType by MI reference: " + CvInteractorType.getProteinMI();

                    // we should never get here ! but just in case ...
                    MessageHolder.getInstance().addCheckerMessage( new Message( message ) );
                    System.out.println( message );

                    return null;
                }

                String shortlabel = proteinInteractor.getShortlabel().toLowerCase();

                BioSource bioSource = OrganismChecker.getBioSource( proteinInteractor.getOrganism() );
                if ( bioSource == null ) {
                    String message = "Could not find a BioSource using the definition: " + proteinInteractor.getOrganism();

                    // we should never get here ! but just in case ...
                    MessageHolder.getInstance().addCheckerMessage( new Message( message ) );
                    System.out.println( message );

                    return null;
                }


                protein = new ProteinImpl( IntactContext.getCurrentInstance().getInstitution(), bioSource, shortlabel, proteinType );
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().persist( (ProteinImpl)protein );

                // add Xrefs
                createXref(  protein, proteinInteractor.getPrimaryXref(), true );

                Collection secondaryXrefs = proteinInteractor.getSecondaryXrefs();
                for ( Iterator iterator = secondaryXrefs.iterator(); iterator.hasNext(); ) {
                    XrefTag xrefTag = (XrefTag) iterator.next();
                    createXref(  protein, xrefTag, false );
                }

                // update sequence/crc64 if any
                if ( proteinInteractor.getSequence() != null && proteinInteractor.getSequence().length() > 0 ) {
                    // update sequence
                    protein.setSequence(  proteinInteractor.getSequence() );

                    // update CRC64
                    protein.setCrc64( Crc64.getCrc64( proteinInteractor.getSequence() ) );
                }

                // add no-uniprot-update annotation
                Annotation annotation = new Annotation( IntactContext.getCurrentInstance().getInstitution(),
                                                        ControlledVocabularyRepository.getNoUniprotUpdateTopic() );
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getAnnotationDao().persist( annotation );
                proteinType.addAnnotation( annotation );
                IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getProteinDao().update( (ProteinImpl)protein );

                System.out.println( "Created 1 protein (" + protein.getShortLabel() + ") for Primary ID: " +
                                    proteinId + ", Database: " + db + " (Not UniProt)" );
            }

        } catch ( IntactException ie ) {
            String message = "Could not for the protein having Biosource(" +
                             proteinInteractor.getOrganism().getTaxId() + ") and Xref: " + db + "(" +
                             proteinId + "). Reason: " + ie.getMessage();
            ie.printStackTrace();

            // we should never get here ! but just in case ...
            MessageHolder.getInstance().addCheckerMessage( new Message( message ) );
            System.out.println( message );
        }

        return protein;
    }
}