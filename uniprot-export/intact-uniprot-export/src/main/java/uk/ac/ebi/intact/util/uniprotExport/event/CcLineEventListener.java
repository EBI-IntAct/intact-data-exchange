/**
 * Copyright (c) 2002-2006 The European Bioinformatics Institute, and others.
 * All rights reserved. Please see the file LICENSE
 * in the root directory of this distribution.
 */
package uk.ac.ebi.intact.util.uniprotExport.event;

import java.util.EventListener;

/**
 * TODO comment this!
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since <pre>30-Aug-2006</pre>
 */
public interface CcLineEventListener extends EventListener
{

    void processNonBinaryInteraction(NonBinaryInteractionFoundEvent evt);

    void drLineProcessed(DrLineProcessedEvent evt);

    void ccLineCreated(CcLineCreatedEvent evt);

}
