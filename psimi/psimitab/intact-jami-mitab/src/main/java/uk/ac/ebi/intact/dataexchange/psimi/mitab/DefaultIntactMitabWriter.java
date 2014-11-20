package uk.ac.ebi.intact.dataexchange.psimi.mitab;

import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.ComplexType;
import psidev.psi.mi.jami.model.InteractionCategory;
import psidev.psi.mi.jami.tab.MitabVersion;
import psidev.psi.mi.jami.tab.extension.factory.MitabWriterFactory;
import psidev.psi.mi.jami.tab.extension.factory.options.MitabDataSourceOptions;
import psidev.psi.mi.jami.tab.extension.factory.options.MitabWriterOptions;
import psidev.psi.mi.jami.tab.io.writer.DefaultMitabWriter;

import java.util.Map;

/**
 * Generic writer for intact MITAB
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/05/14</pre>
 */

public class DefaultIntactMitabWriter extends DefaultMitabWriter {

    public void initialiseContext(Map options) {
        IntactMitabWriterFactory factory = IntactMitabWriterFactory.getInstance();
        InteractionCategory category = InteractionCategory.mixed;
        ComplexType type = ComplexType.n_ary;
        MitabVersion version = MitabVersion.v2_7;
        boolean extended = false;

        if (options == null || !options.containsKey(MitabWriterOptions.OUTPUT_OPTION_KEY)){
            throw new IllegalStateException("The Mitab interaction writer has not been initialised. The options for the Mitab interaction writer " +
                    "should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write the interactions.");
        }

        if (options.containsKey(MitabWriterOptions.INTERACTION_CATEGORY_OPTION_KEY)){
            Object value = options.get(MitabDataSourceOptions.INTERACTION_CATEGORY_OPTION_KEY);
            if (value instanceof InteractionCategory){
                category = (InteractionCategory)value;
            }
        }
        if (options.containsKey(MitabWriterOptions.COMPLEX_TYPE_OPTION_KEY)){
            Object value = options.get(MitabDataSourceOptions.COMPLEX_TYPE_OPTION_KEY);
            if (value instanceof ComplexType){
                type = (ComplexType)value;
            }
        }
        if (options.containsKey(MitabWriterOptions.MITAB_VERSION_OPTION)){
            Object value = options.get(MitabWriterOptions.MITAB_VERSION_OPTION);
            if (value instanceof MitabVersion){
                version = (MitabVersion)value;
            }
        }
        if (options.containsKey(MitabWriterOptions.MITAB_EXTENDED_OPTION)){
            Object value = options.get(MitabWriterOptions.MITAB_EXTENDED_OPTION);
            if (value != null){
                extended = (Boolean)value;
            }
        }

        setDelegate(factory.createMitabWriter(category, type, version, extended));
        getDelegate().initialiseContext(options);
    }
}
