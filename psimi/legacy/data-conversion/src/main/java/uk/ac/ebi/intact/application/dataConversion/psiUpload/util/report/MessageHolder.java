/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */

package uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * That class hold messages created during the Paring and checking process.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.parser
 * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.checker
 */
public class MessageHolder {

    private static final String NEW_LINE = System.getProperty( "line.separator" );
    private static final char TAB = '\t';


    private Collection parserMessages;
    private Collection checkerMessages;
    private Collection persisterMessages;

    private static MessageHolder ourInstance = new MessageHolder();

    public static MessageHolder getInstance() {
        return ourInstance;
    }

    private MessageHolder() {
        parserMessages = new ArrayList();
        checkerMessages = new ArrayList();
        persisterMessages = new ArrayList();
    }

    //////////////////////////
    // Clear messages

    public void clearAllMessages() {
        clearParserMessage();
        clearCheckerMessage();
    }

    public void clearParserMessage() {
        parserMessages.clear();
    }

    public void clearCheckerMessage() {
        checkerMessages.clear();
    }

    public void clearPersisterMessage() {
        persisterMessages.clear();
    }


    /////////////////////////
    // Add message

    public void addParserMessage( Message message ) {
        parserMessages.add( message );
    }

    public void addCheckerMessage( Message message ) {
        System.err.println( message.getText() );
        checkerMessages.add( message );
    }

    public void addPersisterMessage( Message message ) {
        System.err.println( message.getText() );
        persisterMessages.add( message );
    }


    ////////////////////////
    // Getters

    public Collection getCheckerMessages() {
        return checkerMessages;
    }

    public Collection getParserMessages() {
        return parserMessages;
    }

    public Collection getPersisterMessages() {
        return persisterMessages;
    }

    public boolean parserMessageExists() {
        return parserMessages.size() > 0;
    }

    public boolean checkerMessageExists() {
        return checkerMessages.size() > 0;
    }

    public boolean persisterMessageExists() {
        return persisterMessages.size() > 0;
    }


    ///////////////////////
    // Reporting

    private void printReport( final Collection messages,
                              final String title,
                              StringBuffer sb ) {

        if ( sb == null ) {
            sb = new StringBuffer( 128 );
        }

        sb.append( title ).append( '(' ).append( messages.size() ).append( " message" );
        sb.append( ( messages.size() > 1 ? "s)" : ")" ) ).append( ':' );
        sb.append( NEW_LINE );

        for ( Iterator iterator = messages.iterator(); iterator.hasNext(); ) {
            Message message = (Message) iterator.next();
            sb.append( TAB ).append( message );
            sb.append( NEW_LINE ).append( NEW_LINE );

        }

        sb.append( NEW_LINE );
    }

    public void printCheckerReport( final PrintStream out ) {

        StringBuffer sb = new StringBuffer( 128 );
        printReport( checkerMessages, "Checker report", sb );
        out.print( sb.toString() );
    }

    public void printParserReport( final PrintStream out ) {

        StringBuffer sb = new StringBuffer( 128 );
        printReport( parserMessages, "Parser report", sb );
        out.print( sb.toString() );
    }

    public void printPersisterReport( final PrintStream out ) {

        StringBuffer sb = new StringBuffer( 128 );
        printReport( persisterMessages, "Persister report", sb );
        out.print( sb.toString() );
    }


}
