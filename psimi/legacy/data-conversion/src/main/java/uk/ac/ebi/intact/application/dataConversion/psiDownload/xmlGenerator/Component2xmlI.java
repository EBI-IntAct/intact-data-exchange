// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.Component;

/**
 * Implements the tranformation of an IntAct Component into PSI XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Component2xmlI {

    /////////////////////
    // Constants

    public static final String PROTEIN_PARTICIPANT_TAG_NAME = "proteinParticipant";
    public static final String PARENT_TAG_NAME = "participantList";

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
    public Element create( UserSessionDownload session, Element parent, Component component );
}