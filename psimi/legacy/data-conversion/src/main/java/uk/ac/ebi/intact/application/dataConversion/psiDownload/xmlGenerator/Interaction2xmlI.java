// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.Interaction;

/**
 * Define a common interface between various implementation of Interaction's XML transformer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Interaction2xmlI {

    /////////////////////////
    // Constants

    public static final String INTERACTION_TAG_NAME = "interaction";

    ///////////////////////////
    // Methods

    /**
     * Generated an interaction out of an IntAct Interaction.
     *
     * @param session
     * @param parent      the Element to which we will add the proteinInteractor.
     * @param interaction the IntAct Interaction that we convert to PSI.
     *
     * @return the generated interaction Element.
     */
    public Element create( UserSessionDownload session, Element parent, Interaction interaction );
}