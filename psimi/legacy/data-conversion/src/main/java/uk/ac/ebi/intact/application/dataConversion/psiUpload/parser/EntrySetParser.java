/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntrySetTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntryTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * That class is parsing a set of Entry.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public final class EntrySetParser {

    /**
     * Parse an &lt;entrySet&gt;
     *
     * @param entrySet the entrySet to parse.
     *
     * @return the result of the parsing of that entrySet.
     */
    public EntrySetTag process( Element entrySet ) {

        if ( false == "entrySet".equals( entrySet.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( entrySet, "ERROR - We should be in entrySet tag." ) );
        }

        NodeList someEntries = entrySet.getElementsByTagName( "entry" );
        int count = someEntries.getLength();
        Collection entries = new ArrayList( count );

        for ( int i = 0; i < someEntries.getLength(); i++ ) {
            Node entryNode = someEntries.item( i );
            final EntryTag entry = EntryParser.process( (Element) entryNode );
            if ( entry == null ) {

            } else {
                entries.add( entry );
            }
        } // entries


        return new EntrySetTag( entries );
    }
}
