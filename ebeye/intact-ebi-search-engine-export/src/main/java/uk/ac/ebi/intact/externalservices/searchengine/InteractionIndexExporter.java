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
import uk.ac.ebi.intact.persistence.dao.InteractionDao;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exports interactions for the EBI search engine.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23-Nov-2006</pre>
 */
public class InteractionIndexExporter extends AbstractIndexExporter<Interaction> {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( InteractionIndexExporter.class );

    public static final String INDEX_NAME = "IntAct.Interaction";
    public static final String DESCRIPTION = "Molecular interaction";
    public static final int CHUNK_SIZE = 50;

    ////////////////////////////
    // Instance variables

    private Integer count = null;

    //////////////////////////
    // Constructor

    public InteractionIndexExporter( File output ) {
        super( output );
    }

    public InteractionIndexExporter( Writer writer ) {
        super( writer );
    }

    ////////////////////////////
    // AbstractIndexExporter

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
            throw new IndexerException( e );
        }
    }

    public void exportEntry( Interaction interaction ) throws IndexerException {
        try {
            Writer out = getOutputWriter();

            final String i = INDENT + INDENT;
            final String ii = INDENT + INDENT+ INDENT;
            final String iii = INDENT + INDENT + INDENT + INDENT;

            out.write( i + "<entry id=\"" + interaction.getAc() + "\">" + NEW_LINE );
            out.write( ii + "<name>" + interaction.getShortLabel() + "</name>" + NEW_LINE );
            if ( interaction.getFullName() != null ) {
                out.write( ii + "<description>" + escapeXml( interaction.getFullName() ) + "</description>" + NEW_LINE );
            }
            out.write( ii + "<dates>" + NEW_LINE );
            writeCreationDate( out, interaction.getCreated(), iii );
            writeLastUpdateDate( out, interaction.getUpdated(), iii );
            out.write( ii + "</dates>" + NEW_LINE );


            boolean hasXrefs = !interaction.getXrefs().isEmpty();
            boolean hasLinks = !interaction.getComponents().isEmpty();

            if ( hasXrefs || hasLinks ) {
                out.write( ii + "<cross_references>" + NEW_LINE );

                if ( hasXrefs ) {
                    for ( Xref xref : interaction.getXrefs() ) {
                        String db = xref.getCvDatabase().getShortLabel();
                        String id = xref.getPrimaryId();
                        writeRef( out, db, id, iii );
                    }
                }

                // Add refs to interactions and experiments
                if ( hasLinks ) {

                    Set<String> interactors = new HashSet<String>();
                    Set<String> experimentAcs = new HashSet<String>();

                    for ( Component c : interaction.getComponents() ) {

                        Interactor interactor = c.getInteractor();
                        interactors.add( interactor.getAc() );

                        for ( Experiment experiment : interaction.getExperiments() ) {
                            experimentAcs.add( experiment.getAc() );
                        }
                    }

                    for ( String ac : experimentAcs ) {
                        writeRef( out, ExperimentIndexExporter.INDEX_NAME, ac, iii );
                    }

                    for ( String ac : interactors ) {
                        writeRef( out, InteractorIndexExporter.INDEX_NAME, ac, iii );
                    }
                }

                out.write( ii + "</cross_references>" + NEW_LINE );
            }

            // TODO export Annotations ?

            // TODO export respective Components' xref and aliases and annotation

            out.write( ii + "<additional_fields>" + NEW_LINE );
            for ( Alias alias : interaction.getAliases() ) {
                String aliasName = escapeXml( alias.getName() );
                writeField( out, alias.getCvAliasType().getShortLabel(), aliasName, iii );
            }

            // export Controlled vocabularies

            // interaction detection method
            writeCvTerm( out, interaction.getCvInteractionType(), iii );

            // participant detection method and interaction type
            Set<CvObject> cvs = new HashSet<CvObject>();
            for ( Experiment e : interaction.getExperiments() ) {
                cvs.add( e.getCvInteraction() );
                cvs.add( e.getCvIdentification() );
            }
            for ( CvObject cv : cvs ) {
                writeCvTerm( out, cv, iii );
            }

            out.write( ii + "</additional_fields>" + NEW_LINE );

            out.write( i + "</entry>" + NEW_LINE );
        } catch ( IOException e ) {
            throw new IndexerException( e );
        }
    }

    public void exportEntries() throws IndexerException {
        int current = 0;

        log.debug( "Starting export of " + count + " interaction(s)." );

        while ( current < count ) {
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            InteractionDao idao = daoFactory.getInteractionDao();
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            List<InteractionImpl> interactions = idao.getAll( current, CHUNK_SIZE );

            if ( log.isDebugEnabled() ) {
                log.debug( "Exporting interaction range " + current + ".." + Math.min( count, current + CHUNK_SIZE ) +
                           " out of " + count );
            }

            for ( Interaction interaction : interactions ) {
                current++;
                exportEntry( interaction );
            }

            try {
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
            } catch ( IntactTransactionException e ) {
                throw new IndexerException( e );
            }
        }
    }

    public int getEntryCount() throws IndexerException {
        if ( count == null ) {

            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            InteractionDao idao = daoFactory.getInteractionDao();

            IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            // TODO do not take into account interactor that do not interact.

            count = idao.countAll();

            try {
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
            } catch ( IntactTransactionException e ) {
                throw new IndexerException( e );
            }
        }

        return count;
    }
}