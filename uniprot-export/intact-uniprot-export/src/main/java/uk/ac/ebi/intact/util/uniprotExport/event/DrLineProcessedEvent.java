/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30-Aug-2006</pre>
 */
public class DrLineProcessedEvent extends CcLineEvent
{

    private static final Log log = LogFactory.getLog(DrLineProcessedEvent.class);

    private String uniprotId;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public DrLineProcessedEvent(Object source, String uniprotId)
    {
        super(source);
    }


    public String getUniprotId()
    {
        return uniprotId;
    }
}
