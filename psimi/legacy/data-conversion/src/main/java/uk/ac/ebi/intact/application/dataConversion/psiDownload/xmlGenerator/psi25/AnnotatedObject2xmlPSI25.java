// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.psi25;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.AbstractAnnotatedObject2xml;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator.util.ToolBox;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.CvAliasType;

import java.util.Iterator;

/**
 * Process the common behaviour of AnnotatedObject when exporting PSI version 2.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotatedObject2xmlPSI25 extends AbstractAnnotatedObject2xml {

    public static final String TYPE_ATTRIBUTE_NAME = "type";
    public static final String TYPE_AC_ATTRIBUTE_NAME = "typeAc";

    ////////////////////////////////////////
    // Implementation of abstract methods

    /**
     * Convert shortlabel and fullname of an AnnotatedObject into PSI XML. <br> The generated tags are attached to the
     * given parent.
     *
     * @param session pre-initialised user's session.
     * @param parent  the tag to which we attached the generated Data.
     * @param object  the AnnotatedObject from which we are converting the shortlabel and fullname.
     *
     * @return the generated data.
     */
    public Element createNames( UserSessionDownload session, Element parent, AnnotatedObject object ) {

        if ( session == null ) {
            throw new IllegalArgumentException( "You must give a non null UserSessionDownload." );
        }

        if ( object == null ) {
            throw new IllegalArgumentException( "You must give a non null Annotation." );
        }

        if ( parent == null ) {

            throw new IllegalArgumentException( "You must give a non null parent Element." );

        } else {

            ckeckParentName( parent );
        }

        // NOTE: children terms are:
        //       shortlabel [fullname] [alias]

        Element names = session.createElement( "names" );

        createShortlabel( session, names, object );

        createFullname( session, names, object );

        // alias is NOT mandatory
        if ( false == object.getAliases().isEmpty() ) {

            for ( Iterator iterator = object.getAliases().iterator(); iterator.hasNext(); ) {
                Alias alias = (Alias) iterator.next();

                Element aliasElement = session.createElement( "alias" );
                Text aliasText = session.createTextNode( alias.getName() );
                aliasElement.appendChild( aliasText );

                // set CvAliasType if any
                if ( alias.getCvAliasType() != null ) {
                    CvAliasType aliasType = alias.getCvAliasType();
                    String psiRef = ToolBox.getPsiReference( aliasType );
                    if ( psiRef != null ) {
                        aliasElement.setAttribute( TYPE_AC_ATTRIBUTE_NAME, psiRef );
                    }

                    aliasElement.setAttribute( TYPE_ATTRIBUTE_NAME, aliasType.getShortLabel() );
                }

                names.appendChild( aliasElement );
            }
        }

        // append the term to the given parent
        parent.appendChild( names );

        return names;
    }
}