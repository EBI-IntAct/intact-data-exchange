// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Feature;
import uk.ac.ebi.intact.model.Xref;

import java.util.Collection;
import java.util.Iterator;

/**
 * Behaviour shared across Feature's PSI implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Feature2xmlCommons {

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Feature2xmlCommons ourInstance = new Feature2xmlCommons();

    public static Feature2xmlCommons getInstance() {
        return ourInstance;
    }

    private Feature2xmlCommons() {
    }

    ////////////////////////////
    // Pachage methods

    /**
     * Generate and add to the given element the Xrefs of the feature.
     *
     * @param session
     * @param parent  The element to which we add the xref tag and its content.
     * @param feature the IntAct feature from which we get the Xrefs.
     */
    public void createFeatureXrefs( UserSessionDownload session, Element parent, Feature feature ) {

        // TODO test it: with interpro/identity and without, a primaryRef should be generated.

        if ( feature.getXrefs().isEmpty() ) {
            return;
        }

        Element xrefElement = session.createElement( "xref" );

        // 1. get interpro xrefs
        Collection interproXrefs = AbstractAnnotatedObject2xml.getXrefByDatabase( feature, CvDatabase.INTERPRO );

        // 2. process the identity
        Xref identity = null;
        if ( false == interproXrefs.isEmpty() ) {
            identity = (Xref) interproXrefs.iterator().next();
            // create xref
            Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, identity );
        }

        // 3. generate all other xrefs
        for ( Iterator iterator = feature.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( identity == null ) {

                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, xref );
                identity = xref;

            } else if ( xref != identity ) {

                // create xref
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        // 4. Attaching the newly created element to the parent...
        parent.appendChild( xrefElement );
    }
}