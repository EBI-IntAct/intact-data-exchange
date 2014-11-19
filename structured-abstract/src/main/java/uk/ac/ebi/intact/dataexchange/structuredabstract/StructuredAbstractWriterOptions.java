package uk.ac.ebi.intact.dataexchange.structuredabstract;

import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;

/**
 * Class that lists all possible options for structured abstract writers.
 * All options listed in InteractionWriterOptions are also valid for structured abstract writers.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>06/12/13</pre>
 */

public class StructuredAbstractWriterOptions extends InteractionWriterOptions {

    /**
     * The abstract type. It should be an enum of type AbstractOutputType. If it is not provided, it will be AbstractOutputType.ABSTRACT_HTML_OUTPUT
     */
    public static final String STRUCTURED_ABSTRACT_TYPE = "structured_abstract_type";

    /**
     * The structured abstract format value to use with StructuredAbstractWriterOptions.OUTPUT_FORMAT_OPTION_KEY
     */
    public static final String STRUCTURED_ABSTRACT_FORMAT = "structured_abstract";
}
