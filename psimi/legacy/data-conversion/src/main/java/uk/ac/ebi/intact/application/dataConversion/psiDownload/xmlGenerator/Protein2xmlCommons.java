// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Protein;
import uk.ac.ebi.intact.model.Xref;

import java.util.Collection;
import java.util.Iterator;

/**
 * Behaviour shared across Protein's PSI implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Protein2xmlCommons {

    /////////////////////////////
    // Singleton's methods

    private static Protein2xmlCommons ourInstance = new Protein2xmlCommons();

    public static Protein2xmlCommons getInstance() {
        return ourInstance;
    }

    private Protein2xmlCommons() {
    }

    ///////////////////////////////
    // pachage Methods

    /**
     * get the value what will be used as ID of the protein.
     *
     * @param protein the protein for which we need an ID.
     *
     * @return the ID of the protein.
     */
    public String getProteinId( Protein protein ) {

        // TODO test that.

        // TODO what if no AC available ? shortlabel (could be redondant) ? Object pointer/HashCode (will be unique in the scope of the document) ?
        return protein.getAc();
    }

    /**
     * Generate the xref tag of the given protein. That content is attached to the given parent Element.
     *
     * @param session
     * @param parent  the proteinInteractor Element to which we will attach the Xref Element and its content.
     * @param protein the protein from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    public Element createProteinXrefs_old( UserSessionDownload session, Element parent, Protein protein ) {

        Element xrefElement = session.createElement( "xref" );
        parent.appendChild( xrefElement );

        // 1. get uniprot xrefs
        Collection uniprotXrefs = AbstractAnnotatedObject2xml.getXrefByDatabase( protein, CvDatabase.UNIPROT );

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

        // 3. process remaining uniprot xrefs
        for ( Iterator iterator = uniprotXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();
            if ( xref != identity ) {
                // create xref
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        // 4. generate all other xrefs (GO, SGD...)
        for ( Iterator iterator = protein.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( !uniprotXrefs.contains( xref ) ) {
                // create xref
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        return xrefElement;
    }


    /**
     * Generate the xref tag of the given interactor. That content is attached to the given parent Element.
     *
     * @param session
     * @param parent  the interactor Element to which we will attach the Xref Element and its content.
     * @param protein the interactor from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    public Element createProteinXrefs( UserSessionDownload session, Element parent, Protein protein ) {

        Element xrefElement = session.createElement( "xref" );

        // 1. get uniprot xrefs
        Collection identityXrefs = AbstractAnnotatedObject2xml.getXrefByQualifier( protein, CvXrefQualifier.IDENTITY );

        if ( identityXrefs.isEmpty() ) {
            // if not identity were found, try primary-reference
            identityXrefs = AbstractAnnotatedObject2xml.getXrefByQualifier( protein, CvXrefQualifier.PRIMARY_REFERENCE );
        }

        // 2. process the identities
        Xref identity = null;
        for ( Iterator iterator = identityXrefs.iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( identity == null ) {
                // keep reference
                identity = xref;

                // create xref
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, identity );

            } else {

                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        // 3. generate all other xrefs (GO, SGD...)
        for ( Iterator iterator = protein.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( identity == null ) {
                // keep reference
                identity = xref;

                // create xref
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, identity );

            } else {

                if ( !identityXrefs.contains( xref ) ) {
                    // create xref
                    Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
                }
            }
        }

        // 4. generate IntAct reference
        Xref2xmlFactory.getInstance( session ).createIntactReference( session, xrefElement, protein );

        // 5. attach the xref to the parent node.
        if ( xrefElement.hasChildNodes() ) {
            // don't insert an empty tag.
            parent.appendChild( xrefElement );
        }

        return xrefElement;
    }
}