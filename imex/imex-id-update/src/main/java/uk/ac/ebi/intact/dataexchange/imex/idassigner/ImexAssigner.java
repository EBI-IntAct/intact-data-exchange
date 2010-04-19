/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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

package uk.ac.ebi.intact.dataexchange.imex.idassigner;

import edu.ucla.mbi.imex.central.ws.IcentralFault;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralClient;
import uk.ac.ebi.intact.bridges.imexcentral.ImexCentralException;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Assigns IMEx IDs to IntAct publications, synchronizing with IMExCentral if necessary.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexAssigner {

    private static final Log log = LogFactory.getLog( ImexAssigner.class );

    public static final Pattern IMEX_PUBLICATION_ID = Pattern.compile( "IM-\\d" );

    public static final Pattern IMEX_INTERACTION_ID = Pattern.compile( "IM-\\d+-\\d+" );

    private boolean dryRun = true;

    static List<String> excludedPublications = new ArrayList<String>();

    static {
        excludedPublications.add( "" );
    }

    private CvDatabase psimi;
    private CvDatabase intact;
    private CvDatabase imex;
    private CvXrefQualifier imexPrimary;
    private CvXrefQualifier imexSecondary;
    private CvXrefQualifier imexSource;
    private CvTopic lastImexAssigned;
    private CvTopic fullCoverage;
    private CvTopic imexCuration;

    public ImexAssigner() {
        initializeCvs();
    }

    private void initializeCvs() {
        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        psimi = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.PSI_MI_MI_REF );
        if ( psimi == null ) {
            throw new IllegalArgumentException( "You must give a non null psimi" );
        }

        intact = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.INTACT_MI_REF );
        if ( intact == null ) {
            throw new IllegalArgumentException( "You must give a non null intact" );
        }

        imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
        if ( imex == null ) {
            throw new IllegalArgumentException( "You must give a non null imex" );
        }

        imexPrimary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
        if ( imexPrimary == null ) {
            throw new IllegalArgumentException( "You must give a non null imexPrimary" );
        }

        imexSecondary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( "MI:0952" );
        if ( imexSecondary == null ) {
            throw new IllegalArgumentException( "You must give a non null imexSecondary" );
        }

        // TODO switch to MI when we get one !
        imexSource = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByShortLabel( "imex source" );
        if ( imexSource == null ) {
            throw new IllegalArgumentException( "You must give a non null imexSource" );
        }

        // TODO switch to MI when we get one !
        lastImexAssigned = daoFactory.getCvObjectDao( CvTopic.class ).getByShortLabel( "last-imex-assigned" );
        if ( lastImexAssigned == null ) {
            throw new IllegalArgumentException( "You must give a non null lastImexAssigned" );
        }

        imexCuration = daoFactory.getCvObjectDao( CvTopic.class ).getByPsiMiRef( "MI:0959" );
        if ( imexCuration == null ) {
            throw new IllegalArgumentException( "You must give a non null imexCuration" );
        }

        fullCoverage = daoFactory.getCvObjectDao( CvTopic.class ).getByPsiMiRef( "MI:0957" );
        if ( fullCoverage == null ) {
            throw new IllegalArgumentException( "You must give a non null fullCoverage" );
        }
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun( boolean dryRun ) {
        log.info( "DryRun set to " + dryRun );
        this.dryRun = dryRun;
    }

    public void update( String icUsername, String icPassword ) throws Exception {

        // TODO implement a Report object
        // TODO implement a listener approach and default implementation logging updates in CSV files (like in protein-update)
        //      processed | imex-publication | updated-imex-publication

        ImexCentralClient icc = new ImexCentralClient( icUsername, icPassword, ImexCentralClient.IC_TEST );

        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        List<Publication> publications = daoFactory.getPublicationDao().getAll();
        int totalPublicationCount = 0;
        int imexPublicationCount = 0;
        int imexExperimentCount = 0;
        int imexInteractionCount = 0;

        int newPublicationIdCount = 0;
        int newInteractionIdCount = 0;
        int newSecondaryIdCount = 0;

        int badLastImexAssigned = 0;
        int imexIdMismatch = 0;

        for ( Publication publication : publications ) {

            try {

            totalPublicationCount++;

            System.out.println( "Publication: " + publication.getShortLabel() );
            if ( excludedPublications.contains( publication.getShortLabel() ) ) {
                System.err.println( "Publication excluded by user ... skipping." );
                continue;
            }

            // Check if the publication is an imex candidate
            if ( isIntactImexExportable( publication ) ) {

                final String publicationId = publication.getPublicationId();
                edu.ucla.mbi.imex.central.ws.Publication imexPublication = icc.getPublicationById( publicationId );
                if( imexPublication != null ) {
                    System.out.println( "\tPublication already registered in the IMExCentral." );
                    System.out.println( "\t\t" + printImexPublication( imexPublication ) );
                } else {
                    System.out.println( "\tPublication not yet registered in the IMExCentral." );
                    imexPublication = icc.createPublicationById( publicationId );
                    System.out.println( "\tCreating a new record in IMExCentral" );
                    System.out.println( "\t\t" + printImexPublication( imexPublication ) );
                }

                // TODO PublicationUtils.isAccepted( Publication p )
                // TODO PublicationUtils.isToBeReviewed( Publication p )
                // TODO PublicationUtils.isOnHold( Publication p )


                // TODO update publications status according to accepted/on-hold/to-be-reviewed
                // TODO creator is always ADMIN, update that to the real curators' login

                imexPublicationCount++;

                System.out.println( "\tInvolves at least one protein-protein interaction." );

                updateImexAnnotations( daoFactory, imexCuration, fullCoverage, publication, " " );

                int lastImexId = getLastImexIdAssigned( publication, lastImexAssigned );
                System.out.println( "\tLast imex id assigned was: " + ( lastImexId == 0 ? "none" : lastImexId ) );

                String icImexId = imexPublication.getImexAccession();

                String imexId;
                if ( ! containsPrimaryImexId( publication ) ) {

                    if( ! icImexId.equals( "N/A" ) ) {
                        // already assigned, use it
                        System.out.println( "\tIMExCentral already had an ID for that publication, synchronizing..." );
                        imexId = icImexId;
                    } else {
                        // request a new id from IMExCentral
                        System.out.println( "\tRequesting a new IMEx ID from IMExCentral..." );
                        imexPublication = icc.getPublicationImexAccession( publicationId, true );
                        icImexId = imexPublication.getImexAccession();
                        imexId = icImexId;
                    }

                    System.out.println( "\tIMEx ID: " + imexId );

                    newPublicationIdCount++;

                    // Add imex-primary to the publication / exp
                    PublicationXref pubXref = new PublicationXref( publication.getOwner(), imex, imexId, imexPrimary );
                    publication.addXref( pubXref );
                    if ( !dryRun ) daoFactory.getXrefDao().persist( pubXref );

                    System.out.println( "\tCreated publication's IMEx primary: " + imexId );

                } else {

                    // Retrieve IMEx id
                    final Xref x = getPrimaryImexId( publication );
                    imexId = x.getPrimaryId();

                    if( ! imexId.equals( icImexId ) ) {
                        System.out.println( "\tERROR: the IMEx ID stored locally ("+imexId+") and the one in IMExCentral ("+icImexId+") are different" );
                        imexIdMismatch++;
                    }

                    System.out.println( "\tFound publication's IMEx primary: " + imexId );
                }

                // Assign sequential imex ids to the interactions
                boolean hasAssignedIdYet = true;
                if ( lastImexId == 0 ) {
                    System.out.println( "\tWe haven't assigned any IMEx id to these interactions yet." );
                    hasAssignedIdYet = false;
                }

                for ( Experiment experiment : publication.getExperiments() ) {

                    System.out.println( "\tProcessing experiment: " + experiment.getShortLabel() );

                    imexExperimentCount++;

                    updateImexAnnotations( daoFactory, imexCuration, fullCoverage, experiment, "\t" );

                    // Add imex id so that PSI-MI XML export run smoothly without requiring code update
                    if ( !containsPrimaryImexId( experiment ) ) {
                        // Add imex-primary to the exp
                        ExperimentXref expXref = new ExperimentXref( publication.getOwner(), imex, imexId, imexPrimary );
                        experiment.addXref( expXref );
                        if ( !dryRun ) daoFactory.getXrefDao().persist( expXref );

                        System.out.println( "\tAdded missing Experiment's IMEx primary: " + imexId );
                    }

                    for ( Interaction interaction : experiment.getInteractions() ) {

                        System.out.println( "\t\tInteraction: " + interaction.getShortLabel() + " (AC: " + interaction.getAc() + ")" );

                        if ( !involvesOnlyProteins( interaction ) ) {

                            System.out.println( "\t\tThis interaction doesn't only involve proteins ... skip it." );

                        } else {

                            imexInteractionCount++;

                            if ( AnnotatedObjectUtils.searchXrefs( interaction, imexSource ).isEmpty() ) {
                                // Add xref imex-source pointing to intact in each interaction
                                final InteractorXref sourceXref = new InteractorXref( interaction.getOwner(), psimi, intact.getIdentifier(), imexSource );
                                interaction.addXref( sourceXref );

                                if ( !dryRun ) daoFactory.getXrefDao().persist( sourceXref );

                                System.out.println( "\t\tAdded Interaction's imex-source to IntAct" );
                            }


                            final Xref primaryXref = getPrimaryImexId( interaction );
                            if ( primaryXref == null ) {
                                // Create a new one
                                lastImexId = addImexPrimary( daoFactory, interaction, imex, imexPrimary, imexId, lastImexId );
                                newInteractionIdCount++;

                            } else {

                                // check and update if necessary
                                if ( !IMEX_INTERACTION_ID.matcher( primaryXref.getPrimaryId() ).matches() ) {
                                    // We have an old IMEx id, change qualifier from primary to secondary
                                    primaryXref.setCvXrefQualifier( imexSecondary );
                                    if ( !dryRun ) daoFactory.getXrefDao().update( primaryXref );
                                    System.out.println( "\t\tUpdated Xref qualifier from imex-primary to imex-secondary for old IMEx id '" + primaryXref.getPrimaryId() );

                                    newSecondaryIdCount++;

                                    // Create a new primary imex id
                                    lastImexId = addImexPrimary( daoFactory, interaction, imex, imexPrimary, imexId, lastImexId );
                                } else {

                                    if ( !hasAssignedIdYet ) {

                                        System.err.println( "Despite the fast that publication didn't have an " +
                                                            "annotation 'last-imex-assigned' some interaction do " +
                                                            "have IMEx ids assigned (e.g. " + primaryXref.getPrimaryId()
                                                            + "). There is a chance we are assigning the same IDs to " +
                                                            "different interactions." );

                                        badLastImexAssigned++;

                                        // TODO implement a routine to go through the interaction set and identify what the last-imex-assigned really is.
                                    }

                                    System.out.println( "\t\tAn IMEx primary was already present: " + primaryXref.getPrimaryId() );
                                }
                            }
                        } // is PPI

                    } // interactions

                } // experiments

                // Store the last-imex-assigned annotation (publication level)
                updateLastImexIdAssignedAnnotation( daoFactory, lastImexAssigned, publication, lastImexId );

            } // IMEX exportable publication

            System.out.println( "-------------------------------------------------------------------------------" );

            } catch ( ImexCentralException ice ) {

                log.error( "An error occured while processing publication: " + publication.getPublicationId(), ice );

                if( ice.getCause() instanceof IcentralFault ) {
                    IcentralFault f = ((IcentralFault)ice.getCause());
                    log.error( "This exception was thrown by UCLA's IMEx Central Web Service and provided the " +
                               "following extra information. Error code: " + f.getFaultInfo().getFaultCode() +
                               ". Message: '"+ f.getFaultInfo().getMessage() +"'.");
                }
            }

        } // all publications

        // Note: this is going to blow up potentially if we load too much data !
        IntactContext.getCurrentInstance().getDataContext().commitTransaction( transactionStatus );

        System.out.println( "Total Publications processed: " + totalPublicationCount );

        System.out.println( "IMEx Publications processed:  " + imexPublicationCount );
        System.out.println( "IMEx Experiments processed:   " + imexExperimentCount );
        System.out.println( "IMEx Interactions processed:  " + imexInteractionCount );

        System.out.println( "New Publication id assigned:  " + newPublicationIdCount );
        System.out.println( "New Interaction id assigned:  " + newInteractionIdCount );
        System.out.println( "Ids made 'imex secondary':    " + newSecondaryIdCount );

        System.out.println( "Outdated 'last-imex-assigned':" + badLastImexAssigned );
        System.out.println( "Mismatching IMEx ID:          " + imexIdMismatch );
    }

    private String printImexPublication( edu.ucla.mbi.imex.central.ws.Publication imexPublications ) {
        return "[IMEx ID: " + imexPublications.getImexAccession() +
               " | Created: " + imexPublications.getCreationDate() +
               " | Owner: " + imexPublications.getOwner() +
               " | Status: " + imexPublications.getStatus() +
               "]";
    }

    private void updateImexAnnotations( DaoFactory daoFactory,
                                        CvTopic imexCuration,
                                        CvTopic fullCoverage,
                                        AnnotatedObject ao,
                                        String logPrefix ) {

        Annotation fullCoverageAnnot = buildAnnotation( ao.getOwner(), fullCoverage, "Only protein-protein interactions" );
        Annotation imexCurationAnnot = buildAnnotation( ao.getOwner(), imexCuration, "" );

        if ( !hasAnnotation( ao, fullCoverageAnnot ) ) {
            addNewAnnotation( daoFactory, ao, fullCoverageAnnot );
            System.out.println( logPrefix + "Added 'full coverage' annotation to " + ao.getClass().getSimpleName() + "." );
        } else {
            System.out.println( "Annotation 'full coverage' is already present on " + ao.getClass().getSimpleName() + ": " + ao.getShortLabel() );
        }

        if ( !hasAnnotation( ao, imexCurationAnnot ) ) {
            addNewAnnotation( daoFactory, ao, imexCurationAnnot );
            System.out.println( logPrefix + "Added 'imex curation' annotation to " + ao.getClass().getSimpleName() + "" );
        } else {
            System.out.println( "Annotation 'imex curation' is already present on " + ao.getClass().getSimpleName() + ": " + ao.getShortLabel() );
        }
    }

    private Annotation buildAnnotation( Institution owner, CvTopic topic, String text ) {
        return new Annotation( owner, topic, text );
    }

    private void updateLastImexIdAssignedAnnotation( DaoFactory daoFactory, CvTopic lastImexAssigned, Publication publication, int lastIMexId ) {
        final Annotation annot = getLastImexIdAnnotation( publication, lastImexAssigned );
        if ( annot != null ) {
            annot.setAnnotationText( String.valueOf( lastIMexId ) );
            if ( !dryRun ) daoFactory.getAnnotationDao().update( annot );
            System.out.println( "Updated existing last-imex-assigned to " + lastIMexId );
        } else {
            // create it
            final Annotation a = new Annotation( publication.getOwner(), lastImexAssigned, String.valueOf( lastIMexId ) );
            addNewAnnotation( daoFactory, publication, a );

            System.out.println( "Created new last-imex-assigned with value " + lastIMexId );
        }
    }

    private boolean hasAnnotation( AnnotatedObject ao, Annotation a ) {
        for ( Annotation annotation : ao.getAnnotations() ) {
            if ( annotation.getCvTopic().equals( a.getCvTopic() ) ) {
                final String t1 = annotation.getAnnotationText();
                final String t2 = a.getAnnotationText();
                if ( StringUtils.equals( t1, t2 ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    private void addNewAnnotation( DaoFactory daoFactory, AnnotatedObject ao, Annotation a ) {
        if ( !dryRun ) {
            daoFactory.getAnnotationDao().persist( a );
            ao.addAnnotation( a );
            if ( ao instanceof Publication ) {
                daoFactory.getPublicationDao().update( ( Publication ) ao );
            } else if ( ao instanceof Experiment ) {
                daoFactory.getExperimentDao().update( ( Experiment ) ao );
            } else if ( ao instanceof InteractionImpl ) {
                daoFactory.getInteractionDao().update( ( InteractionImpl ) ao );
            } else {
                throw new IllegalStateException( "Unsupported Object of type '" + ao.getClass().getSimpleName() + "', cannot add an annotation." );
            }
        }
    }

    private int addImexPrimary( DaoFactory daoFactory,
                                Interaction interaction,
                                CvDatabase imex,
                                CvXrefQualifier imexSecondary,
                                String publicationImexId,
                                int lastImexId ) {
        lastImexId++;
        final String newImexId = publicationImexId + "-" + lastImexId;
        final InteractorXref sourceXref = new InteractorXref( interaction.getOwner(), imex, newImexId, imexSecondary );
        interaction.addXref( sourceXref );

        if ( !dryRun ) daoFactory.getXrefDao().persist( sourceXref );

        System.out.println( "\t\tAdded IMEx primary on Interaction: " + newImexId );

        return lastImexId;
    }

    private int getLastImexIdAssigned( Publication publication, CvTopic lastImexAssigned ) {
        for ( Annotation a : publication.getAnnotations() ) {
            if ( a.getCvTopic().equals( lastImexAssigned ) ) {
                return Integer.parseInt( a.getAnnotationText() );
            }
        }
        return 0;
    }

    private Annotation getLastImexIdAnnotation( Publication publication, CvTopic lastImexAssigned ) {
        for ( Annotation a : publication.getAnnotations() ) {
            if ( a.getCvTopic().equals( lastImexAssigned ) ) {
                return a;
            }
        }
        return null;
    }

    private boolean isIntactImexExportable( Publication publication ) {
        final Collection<Experiment> experiments = publication.getExperiments();

        if ( experiments.isEmpty() ) {
            return false;
        }

        boolean atLeastOneExperimentWithPPI = false;
        for ( Experiment experiment : publication.getExperiments() ) {

            // Note: once that boolean is true, the method is not executed anymore as || shortcuts evaluation
            atLeastOneExperimentWithPPI = atLeastOneExperimentWithPPI || hasAtLeastOnePPI( experiment );

            if ( !matchesPublicationAndYear( experiment,
                                             Arrays.asList( "Cell (0092-8674)",
                                                            "Proteomics (1615-9853)",
                                                            "Cancer Cell (1535-6108)" ),
                                             2006 ) ) {
                return false;
            }
        }

        if ( !atLeastOneExperimentWithPPI ) {
            System.out.println( "Not a single experiment in this publication involved protein-protein interactions." );
            return false;
        }

        return true;
    }

    private boolean hasAtLeastOnePPI( Experiment experiment ) {
        for ( Interaction interaction : experiment.getInteractions() ) {
            if ( involvesOnlyProteins( interaction ) ) {
                return true;
            }
        }
        return false;
    }

    private boolean involvesOnlyProteins( Interaction interaction ) {
        for ( Component component : interaction.getComponents() ) {
            if ( !( component.getInteractor() instanceof ProteinImpl ) ) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesPublicationAndYear( Experiment experiment, List<String> journalNames, int fromYear ) {
        boolean acceptPub = false;
        boolean acceptYear = false;

        String journal = null;
        int year = 0;
        for ( Annotation annotation : experiment.getAnnotations() ) {
            if ( CvTopic.JOURNAL_MI_REF.equals( annotation.getCvTopic().getIdentifier() ) &&
                 journalNames.contains( annotation.getAnnotationText() ) ) {

                journal = annotation.getAnnotationText();
                acceptPub = true;

            } else if ( CvTopic.PUBLICATION_YEAR_MI_REF.equals( annotation.getCvTopic().getIdentifier() ) ) {
                String pubYearStr = annotation.getAnnotationText();
                int pubYear = Integer.parseInt( pubYearStr );

                year = pubYear;

                if ( pubYear >= fromYear ) {
                    acceptYear = true;
                }
            }

            if ( acceptPub && acceptYear ) {
                System.out.println( "\tExperiment '" + experiment.getShortLabel() + "' was annotated from '" + journal +
                                    "' from " + year + ", thus enabling IMEx export" );
                return true;
            }
        } // annotations

        System.out.println( "\tExperiment '" + experiment.getShortLabel() +
                            "' was not annotated from an IMEx exportable journal" );
        return false;
    }

    public boolean containsPrimaryImexId( AnnotatedObject ao ) {
        return getPrimaryImexId( ao ) != null;
    }

    public Xref getPrimaryImexId( AnnotatedObject ao ) {
        final Collection<PublicationXref> xrefs = AnnotatedObjectUtils.searchXrefs( ao,
                                                                                    CvDatabase.IMEX_MI_REF,
                                                                                    CvXrefQualifier.IMEX_PRIMARY_MI_REF );
        switch ( xrefs.size() ) {
            case 0:
                return null;
            case 1:
                return xrefs.iterator().next();
            default:
                throw new IllegalStateException( "Found " + xrefs.size() + " IMEx primary ids on " +
                                                 ao.getClass().getSimpleName() + ": " + ao.getShortLabel() +
                                                 "(" + ao.getAc() + ")" );
        }
    }

    ////////////////////////
    // DEMO

    public static void main( String[] args ) throws Exception {

        if ( args.length < 3 ) {
            System.err.println( "Usage: java " +
                                "-Djavax.net.ssl.trustStore=<path.to.keystore> " +
                                "-Djavax.net.ssl.keyStorePassword=<password> " +
                                "ImexAssigner <imexcentral.username> <imexcentral.password> <intact.db.sid>" );
            System.exit( 1 );
        }

        final String icUsername = args[0];
        final String icPassword = args[1];

        final String database = args[2];
        IntactContext.initContext( new String[]{"/META-INF/" + database + ".spring.xml"} );

        ImexAssigner assigner = new ImexAssigner();
        assigner.setDryRun( true );
        assigner.update( icUsername, icPassword );
    }
}

