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

import com.google.common.collect.Lists;
import edu.ucla.mbi.imex.central.ws.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.springframework.transaction.TransactionStatus;
import uk.ac.ebi.intact.bridges.imexcentral.*;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ImexUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ImexUpdateListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.LoggingImexUpdateListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.listener.ReportWriterListener;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.FileImexUpdateReportHandler;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.util.AnnotatedObjectUtils;
import uk.ac.ebi.intact.model.util.ExperimentUtils;
import uk.ac.ebi.intact.model.util.PublicationUtils;

import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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

    // to allow listener
    protected EventListenerList listenerList = new EventListenerList();

    private ImexAssignerConfig imexUpdateConfig;

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

    boolean isCvsInitialized;

    private ImexCentralClient imexCentral;
    private static final String INTACT_PSICQUIC_REST_URL = "http://www.ebi.ac.uk/Tools/webservices/psicquic/intact/webservices/current/search/";


    //////////////////
    // Constructors

    public ImexAssigner( String icUsername, String icPassword, String icEndpoint ) throws ImexCentralException {
        this.imexCentral = new DefaultImexCentralClient( icUsername, icPassword, icEndpoint );
    }

    public ImexAssigner( ImexCentralClient client ) {
        this.imexCentral = client;
    }

    /////////////////////
    // Listeners

    protected void registerListeners() {
        addListener( new LoggingImexUpdateListener() );

        final File directory = imexUpdateConfig.getUpdateLogsDirectory();
        if( directory != null ) {
            final FileImexUpdateReportHandler handler;
            try {
                handler = new FileImexUpdateReportHandler( directory );
            } catch ( IOException e ) {
                throw new RuntimeException( "Filed to initialize ReportWriterListener: " + directory );
            }
            addListener( new ReportWriterListener( handler ) );
        }
    }

    public void addListener( ImexUpdateListener listener) {
        listenerList.add( ImexUpdateListener.class, listener);
    }

    public void removeListener( ImexUpdateListener listener) {
        listenerList.remove( ImexUpdateListener.class, listener);
    }

    protected <T> List<T> getListeners(Class<T> listenerClass) {
        List list = new ArrayList();

        Object[] listeners = listenerList.getListenerList();

        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ImexUpdateListener.class) {
                if (listenerClass.isAssignableFrom(listeners[i+1].getClass())) {
                    list.add(listeners[i+1]);
                }
            }
        }
        return list;
    }

    private void registerListenersIfNotDoneYet() {
        if (listenerList.getListenerCount() == 0) {
            registerListeners();
        }

        if (listenerList.getListenerCount() == 0) {
            throw new IllegalStateException("No listener registered for ProteinProcessor");
        }
    }

    public void fireOnProcessPublication( ImexUpdateEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onProcessPublication(evt);
        }
    }

    public void fireOnPublicationUpToDate( ImexUpdateEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onPublicationUpToDate(evt);
        }
    }

    public void fireOnProcessImexPublication( ImexUpdateEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onProcessImexPublication(evt);
        }
    }

    public void fireOnNewImexIdAssignedToPublication( ImexUpdateEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onImexIdAssignedToPublication(evt);
        }
    }

    public void fireOnNewImexIdAssignedToInteraction( ImexUpdateEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onImexIdAssignedToInteraction(evt);
        }
    }

    public void fireOnImexIdMismatchFound( ImexUpdateEvent evt) {
        for (ImexUpdateListener listener : getListeners(ImexUpdateListener.class)) {
            listener.onImexIdMismatchFound(evt);
        }
    }

    ////////////////////
    // IMEx ID UPDATE


    public ImexAssignerConfig getImexUpdateConfig() {
        return imexUpdateConfig;
    }

    public void setImexUpdateConfig( ImexAssignerConfig imexUpdateConfig ) {
        this.imexUpdateConfig = imexUpdateConfig;
    }

    private void initializeCvs() {

        if( ! isCvsInitialized ) {

            final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
            psimi = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.PSI_MI_MI_REF );
            if ( psimi == null ) {
                throw new IllegalArgumentException( "Could not find CV term:  psimi" );
            }

            intact = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.INTACT_MI_REF );
            if ( intact == null ) {
                throw new IllegalArgumentException( "Could not find CV term:  intact" );
            }

            imex = daoFactory.getCvObjectDao( CvDatabase.class ).getByPsiMiRef( CvDatabase.IMEX_MI_REF );
            if ( imex == null ) {
                throw new IllegalArgumentException( "Could not find CV term: imex" );
            }

            imexPrimary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( CvXrefQualifier.IMEX_PRIMARY_MI_REF );
            if ( imexPrimary == null ) {
                throw new IllegalArgumentException( "Could not find CV term: imexPrimary" );
            }

            imexSecondary = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByPsiMiRef( "MI:0952" );
            if ( imexSecondary == null ) {
                throw new IllegalArgumentException( "Could not find CV term: imexSecondary" );
            }

            // TODO switch to MI when we get one !
            imexSource = daoFactory.getCvObjectDao( CvXrefQualifier.class ).getByShortLabel( "imex source" );
            if ( imexSource == null ) {
                throw new IllegalArgumentException( "Could not find CV term: imexSource" );
            }

            // TODO switch to MI when we get one !
            lastImexAssigned = daoFactory.getCvObjectDao( CvTopic.class ).getByShortLabel( "last-imex-assigned" );
            if ( lastImexAssigned == null ) {
                throw new IllegalArgumentException( "Could not find CV term: last-imex-assigned" );
            }

            imexCuration = daoFactory.getCvObjectDao( CvTopic.class ).getByPsiMiRef( "MI:0959" );
            if ( imexCuration == null ) {
                throw new IllegalArgumentException( "Could not find CV term: imexCuration" );
            }

            fullCoverage = daoFactory.getCvObjectDao( CvTopic.class ).getByPsiMiRef( "MI:0957" );
            if ( fullCoverage == null ) {
                throw new IllegalArgumentException( "Could not find CV term: fullCoverage" );
            }

            isCvsInitialized = true;
        }
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun( boolean dryRun ) {
        log.info( "DryRun set to " + dryRun );
        this.dryRun = dryRun;
    }

    public void update() {

        registerListenersIfNotDoneYet();

        initializeCvs();

        // TODO implement a Report object
        // TODO implement a listener approach and default implementation logging updates in CSV files (like in protein-update)
        //      processed | imex-publication | updated-imex-publication


        final TransactionStatus transactionStatus = IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();

        // TODO browse publication by smaller chunk to minimize memory usage.
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

        int publicationInProgressCount = 0;
        int publicationReleasedCount = 0;
        int publicationAcceptedButNotReleasedCount = 0;


        for ( Publication publication : publications ) {

            fireOnProcessPublication( new ImexUpdateEvent( this, publication ) );

            try {

            totalPublicationCount++;

            System.out.println( "Publication: " + publication.getShortLabel() );
            if ( excludedPublications.contains( publication.getShortLabel() ) ) {
                System.err.println( "Publication excluded by user ... skipping." );
                continue;
            }

            // Check if the publication is an imex candidate
            if ( isIntactImexExportable( publication ) || isCuratorTriggered( publication, daoFactory ) ) {

                fireOnProcessImexPublication( new ImexUpdateEvent( this, publication ) );

                // TODO do we want to assign IMEx id to publication if no interactions are exportable (no PPI)

                final String publicationId = publication.getPublicationId();
                edu.ucla.mbi.imex.central.ws.Publication imexPublication = imexCentral.getPublicationById( publicationId );

                if( imexPublication == null ) {
                    // attempt a search by IMEx id as the publication could have gone from 'unassigned' to a real PMID
                    final Xref x = ImexUtils.getPrimaryImexId( publication );
                    if( x != null ) {
                        String imexId = x.getPrimaryId();
                        imexPublication = imexCentral.getPublicationById( imexId );

                        if( imexPublication != null ) {
                            System.out.println( "Could not find this publication by PMID ("+ publicationId +") but did by IMEx id ("+ imexId +"). " +
                                                "It may be that the PMID was changes in IntAct since the record was registered." );

                            // TODO update the PMID to what is currently in IntAct
//                        imexPublication.setIdentifier( publicationId );
                        } else {
                            System.err.println( "WARNING - This publication has an IMEx id that isn't registered in IMEx Central ("+imexCentral.getEndpoint()+")" );
                        }
                    }
                }

                if( imexPublication != null ) {

                    // TODO fire publication registered in IMExCentral

                    System.out.println( "\tPublication already registered in the IMExCentral." );
                    System.out.println( "\t\t" + printImexPublication( imexPublication ) );

                } else {
            
                    // TODO fire register new publication in IMExCentral

                    System.out.println( "\tPublication not yet registered in the IMExCentral." );

                    // FIXME this is breaking on unassigned publications !!!!
                    if( publicationId.startsWith( "unassigned" ) ) {
                        // this is a pre-publication, IMEx central doesn't support that yet. Skip registration
                        System.out.println( "\tSkipping registration in IMEx Central until it supports pre-publication with internal ID: " + publicationId );
                    } else {
                        imexPublication = imexCentral.createPublicationById( publicationId );
                        System.out.println( "\tCreating a new record in IMExCentral" );
                        System.out.println( "\t\t" + printImexPublication( imexPublication ) );
                    }
                }

                // update publications status

                PublicationStatus intactStatus = getPublicationStatus( publication );
                switch( intactStatus ) {
                    case INPROGRESS:
                        publicationInProgressCount++;
                        break;
                    case PROCESSED:
                        publicationAcceptedButNotReleasedCount++;
                        break;
                    case RELEASED:
                        publicationReleasedCount++;
                        break;
                    default:
                }

                if( imexPublication != null && ! intactStatus.toString().equals( imexPublication.getStatus() ) ) {

                    // TODO fire publication status changed

                    System.out.println( "\tAttempting to update IMEx publication status from '" +
                                        imexPublication.getStatus() + "' to '" + intactStatus + "' ..." );

                    imexCentral.updatePublicationStatus( publicationId, intactStatus, null );
                }

                // TODO only update adminGroup/adminUser when the value is different

                final String institution = "INTACT";
                imexCentral.updatePublicationAdminGroup( publicationId, Operation.ADD, institution );
                System.out.println( "Updated publication admin group to: " + institution );

                // Note: we assume the curators' login in intact are the same in IMExCentral. Also they must be all lowercase in IMExCentral.
                String curator = getPublicationCreator( publication ).toLowerCase();
                try {
                    imexCentral.updatePublicationAdminUser( publicationId, Operation.ADD, curator );
                    System.out.println( "Updated publication admin user to: " + curator );
                } catch ( ImexCentralException e ) {
                    IcentralFault f = (IcentralFault) e.getCause();
                    if( f.getFaultInfo().getFaultCode() == 10 ) {
                        // unknown user, we automaticaly reassigne this record to user phantom
                        curator = "phantom";  // this will apply to all curators that have left the group
                        imexCentral.updatePublicationAdminUser( publicationId, Operation.ADD, curator );
                        System.out.println( "Updated publication admin user to: " + curator );
                    }
                }


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
                        imexPublication = imexCentral.getPublicationImexAccession( publicationId, true );
                        icImexId = imexPublication.getImexAccession();
                        imexId = icImexId;
                    }

                    System.out.println( "\tIMEx ID: " + imexId );

                    newPublicationIdCount++;

                    // Add imex-primary to the publication / exp
                    PublicationXref pubXref = new PublicationXref( publication.getOwner(), imex, imexId, imexPrimary );
                    publication.addXref( pubXref );
                    if ( !dryRun ) daoFactory.getXrefDao().persist( pubXref );

                    fireOnNewImexIdAssignedToPublication( new ImexUpdateEvent( this, publication ) );

                    System.out.println( "\tCreated publication's IMEx primary: " + imexId );

                } else {

                    // Retrieve local IMEx id
                    final Xref x = ImexUtils.getPrimaryImexId( publication );
                    imexId = x.getPrimaryId();

                    if( ! icImexId.equals("N/A") &&  ! imexId.equals( icImexId ) ) {
                        System.out.println( "\tERROR: the IMEx ID stored locally ("+imexId+") and the one in IMExCentral ("+icImexId+", "+ imexCentral.getEndpoint() +") are different" );
                        imexIdMismatch++;

                        fireOnImexIdMismatchFound( new ImexUpdateEvent( this, publication, "IMExCentral: "+icImexId +" | LocalDB: " + imexId) );
                    }

                    fireOnPublicationUpToDate( new ImexUpdateEvent( this, publication ) );

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


                            final Xref primaryXref = ImexUtils.getPrimaryImexId( interaction );
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

                                        System.err.println( "Despite the fact that publication didn't have an " +
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

            } catch (Throwable t ) {

//               TODO fireError();
                log.error( "Error while processing publication: " + publication.getPublicationId(), t );
                t.printStackTrace();
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

        System.out.println( "Publication processed (! accepted)    : " + publicationInProgressCount );
        System.out.println( "Publication completed but not released: " + publicationAcceptedButNotReleasedCount );
        System.out.println( "Publication released (PSICQUIC)       : " + publicationReleasedCount );
    }

    private String getPublicationCreator( Publication publication ) {

        Experiment firstExperiment = null;
        // search for first experiment
        for ( Experiment exp : publication.getExperiments() ) {
            if( exp.getShortLabel().endsWith( "-1" )) {
                firstExperiment = exp;
                break;
            }
        }

        // pick the curator's name
        if( firstExperiment != null ) {
            return firstExperiment.getCreator();
        }

        // not found
        return null;
    }

    private PublicationStatus getPublicationStatus( Publication publication ) {
        // IMEx central has currently the following publication states available:
        // NEW / RESERVED / INPROGRESS / RELEASED / DISCARDED / INCOMPLETE / PROCESSED

        // in IntAct we can map to these statuses using the following annotation topics:
        //     not accepted                     --> INPROGRESS
        //     accepted                         --> PROCESSED
        //     accepted + available on PSICQUIC --> RELEASED         (needs a run of the tool post release !!)

        PublicationStatus status;

        final boolean accepted = PublicationUtils.isAccepted( publication );

        if( ! accepted ) {

            status = PublicationStatus.INPROGRESS;

        } else {

            status = PublicationStatus.PROCESSED;

            System.out.println( "The publication was accepted, now checking if it is already released." );
            PsicquicSimpleClient client = new PsicquicSimpleClient( INTACT_PSICQUIC_REST_URL );
            final String publicationId = publication.getShortLabel();
            try {

                final long count = client.countByQuery("pubid:" + publicationId);
                if(count > 0) {
                    status = PublicationStatus.RELEASED;
                }

            } catch ( IOException e ) {
                System.err.println( "Failed to query IntAct PSICQUIC for PMID: " + publicationId );
                e.printStackTrace();
            }
        }

        return status;
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
        Annotation imexCurationAnnot = buildAnnotation( ao.getOwner(), imexCuration, null );

        if ( !hasAnnotation( ao, fullCoverageAnnot ) ) {
            addNewAnnotation( daoFactory, ao, fullCoverageAnnot );
            System.out.println( logPrefix + "Added 'full coverage' annotation to " + ao.getClass().getSimpleName() + "." );
        } else {
            System.out.println( "Annotation 'full coverage' is already present on " + ao.getClass().getSimpleName() + ": " + ao.getShortLabel() );
            checkDuplicate( ao, fullCoverageAnnot );
        }

        if ( !hasAnnotation( ao, imexCurationAnnot ) ) {
            addNewAnnotation( daoFactory, ao, imexCurationAnnot );
            System.out.println( logPrefix + "Added 'imex curation' annotation to " + ao.getClass().getSimpleName() + "" );
        } else {
            System.out.println( "Annotation 'imex curation' is already present on " + ao.getClass().getSimpleName() + ": " + ao.getShortLabel() );
            checkDuplicate( ao, imexCurationAnnot );
        }
    }

    private void checkDuplicate(AnnotatedObject ao, Annotation annot) {

        Collection<Annotation> collected = getAnnotations(ao, annot);
        if(collected.size() > 1) {
            Iterator<Annotation> iterator = collected.iterator();
            iterator.next(); // skip first one
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
            while (iterator.hasNext()) {
                Annotation annotation = iterator.next();

                ao.removeAnnotation( annotation );
                update(ao);
                daoFactory.getAnnotationDao().delete( annotation );                

                System.out.println( "WARNING - Removed duplicated annotation '"+ annot.getCvTopic().getShortLabel() +"' on " +
                                    ao.getClass().getSimpleName() + ": " + ao.getAc());
            }
        }
    }

    private void update(AnnotatedObject ao) {
        DaoFactory daoFactory = IntactContext.getCurrentInstance().getDaoFactory();
        if ( ao instanceof Publication) {
            daoFactory.getPublicationDao().update( ( Publication ) ao );
        } else if ( ao instanceof Experiment) {
            daoFactory.getExperimentDao().update( ( Experiment ) ao );
        } else if ( ao instanceof InteractionImpl) {
            daoFactory.getInteractionDao().update( ( InteractionImpl ) ao );
        } else {
            throw new IllegalStateException( "Unsupported Object of type '" + ao.getClass().getSimpleName() + "', cannot add an annotation." );
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
        return ! getAnnotations(ao, a).isEmpty();
    }

    private Collection<Annotation> getAnnotations( AnnotatedObject ao, Annotation a ) {
        Collection<Annotation> collected = Lists.newArrayList();
        for ( Annotation annotation : ao.getAnnotations() ) {
            if ( annotation.getCvTopic().equals( a.getCvTopic() ) ) {
                final String t1 = annotation.getAnnotationText();
                final String t2 = a.getAnnotationText();
                if ( StringUtils.equals( t1, t2 ) ) {
                    collected.add( annotation );
                }
            }
        }
        return collected;
    }

    private void addNewAnnotation( DaoFactory daoFactory, AnnotatedObject ao, Annotation a ) {
        if ( !dryRun ) {
            daoFactory.getAnnotationDao().persist( a );
            ao.addAnnotation( a );
            update(ao);
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

        fireOnNewImexIdAssignedToInteraction(
                new ImexUpdateEvent( this,
                                     interaction.getExperiments().iterator().next().getPublication(),
                                     interaction, "" ) );

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

            final boolean publicationMatch = matchesPublicationAndYear( experiment,
                                                                        Arrays.asList( "Cell (0092-8674)",
                                                                                       "Proteomics (1615-9853)",
                                                                                       "Cancer Cell (1535-6108)",
                                                                                       "J Mol Signal.",
                                                                                       "Oncogene (0950-9232)"),
                                                                        2006 );

            final boolean datasetMatch = hasDataset( experiment, Arrays.asList( "BioCreative - Critical Assessment of Information Extraction systems in Biology" ) );

            if ( ! ( publicationMatch || datasetMatch ) ) {
                return false;
            }

            if( ! ExperimentUtils.isAccepted( experiment ) ) {
                return false;
            }

            // Note: once that boolean is true, the method is not executed anymore as || shortcuts evaluation
            atLeastOneExperimentWithPPI = atLeastOneExperimentWithPPI || hasAtLeastOnePPI( experiment );
        }

        if ( !atLeastOneExperimentWithPPI ) {
            System.out.println( "Not a single experiment in this publication involved protein-protein interactions." );
            return false;
        }

        return true;
    }

    private boolean isCuratorTriggered( Publication publication, DaoFactory daoFactory ) {
        final Collection<Experiment> experiments = publication.getExperiments();

        boolean isCuratorTriggered = false;

        // Irrespectively of the journal, if at least one experiment has an xref(db=imex, qualifier=imex-primary)
        // we will assign IMEx ids to it.

        // search for an experiment with IMEx id
        Xref imexIdXref = null;
        for ( Experiment experiment : experiments ) {
            if( containsPrimaryImexId( experiment ) ) {
                imexIdXref = ImexUtils.getPrimaryImexId( experiment );
                System.out.println( "Experiment '"+ experiment.getShortLabel() +"' has an IMEx id assigned ("+
                                    imexIdXref.getPrimaryId() +"), enable export of the whole publication." );
                isCuratorTriggered = true;
                break;
            }
        }

        if( imexIdXref != null ) {
            // check that we have the same id on the publication, if not, add it.
            final Xref pubImexIdXref = ImexUtils.getPrimaryImexId( publication );
            if( pubImexIdXref != null ) {
                if( imexIdXref.getPrimaryId().equals( pubImexIdXref.getPrimaryId() ) ) {
                    System.out.println( "Publication and experiment have the same IMEx id." );
                } else {
                    throw new IllegalStateException( "WARNING: Publication '"+ publication.getShortLabel()+"' and experiment '"+
                                                     imexIdXref.getParentAc() +"' different IMEx ID, respectively: " +
                                                     pubImexIdXref.getPrimaryId() + " and " + imexIdXref.getPrimaryId() );
                }
            } else {
                // assign that IMEx id to the publication
                PublicationXref pubXref = new PublicationXref( publication.getOwner(), imex, imexIdXref.getPrimaryId(), imexPrimary );
                publication.addXref( pubXref );
                if ( !dryRun ) daoFactory.getXrefDao().persist( pubXref );

                fireOnNewImexIdAssignedToPublication( new ImexUpdateEvent( this, publication, "Based on IMEx ID assigned to experiment by curator" ) );

                System.out.println( "\tCreated publication's IMEx primary: " + imexIdXref.getPrimaryId() + " based on experiment's IMEx ID." );
            }
        }

        return isCuratorTriggered;
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

    private boolean hasDataset( Experiment experiment, List<String> datasetNames ) {
        String dataset;
        for ( Annotation annotation : experiment.getAnnotations() ) {
            if ( CvTopic.DATASET_MI_REF.equals( annotation.getCvTopic().getIdentifier() ) &&
                datasetNames.contains( annotation.getAnnotationText() ) ) {

                dataset = annotation.getAnnotationText();
                System.out.println( "\tExperiment '" + experiment.getShortLabel() + "' was annotated with dataset '" + dataset +
                                    "', thus enabling IMEx export" );
                return true;
            }
        }
        return false;
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
        return ImexUtils.getPrimaryImexId( ao ) != null;
    }

    ////////////////////////
    // DEMO

    private static DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

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

        ImexAssigner assigner = new ImexAssigner( icUsername, icPassword, DefaultImexCentralClient.IC_TEST );

        assigner.setDryRun( true );

        final ImexAssignerConfig config = new ImexAssignerConfig();
        config.setUpdateLogsDirectory( new File( "imex-assigner-" + formatter.format( new Date() )) );
        assigner.setImexUpdateConfig( config );

        assigner.update();
    }
}

