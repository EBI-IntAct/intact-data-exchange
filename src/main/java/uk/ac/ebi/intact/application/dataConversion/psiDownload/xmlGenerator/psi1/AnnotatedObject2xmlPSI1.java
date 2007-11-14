// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi1;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.AbstractAnnotatedObject2xml;
import uk.ac.ebi.intact.model.AnnotatedObject;

/**
 * Process the common behaviour of AnnotatedObject when exporting PSI version 1.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotatedObject2xmlPSI1 extends AbstractAnnotatedObject2xml {

    ////////////////////////////////////////
    // Implementation of abstract methods

    /**
     * Convert shortlabel and fullname of an AnnotatedObject into PSI XML. <br> The generated tags are attached to the
     * given parent.
     *
     * @param session pre-initialised user's session.
     * @param parent  the tag to which we attached the generated Data.
     * @param object  the AnnotatedObject from which we are converting the shortlabel and fullname.
     *
     * @return the generated data.
     */
    public Element createNames( UserSessionDownload session, Element parent, AnnotatedObject object ) {

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( object == null ) {
            throw new IllegalArgumentException( "You must give a non null Annotation." );
        }

        if ( parent == null ) {

            throw new IllegalArgumentException( "You must give a non null parent Element." );

        } else {

            ckeckParentName( parent );
        }

        // NOTE: children terms are:
        //       shortlabel [fullname]

        Element names = session.createElement( "names" );

        createShortlabel( session, names, object );

        createFullname( session, names, object );

        // append the term to the given parent
        parent.appendChild( names );

        return names;
    }
}