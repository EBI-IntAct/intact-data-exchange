// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.AbstractXref2Xml;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.Xref2xmlFactory;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.util.ToolBox;
import uk.ac.ebi.intact.model.*;

/**
 * Convert an IntAct Xref to PSI XML.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Xref2xmlPSI2 extends AbstractXref2Xml {

    //////////////////////////
    // Constants

    public static final String XREF_DB_AC = "dbAc";
    public static final String XREF_REFTYPE = "refType";
    public static final String XREF_REFTYPE_AC = "refTypeAc";

    /////////////////////////////
    // Singleton's methods

    private static Xref2xmlPSI2 ourInstance = new Xref2xmlPSI2();

    public static Xref2xmlPSI2 getInstance() {
        return ourInstance;
    }

    private Xref2xmlPSI2() {
    }

    //////////////////////////
    // Public methods

    /**
     * Build a primaryRef out of an IntAct AnnotatedObject.
     * <p/>
     * <pre>
     * <b>Rules</b>:
     *    object.ac           --->   id
     *    object.shortalbel   --->   secondary
     *    'intact'            --->   db
     * </pre>
     *
     * @param session
     * @param parent  the parent Element to which we will attach the primaryRef
     * @param object  the object from which we generate the IntAct primaryRef, based on its AC.
     *
     * @return a newly created primaryRef, or null if no AC available.
     */
    public Element createIntactReference( UserSessionDownload session, Element parent, AnnotatedObject object ) {

        Element element = null;

        if ( object.getAc() != null ) {

            if ( session.getSourceDatabase() != null ) {

                Xref xref;

                if (object instanceof Experiment)
                {
                    xref = new ExperimentXref( object.getOwner(),
                                      session.getSourceDatabase(),
                                      object.getAc(),
                                      object.getShortLabel(),
                                      null, null );
                }
                else if (object instanceof Interactor)
                {
                    xref = new InteractorXref( object.getOwner(),
                                      session.getSourceDatabase(),
                                      object.getAc(),
                                      object.getShortLabel(),
                                      null, null );
                }
                else if (object instanceof BioSource)
                {
                    xref = new BioSourceXref( object.getOwner(),
                                      session.getSourceDatabase(),
                                      object.getAc(),
                                      object.getShortLabel(),
                                      null, null );
                }
                else if (object instanceof Feature)
                {
                    xref = new FeatureXref( object.getOwner(),
                                      session.getSourceDatabase(),
                                      object.getAc(),
                                      object.getShortLabel(),
                                      null, null );
                }
                else if (object instanceof Publication)
                {
                    xref = new PublicationXref( object.getOwner(),
                                      session.getSourceDatabase(),
                                      object.getAc(),
                                      object.getShortLabel(),
                                      null, null );
                }
                else
                {
                    throw new RuntimeException("Not Xref type found for this object: "+object.getClass()+
                            " ; Maybe it is a new Xref type and Xref2xmlPSI2 is not yet implemented for it" +
                            "This might happen for Xref2xmlPSI1 too");
                }

                if ( parent.getChildNodes().getLength() == 0 ) {
                    Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, parent, xref );
                } else {
                    Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, parent, xref );
                }

            } else {

                // in case it hasn't been specified, generate IntAct Xref.
                if ( parent.getChildNodes().getLength() == 0 ) {
                    element = session.createElement( PRIMARY_REF );
                } else {
                    element = session.createElement( SECONDARY_REF );
                }

                parent.appendChild( element );

                element.setAttribute( XREF_DB, "intact" );
                element.setAttribute( XREF_DB_AC, "MI:0469" );
                element.setAttribute( XREF_ID, object.getAc() );
                element.setAttribute( XREF_SECONDARY, object.getShortLabel() );
            }
        }

        return element;
    }

    ///////////////////////////
    // Private methods

    /**
     * Add an cross reference in PSI Format to a DOM element.
     *
     * @param session pre-initialized user environment.
     * @param parent  dom element on which the Xref will be added.
     * @param xref    Intact Xref to convert into PSI.
     * @param tagName The name of the tag we will add to parent.
     */
    protected Element createRef( UserSessionDownload session, Element parent, Xref xref, String tagName ) {

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( xref == null ) {
            throw new IllegalArgumentException( "You must give a non null Xref." );
        }

        if ( parent == null ) {

            throw new IllegalArgumentException( "You must give a non null parent Element." );

        } else {

            if ( !PARENT_TERM_NAMES.contains( parent.getNodeName() ) ) {
                throw new IllegalArgumentException( "The parent term ( " + parent.getNodeName() +
                                                    " ) should be <xref> or <bibref>." );
            }
        }

        Element element = session.createElement( tagName );

        //local elements processing...
        element.setAttribute( XREF_DB, xref.getCvDatabase().getShortLabel() );

        String dbPsiMi = ToolBox.getPsiReference( xref.getCvDatabase() );
        if ( dbPsiMi != null ) {
            element.setAttribute( XREF_DB_AC, dbPsiMi );
        }

        element.setAttribute( XREF_ID, xref.getPrimaryId() );

        String sSecondary = xref.getSecondaryId();
        if ( null != sSecondary && sSecondary.length() > 0) {
            element.setAttribute( XREF_SECONDARY, sSecondary );
        }

        String sVersion = xref.getDbRelease();
        if ( null != sVersion && sVersion.length() > 0) {
            element.setAttribute( XREF_VERSION, sVersion );
        }

        CvXrefQualifier qualifier = xref.getCvXrefQualifier();
        if ( qualifier != null ) {
            element.setAttribute( XREF_REFTYPE, qualifier.getShortLabel() );

            // cache MI numbers
            String psiMi = ToolBox.getPsiReference( qualifier );
            if ( psiMi != null ) {
                element.setAttribute( XREF_REFTYPE_AC, psiMi );
            }
        }

        // add when we have it
        parent.appendChild( element );

        return element;
    }
}