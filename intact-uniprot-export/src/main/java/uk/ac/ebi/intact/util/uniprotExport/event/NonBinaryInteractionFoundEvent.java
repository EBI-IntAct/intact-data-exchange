/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.Interaction;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30-Aug-2006</pre>
 */
public class NonBinaryInteractionFoundEvent extends CcLineEvent
{

    private static final Log log = LogFactory.getLog(NonBinaryInteractionFoundEvent.class);

    private Interaction interaction;

    /**
     * Constructs a non binary interaction Event.
     *
     * @param source The object on which the Event initially occurred.
     * @param interaction the non binary interaction
     * @throws IllegalArgumentException if source is null.
     *
     */
    public NonBinaryInteractionFoundEvent(Object source, Interaction interaction)
    {
        super(source);
        this.interaction = interaction;
    }


    public Interaction getInteraction()
    {
        return interaction;
    }
}
