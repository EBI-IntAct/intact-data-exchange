package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import org.joda.time.DateTime;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.ImexUtils;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.ImexUpdateReportHandler;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.ReportWriter;
import uk.ac.ebi.intact.model.*;

import java.io.IOException;

/**
 * Write to disk on event fired.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.1
 */
public class ReportWriterListener extends AbstractImexUpdateListener {

    private static final String EMPTY_VALUE = "-";
                                    
    private ImexUpdateReportHandler reportHandler;

    public ReportWriterListener( ImexUpdateReportHandler reportHandler ) {
        if ( reportHandler == null ) {
            throw new IllegalArgumentException( "You must give a non null reportHandler" );
        }
        this.reportHandler = reportHandler;
    }

    //////////////////////////////////
    // AbstractImexUpdateListener

    @Override
    public void onProcessPublication( ImexUpdateEvent evt ) throws ProcessorException {
        try {
            writeDefaultLine( reportHandler.getProcessedWriter(), evt.getPublication(), evt.getMessage() );
        } catch ( IOException e ) {
            throw new ProcessorException("Error while processing publication " + evt.getPublication().getShortLabel(), e );
        }
    }

    @Override
    public void onProcessImexPublication( ImexUpdateEvent evt ) throws ProcessorException {
        try {
            writeDefaultLine( reportHandler.getProcessImexPublicationWriter(), evt.getPublication(), evt.getMessage() );
        } catch ( IOException e ) {
            throw new ProcessorException( "Error while processing publication " + evt.getPublication().getShortLabel(), e );
        }
    }

    @Override
    public void onPublicationUpToDate( ImexUpdateEvent evt ) throws ProcessorException {
        try {
            writeDefaultLine( reportHandler.getPublicationUpToDateWriter(), evt.getPublication(), evt.getMessage() );
        } catch ( IOException e ) {
            throw new ProcessorException( "Error while processing publication " + evt.getPublication().getShortLabel(), e );
        }
    }

    @Override
    public void onImexIdAssignedToPublication( ImexUpdateEvent evt ) throws ProcessorException {
        try {
            writeDefaultLine( reportHandler.getImexIdAssignedToPublicationWriter(), evt.getPublication(), evt.getMessage() );
        } catch ( IOException e ) {
            throw new ProcessorException( "Error while processing publication " + evt.getPublication().getShortLabel(), e );
        }
    }
    
    @Override
    public void onImexError( ImexErrorEvent evt ) throws ProcessorException{
        try {
            ReportWriter writer = reportHandler.getImexErrorWriter();
            writer.writeHeaderIfNecessary("Publication id",
                    "Imex id",
                    "Interaction ac",
                    "Experiment ac",
                    "Error type",
                    "Error message");

            String pubId = dashIfNull(evt.getPublicationId());
            String imex = dashIfNull(evt.getImexId());
            String interactionAc = dashIfNull(evt.getInteractionAc());
            String experimentAc = dashIfNull(evt.getExperimentAc());
            String errorType = evt.getErrorType() != null ? evt.getErrorType().toString() : "-";
            String errorMessage = dashIfNull(evt.getErrorMessage());

            writer.writeColumnValues(pubId,
                    imex,
                    interactionAc,
                    experimentAc,
                    errorType,
                    errorMessage);
            writer.flush();

        } catch ( IOException e ) {
            throw new ProcessorException( "Error while flushing error event ", e );
        }
    }

    @Override
    public void onImexIdAssignedToInteraction( ImexUpdateEvent evt ) throws ProcessorException {
        try {
            ReportWriter writer = reportHandler.getImexIdAssignedToInteractionWriter();
            writer.writeHeaderIfNecessary("Interaction AC",
                                          "shortlabel",
                                          "Interaction IMEx ID",
                                          "Publication AC",
                                          "Publication ID",
                                          "Publication IMEx ID");

            Publication publication = evt.getPublication();
            Interaction interaction = evt.getInteraction();

            writer.writeColumnValues(interaction.getAc(),
                                     interaction.getShortLabel(),
                                     dashIfNull( getImexId( interaction ) ),
                                     publication.getAc(),
                                     publication.getPublicationId(),
                                     dashIfNull( getImexId( publication ) ) );
            writer.flush();

        } catch ( IOException e ) {
            throw new ProcessorException( "Error while processing publication " + evt.getPublication().getShortLabel(), e );
        }
    }

    @Override
    public void onImexIdMismatchFound( ImexUpdateEvent evt ) throws ProcessorException {
        try {
            writeDefaultLine( reportHandler.getImexIdMismatchFoundWriter(), evt.getPublication(), evt.getMessage() );
        } catch ( IOException e ) {
            throw new ProcessorException( "Error while processing publication " + evt.getPublication().getShortLabel(), e );
        }
    }

    //////////////////
    // Utilities

    private void writeDefaultHeaderIfNecessary(ReportWriter writer) throws IOException {
        if (writer != null) {
            writer.writeHeaderIfNecessary("datetime", "Publication AC", "Publication Identifier", "IMEx ID", "Curator", "Message");

            writer.flush();
        }
    }

    private void writeDefaultLine( ReportWriter writer, Publication publication ) throws IOException {
        writeDefaultLine( writer, publication, null );
    }

    private void writeDefaultLine( ReportWriter writer, Publication publication, String message ) throws IOException {
        writeDefaultHeaderIfNecessary( writer );
        if ( writer != null ) {
            String imexId = dashIfNull( getImexId( publication ) );
            String curator = dashIfNull( getCurator( publication ) );
            message = dashIfNull( message );

            writer.writeColumnValues( new DateTime().toString(),
                                      publication.getAc(),
                                      publication.getShortLabel(),
                                      imexId,
                                      curator,
                                      message );
            writer.flush();
        }
    }

    private String getCurator( Publication publication ) {
        Experiment first = null;
        for ( Experiment e : publication.getExperiments() ) {
            if( first == null ) {
                first = e;
            } else {
                if( e.getCreated().before( first.getCreated() ) ) {
                    first = e;
                }
            }
        }

        if( first != null ) {
            return first.getCreator();
        }
        
        return null;
    }

    private String getImexId( AnnotatedObject ao ) {
        final Xref xref = ImexUtils.getPrimaryImexId( ao );
        if( xref == null ) {
            return null;
        }
        return xref.getPrimaryId();
    }

    private String dashIfNull( String str ) {
        str = ( str != null ) ? str : EMPTY_VALUE;
        return str;
    }

    private String booleanToYesNo( boolean bool ) {
        return bool ? "Y" : "N";
    }
}
