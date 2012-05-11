package uk.ac.ebi.intact.dataexchange.imex.idassigner.listener;

import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.ImexErrorEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.IntactUpdateEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.events.NewAssignedImexEvent;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.ImexUpdateReportHandler;
import uk.ac.ebi.intact.dataexchange.imex.idassigner.report.ReportWriter;

import java.io.IOException;
import java.util.Set;

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
    public void onNewImexAssigned( NewAssignedImexEvent evt ) throws ProcessorException{
        try {
            ReportWriter writer = reportHandler.getNewImexAssignedWriter();
            writer.writeHeaderIfNecessary("Publication id",
                    "Imex id",
                    "Interaction ac",
                    "Interaction imex id");

            String pubId = dashIfNull(evt.getPublicationId());
            String imex = dashIfNull(evt.getImexId());
            String intAc = dashIfNull(evt.getInteractionAc());
            String intImex = dashIfNull(evt.getInteractionImexId());

            writer.writeColumnValues(pubId,
                    imex,
                    intAc,
                    intImex);
            writer.flush();

        } catch ( IOException e ) {
            throw new ProcessorException( "Error while flushing intact update event ", e );
        }
    }

    @Override
    public void onIntactUpdate( IntactUpdateEvent evt ) throws ProcessorException{
        try {
            ReportWriter writer = reportHandler.getIntactUpdateWriter();
            writer.writeHeaderIfNecessary("Publication id",
                    "Imex id",
                    "Number updated experiments",
                    "Number updated interactions",
                    "Updated experiments",
                    "Updated interactions");

            String pubId = dashIfNull(evt.getPublicationId());
            String imex = dashIfNull(evt.getImexId());
            int numberExperiments = evt.getUpdatedExp().size();
            int numberInteractions = evt.getUpdatedInteraction().size();
            String experimentAc = writeObjectAcsFor(evt.getUpdatedExp());
            String interactionAc = writeObjectAcsFor(evt.getUpdatedInteraction());

            writer.writeColumnValues(pubId,
                    imex,
                    Integer.toString(numberExperiments),
                    Integer.toString(numberInteractions),
                    experimentAc.equals("") ? "-": experimentAc,
                    interactionAc.equals("") ? "-": interactionAc);
            writer.flush();

        } catch ( IOException e ) {
            throw new ProcessorException( "Error while flushing intact update event ", e );
        }
    }
    
    private String writeObjectAcsFor(Set<String> intactobjects){
        StringBuffer buffer = new StringBuffer();
        
        for (String obj : intactobjects){
            buffer.append(obj);
            buffer.append(", ");
        }
        return buffer.toString();
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

    //////////////////
    // Utilities

    private String dashIfNull( String str ) {
        str = ( str != null ) ? str : EMPTY_VALUE;
        return str;
    }

    private String booleanToYesNo( boolean bool ) {
        return bool ? "Y" : "N";
    }
}
