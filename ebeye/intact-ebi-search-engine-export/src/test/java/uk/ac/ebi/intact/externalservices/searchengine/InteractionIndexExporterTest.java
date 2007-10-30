package uk.ac.ebi.intact.externalservices.searchengine;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.core.persister.standard.InteractionPersister;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.model.Interaction;

import java.io.StringWriter;
import java.io.Writer;

/**
 * InteractionIndexExporter Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>11/24/2006</pre>
 */
public class InteractionIndexExporterTest extends AbstractIndexExporterTestCase {

    @Test
    public void testBuildIndex() throws Exception {
        Interaction interaction = getMockBuilder().createInteractionRandomBinary();

        persistInteraction(interaction);

        Writer writer = new StringWriter();

        IndexExporter exporter = new InteractionIndexExporter( writer );
        exporter.buildIndex();

        System.out.println(writer.toString());

        int lineCount = writer.toString().split(System.getProperty("file.separator")).length;
        Assert.assertEquals(24, lineCount);
    }
}
