/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.*;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

import java.util.Collection;

/**
 * That class parses a feature tag and create a <code>FeatureTag</code> object.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag
 */
public class FeatureParser {

    public static FeatureTag process( Element element ) {

        if ( false == "feature".equals( element.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in feature tag." ) );
        }


        // OPTIONAL - get xref
        Element xrefElement = DOMUtil.getFirstElement( (Element) element, "xref" );
        Collection xrefs = null;
        if ( xrefElement != null ) {
            xrefs = XrefParser.process( xrefElement );
        }


        // get feature description (shortlabel and xref mandatory)
        // TODO take shortlabel and fullname out as there are no longer mandatory
        String descriptionShortLabel = null;
        String descriptionFullName = null;
        FeatureTypeTag featureType = null;

        // MANDATORY - eg. Binding Site, MI:0117 (uk.ac.ebi.intact.model.CvFeatureType)
        Element descriptionElement = DOMUtil.getFirstElement( (Element) element, "featureDescription" );
        if ( descriptionElement != null ) {
            Element descriptionNameElement = DOMUtil.getFirstElement( descriptionElement, "names" );
            descriptionShortLabel = DOMUtil.getShortLabel( descriptionNameElement );
            descriptionFullName = DOMUtil.getFullName( descriptionNameElement );

            Element typeXrefElement = DOMUtil.getFirstElement( descriptionElement, "xref" );
            if ( typeXrefElement != null ) {
                XrefTag typeXref = XrefParser.processPrimaryRef( typeXrefElement );
                featureType = new FeatureTypeTag( typeXref );
            }
        }


        // OPTIONAL - get feature detection
        Element detectionElement = DOMUtil.getFirstElement( (Element) element, "featureDetection" );
        FeatureDetectionTag featureDetection = null;
        if ( detectionElement != null ) {
            Element detectionXrefElement = DOMUtil.getFirstElement( detectionElement, "xref" );
            XrefTag detectionXref = XrefParser.processPrimaryRef( detectionXrefElement );

            featureDetection = new FeatureDetectionTag( detectionXref );
        }


        // get location details
        Element locationElement = DOMUtil.getFirstElement( (Element) element, "location" );
        LocationTag location = LocationParser.process( locationElement );


        // creation of the featureTag
        FeatureTag feature = null;
        try {
            feature = new FeatureTag( descriptionShortLabel, descriptionFullName,
                                      featureType,
                                      location,
                                      featureDetection,
                                      xrefs );
        } catch ( IllegalArgumentException e ) {

            MessageHolder.getInstance().addParserMessage( new Message( element, e.getMessage() ) );
        }

        return feature;
    }
}
