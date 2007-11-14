/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.Constants;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionTypeTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;
import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class converts the XML DOM to an object.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class InteractionTypeParser {

    /**
     * Process a &lt;interactionType&gt;. <br> In order to map it to the IntAct data, we look for the psi-mi
     * <code>primaryRef</code>, and look-up in intact using its <code>id</code>.
     *
     * @param element &lt;interactionType&gt;.
     *
     * @return a <code>InteractionTypeTag</code> if the XML contains a walid psi-mi descriptor.
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.InteractionTypeTag
     */
    public static InteractionTypeTag process( final Element element ) {

        if ( false == "interactionType".equals( element.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in interactionType tag." ) );
        }

        final Element interactionTypeXref = DOMUtil.getFirstElement( element, "xref" );

        // Look at either primaryRef and secondaryRef
        final XrefTag xref = XrefParser.getXrefByDb( interactionTypeXref, CvDatabase.PSI_MI );

        InteractionTypeTag interactionType = null;
        try {
            interactionType = new InteractionTypeTag( xref );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, e.getMessage() ) );
        }

        return interactionType;
    }
}