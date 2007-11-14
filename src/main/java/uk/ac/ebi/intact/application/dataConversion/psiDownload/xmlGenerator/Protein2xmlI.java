// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.Protein;

/**
 * Define a common interface between various implementation of Protein's XML transformer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Protein2xmlI {

    //////////////////////////////
    // Cosntants

    public static final String PROTEIN_INTERACTOR_TAG_NAME = "proteinInteractor";
    public static final String PROTEIN_INTERACTOR_REF_TAG_NAME = "proteinInteractorRef";
    public static final String PROTEIN_PARTICIPANT_REF_TAG_NAME = "proteinParticipantRef";

    public static final String PARENT_TAG_NAME = "proteinParticipant";

    ///////////////////////////////
    // Public methods

    /**
     * Generates an proteinInteractorRef out of an IntAct Protein.
     *
     * @param session
     * @param parent  the Element to which we will add the proteinInteractorRef.
     * @param protein the IntAct Protein that we convert to PSI.
     *
     * @return the generated proteinInteractorRef Element.
     */
    public Element createProteinInteracorReference( UserSessionDownload session, Element parent, Protein protein );

    /**
     * Generates an proteinParticipantRef out of an IntAct Protein.
     *
     * @param session
     * @param parent  the Element to which we will add the proteinParticipantRef.
     * @param protein the IntAct Protein that we convert to PSI.
     *
     * @return the generated proteinParticipantRef Element.
     */
    public Element createParticipantReference( UserSessionDownload session, Element parent, Protein protein );

    /**
     * Generated an proteinInteractor out of an IntAct Protein.
     *
     * @param session
     * @param parent  the Element to which we will add the proteinInteractor.
     * @param protein the IntAct Protein that we convert to PSI.
     *
     * @return the generated proteinInteractor Element.
     */
    public Element create( UserSessionDownload session, Element parent, Protein protein );
}