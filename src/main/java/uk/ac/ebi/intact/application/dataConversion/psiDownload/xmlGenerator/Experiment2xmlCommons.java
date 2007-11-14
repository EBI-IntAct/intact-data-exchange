// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.apache.commons.collections.CollectionUtils;
import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.PsiVersion;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.model.Experiment;
import uk.ac.ebi.intact.model.Xref;

import java.util.Collection;
import java.util.Iterator;

/**
 * Behaviour shared across Experiment's PSI implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Experiment2xmlCommons {

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static Experiment2xmlCommons ourInstance = new Experiment2xmlCommons();

    public static Experiment2xmlCommons getInstance() {
        return ourInstance;
    }

    private Experiment2xmlCommons() {
    }

    //////////////////////
    // Package methods

    /**
     * Generate and add to the given element the Xrefs of the experiment (but no pubmed). <br> The given set of pubmed
     * Xref is used to filter those not to generate again.
     *
     * @param session
     * @param element     The element to which we add the xref tag and its content.
     * @param experiment  the IntAct experiment from which we get the Xrefs.
     * @param pubmedXrefs the Xrefs that have already been generated. Is not null, but can be empty.
     */
    public void createExperimentXrefs( UserSessionDownload session,
                                       Element element,
                                       Experiment experiment,
                                       Collection pubmedXrefs ) {

        Element xrefElement = session.createElement( "xref" );
        element.appendChild( xrefElement );

        // add the AC of the experiment as Xref.
        Xref2xmlFactory.getInstance( session ).createIntactReference( session, xrefElement, experiment );

        Collection xrefs = experiment.getXrefs();
        if ( pubmedXrefs.size() == xrefs.size() ) {
            // no xrefs to be generated.
            return;
        }

        Collection xrefsToGenerated = CollectionUtils.subtract( xrefs, pubmedXrefs );
        Iterator iterator = xrefsToGenerated.iterator();
        Xref xref = null;

        // generate primaryRef
        if ( iterator.hasNext() ) {
            xref = (Xref) iterator.next();
            if ( xrefElement.getChildNodes().getLength() == 0 ) {
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, xref );
            } else {
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        // generate secondaryRef
        while ( iterator.hasNext() ) {
            xref = (Xref) iterator.next();
            Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
        }
    }

    /**
     * generate the bibliographical reference of the Experiment out of the Xref having the CvDatabase pubmed. We
     * distinguish primary and secondaryRef using the CvXrefQualifier of the Xref: primary-reference gives the
     * primaryRef and see-also gives the secondaryRef.
     *
     * @param session
     * @param parent     the element to which we will add the bibRef.
     * @param experiment the experiment from which we get the Xrefs.
     *
     * @return the subset of the Experiment's Xref out of which we have generated the bibRef. Never null but can be
     *         empty.
     */
    public Collection createBibRef( UserSessionDownload session, Element parent, Experiment experiment ) {

        // 1. Get pubmed xrefs.
        Collection pubmedXrefs = AbstractAnnotatedObject2xml.getXrefByDatabase( experiment, CvDatabase.PUBMED );

        if ( pubmedXrefs.isEmpty() ) {

            // 2. Nothing to generate, exit.
            return pubmedXrefs;

        } else {

            Element bibRefElement = session.createElement( "bibref" );
            parent.appendChild( bibRefElement );
            Element xrefElement = session.createElement( "xref" );
            bibRefElement.appendChild( xrefElement );

            // one to many xrefs available, generate them
            // put primary-reference as primaryRef, see-also as secondaryRef, any other as warning.

            // 2. Process primary-reference
            Xref primaryReference = null;
            for ( Iterator iterator = pubmedXrefs.iterator(); iterator.hasNext(); ) {
                Xref xref = (Xref) iterator.next();

                if ( xref.getCvXrefQualifier() != null &&
                     CvXrefQualifier.PRIMARY_REFERENCE.equals( xref.getCvXrefQualifier().getShortLabel() ) ) {

                    // keep reference
                    primaryReference = xref;

                    // create xref
                    Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, primaryReference );

                    // break the loop as there is only one primary-reference
                    break;
                }
            }

            // 3. Handle the case when no primary-reference found.
            if ( primaryReference == null ) {
                // if no primary-reference selected, we pick a random one
                // keep reference
                primaryReference = (Xref) pubmedXrefs.iterator().next();

                // create xref
                Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, primaryReference );
            }

            // 4. Process see-also
            for ( Iterator iterator = pubmedXrefs.iterator(); iterator.hasNext(); ) {
                Xref xref = (Xref) iterator.next();

                if ( xref != primaryReference ) {
                    // create xref
                    Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
                }
            }
        } // if any pubmed xrefs.

        // if PSI version 2 we may generate an attributeList
        if ( session.getPsiVersion().equals( PsiVersion.VERSION_2 ) ) {

            // TODO find out what do we put under attributeList.
        }

        return pubmedXrefs;

    } // createBibRef

    /**
     * get the value what will be used as ID of the experiment.
     *
     * @param experiment the experiment for which we need an ID.
     *
     * @return the ID of the experiment.
     */
    public String getExperimentId( Experiment experiment ) {

        // TODO test that.

        // TODO what if no AC available ? shortlabel (could be redondant) ? Object pointer/HashCode (will be unique in the scope of the document) ?
        return experiment.getAc();
    }
}