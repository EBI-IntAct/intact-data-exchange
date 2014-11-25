package uk.ac.ebi.intact.dataexchange.psimi.exporter.simple;

import psidev.psi.mi.jami.model.Interaction;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.IntactPsiMitab;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;

/**
 * IntAct JAMI db importer which can export interactions loaded from the database in a single standard file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactStandardFileExporter<I extends Interaction> extends AbstractIntactInteractionExporter<I> {

    @Override
    protected void registerWriters() {
        // register default MI writers
        IntactPsiMitab.initialiseAllIntactMitabWriters();

        // override writers for Intact xml
        IntactPsiXml.initialiseAllIntactXmlWriters();
    }
}
