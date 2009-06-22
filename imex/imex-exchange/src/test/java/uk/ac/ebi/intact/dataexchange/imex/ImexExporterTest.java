package uk.ac.ebi.intact.dataexchange.imex;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Publication;
import uk.ac.ebi.intact.model.Experiment;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.PsimiXmlWriter;

/**
 * ImexExporter Tester.
 *
 * @author Samuel Kerrien (skerrien@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.0
 */
public class ImexExporterTest extends IntactBasicTestCase {

    @Autowired
    private ImexExporter exporter;

    @Test
    public void exportPublication() throws Exception {
        final String pmid = "12345678";
        final Publication publication = getMockBuilder().createPublication( pmid );
        publication.getXrefs().clear();
        publication.addXref( getMockBuilder().createPrimaryReferenceXref( publication, pmid ) );

        final Experiment experiment = getMockBuilder().createExperimentRandom( "toto-2009-1", 1 );
        experiment.getXrefs().clear();
        experiment.addXref( getMockBuilder().createPrimaryReferenceXref( experiment, pmid ) );
        experiment.setPublication( null );

        publication.addExperiment( experiment );

        final EntrySet entrySet = exporter.exportPublication( publication );

        System.out.println( new PsimiXmlWriter().getAsString( entrySet ) );
    }

    @Test
    public void exportPublications() {

    }
}
