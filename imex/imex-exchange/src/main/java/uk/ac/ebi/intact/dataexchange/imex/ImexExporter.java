package uk.ac.ebi.intact.dataexchange.imex;

import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ConverterContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.ImexExportProfile;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import psidev.psi.mi.xml.model.EntrySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.collect.Lists;

import java.util.Collection;

/**
 * Allows to export XML data formatted as specified in the IMEx exchange.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.0
 */
public class ImexExporter {

    @Autowired
    private PsiExchange psiExchange;

    protected ImexExporter() {
    }

    public ImexExporter( PsiExchange psiExchange ) {
        this.psiExchange = psiExchange;
    }

    /**
     * Exports a single publication as an EntrySet.
     *
     * @param publication the publication to be exported (non null).
     * @return a non null EntrySet.
     */
    @Transactional( readOnly = true )
    public EntrySet exportPublication( Publication publication ) {

        if ( publication == null ) {
            throw new IllegalArgumentException( "You must give a non null publication" );
        }

        // TODO check that all eperiments are accepted -- use Arnaud's methods here

        // TODO publication should be from our institution

        // TODO experiments and interaction shoul dhave imex identifiers


        // Collect all interaction to be exported
        Collection<Interaction> interactions = Lists.newArrayList();
        for ( Experiment experiment : publication.getExperiments() ) {
            for ( Interaction interaction : experiment.getInteractions() ) {
                interactions.add( interaction );
            }
        }

        // Configure the exporter
        final ConverterContext context = ConverterContext.getInstance();
        context.configure( new ImexExportProfile() );

        // Build an IntactEntry from the publication
        IntactEntry intactEntry = new IntactEntry( interactions );
        final EntrySet entrySet = psiExchange.exportToEntrySet( intactEntry );

        final int entryCount = entrySet.getEntries().size();
        if( entryCount != 1 ) {
            throw new IllegalStateException( "We expected to get a single entry when comverting publication " +
                                             publication.getShortLabel() + ", instead,  " + entryCount +
                                             " were created. Abort." );
        }

        return entrySet;
    }

    /**
     * Exports a list of publications as an EntrySet.
     *
     * @param publications the list of publication to be exported (non null, non empty).
     * @return a non null EntrySet.
     */
    public EntrySet exportPublications( Publication... publications ) {

        if ( publications == null ) {
            throw new IllegalArgumentException( "You must give a non null list of publications" );
        }

        if ( publications.length == 0 ) {
            throw new IllegalArgumentException( "You must give at least one publication to export." );
        }

        EntrySet entrySet = null;

        for ( Publication publication : publications ) {

            final EntrySet es = exportPublication( publication );
            if( entrySet == null ) {
                entrySet = es;
            } else {
                entrySet.getEntries().add( es.getEntries().iterator().next() );
            }
        }

        return entrySet;
    }
}