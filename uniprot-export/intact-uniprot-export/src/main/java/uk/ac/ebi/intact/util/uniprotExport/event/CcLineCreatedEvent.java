/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.util.uniprotExport.CcLine;

import java.util.List;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30-Aug-2006</pre>
 */
public class CcLineCreatedEvent extends CcLineEvent
{

    private static final Log log = LogFactory.getLog(CcLineCreatedEvent.class);

    private String uniprotId;
    private List<CcLine> ccLines;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public CcLineCreatedEvent(Object source, String uniprotId, List<CcLine> ccLines)
    {
        super(source);
        this.uniprotId = uniprotId;
        this.ccLines = ccLines;
    }


    public String getUniprotId()
    {
        return uniprotId;
    }

    public List<CcLine> getCcLines()
    {
        return ccLines;
    }
}
