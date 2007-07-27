// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.*;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;

/**
 * Utility class that allow to print out XML code from a given XML Node.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class DisplayXML {

    /**
     * Escape special XML characters.
     *
     * @param s the string in which we may have special characters.
     *
     * @return the given string in which we have escaped special characters
     */
    private static String escapeXML( String s ) {

        if ( s == null ) {
            return "";
        }

        return StringEscapeUtils.escapeXml( s );
    }

    public static void print( Node node ) {
        print( node, System.out );
    }

    public static void print( Node node, PrintStream out ) {

        print( node, out, "" );
    }

    /**
     * Print The XML code related to the given Node.
     *
     * @param node   the node to print out.
     * @param out    where to print it out.
     * @param indent indentation for the next level.
     */
    private static void print( Node node, PrintStream out, String indent ) {

        int type = node.getNodeType();

        switch ( type ) {

            case Node.DOCUMENT_NODE:
                print( ( (Document) node ).getDocumentElement() );

                break;

            case Node.ELEMENT_NODE:
                out.print( indent + "<" + node.getNodeName() );
                NamedNodeMap attrs = node.getAttributes();
                int len = attrs.getLength();
                for ( int i = 0; i < len; i++ ) {
                    Attr attr = (Attr) attrs.item( i );
                    out.print( " " + attr.getNodeName() + "=\"" + escapeXML( attr.getNodeValue() ) + "\"" );
                }

                NodeList children = node.getChildNodes();
                len = children.getLength();

                if ( len == 0 ) {

                    out.print( "/>\n" );

                } else if ( len == 1 && ( children.item( 0 ) instanceof Text ) ) {

                    // if the Element only contains text, don't break line.
                    out.print( ">" );
                    print( children.item( 0 ), out, "" );
                    out.print( "</" + node.getNodeName() + ">\n" );

                } else {

                    out.print( ">\n" );

                    for ( int i = 0; i < len; i++ ) {
                        print( children.item( i ), out, indent + "  " );
                    }

                    out.print( indent + "</" + node.getNodeName() + ">\n" );
                }

                break;

            case Node.ENTITY_REFERENCE_NODE:

                out.print( indent + "&" + node.getNodeName() + ";" );
                break;

            case Node.CDATA_SECTION_NODE:

                out.print( indent + "<![CDATA[" + node.getNodeValue() + "]]>\n" );
                break;

            case Node.TEXT_NODE:

                out.print( escapeXML( node.getNodeValue() ) );
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                out.print( indent + "<?" + node.getNodeName() );
                String data = node.getNodeValue();

                if ( data != null && data.length() > 0 ) {
                    out.print( " " + data );
                }

                out.println( "?>\n" );
                break;

            case Node.COMMENT_NODE:
                out.print( indent + "<!--" + node.getNodeValue() + "-->\n" );
                break;

            default:
                throw new IllegalArgumentException( "Type of node not supported, node name: " + node.getNodeName() + " type:" + node.getNodeType() );
        }
    }

    /**
     * Print The XML code related to the given Node.
     *
     * @param node   the node to print out.
     * @param out    where to print it out.
     * @param indent indentation for the next level.
     */
    public static void write( Node node, Writer out, String indent ) throws IOException {

        int type = node.getNodeType();

        switch ( type ) {

            case Node.DOCUMENT_NODE:

                write( ( (Document) node ).getDocumentElement(), out, indent );
                break;

            case Node.ELEMENT_NODE:
                out.write( indent + "<" + node.getNodeName() );
                NamedNodeMap attrs = node.getAttributes();
                int len = attrs.getLength();
                for ( int i = 0; i < len; i++ ) {
                    Attr attr = (Attr) attrs.item( i );
                    out.write( " " + attr.getNodeName() + "=\"" + escapeXML( attr.getNodeValue() ) + "\"" );
                }

                NodeList children = node.getChildNodes();
                len = children.getLength();

                if ( len == 0 ) {

                    out.write( "/>\n" );

                } else if ( len == 1 && ( children.item( 0 ) instanceof Text ) ) {

                    // if the Element only contains text, don't break line.
                    out.write( ">" );
                    write( children.item( 0 ), out, "" );
                    out.write( "</" + node.getNodeName() + ">\n" );

                } else {

                    out.write( ">\n" );

                    for ( int i = 0; i < len; i++ ) {
                        write( children.item( i ), out, indent + "  " );
                    }

                    out.write( indent + "</" + node.getNodeName() + ">\n" );
                }

                break;

            case Node.ENTITY_REFERENCE_NODE:

                out.write( indent + "&" + node.getNodeName() + ";" );
                break;

            case Node.CDATA_SECTION_NODE:

                out.write( indent + "<![CDATA[" + node.getNodeValue() + "]]>\n" );
                break;

            case Node.TEXT_NODE:

                out.write( escapeXML( node.getNodeValue() ) );
                break;

            case Node.PROCESSING_INSTRUCTION_NODE:
                out.write( indent + "<?" + node.getNodeName() );
                String data = node.getNodeValue();

                if ( data != null && data.length() > 0 ) {
                    out.write( " " + data );
                }

                out.write( "?>\n" );
                break;

            case Node.COMMENT_NODE:
                out.write( indent + "<!--" + node.getNodeValue() + "-->\n" );
                break;

            default:
                throw new IllegalArgumentException( "Type of node not supported, node name: " + node.getNodeName() + " type:" + node.getNodeType() );
        }
    }


    /**
     * D E M O
     */
    public static void main( String[] args ) {

        DOMImplementationImpl impl = new DOMImplementationImpl();
        Document document = impl.createDocument( "net:foo", "a", null );  //doctype only used by dtds !

        Element a = document.getDocumentElement();

        Element b = document.createElement( "b" );
        Element c = document.createElement( "c" );

        a.appendChild( b );
        a.appendChild( c );
        print( a, System.out );

        System.out.println( "\n\n" );

        b.appendChild( c );
        print( a, System.out );
    }
}