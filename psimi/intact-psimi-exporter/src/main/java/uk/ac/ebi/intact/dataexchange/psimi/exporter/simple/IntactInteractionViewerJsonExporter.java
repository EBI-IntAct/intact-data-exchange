package uk.ac.ebi.intact.dataexchange.psimi.exporter.simple;

import psidev.psi.mi.jami.json.InteractionViewerJson;
import psidev.psi.mi.jami.model.Interaction;

/**
 * IntAct JAMI json exporter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactInteractionViewerJsonExporter<I extends Interaction> extends AbstractIntactInteractionExporter<I> {

    @Override
    protected void registerWriters() {
        // register default MI json writers
        InteractionViewerJson.initialiseAllMIJsonWriters();
    }
}
