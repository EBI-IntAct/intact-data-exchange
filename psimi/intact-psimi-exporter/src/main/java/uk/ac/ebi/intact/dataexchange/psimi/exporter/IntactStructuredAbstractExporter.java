package uk.ac.ebi.intact.dataexchange.psimi.exporter;

import psidev.psi.mi.jami.html.MIHtml;
import psidev.psi.mi.jami.model.Interaction;

/**
 * IntAct JAMI db importer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactStructuredAbstractExporter<I extends Interaction> extends AbstractIntactDbInteractionExporter<I> {

    @Override
    protected void registerWriters() {
        // register default MI html writers
        IntactStructuredAbstract.initialiseAllStructuredAbstractWriters();
    }
}
