// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2.CvObject2xmlPSI2;
import uk.ac.ebi.intact.model.Range;


/**
 * Process the common behaviour of an IntAct Range when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Range2xmlPSI25 {

    public static final String FEATURE_RANGE_NAME = "featureRange";

    /////////////////////////////
    // Singleton's methods

    private static Range2xmlPSI25 ourInstance = new Range2xmlPSI25();

    protected static Range2xmlPSI25 getInstance() {
        return ourInstance;
    }

    private Range2xmlPSI25() {
    }

    //////////////////////////////
    // Public methods

    public void create( UserSessionDownload session, Element parent, Range range ) {

        // NOTE : startStatus begin beginInterval endStatus end endInterval isLink


        Element locationElement = session.createElement( FEATURE_RANGE_NAME );

        // 1. Generating startStatus ...
        if ( range.getFromCvFuzzyType() != null ) {
            CvObject2xmlPSI2.getInstance().createStartStatus( session, locationElement, range.getFromCvFuzzyType() );
        } else {
            // generate certain.
            CvObject2xmlPSI2.getInstance().createStartCertainStatus( session, locationElement );
        }

        // Processing begin
        Element beginElement = null;

        if ( range.getFromIntervalStart() == range.getFromIntervalEnd() ) {

            // build a begin tag
            beginElement = session.createElement( "begin" );
            beginElement.setAttribute( "position", "" + range.getFromIntervalEnd() );

        } else {

            // build a beginInterval tag
            beginElement = session.createElement( "beginInterval" );
            beginElement.setAttribute( "begin", "" + range.getFromIntervalStart() );
            beginElement.setAttribute( "end", "" + range.getFromIntervalEnd() );
        }

        locationElement.appendChild( beginElement );

        // 3. Generating endStatus ...
        if ( range.getFromCvFuzzyType() != null ) {
            CvObject2xmlPSI2.getInstance().createEndStatus( session, locationElement, range.getFromCvFuzzyType() );
        } else {
            // generate certain.
            CvObject2xmlPSI2.getInstance().createEndCertainStatus( session, locationElement );
        }

        // 4. Generating end [or] endInterval
        Element endElement = null;

        if ( range.getToIntervalStart() == range.getToIntervalEnd() ) {

            // build a begin tag
            endElement = session.createElement( "end" );
            endElement.setAttribute( "position", "" + range.getToIntervalEnd() );

        } else {

            // build a beginInterval tag
            endElement = session.createElement( "endInterval" );
            endElement.setAttribute( "begin", "" + range.getToIntervalStart() );
            endElement.setAttribute( "end", "" + range.getToIntervalEnd() );
        }

        locationElement.appendChild( endElement );

        // 5. Generating isLink...
        if ( range.isLinked() ) {

            Element isLinkElement = session.createElement( "isLink" );
            Text booleanText = session.createTextNode( "true" );
            isLinkElement.appendChild( booleanText.cloneNode( true ) );
            locationElement.appendChild( isLinkElement );
        }

        // Attaching the newly created element to the parent...
        parent.appendChild( locationElement );
    }
}