// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.CvObject;

/**
 * Define a common interface between various implementation of CvObject's XML transformer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface CvObject2xmlI {

    ////////////////////////
    // public method

    /**
     * Generic call that generates the XML representation of the given CvObject.
     *
     * @param session
     * @param parent   the parent to which we wil attach the generated XML document.
     * @param cvObject the CvObject of which we generate the XML representation.
     *
     * @return the XML representation of the given CvObject.
     */
    public Element create( UserSessionDownload session, Element parent, CvObject cvObject );
}