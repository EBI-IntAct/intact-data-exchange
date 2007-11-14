// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.SmallMolecule;
import uk.ac.ebi.intact.model.Xref;

import java.util.Collection;
import java.util.Iterator;

/**
 * Behaviour shared across small molecule's PSI implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class SmallMolecule2xmlCommons {

    /////////////////////////////
    // Singleton's methods

    private static SmallMolecule2xmlCommons ourInstance = new SmallMolecule2xmlCommons();

    public static SmallMolecule2xmlCommons getInstance() {
        return ourInstance;
    }

    private SmallMolecule2xmlCommons() {
    }

    ///////////////////////////////
    // pachage Methods

    /**
     * get the value what will be used as ID of the small molecule.
     *
     * @param smallMolecule the small molecule for which we need an ID.
     *
     * @return the ID of the small molecule.
     */
    public String getSmallMoleculeId( SmallMolecule smallMolecule ) {

        // TODO test that.

        // TODO what if no AC available ? shortlabel (could be redondant) ? Object pointer/HashCode (will be unique in the scope of the document) ?
        return smallMolecule.getAc();
    }

    /**
     * Generate the xref tag of the given small molecule. That content is attached to the given parent Element.
     *
     * @param session
     * @param parent        the proteinInteractor Element to which we will attach the Xref Element and its content.
     * @param smallMolecule the small molecule from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    public Element createSmallMoleculeXrefs( UserSessionDownload session, Element parent, SmallMolecule smallMolecule ) {

        Element xrefElement = session.createElement( "xref" );
        parent.appendChild( xrefElement );

        // TODO define here what should be the priority on CvDatabase: my guess RESID, CABRI...

        // 1. get RESID xrefs
        Collection uniprotXrefs = AbstractAnnotatedObject2xml.getXrefByDatabase( smallMolecule, CvDatabase.RESID );

        // 2. process the identity
        Xref identity = null;
        for ( Iterator iterator = uniprotXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();
            if ( xref.getCvXrefQualifier() != null &&
                 CvXrefQualifier.IDENTITY.equals( xref.getCvXrefQualifier().getShortLabel() ) ) {

                // keep reference
                identity = xref;

                // create xref
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, identity );

                // break the loop as there is only one identity
                break;
            }
        }

        // 3. process remaining RESID xrefs
        for ( Iterator iterator = uniprotXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();
            if ( xref != identity ) {
                // create xref
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        // 4. generate all other xrefs (GO, SGD...)
        for ( Iterator iterator = smallMolecule.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( !uniprotXrefs.contains( xref ) ) {
                // create xref
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        return xrefElement;
    }
}