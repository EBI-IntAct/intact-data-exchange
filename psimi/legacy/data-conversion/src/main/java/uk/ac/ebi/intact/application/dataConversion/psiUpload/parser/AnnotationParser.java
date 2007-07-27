/*
 * Copyright (c) 2002 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion.psiUpload.parser;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.AnnotationTag;
import uk.ac.ebi.intact.application.dataConversion.util.DOMUtil;

/**
 * That class reflects what is needed to create an IntAct <code>Annotation</code>.
 * <p/>
 * <pre>
 * <p/>
 *         &lt;attribute name="comment"&gt;CAV1 was expressed as GST fusion in E. coli.&lt;/attribute&gt;
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationParser {

    public static AnnotationTag process( final Element element ) {
        final String type = element.getAttribute( "name" );
        final String text = DOMUtil.getSimpleElementText( element );

        return new AnnotationTag( type, text );
    }
}
