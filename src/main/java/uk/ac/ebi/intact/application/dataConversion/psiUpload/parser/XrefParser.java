/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * That class . <br> Called XrefParser to avois clash with uk.ac.ebi.intact.model.Xref
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class XrefParser {

    /**
     * Method accesible within the package.
     *
     * @param xrefElement
     * @param db
     *
     * @return
     */
    static XrefTag getXrefByDb( final Element xrefElement,
                                final String db ) {

        if ( xrefElement == null ) {
            throw new IllegalArgumentException( "the xref element must not be null." );
        }

        if ( db == null || "".equals( db.trim() ) ) {
            throw new IllegalArgumentException( "the db given must not be null or empty." );
        }

        XrefTag interactionXref = null;

        XrefTag xref = XrefParser.processPrimaryRef( xrefElement );
        if ( db.equalsIgnoreCase( xref.getDb() ) ) {
            interactionXref = xref;
        } else {
            final Collection interactionDetectionXrefs = XrefParser.processSecondaryRef( xrefElement );
            for ( Iterator iterator = interactionDetectionXrefs.iterator(); iterator.hasNext(); ) {
                xref = (XrefTag) iterator.next();
                if ( db.equals( xref.getDb() ) ) {
                    if ( interactionXref == null ) {
                        interactionXref = xref;
                    } else {
                        // ERROR multiple definition of a psi element
                        System.err.println( "Multiple instance of that database (" + db + ")in the xref set." );
                    }
                }
            }
        }

        return interactionXref;
    }

    /**
     * Collect all primary and secondary Xrefs.
     * <p/>
     * <pre>
     *   <xref>
     *       <primaryRef db="Swiss-Prot" id="P10912"/>
     * <p/>
     *       <secondaryRef db="Swiss-Prot" id="P11111"/>
     *       <secondaryRef db="Swiss-Prot" id="P22222"/>
     *       <secondaryRef db="Swiss-Prot" id="P33333"/>
     *   </xref>
     * </pre>
     *
     * @return an intact Xref collection. Empty collection if something goes wrong.
     */
    public static Collection process( final Element root ) {
        Collection xrefs;

        xrefs = processSecondaryRef( root );
        XrefTag xref = processPrimaryRef( root );

        if ( xref != null ) {
            xrefs.add( xref );
        }

        return xrefs;
    }

    /**
     * Extract Primary Xref.
     * <p/>
     * <pre>
     *   <xref>
     *       <primaryRef db="Swiss-Prot" id="P10912"/>
     *   </xref>
     * </pre>
     *
     * @return an intact Xref or null if something goes wrong.
     */
    public static XrefTag processPrimaryRef( final Element root ) {
        XrefTag xref = null;

        final String nodeName = root.getNodeName();
        if ( false == "xref".equals( nodeName ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, "ERROR - We should be in xref tag, <" + root.getNodeName() + "> instead." ) );
        }

        final Element xrefNode = DOMUtil.getFirstElement( root, "primaryRef" ); // and the secondary ?!
        try {
            xref = createXref( xrefNode, XrefTag.PRIMARY_REF );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, e.getMessage() ) );
        }

        return xref;
    }

    /**
     * Extract data from an xref Element and produce an Intact Xref
     * <p/>
     * <pre>
     *   <xref>
     *       <secondaryRef db="Swiss-Prot" id="P11111"/>
     *       <secondaryRef db="Swiss-Prot" id="P22222"/>
     *       <secondaryRef db="Swiss-Prot" id="P33333"/>
     *   </xref>
     * </pre>
     *
     * @return a Collection of intact Xref or empty if something goes wrong.
     */
    public static Collection processSecondaryRef( final Element root ) {

        if ( false == "xref".equals( root.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( root, "ERROR - We should be in xref tag." ) );
        }

        final NodeList someXrefs = root.getElementsByTagName( "secondaryRef" );
        final int count = someXrefs.getLength();
        final Collection xrefs = new ArrayList( count );

        for ( int i = 0; i < count; i++ ) {
            final Element xrefNode = (Element) someXrefs.item( i );

            XrefTag xref;
            try {
                xref = createXref( xrefNode, XrefTag.SECONDARY_REF );
                if ( xref != null ) {
                    xrefs.add( xref );
                }
            } catch ( IllegalArgumentException e ) {
                MessageHolder.getInstance().addParserMessage( new Message( root, e.getMessage() ) );
            }
        } // secondaryRefs

        return xrefs;
    }

    private static XrefTag createXref( final Element xrefNode, final short type ) {

        XrefTag xref = null;
        try {
            String db = xrefNode.getAttribute( "db" );
            if ( db != null ) {
                db = db.toLowerCase();
            }

            xref = new XrefTag( type,
                                xrefNode.getAttribute( "id" ),
                                db,
                                xrefNode.getAttribute( "secondary" ),
                                xrefNode.getAttribute( "version" ) );

        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( xrefNode, e.getMessage() ) );
        }

        return xref;
    }
}
