// Copyright (c) 2002-2003 The European Bioinformatics Institute, and others.
// All rights reserved. Please see the file LICENSE
// in the root directory of this distribution.

package uk.ac.ebi.intact.application.dataConversion.psiDownload.xmlGenerator;

import org.w3c.dom.Element;
import uk.ac.ebi.intact.model.BioSource;

import java.util.Map;

/**
 * Behaviour shared across BioSource's PSI implementation.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 */
public class BioSource2xmlCommons {

    //////////////////////////////////////
    // Singleton's attribute and methods

    private static BioSource2xmlCommons ourInstance = new BioSource2xmlCommons();

    public static BioSource2xmlCommons getInstance() {
        return ourInstance;
    }

    private BioSource2xmlCommons() {
    }

    ///////////////////////
    // Cache management

    /**
     * Checks if the given BioSource has already been generated as XML content. <br> If so, that content is cloned which
     * is faster than recreating it.
     *
     * @param cache     the cache ( BioSource -> DOM Element)
     * @param bioSource the BioSource we want to check.
     *
     * @return the XML representation (as the root of a DOM tree) of the given BioSource, or null if it hasn't been
     *         generated  yet.
     */
    public Element getXmlFromCache( Map cache, BioSource bioSource ) {

        Element element = (Element) cache.get( bioSource );

        if ( element != null ) {
            // if that element has already been generated, we clone it.
            element = (Element) element.cloneNode( true );
        }

        return element;
    }

    /**
     * Store in the cache the XML representation related to the given BioSource instance.
     *
     * @param cache     the cache ( BioSource -> DOM Element)
     * @param bioSource the BioSource we wanted to comvert to XML.
     * @param element   The DOM root (as an Element) of the XML representation of the given BioSource.
     */
    public void updateCache( Map cache, BioSource bioSource, Element element ) {

        cache.put( bioSource, element );
    }
}