package uk.ac.ebi.intact.dataexchange.psimi.mitab;

import psidev.psi.mi.jami.commons.MIFileType;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.ComplexType;
import psidev.psi.mi.jami.model.InteractionCategory;
import psidev.psi.mi.jami.tab.MitabVersion;
import psidev.psi.mi.jami.tab.extension.factory.options.MitabWriterOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a central access to basic methods in psi-jami.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/07/13</pre>
 */

public class IntactPsiMitab {

    public static void initialiseAllIntactMitabWriters() {
        InteractionWriterFactory writerFactory = InteractionWriterFactory.getInstance();

        Map<String, Object> supportedOptions28 = createMITABInteractionWriterOptions(null, null, null, null);
        writerFactory.registerDataSourceWriter(DefaultIntactMitabWriter.class, supportedOptions28);
    }

    private static Map<String, Object> createMITABInteractionWriterOptions(InteractionCategory interactionCategory, ComplexType complexType,
                                                                           MitabVersion version, Boolean extended) {
        Map<String, Object> supportedOptions4 = new HashMap<String, Object>(9);
        supportedOptions4.put(InteractionWriterOptions.OUTPUT_FORMAT_OPTION_KEY, MIFileType.mitab.toString());
        supportedOptions4.put(InteractionWriterOptions.INTERACTION_CATEGORY_OPTION_KEY, interactionCategory);
        supportedOptions4.put(InteractionWriterOptions.COMPLEX_TYPE_OPTION_KEY, complexType);
        supportedOptions4.put(InteractionWriterOptions.COMPLEX_EXPANSION_OPTION_KEY, null);
        supportedOptions4.put(MitabWriterOptions.MITAB_HEADER_OPTION, null);
        supportedOptions4.put(MitabWriterOptions.MITAB_VERSION_OPTION, version);
        supportedOptions4.put(InteractionWriterOptions.OUTPUT_OPTION_KEY, null);
        supportedOptions4.put(MitabWriterOptions.MITAB_EXTENDED_OPTION, extended);
        return supportedOptions4;
    }
}
