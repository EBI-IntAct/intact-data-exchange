/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.externalservices.searchengine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.dao.ExperimentDao;
import uk.ac.ebi.intact.business.IntactTransactionException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Exports experiments for the EBI search engine.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23-Nov-2006</pre>
 */
public class ExperimentIndexExporter extends AbstractIndexExporter<Experiment> {

    /**
     * Sets up a logger for that class.
     */
    public static final Log log = LogFactory.getLog( ExperimentIndexExporter.class );

    public static final String INDEX_NAME = "IntAct.Experiment";
    public static final String DESCRIPTION = "Experimental procedures that allowed to discover molecular interactors";
    public static final int CHUNK_SIZE = 10;

    ////////////////////////////
    // Instance variables

    private Integer count = null;

    //////////////////////////
    // Constructor

    public ExperimentIndexExporter( File output ) {
        super( output );
    }

    public ExperimentIndexExporter( Writer writer ) {
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

    public void exportEntries() throws IndexerException {
        int current = 0;
        log.debug( "Starting export of " + count + " experiment(s)." );
        while ( current < count ) {
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            ExperimentDao edao = daoFactory.getExperimentDao();
            IntactContext.getCurrentInstance().getDataContext().beginTransaction();

            List<Experiment> experiments = edao.getAll( current, CHUNK_SIZE );
            if ( log.isDebugEnabled() ) {
                log.debug( "Exporting experiment range " + current + ".." + Math.min( count, current + CHUNK_SIZE ) +
                           " out of " + count );
            }
            for ( Experiment experiment : experiments ) {
                current++;
                exportEntry( experiment );
            }

            try {
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
            } catch ( IntactTransactionException e ) {
                throw new IndexerException( e );
            }
        }
    }

    public void exportEntry( Experiment experiment ) throws IndexerException {

        try {
            Writer out = getOutputWriter();

            final String i = INDENT + INDENT;
            final String ii = INDENT + INDENT+ INDENT;
            final String iii = INDENT + INDENT + INDENT + INDENT;

            out.write( i + "<entry id=\"" + experiment.getAc() + "\">" + NEW_LINE );
            out.write( ii + "<name>" + experiment.getShortLabel() + "</name>" + NEW_LINE );
            if ( experiment.getFullName() != null ) {
                out.write( ii +"<description>" + escapeXml( experiment.getFullName() ) + "</description>" + NEW_LINE );
            }
            out.write( ii + "<dates>" + NEW_LINE );
            writeCreationDate( out, experiment.getCreated(), iii );
            writeLastUpdateDate( out, experiment.getUpdated(), iii );
            out.write( ii + "</dates>" + NEW_LINE );

            boolean hasXrefs = !experiment.getXrefs().isEmpty();
            boolean hasLinks = !experiment.getInteractions().isEmpty();

            if ( hasXrefs || hasLinks ) {
                out.write( ii + "<cross_references>" + NEW_LINE );

                if ( hasXrefs ) {
                    for ( Xref xref : experiment.getXrefs() ) {
                        String db = xref.getCvDatabase().getShortLabel();
                        String id = xref.getPrimaryId();
                        writeRef( out, db, id, iii );
                    }
                }

                // Add refs to interactors and experiments
                if ( hasLinks ) {

                    Set<String> interactors = new HashSet<String>();

                    for ( Interaction interactor : experiment.getInteractions() ) {

                        writeRef( out, InteractionIndexExporter.INDEX_NAME, interactor.getAc(), iii );
                        for ( Component c : interactor.getActiveInstances() ) {
                            interactors.add( c.getInteractor().getAc() ); // Build non redundant list
                        }
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
            for ( Alias alias : experiment.getAliases() ) {
                String aliasName = escapeXml( alias.getName() );
                writeField( out, alias.getCvAliasType().getShortLabel(), aliasName, iii );
            }

            writeCvTerm( out, experiment.getCvInteraction(), iii );
            writeCvTerm( out, experiment.getCvIdentification(), iii );

            Set<CvObject> cvs = new HashSet<CvObject>();
            for ( Interaction interactor : experiment.getInteractions() ) {
                cvs.add( interactor.getCvInteractionType() );
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

    public int getEntryCount() throws IndexerException {
        if ( count == null ) {

            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            ExperimentDao edao = daoFactory.getExperimentDao();

            IntactContext.getCurrentInstance().getDataContext().beginTransaction();
            // TODO do not take into account interactor that do not interact.

            count = edao.countAll();

            try {
                IntactContext.getCurrentInstance().getDataContext().commitTransaction();
            } catch ( IntactTransactionException e ) {
                throw new IndexerException( e );
            }
        }

        return count;
    }
}