// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.ConfidenceTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;

/**
 * TODO document this ;o)
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class ConfidenceParser {

    /**
     * Process a &lt;cellType&gt; in an organism. <br> In order to map it to the IntAct data, we look for the psi-mi
     * <code>primaryRef</code>, and look-up in intact using its <code>id</code>.
     *
     * @param element &lt;cellType&gt;.
     *
     * @return a <code>CellTypeTag</code> if the XML contains a walid psi-mi descriptor.
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.CellTypeTag
     */
    public static ConfidenceTag process( final Element element ) {

        if ( false == "confidence".equals( element.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in confidence tag." ) );
        }

        String unit = element.getAttribute( "unit" );
        String value = element.getAttribute( "value" );

        ConfidenceTag confidenceTag = null;
        try {
            confidenceTag = new ConfidenceTag( unit, value );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, e.getMessage() ) );
        }

        return confidenceTag;
    }
}