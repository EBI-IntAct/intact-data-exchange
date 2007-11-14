/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.LocationIntervalTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.LocationTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

/**
 * That class parses a feature tag and create a <code>FeatureTag</code> object.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.FeatureTag
 */
public class LocationParser {

    public static LocationTag process( Element element ) {

        if ( false == "location".equals( element.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in location tag." ) );
        }

        /**
         *  <location>
         *      <begin position="2"/>
         *      <endInterval begin="10" end="13"/>
         *  </location>
         *
         *  <location >
         *      <beginInterval begin="10" end="13/>
         *      <end position="2"/>
         *  </location>
         *
         *  <location >
         *      <position position="7"/>
         *  </location>
         *
         *  <location >
         *      <site position="122"/>
         *  </location>
         */

        long fromIntervalStart = 0;
        long fromIntervalEnd = 0;

        long toIntervalStart = 0;
        long toIntervalEnd = 0;

        // look for all possibilities of location
        Element positionElement = DOMUtil.getFirstElement( element, "position" );
        Element siteElement = DOMUtil.getFirstElement( element, "site" );
        Element beginElement = DOMUtil.getFirstElement( element, "begin" );
        Element beginIntervalElement = DOMUtil.getFirstElement( element, "beginInterval" );
        Element endElement = DOMUtil.getFirstElement( element, "end" );
        Element endIntervalElement = DOMUtil.getFirstElement( element, "endInterval" );


        // check if there is any mix up
        // TODO - if the XML is checked to be valid beforehand, we can drop that check.
        boolean error = false;
        String errorMessage = null;
        if ( beginElement != null && beginIntervalElement != null ) {
            // if there is two kind of begin
            errorMessage = "Cannot interpret 'start' and 'startInterval' in the same location";
            error = true;

        } else if ( endElement != null && endIntervalElement != null ) {
            // if there is two kind of end
            errorMessage = "Cannot interpret 'end' and 'endInterval' in the same location";
            error = true;


        } else if ( ( beginElement != null || beginIntervalElement != null )
                    &&
                    !( endElement != null || endIntervalElement != null ) ) {
            // cannot have a begin without end
            errorMessage = "Cannot interpret 'start' without 'end' in the same location";
            error = true;


        } else if ( !( beginElement != null || beginIntervalElement != null )
                    &&
                    ( endElement != null || endIntervalElement != null ) ) {
            // cannot have a end without begin
            errorMessage = "Cannot interpret 'end' without 'start' in the same location";
            error = true;


        } else if ( ( ( beginElement != null || beginIntervalElement != null )
                      ||
                      ( endElement != null || endIntervalElement != null ) )
                    &&
                    ( siteElement != null || positionElement != null ) ) {
            // if there is a begin or end what-so-ever and a position or site
            errorMessage = "Cannot interpret 'start' and/or 'end' added to 'position' or 'site' in the same location";
            error = true;
        }

        // if the location was correctly given.
        if ( !error ) {

            if ( positionElement != null ) {

                String position = positionElement.getAttribute( "position" );
                fromIntervalStart = Long.parseLong( position );
                fromIntervalEnd = toIntervalEnd = toIntervalStart = fromIntervalStart;

            } else if ( siteElement != null ) {

                String site = siteElement.getAttribute( "position" );
                fromIntervalStart = Long.parseLong( site );
                fromIntervalEnd = toIntervalEnd = toIntervalStart = fromIntervalStart;

            } else {

                if ( beginElement != null ) {

                    String begin = beginElement.getAttribute( "position" );
                    fromIntervalStart = Long.parseLong( begin );
                    fromIntervalEnd = fromIntervalStart;

                } else if ( beginIntervalElement != null ) {

                    String beginStart = beginIntervalElement.getAttribute( "begin" );
                    String beginStop = beginIntervalElement.getAttribute( "end" );
                    fromIntervalStart = Long.parseLong( beginStart );
                    fromIntervalEnd = Long.parseLong( beginStop );
                }


                if ( endElement != null ) {

                    String start = endElement.getAttribute( "position" );
                    toIntervalStart = Long.parseLong( start );
                    toIntervalEnd = toIntervalStart;

                } else if ( endIntervalElement != null ) {

                    String beginStart = endIntervalElement.getAttribute( "begin" );
                    String beginStop = endIntervalElement.getAttribute( "end" );
                    toIntervalStart = Long.parseLong( beginStart );
                    toIntervalEnd = Long.parseLong( beginStop );
                }
            }

        } else {

            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - " + errorMessage ) );
        }

        LocationIntervalTag from = new LocationIntervalTag( fromIntervalStart, fromIntervalEnd );
        LocationIntervalTag to = new LocationIntervalTag( toIntervalStart, toIntervalEnd );
        LocationTag location = null;
        try {

            location = new LocationTag( from, to );

        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, e.getMessage() ) );
        }

        return location;
    }
}
