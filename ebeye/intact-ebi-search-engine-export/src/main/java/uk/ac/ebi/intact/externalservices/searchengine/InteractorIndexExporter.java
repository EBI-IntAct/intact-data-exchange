/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.externalservices.searchengine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.InteractorDao;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exports interactors for the EBI search engine.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23-Nov-2006</pre>
 */
public class InteractorIndexExporter extends AbstractIndexExporter<Interactor> {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( InteractorIndexExporter.class );

    public static final int CHUNK_SIZE = 50;

    public static final String INDEX_NAME = "IntAct.Interactor";
    public static final String DESCRIPTION = "Molecule taking part in an Interaction.";

    ////////////////////////
    // Instance variables

    private Integer count = null;

    //////////////////////////
    // Constructor

    public InteractorIndexExporter( File output ) {
        super( output );
    }

    public InteractorIndexExporter( Writer writer ) {
        super( writer );
    }

    ////////////////////////
    // Export

    public void exportHeader() throws IndexerException {

        try {
            Writer out = getOutputWriter();

            writeXmlHeader( out );

            out.write( "<database>" + NEW_LINE );
            out.write( INDENT + "<name>" + INDEX_NAME + "</name>" + NEW_LINE );
            out.write( INDENT + "<description>" + DESCRIPTION + "</description>" + NEW_LINE );
            out.write( INDENT + "<release>" + getRelease() + "</release>" + NEW_LINE );
            out.write( INDENT + "<release_date>" + getCurrentDate() + "</release_date>" + NEW_LINE );
            out.write( INDENT + "<entry_count>" + getEntryCount() + "</entry_count>" + NEW_LINE );
        } catch ( IOException e ) {
            throw new IndexerException( "Error while writing index header", e );
        }
    }

    public int getEntryCount() throws IndexerException {
        if ( count == null ) {

            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();

            InteractorDao<InteractorImpl> interactorDao = daoFactory.getInteractorDao();

            IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            count = interactorDao.countInteractorInvolvedInInteraction();

            try {
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
            } catch ( IntactTransactionException e ) {
                throw new IndexerException( "Error while closing transaction.", e );
            }
        }

        return count;
    }

    public void exportEntries() throws IndexerException {
        int current = 0;

        log.debug( "Starting export of " + count + " interactor(s)." );

        while ( current < count ) {
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            InteractorDao pdao = daoFactory.getInteractorDao();
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            List<Interactor> interactors = pdao.getInteractorInvolvedInInteraction( current, CHUNK_SIZE );

            if ( log.isDebugEnabled() ) {
                log.debug( "Exporting interactor range " + current + ".." + Math.min( count, current + CHUNK_SIZE ) +
                           " out of " + count );
            }

            for ( Interactor interactor : interactors ) {
                current++;
                exportEntry( interactor );
            }

            try {
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
            } catch ( IntactTransactionException e ) {
                throw new IndexerException( "Error when closing transaction.", e );
            }
        }
    }

    public void exportEntry( Interactor interactor ) throws IndexerException {

        try {
            Writer out = getOutputWriter();

            final String i = INDENT + INDENT;
            final String ii = INDENT + INDENT + INDENT;
            final String iii = INDENT + INDENT + INDENT + INDENT;

            out.write( i + "<entry id=\"" + interactor.getAc() + "\">" + NEW_LINE );
            out.write( ii + "<name>" + interactor.getShortLabel() + "</name>" + NEW_LINE );
            if ( interactor.getFullName() != null ) {
                out.write( ii + "<description>" + escapeXml( interactor.getFullName() ) + "</description>" + NEW_LINE );
            }
            out.write( ii + "<dates>" + NEW_LINE );
            writeCreationDate( out, interactor.getCreated(), iii );
            writeLastUpdateDate( out, interactor.getUpdated(), iii );
            out.write( ii + "</dates>" + NEW_LINE );


            boolean hasXrefs = !interactor.getXrefs().isEmpty();
            boolean hasLinks = !interactor.getActiveInstances().isEmpty();

            if ( hasXrefs || hasLinks ) {
                out.write( ii + "<cross_references>" + NEW_LINE );

                if ( hasXrefs ) {
                    for ( Xref xref : interactor.getXrefs() ) {

                        String db = xref.getCvDatabase().getShortLabel();
                        String id = xref.getPrimaryId();
                        writeRef( out, db, id, iii );
                    }
                }

                // Add refs to interactions and experiments
                if ( hasLinks ) {

                    Set<String> interactionAcs = new HashSet<String>();
                    Set<String> experimentAcs = new HashSet<String>();

                    for ( Component c : interactor.getActiveInstances() ) {

                        Interaction interaction = c.getInteraction();
                        interactionAcs.add( interaction.getAc() );

                        for ( Experiment experiment : interaction.getExperiments() ) {
                            experimentAcs.add( experiment.getAc() );
                        }
                    }

                    for ( String ac : experimentAcs ) {
                        writeRef( out, ExperimentIndexExporter.INDEX_NAME, ac, iii );
                    }

                    for ( String ac : interactionAcs ) {
                        writeRef( out, InteractionIndexExporter.INDEX_NAME, ac, iii );
                    }
                }

                out.write( ii + "</cross_references>" + NEW_LINE );
            }

            // TODO export Annotations

            // TODO export respective Components' xref and aliases and annotation

            out.write( ii + "<additional_fields>" + NEW_LINE );
            for ( Alias alias : interactor.getAliases() ) {
                String aliasName = escapeXml( alias.getName() );
                writeField( out, alias.getCvAliasType().getShortLabel(), aliasName, iii );
            }

            Set<CvObject> cvs = new HashSet<CvObject>();
            for ( Component c : interactor.getActiveInstances() ) {
                Interaction interaction = c.getInteraction();
                cvs.add( interaction.getCvInteractionType() );
                for ( Experiment e : interaction.getExperiments() ) {
                    cvs.add( e.getCvIdentification() );
                    cvs.add( e.getCvInteraction() );
                }
            }
            for ( CvObject cv : cvs ) {
                writeCvTerm( out, cv, iii );
            }

            out.write( ii + "</additional_fields>" + NEW_LINE );

            out.write( i + "</entry>" + NEW_LINE );

        } catch ( IOException e ) {
            throw new IndexerException( "Error while writing index", e );
        }
    }
}