/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;
import uk.ac.ebi.intact.application.dataConversion.util.DisplayXML;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Message {

    private static final String NEW_LINE = System.getProperty( "line.separator" );

    private Node node;
    private String context;
    private String text;


    //////////////////////////////
    // Constructor

    public Message( String context, String text ) {

        this( text );
        this.context = context;
    }

    public Message( Element element, String text ) {

        this( text );
        this.node = element;
        this.context = DOMUtil.getContext( element );
    }

    public Message( String text ) {

        if ( text == null || "".equals( text.trim() ) ) {
            throw new IllegalArgumentException( "You must give a non null message" );
        }

        this.text = text;
    }


    //////////////////////////////
    // Getters

    public String getContext() {
        return context;
    }

    public String getText() {
        return text;
    }

    public void printXmlContent() {
        if ( node != null ) {
            DisplayXML.print( node, System.out );
        } else {
            System.out.println( "No XML content stored in that message." );
        }
    }


    //////////////////////////////
    // Equality

    public boolean equals( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( !( o instanceof Message ) ) {
            return false;
        }

        final Message message = (Message) o;

        if ( context != null ? !context.equals( message.context ) : message.context != null ) {
            return false;
        }
        if ( !text.equals( message.text ) ) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        result = ( context != null ? context.hashCode() : 0 );
        result = 29 * result + text.hashCode();
        return result;
    }


    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append( "Message{" );
        if ( context != null ) {
            buf.append( " context: " ).append( context ).append( ',' );
            buf.append( NEW_LINE ).append( '\t' ).append( '\t' ).append( '\t' );
        }
        buf.append( " text: " ).append( text.trim() );
        buf.append( " }" );
        return buf.toString();
    }
}
