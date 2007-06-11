/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.dbutil.cv;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.model.CvObject;
import uk.ac.ebi.intact.model.Xref;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Utility tool to query Sequence object via JDBC on either Oracle or PostgreSQL.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03-Mar-2006</pre>
 */
public class SequenceManager {

    private static final Log log = LogFactory.getLog(SequenceManager.class);

    private SequenceManager() {
    }

    /**
     * Has the existence of the sequence been checked already ?
     */
    private static boolean sequenceChecked = false;

    public static final String SEQUENCE_NAME = "CVOBJECT_ID";

    private static Dialect dialect;

    /**
     * Checks if the given sequence name exists.
     *
     * @param connection   the connection to the database.
     * @param sequenceName the name of the sequence to check upon.
     *
     * @return true if the sequence exists, false otherwise.
     *
     * @throws SQLException if an error occurs.
     */
    private static boolean sequenceExists( Connection connection, String sequenceName ) throws SQLException {

        if ( sequenceName == null ) {
            throw new IllegalArgumentException();
        }

        boolean exists = false;

        String sequencesListSql = getDialect().getQuerySequencesString();
        PreparedStatement stat = connection.prepareStatement(sequencesListSql);

        ResultSet rs = null;
        try
        {
            rs = stat.executeQuery();
            while (rs.next())
            {
                String seqName = rs.getString(1);

                if (seqName.equals(sequenceName))
                {
                    exists = true;
                    break;
                }
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }

        return exists;
    }

    private static void createSequenceInDb(Connection connection, String sequenceName) throws SQLException
    {
        String[] createSquenceSqls = getDialect().getCreateSequenceStrings(sequenceName);

        for (String sql : createSquenceSqls)
        {
            PreparedStatement stat = connection.prepareStatement(sql);
            stat.executeUpdate();
        }
    }

    public static void checkIfCvSequenceExists( ) throws IntactException, SQLException {

        if ( ! sequenceChecked ) {

            log.debug( "Checking if the sequence if present..." );

            Connection connection = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().connection();

            if ( ! sequenceExists( connection, SEQUENCE_NAME ) ) {
                //throw new IllegalStateException( "The sequence " + SEQUENCE_NAME + " doesn't not exist in your database. Please create it." );
                log.debug("Creating sequence in db: "+SEQUENCE_NAME);
                createSequenceInDb(connection, SEQUENCE_NAME);
            }

            log.debug( "Sequence OK." );

            sequenceChecked = true;
        }
    }


    /**
     * Returns the next id for the given sequence.
     *
     * @param connection   the database connection.
     * @param sequenceName the name of the sequence.
     *
     * @return the next id.
     *
     * @throws IntactException if an error occur.
     */
    public static long getNextSequenceValue( Connection connection, String sequenceName ) throws IntactException {

        if ( connection == null ) {
            throw new IllegalArgumentException( "You must give a non null Connection." );
        }

        if ( sequenceName == null ) {
            throw new IllegalArgumentException( "You must give a non null sequence name." );
        }

        try
        {
            checkIfCvSequenceExists( );
        }
        catch (SQLException e)
        {
            log.error(e);
            throw new IntactException("Exception checking sequence", e);
        }

        int nextVal = 0;
        try
        {
            String selectNextValueSql = getDialect().getSequenceNextValString(SEQUENCE_NAME);
            PreparedStatement statement = connection.prepareStatement(selectNextValueSql);

            nextVal = 0;

            ResultSet rs = null;
            try
        {
            rs = statement.executeQuery();
                rs.next();
                nextVal = rs.getInt(1);
            }
            finally
            {
                if (rs != null)
                {
                    rs.close();
                }
            }
        }
        catch (SQLException e)
        {
            throw new IntactException("Exception getting next sequence value", e);
        }

        return nextVal;
    }


    public static void synchronizeUpTo( long id ) {

        Connection connection = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().connection();
        long current = getNextSequenceValue( connection, SEQUENCE_NAME );

        if ( current < id ) {
            log.warn( "The current state of the sequence( " + SEQUENCE_NAME + " ) is wrong." );
            log.info( "Synchronizing up to " + id + "..." );
        }

        while ( current+1 <= id ) {
            current = getNextSequenceValue( connection, SEQUENCE_NAME );
        }

        log.info( "The sequence(" + SEQUENCE_NAME + ") is now set to : " + current );
    }

    private static String formatId( long id ) {

        String prefix = null;

        if ( id < 10 ) {
            prefix = "IA:000";
        } else if ( id < 100 ) {
            prefix = "IA:00";
        } else if ( id < 1000 ) {
            prefix = "IA:0";
        } else if ( id < 10000 ) {
            prefix = "IA:";
        } else {
            throw new IllegalStateException( "We have used all possible id space." );
        }

        return prefix + id;
    }

    public static String getNextId( ) throws IntactException {
        Connection connection = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().connection();
        long id;
        String nextId = null;
        Collection cvObjects;

        try {
            checkIfCvSequenceExists( );
        } catch ( SQLException e ) {
            throw new IntactException( "Error while checking if the sequence is present in the database.", e );
        }

        do {
            id = getNextSequenceValue( connection, SEQUENCE_NAME );

            nextId = formatId( id );

            // to be on the safe side, we make sure that the generated id is not in use in the database.
            // if it is found to be, messages will be displayed and the next id will be retreived until we
            // find a suitable one.
            // Given that we are relying on Database's sequences, we don't expect to encounter that issue,
            // that check may be removed later on.

            cvObjects = IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getCvObjectDao(CvObject.class).getByXrefLike(nextId);

            if ( ! cvObjects.isEmpty() ) {
                // display error if the IA:xxxx was already in use.
                log.error( "=========================================================" );
                log.error( "--- ERROR ---" );
                log.error( "Generated the next IntAct id: " + nextId );
                log.error( "Though it was used by " + cvObjects.size() + " object(s) already:" );
                for ( Iterator iterator2 = cvObjects.iterator(); iterator2.hasNext(); ) {
                    CvObject cv = (CvObject) iterator2.next();
                    System.out.println( "* " + cv.getShortLabel() + " (" + cv.getAc() + ")" );
                    for ( Iterator iterator1 = cv.getXrefs().iterator(); iterator1.hasNext(); ) {
                        Xref xref = (Xref) iterator1.next();
                        System.out.println( "  " + xref );
                    }
                }
                System.out.println( "=========================================================" );
            }

        } while ( ! cvObjects.isEmpty() );

        return nextId;
    }

    private static Dialect getDialect()
    {
        if (dialect != null)
        {
            return dialect;
        }

        Configuration conf = (Configuration)
                IntactContext.getCurrentInstance().getConfig().getDefaultDataConfig().getConfiguration();

        dialect = Dialect.getDialect(conf.getProperties());
        return dialect;
    }

    // Documentation:
    // http://forum.java.sun.com/thread.jspa?threadID=633017&tstart=300

    /* On Oracle
       CREATE SEQUENCE cvobject_id START WITH 51 INCREMENT BY 1;
       GRANT SELECT ON cvobject_id TO INTACT_SELECT ;

       On PostgreSQL
       CREATE SEQUENCE cvobject_id MINVALUE 1 INCREMENT 1;
       GRANT ALL ON cvobject_id TO PUBLIC;
     */

    public static void main( String[] args ) throws IntactException, SQLException {

            System.out.println( "Database: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory().getBaseDao().getDbName() );

            for ( int i = 0; i < 3; i++ ) {
                String id = getNextId( );
                System.out.println( "id = " + id );
            }
    }
}