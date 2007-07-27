// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.Feature;

/**
 * Define a common interface between various implementation of Feature's XML transformer.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public interface Feature2xmlI {

    //////////////////////
    // Constants

    public static final String FEATURE_TAG_NAME = "feature";
    public static final String PARENT_TAG_NAME = "featureList";

    //////////////////////
    // Public method

    /**
     * Generated an feature out of an IntAct Feature.
     * <pre>
     * Rules:
     * <p/>
     * Feature.CvFeatureIdentification is mapped to featureDetection
     * Feature.xref                    is mapped to xref                     // interpro
     * Feature.CvFeatureType           is mapped to featureDescription.xref
     * </pre>
     *
     * @param session
     * @param parent  the Element to which we will add the feature.
     * @param feature the IntAct Feature that we convert to PSI.
     *
     * @return the generated feature Element.
     */
    public void create( UserSessionDownload session, Element parent, Feature feature );
}