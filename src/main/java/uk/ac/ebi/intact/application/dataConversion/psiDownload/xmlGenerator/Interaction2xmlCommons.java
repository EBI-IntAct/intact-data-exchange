// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import uk.ac.ebi.intact.application.dataConversion.PsiConstants;
import uk.ac.ebi.intact.application.dataConversion.psiDownload.UserSessionDownload;
import uk.ac.ebi.intact.model.*;

import java.util.Collection;
import java.util.Iterator;

/**
 * Behaviour shared across Interaction's PSI implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class Interaction2xmlCommons {

    //////////////////////////
    // Singleton's methods

    private static Interaction2xmlCommons ourInstance = new Interaction2xmlCommons();

    public static Interaction2xmlCommons getInstance() {
        return ourInstance;
    }

    private Interaction2xmlCommons() {
    }

    ///////////////////////////
    // Package Methods

    /**
     * Generate the xref tag of the given protein. That content is attached to the given parent Element. <br>
     * <pre>
     *   Rules:
     *   -----
     *           primaryRef:   is the AC of the interaction object
     *           secondaryRef: any other Xrefs
     * </pre>
     *
     * @param session
     * @param parent      the interaction Element to which we will attach the Xref Element and its content.
     * @param interaction the IntAct Interaction from which we get the Xref that will be used to generate the PSI XML.
     *
     * @return the xref tag and its attached content.
     */
    public Element createInteractionXrefs( UserSessionDownload session, Element parent, Interaction interaction ) {

        Element xrefElement = session.createElement( "xref" );

        // 1. Create the primaryRef
        Element primaryRefElement = Xref2xmlFactory.getInstance( session ).createIntactReference( session, xrefElement, interaction );

        // 2. create the secondaryRef (if any)
        for ( Iterator iterator = interaction.getXrefs().iterator(); iterator.hasNext(); ) {
            Xref xref = (Xref) iterator.next();

            if ( primaryRefElement == null ) {
                primaryRefElement = Xref2xmlFactory.getInstance( session ).createPrimaryRef( session, xrefElement, xref );
            } else {
                // create xref
                Xref2xmlFactory.getInstance( session ).createSecondaryRef( session, xrefElement, xref );
            }
        }

        if ( xrefElement.getChildNodes().getLength() > 0 ) {

            // we only display the xref tag if there is a content.
            // if the interaction doesn't have an AC and no Xref, that could happen.
            parent.appendChild( xrefElement );

        } else {

            xrefElement = null;
        }

        return xrefElement;
    }

    /**
     * Generated a dissociation constant in the attributeList only if there is one specified in the Interaction.
     *
     * @param session     the user session.
     * @param parent      the parent to which we attach the attributeList, and then the attribute.
     * @param interaction the interaction from which we will get the dissociation constant.
     *
     * @return the created attribute. May be null.
     *
     * @see uk.ac.ebi.intact.model.Interaction
     */
    public Element createDissociationConstant( UserSessionDownload session, Element parent, Interaction interaction ) {

        // TODO test it

        Element attributeElement = null;

        if ( null != interaction.getKD() ) {

            Element attributeListElement = null;

            // search for an existing attributeList
            NodeList lists = parent.getElementsByTagName( Annotation2xml.ATTRIBUTE_LIST_NODE_NAME );

            if ( lists != null && lists.getLength() == 1 ) {
                attributeListElement = (Element) lists.item( 0 );
            }

            if ( attributeListElement == null ) {
                // create it
                attributeListElement = session.createElement( Annotation2xml.ATTRIBUTE_LIST_NODE_NAME );

                // append the new attributeList to the interaction.
                // this is fine as long as the attributeList and the dissociation constant are created in sequence.
                parent.appendChild( attributeListElement );
            }

            // attach a new annotation as association constant
            attributeElement = session.createElement( Annotation2xml.ATTRIBUTE_NODE_NAME );
            attributeElement.setAttribute( Annotation2xml.NAME, PsiConstants.KD_ATTRIBUTE_NAME );
            Text attributeText = session.createTextNode( interaction.getKD().toString() );
            attributeElement.appendChild( attributeText );

            // append the attribute to the list.
            attributeListElement.appendChild( attributeElement );
        }

        return attributeElement;
    }

    /**
     * Generated a negative flag if there is one specified annotation in the Interaction or one of its Experiment.
     *
     * @param session     the user session.
     * @param parent      the parent to which we attach the attributeList, and then the attribute.
     * @param interaction the interaction from which we will get the dissociation constant.
     *
     * @return the created flag. May be null.
     *
     * @see uk.ac.ebi.intact.model.Annotation
     * @see uk.ac.ebi.intact.model.Interaction
     * @see uk.ac.ebi.intact.model.Experiment
     */
    public Element createNegativeFlag( UserSessionDownload session, Element parent, Interaction interaction ) {

        // TODO test it

        Element element = null;

        if ( isNegative( interaction ) ) {
            element = session.createElement( "negative" );
            Text negativeText = session.createTextNode( "true" );

            // add the text to the node
            element.appendChild( negativeText );

            // add the node to the parent
            parent.appendChild( element );
        }

        return element;
    }

    /**
     * Answers the question: is that AnnotatedObject (Interaction, Experiment) annotated as negative ?
     *
     * @param interaction the interaction we want to introspect
     *
     * @return true if the object is annotated with the 'negative' CvTopic, otherwise false.
     */
    public boolean isNegative( Interaction interaction ) {

        // TODO test it

        boolean isNegative = false;

        Collection annotations = interaction.getAnnotations();
        for ( Iterator iterator = annotations.iterator(); iterator.hasNext() && false == isNegative; ) {
            Annotation annotation = (Annotation) iterator.next();
            final CvTopic negative = annotation.getCvTopic();

            if ( negative != null && CvTopic.NEGATIVE.equals( negative.getShortLabel() ) ) {
                isNegative = true;
            }
        }

        if ( !isNegative ) {
            // check its experiments
            Collection experiments = interaction.getExperiments();

            for ( Iterator iterator = experiments.iterator(); iterator.hasNext(); ) {
                Experiment experiment = (Experiment) iterator.next();

                // is that experiment negative ?
                annotations = experiment.getAnnotations();
                for ( Iterator iterator2 = annotations.iterator(); iterator2.hasNext() && false == isNegative; ) {
                    Annotation annotation = (Annotation) iterator2.next();
                    final CvTopic negative = annotation.getCvTopic();

                    if ( negative != null && CvTopic.NEGATIVE.equals( negative.getShortLabel() ) ) {
                        isNegative = true;
                    }
                }
            }
        }

        return isNegative;
    }
}