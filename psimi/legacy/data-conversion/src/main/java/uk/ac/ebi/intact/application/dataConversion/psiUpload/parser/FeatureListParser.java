/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class FeatureListParser {

    /**
     * Take &lt;featureList&gt; in parameter.
     *
     * @param element
     */
    public static Collection process( final Element element ) {

        Collection features = new ArrayList( 2 );

        final String name = element.getNodeName();
        if ( false == "featureList".equals( name ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in a " +
                                                                                "featureList tag." ) );
            // TODO should we carry on here ? If the tag is not right ... the parsing can only fail !
        }

        final NodeList someFeatures = element.getElementsByTagName( "feature" );
        final int count = someFeatures.getLength();

        for ( int i = 0; i < count; i++ ) {
            final Node featureNode = someFeatures.item( i );

            final FeatureTag feature = FeatureParser.process( (Element) featureNode );

            features.add( feature );
        } // features

        return features;
    }
}