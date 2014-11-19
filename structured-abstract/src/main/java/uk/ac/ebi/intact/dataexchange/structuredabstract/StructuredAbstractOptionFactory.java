package uk.ac.ebi.intact.dataexchange.structuredabstract;

import psidev.psi.mi.jami.model.InteractionCategory;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * The factory to populate the map of options for the InteractionWriterFactory for structured abstract writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/07/13</pre>
 */

public class StructuredAbstractOptionFactory {

    private static final StructuredAbstractOptionFactory instance = new StructuredAbstractOptionFactory();

    private StructuredAbstractOptionFactory(){

    }

    public static StructuredAbstractOptionFactory getInstance() {
        return instance;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param outputFile : the file where to write the interactions
     * @return the options for the HTML InteractionWriter
     */
    public Map<String, Object> getDefaultStructuredAbstractOptions(File outputFile){
        Map<String, Object> options = getStructuredAbstractOptions(outputFile, InteractionCategory.evidence, AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        return options;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param objectCategory : the interaction object type to write
     * @param outputFile : the file where to write the interactions
     * @return the options for the HTML InteractionWriter
     */
    public Map<String, Object> getStructuredAbstractOptions(InteractionCategory objectCategory, File outputFile){
        Map<String, Object> options = getStructuredAbstractOptions(outputFile, objectCategory, AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        return options;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param type : abstract type
     * @param outputFile : the file where to write the interactions
     * @return the options for a HTML interaction writer.
     */
    public Map<String, Object> getStructuredAbstractOptions(AbstractOutputType type, File outputFile){
        Map<String, Object> options = getStructuredAbstractOptions(outputFile, InteractionCategory.evidence, type);
        return options;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param output : the output
     * @return the options for the HTML InteractionWriter
     */
    public Map<String, Object> getDefaultStructuredAbstractOptions(OutputStream output){
        Map<String, Object> options = getStructuredAbstractOptions(output, InteractionCategory.evidence, AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        return options;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param objectCategory : the interaction object type to write
     * @param output : the output
     * @return the options for the HTML InteractionWriter
     */
    public Map<String, Object> getStructuredAbstractOptions(InteractionCategory objectCategory, OutputStream output){
        Map<String, Object> options = getStructuredAbstractOptions(output, objectCategory, AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        return options;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param type : : abstract type
     * @param output : the outputstream
     * @return the options for a HTML interaction writer.
     */
    public Map<String, Object> getStructuredAbstractOptions(AbstractOutputType type, OutputStream output){
        Map<String, Object> options = getStructuredAbstractOptions(output, InteractionCategory.evidence, type);
        return options;
    }

    /**
     * Create the options for a Html interaction writer.
     * @param writer : the writer
     * @return the options for the Html InteractionWriter
     */
    public Map<String, Object> getDefaultStructuredAbstractOptions(Writer writer){
        Map<String, Object> options = getStructuredAbstractOptions(writer, InteractionCategory.evidence, AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        return options;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param objectCategory : the interaction object type to write
     * @param writer : the writer
     * @return the options for the HTML InteractionWriter
     */
    public Map<String, Object> getStructuredAbstractOptions(InteractionCategory objectCategory, Writer writer){
        Map<String, Object> options = getStructuredAbstractOptions(writer, objectCategory, AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        return options;
    }

    /**
     * Create the options for a HTML interaction writer.
     * @param type : abstract type
     * @param writer : the writer
     * @return the options for a HTML interaction writer.
     */
    public Map<String, Object> getStructuredAbstractOptions(AbstractOutputType type, Writer writer){
        Map<String, Object> options = getStructuredAbstractOptions(writer, InteractionCategory.evidence, type);
        return options;
    }

    /**
     * Create the options for the HTML InteractionWriter.
     * @param output
     * @param objectCategory : the interaction object type to write
     * @param type : type of abstract
     * @return the options for the HTML InteractionWriter
     */
    public Map<String, Object> getStructuredAbstractOptions(Object output, InteractionCategory objectCategory, AbstractOutputType type){
        Map<String, Object> options = new HashMap<String, Object>(10);
        options.put(StructuredAbstractWriterOptions.OUTPUT_OPTION_KEY, output);
        options.put(StructuredAbstractWriterOptions.OUTPUT_FORMAT_OPTION_KEY, StructuredAbstractWriterOptions.STRUCTURED_ABSTRACT_FORMAT);
        options.put(StructuredAbstractWriterOptions.INTERACTION_CATEGORY_OPTION_KEY, objectCategory != null ? objectCategory : InteractionCategory.evidence);
        options.put(StructuredAbstractWriterOptions.STRUCTURED_ABSTRACT_TYPE, type != null ? type : AbstractOutputType.ABSTRACT_HTML_OUTPUT);
        return options;
    }
}
