// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.AnnotatedObject;

/**
 * Implements the tranformation of an IntAct AnnotatedObject's annotated into confidence PSI XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Confidence2xmlI {

    ////////////////////////
    // Constant

    public static final String CONFIDENCE_LIST_TAG_NAME = "confidenceList";
    public static final String CONFIDENCE_TAG_NAME = "confidence";
    public static final String CONFIDENCE_VALUE_TAG_NAME = "value";
    public static final String CONFIDENCE_UNIT_TAG_NAME = "unit";

    /////////////////////
    // Public methods

    /**
     * Generated an proteinParticipant out of an IntAct Component.
     *
     * @param session
     * @param parent    the Element to which we will add the proteinParticipant.
     * @param component the IntAct Component that we convert to PSI.
     *
     * @return the generated proteinParticipant Element.
     */
    public Element create( UserSessionDownload session, Element parent, AnnotatedObject ao );
}