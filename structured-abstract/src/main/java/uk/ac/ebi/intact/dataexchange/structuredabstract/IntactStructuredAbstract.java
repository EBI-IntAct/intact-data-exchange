package uk.ac.ebi.intact.dataexchange.structuredabstract;

import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.InteractionCategory;
import uk.ac.ebi.intact.dataexchange.structuredabstract.writer.StructuredAbstractHtmlComplexWriter;
import uk.ac.ebi.intact.dataexchange.structuredabstract.writer.StructuredAbstractHtmlEvidenceWriter;
import uk.ac.ebi.intact.dataexchange.structuredabstract.writer.StructuredAbstractTextComplexWriter;
import uk.ac.ebi.intact.dataexchange.structuredabstract.writer.StructuredAbstractTextEvidenceWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a central access to basic methods to register structured abstract writer.
 *
 * Existing writers :
 * - StructuredAbstractHtmlEvidenceWriter : writer that can write interaction evidences. The option HtmlWriterOptions.StructuredAbstractWriterOptions
 * does not make sense in this case as it can write both n-ary and binary interactions the same way.
 * The option StructuredAbstractWriterOptions.COMPLEX_TYPE_OPTION_KEY will be ignored.
 * It will write abstract of type AbstractOutputType.ABSTRACT_HTML_OUTPUT
 * - StructuredAbstractHtmlComplexWriter : writer that can write biological complexes. The option StructuredAbstractWriterOptions.COMPLEX_TYPE_OPTION_KEY does not make sense in this case
 * as it can write both n-ary and binary interactions the same way. The option StructuredAbstractWriterOptions.COMPLEX_TYPE_OPTION_KEY will be ignored.
 * It will write abstract of type AbstractOutputType.ABSTRACT_HTML_OUTPUT
 * - StructuredAbstractTextEvidenceWriter :  writer that can write interaction evidences. The option HtmlWriterOptions.StructuredAbstractWriterOptions
 * does not make sense in this case as it can write both n-ary and binary interactions the same way.
 * It will write abstract of type AbstractOutputType.ABSTRACT_TEXT_OUTPUT
 * - StructuredAbstractTextComplexWriter : writer that can write biological complexes. The option StructuredAbstractWriterOptions.COMPLEX_TYPE_OPTION_KEY does not make sense in this case
 * as it can write both n-ary and binary interactions the same way. The option StructuredAbstractWriterOptions.COMPLEX_TYPE_OPTION_KEY will be ignored.
 * It will write abstract of type AbstractOutputType.ABSTRACT_TEXT_OUTPUT
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19/11/14</pre>
 */

public class IntactStructuredAbstract {

    /**
     * Register all existing MI html writers in the MI interaction writer factory
     */
    public static void initialiseAllStructuredAbstractWriters(){
        InteractionWriterFactory writerFactory = InteractionWriterFactory.getInstance();

        Map<String, Object> supportedOptions1 = createStructuredAbstractInteractionWriterOptions(InteractionCategory.evidence,
                AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        writerFactory.registerDataSourceWriter(StructuredAbstractHtmlEvidenceWriter.class, supportedOptions1);
        Map<String, Object> supportedOptions2 = createStructuredAbstractInteractionWriterOptions(InteractionCategory.complex,
                AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        writerFactory.registerDataSourceWriter(StructuredAbstractHtmlComplexWriter.class, supportedOptions2);
        Map<String, Object> supportedOptions3 = createStructuredAbstractInteractionWriterOptions(InteractionCategory.evidence,
                AbstractOutputType.ABSTRACT_TXT_OUTPUT);
        writerFactory.registerDataSourceWriter(StructuredAbstractTextEvidenceWriter.class, supportedOptions3);
        Map<String, Object> supportedOptions4 = createStructuredAbstractInteractionWriterOptions(InteractionCategory.complex,
                AbstractOutputType.ABSTRACT_TXT_OUTPUT);
        writerFactory.registerDataSourceWriter(StructuredAbstractTextComplexWriter.class, supportedOptions4);
    }

    private static Map<String, Object> createStructuredAbstractInteractionWriterOptions(InteractionCategory interactionCategory, AbstractOutputType type) {
        Map<String, Object> supportedOptions4 = new HashMap<String, Object>(9);
        supportedOptions4.put(StructuredAbstractWriterOptions.OUTPUT_FORMAT_OPTION_KEY, StructuredAbstractWriterOptions.STRUCTURED_ABSTRACT_FORMAT);
        supportedOptions4.put(StructuredAbstractWriterOptions.INTERACTION_CATEGORY_OPTION_KEY, interactionCategory != null ? interactionCategory :
                InteractionCategory.mixed);
        supportedOptions4.put(StructuredAbstractWriterOptions.STRUCTURED_ABSTRACT_TYPE, type != null ? type : AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        supportedOptions4.put(StructuredAbstractWriterOptions.COMPLEX_TYPE_OPTION_KEY, null);
        supportedOptions4.put(InteractionWriterOptions.OUTPUT_OPTION_KEY, null);
        return supportedOptions4;
    }
}
