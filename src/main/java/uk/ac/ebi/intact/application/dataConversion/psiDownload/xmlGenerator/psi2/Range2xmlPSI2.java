// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi2;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.Range;


/**
 * Process the common behaviour of an IntAct Range when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Range2xmlPSI2 {

    /////////////////////////////
    // Singleton's methods

    private static Range2xmlPSI2 ourInstance = new Range2xmlPSI2();

    protected static Range2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private Range2xmlPSI2() {
    }

    //////////////////////////////
    // Public methods

    public void create( UserSessionDownload session, Element parent, Range range ) {

        Element locationElement = session.createElement( "location" );

        if ( range.isUndetermined() ) {

            Element beginElement = session.createElement( "beginUndetermined" );
            Text booleanText = session.createTextNode( "true" );
            beginElement.appendChild( booleanText );
            locationElement.appendChild( beginElement );

            Element endElement = session.createElement( "endUndetermined" );
            endElement.appendChild( booleanText.cloneNode( true ) );
            locationElement.appendChild( endElement );

        } else {

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

            // Processing end
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

        }

        if ( range.isLinked() ) {

            Element endElement = session.createElement( "isLink" );
            Text booleanText = session.createTextNode( "true" );
            endElement.appendChild( booleanText.cloneNode( true ) );
            locationElement.appendChild( endElement );
        }

        // Attaching the newly created element to the parent...
        parent.appendChild( locationElement );
    }
}