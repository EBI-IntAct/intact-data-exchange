package uk.ac.ebi.intact.dataexchange.psimi.exporter.simple;

import psidev.psi.mi.jami.commons.PsiJami;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.model.Interaction;
import psidev.psi.mi.jami.xml.io.writer.DefaultXmlWriter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;

/**
 * IntAct JAMI collection exporter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactCollectionStandardFileExporter<I extends Interaction> extends AbstractIntactInteractionExporter<I> {


    @Override
    protected void registerWriters() {
        // register default MI writers
        PsiJami.initialiseAllInteractionWriters();

        // override writers for Intact xml
        InteractionWriterFactory.getInstance().removeDataSourceWriter(DefaultXmlWriter.class);
        IntactPsiXml.initialiseAllIntactXmlWriters();
    }
}
