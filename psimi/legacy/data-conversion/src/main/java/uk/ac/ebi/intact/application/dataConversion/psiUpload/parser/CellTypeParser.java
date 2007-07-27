/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.CellTypeTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.Constants;
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
public class CellTypeParser {

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
    public static CellTypeTag process( final Element element ) {

        if ( false == "cellType".equals( element.getNodeName() ) ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, "ERROR - We should be in cellType tag." ) );
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
        if ( null != cellTypeXref ) {
            xref = XrefParser.getXrefByDb( cellTypeXref, CvDatabase.PSI_MI );
        }

        CellTypeTag cellType = null;
        try {
            cellType = new CellTypeTag( xref, shortLabel );
        } catch ( IllegalArgumentException e ) {
            MessageHolder.getInstance().addParserMessage( new Message( element, e.getMessage() ) );
        }

        return cellType;
    }
}