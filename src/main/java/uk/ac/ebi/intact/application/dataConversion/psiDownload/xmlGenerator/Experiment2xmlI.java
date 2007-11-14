// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.Experiment;

/**
 * Define a common interface between various implementation of Experiment's XML transformer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Experiment2xmlI {

    /////////////////////////
    // Constants

    public static final String EXPERIMENT_DESCRIPTION_TAG_NAME = "experimentDescription";
    public static final String EXPERIMENT_REF_TAG_NAME = "experimentRef";

    ////////////////////////
    // Public signatures

    /**
     * Generated an experimentDescription out of an IntAct Experiment.
     *
     * @param session
     * @param parent     the Element to which we will add the experimentDescription.
     * @param experiment the IntAct experiment that we convert to PSI.
     *
     * @return the generated experimentDescription Element.
     */
    public Element createReference( UserSessionDownload session, Element parent, Experiment experiment );

    /**
     * Generated an experimentDescription out of an IntAct Experiment.
     *
     * @param session
     * @param parent     the Element to which we will add the experimentDescription.
     * @param experiment the IntAct experiment that we convert to PSI.
     *
     * @return the generated experimentDescription Element.
     */
    public Element create( UserSessionDownload session, Element parent, Experiment experiment );
}
