/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.Constants;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.TissueTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.XrefTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.Message;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;
import uk.ac.ebi.intact.model.CvDatabase;

/**
 * That class .
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class TissueParser {

    /**
     * Process a <tissue> in an organism. <br> In order to map it to the IntAct data, we look for the psi-mi
     * <code>primaryRef</code>, and look-up in intact using its <code>id</code>.
     *
     * @param element &lt;tissue&gt;.
     *
     * @return a <code>TissueTag</code> if the XML contains a walid psi-mi descriptor.
     *
     * @see uk.ac.ebi.intact.application.dataConversion.psiUpload.model.TissueTag
     */
    public static TissueTag process( final Element element ) {

        if ( false == "tissue".equals( element.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in tissue tag." ) );
        }

        // CAUTION - MAY NOT BE THERE
        final Element names = DOMUtil.getFirstElement( element, "names" );
        String shortLabel = null;
        if ( names != null ) {
            shortLabel = DOMUtil.getShortLabel( names );
        }

        final Element cellTypeXref = DOMUtil.getFirstElement( element, "xref" );

        // Look at either primaryRef and secondaryRef
        XrefTag xref = null;
        if ( cellTypeXref != null ) {
            xref = XrefParser.getXrefByDb( cellTypeXref, CvDatabase.PSI_MI );
        }

        TissueTag tissue = null;
        try {
            tissue = new TissueTag( xref, shortLabel );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, e.getMessage() ) );
        }

        return tissue;
    }
}