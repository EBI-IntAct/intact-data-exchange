// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.SmallMolecule;

/**
 * Define a common interface between various implementation of SmallMolecule's XML transformer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface SmallMolecule2xmlI {

    //////////////////////////////
    // Cosntants

    public static final String SMALL_MOLECULE_NODE_NAME = "smallMoleculeInteractor";
    public static final String SMALL_MOLECULE_REF_TAG_NAME = "smallMoleculeInteractorRef";

    public static final String PARENT_TAG_NAME = "snallMoleculeParticipant";

    ///////////////////////////////
    // Public methods

    /**
     * Generated an proteinInteractorRef out of an IntAct SmallMolecule.
     *
     * @param session
     * @param parent        the Element to which we will add the smallMoleculeInteractorRef.
     * @param smallMolecule the IntAct smallMolecule that we convert to PSI.
     *
     * @return the generated smallMoleculeInteractorRef Element.
     */
    public Element createReference( UserSessionDownload session, Element parent, SmallMolecule smallMolecule );

    /**
     * Generated an smallMoleculeInteractor out of an IntAct SmallMolecule.
     *
     * @param session
     * @param parent        the Element to which we will add the smallMoleculeInteractor.
     * @param smallMolecule the IntAct smallMolecule that we convert to PSI.
     *
     * @return the generated smallMoleculeInteractor Element.
     */
    public Element create( UserSessionDownload session, Element parent, SmallMolecule smallMolecule );
}