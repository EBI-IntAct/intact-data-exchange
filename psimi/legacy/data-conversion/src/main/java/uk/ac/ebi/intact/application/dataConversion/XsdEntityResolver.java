/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.application.dataConversion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Entity resolver for XSD files to avoid connecting to the internet
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01-Aug-2006</pre>
 */
public class XsdEntityResolver implements EntityResolver
{
    private static final Log log = LogFactory.getLog(XsdEntityResolver.class);

    public InputSource resolveEntity(String publicId, String systemId)
    {

        log.debug("File to validate systemId="+systemId+" ; publicId="+publicId);

        InputSource inputSource = null;

        if (systemId != null)
        {
            if (systemId.equals("http://psidev.sourceforge.net/mi/rel25/src/MIF25.xsd"))
            {

                log.debug("Entity without minor build version, resolved using XsdEntityResolver - MIF 2.5.3");

                inputSource = new InputSource(XsdEntityResolver.class.getResourceAsStream("/META-INF/MIF253.xsd"));
            }
            else if (systemId.equals("http://psidev.sourceforge.net/mi/rel25/src/MIF250.xsd"))
            {
                log.debug("Entity resolved using XsdEntityResolver - MIF 2.5.0");

                inputSource = new InputSource(XsdEntityResolver.class.getResourceAsStream("/META-INF/MIF250.xsd"));
            }
            else if (systemId.equals("http://psidev.sourceforge.net/mi/rel25/src/MIF251.xsd"))
            {
                log.debug("Entity resolved using XsdEntityResolver - MIF 2.5.1");

                inputSource = new InputSource(XsdEntityResolver.class.getResourceAsStream("/META-INF/MIF251.xsd"));
            }
            else if (systemId.equals("http://psidev.sourceforge.net/mi/rel25/src/MIF252.xsd"))
            {
                log.debug("Entity resolved using XsdEntityResolver - MIF 2.5.2");

                inputSource = new InputSource(XsdEntityResolver.class.getResourceAsStream("/META-INF/MIF252.xsd"));
            }
            else if (systemId.equals("http://psidev.sourceforge.net/mi/rel25/src/MIF253.xsd"))
            {
                log.debug("Entity resolved using XsdEntityResolver - MIF 2.5.3");

                inputSource = new InputSource(XsdEntityResolver.class.getResourceAsStream("/META-INF/MIF253.xsd"));
            }
            else if (systemId.equals("http://psidev.sourceforge.net/mi/xml/src/MIF.xsd"))
            {
                log.debug("Entity resolved using XsdEntityResolver - MIF 1.0");

                inputSource = new InputSource(XsdEntityResolver.class.getResourceAsStream("/META-INF/MIF10.xsd"));
            }

        }


        if (inputSource != null)
        {
            inputSource.setPublicId(publicId);
            inputSource.setSystemId(systemId);
        }

        // If nothing found, null is returned, for normal processing
        return inputSource;

    }

}
