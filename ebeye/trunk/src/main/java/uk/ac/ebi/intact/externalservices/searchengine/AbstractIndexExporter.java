/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.externalservices.searchengine;

import org.apache.commons.lang.StringEscapeUtils;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.persistence.dao.CvObjectDao;
import uk.ac.ebi.intact.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Index Exporter. Provides default hooks for exporting a complete index.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>23-Nov-2006</pre>
 */
public abstract class AbstractIndexExporter<T extends AnnotatedObject> implements IndexExporter<T> {

    protected static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "dd-MMM-yyyy" );

    public static final String INDENT = "  ";
    public static final String NEW_LINE = "\n"; // Unix style line return.

    private Writer writer;

    private CvDatabase intact;
    private CvDatabase psi;
    private CvXrefQualifier identity;

    public AbstractIndexExporter( File output ) {
        if ( output == null ) {
            throw new IllegalArgumentException( "output file must not be null." );
        }

        if ( !output.exists() ) {
            try {
                output.createNewFile();
            } catch ( IOException e ) {
                throw new IllegalArgumentException( "Could not create a new output file.", e );
            }
        }

        if ( !output.canWrite() ) {
            throw new IllegalArgumentException( "Cannot write on " + output.getAbsolutePath() );
        }

        try {
            this.writer = new FileWriter(output);
        } catch (IOException e) {
            throw new IllegalArgumentException( "Cannot write on " + output.getAbsolutePath(), e );
        }
    }

    public AbstractIndexExporter( Writer writer ) {
        if ( writer == null ) {
            throw new IllegalArgumentException( "output file must not be null." );
        }

        this.writer = writer;
    }

    ////////////////////////
    // Getters

    public String getRelease() {
        return getCurrentDate();
    }

    //////////////////////////
    // Utilities

    protected Writer getOutputWriter() throws IOException {
        return writer;
    }

    protected String getCurrentDate() {
        return DATE_FORMATTER.format( new Date() );
    }

    private void closeIndex() throws IOException {
        writer.flush();
        writer.close();
    }

    private String getMiReference( CvObject cv ) {
        for ( Xref xref : cv.getXrefs() ) {
            if ( getPsi().equals( xref.getCvDatabase() ) ) {
                if ( getIdentity().equals( xref.getCvXrefQualifier() ) ) {
                    return xref.getPrimaryId();
                }
            }
        }
        return null;
    }

    protected CvXrefQualifier getIdentity() {
        if ( identity == null ) {
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            CvObjectDao<CvObject> cvdao = daoFactory.getCvObjectDao();
            identity = cvdao.getByPrimaryId( CvXrefQualifier.class, CvXrefQualifier.IDENTITY_MI_REF );
            if ( identity == null ) {
                throw new IllegalStateException( "Could not find CvXrefQualifier( identity )." );
            }
        }
        return identity;
    }

    protected CvDatabase getIntact() {
        if ( intact == null ) {
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            CvObjectDao<CvObject> cvdao = daoFactory.getCvObjectDao();
            intact = cvdao.getByPrimaryId( CvDatabase.class, CvDatabase.INTACT_MI_REF );
            if ( intact == null ) {
                throw new IllegalStateException( "Could not find CvDatabase( IntAct )." );
            }
        }
        return intact;
    }

    protected CvDatabase getPsi() {
        if ( psi == null ) {
            DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            CvObjectDao<CvObject> cvdao = daoFactory.getCvObjectDao();

            psi = cvdao.getByPrimaryId( CvDatabase.class, CvDatabase.PSI_MI_MI_REF );
            if ( psi == null ) {
                throw new IllegalStateException( "Could not find CvDatabase( IntAct )." );
            }
        }
        return psi;
    }

    protected String escapeXml(String s) {
        return StringEscapeUtils.escapeXml( s );
    }

    ///////////////////////////
    // Writer helper

    protected void writeXmlHeader( Writer out ) throws IOException {
        out.write( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + NEW_LINE );
    }

    protected void writeRef( Writer out, String db, String id, String indent ) throws IOException {
        out.write( indent + "<ref dbname=\"" + db + "\" dbkey=\"" + id + "\" />" + NEW_LINE );
    }

    protected void writeField( Writer out, String name, String text, String indent ) throws IOException {
        out.write( indent + "<field name=\"" + name + "\">" + text + "</field>" + NEW_LINE );
    }

    protected void writeCreationDate( Writer out, Date date, String indent ) throws IOException {
        String creation = DATE_FORMATTER.format( date );
        out.write( indent + "<date type=\"creation\" value=\"" + creation + "\" />" + NEW_LINE );
    }

    protected void writeLastUpdateDate( Writer out, Date date, String indent ) throws IOException {
        String creation = DATE_FORMATTER.format( date );
        out.write( indent + "<date type=\"last_modification\" value=\"" + creation + "\" />" + NEW_LINE );
    }

    private static Map<Class, String> cvtype2name = new HashMap<Class, String>();

    static {
        cvtype2name.put( CvIdentification.class, "participant detection method" );
        cvtype2name.put( CvInteraction.class, "Interaction detection method" );
        cvtype2name.put( CvInteractionType.class, "Interaction type" );
        cvtype2name.put( CvFeatureIdentification.class, "Feature identification method" );
    }

    protected void writeCvTerm( Writer out, CvObject cv, String indent ) throws IOException {

        String title = cvtype2name.get( CgLibUtil.removeCglibEnhanced( cv.getClass() ) );
        if ( title == null ) {
            throw new IllegalStateException( "No title has been assigned to CV type: " + cv.getClass().getName() );
        }

        String sl = cv.getShortLabel();
        String fn = escapeXml( cv.getFullName() );

        writeField( out, title, sl, indent );

        // write fullname only if different from shortlabel and not null.
        if ( !sl.equals( fn ) ) {
            if ( fn != null && fn.length() > 0 ) {
                writeField( out, title, fn, indent );
            }
        }

        String mi = getMiReference( cv );
        if ( mi != null ) {
            writeField( out, title, mi, indent );
        }
    }

    ////////////////////////////
    // IndexExporter methods

    public void exportEntryListStart() throws IndexerException {
        try {
            Writer out = getOutputWriter();
            out.write( INDENT + "<entries>" + NEW_LINE );
        } catch ( IOException e ) {
            throw new IndexerException( e );
        }
    }

    public void exportEntryListEnd() throws IndexerException {
        try {
            Writer out = getOutputWriter();
            out.write( INDENT + "</entries>" + NEW_LINE );
        } catch ( IOException e ) {
            throw new IndexerException( e );
        }
    }

    public void exportFooter() throws IndexerException {
        try {
            Writer out = getOutputWriter();
            out.write( "</database>" + NEW_LINE );
        } catch ( IOException e ) {
            throw new IndexerException( e );
        }
    }

    public void buildIndex() throws IndexerException {
        exportHeader();

        exportEntryListStart();

        exportEntries();

        exportEntryListEnd();

        exportFooter();

        try {
            closeIndex();
        } catch ( IOException e ) {
            throw new IndexerException( e );
        }
    }

    ////////////////////////////
    // Abstract method

    public abstract void exportEntries() throws IndexerException;

    public abstract void exportEntry( T object ) throws IndexerException;

    public abstract int getEntryCount() throws IndexerException;
}