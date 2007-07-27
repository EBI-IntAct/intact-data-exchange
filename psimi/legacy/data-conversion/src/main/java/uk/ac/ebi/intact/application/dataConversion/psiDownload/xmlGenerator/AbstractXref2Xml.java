/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.Xref;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since <pre>29-Jun-2005</pre>
 */
public abstract class AbstractXref2Xml {

    //////////////////////////
    // Constants

    public static final String PRIMARY_REF = "primaryRef";
    public static final String SECONDARY_REF = "secondaryRef";

    // common attributes
    public static final String XREF_ID = "id";
    public static final String XREF_DB = "db";
    public static final String XREF_SECONDARY = "secondary";
    public static final String XREF_VERSION = "version";

    protected static final Set PARENT_TERM_NAMES = new HashSet( 2 );

    static {

        PARENT_TERM_NAMES.add( "xref" );
        PARENT_TERM_NAMES.add( "bibref" );
    }

    //////////////////////////////
    // Abstract methods

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
    public abstract Element createIntactReference( UserSessionDownload session, Element parent, AnnotatedObject object );

    /**
     * Add an cross reference in PSI Format to a DOM element.
     *
     * @param session pre-initialized user environment.
     * @param parent  dom element on which the Xref will be added.
     * @param xref    Intact Xref to convert into PSI.
     * @param tagName The name of the tag we will add to parent.
     */
    protected abstract Element createRef( UserSessionDownload session, Element parent, Xref xref, String tagName );

    //////////////////////////////
    // Public Methods

    /**
     * Build a primaryRef out of an IntAct Xref.
     * <p/>
     * <pre>
     * <b>Rules</b>:
     *    Xref.id                      --->   id
     *    Xref.secondaryId             --->   secondary
     *    Xref.CvDatabase.shortlabel   --->   db
     *    Xref.dbRelease               --->   version
     * </pre>
     *
     * @param session
     * @param parent  the parent Element to which we will attach the primaryRef
     * @param xref
     *
     * @return
     */
    public Element createPrimaryRef( UserSessionDownload session, Element parent, Xref xref ) {

        return createRef( session, parent, xref, PRIMARY_REF );
    }

    /**
     * Build a secondaryRref out of an IntAct Xref.
     * <p/>
     * <pre>
     * <b>Rules</b>:
     *    Xref.id                      --->   id
     *    Xref.secondaryId             --->   secondary
     *    Xref.CvDatabase.shortlabel   --->   db
     *    Xref.dbRelease               --->   version
     * </pre>
     *
     * @param session
     * @param parent  the parent Element to which we will attach the secondaryRef.
     * @param xref    The Xref out of which we extract the data to build the secondaryXref.
     *
     * @return the newly created secondaryId.
     */
    public Element createSecondaryRef( UserSessionDownload session, Element parent, Xref xref ) {

        return createRef( session, parent, xref, SECONDARY_REF );
    }
}
