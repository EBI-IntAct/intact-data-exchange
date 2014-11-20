package uk.ac.ebi.intact.dataexchange.psimi.exporter.simple;

import psidev.psi.mi.jami.model.Interaction;
import uk.ac.ebi.intact.dataexchange.structuredabstract.IntactStructuredAbstract;

/**
 * IntAct JAMI structured abstract
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactStructuredAbstractExporter<I extends Interaction> extends AbstractIntactInteractionExporter<I> {

    @Override
    protected void registerWriters() {
        // register default MI html writers
        IntactStructuredAbstract.initialiseAllStructuredAbstractWriters();
    }
}
