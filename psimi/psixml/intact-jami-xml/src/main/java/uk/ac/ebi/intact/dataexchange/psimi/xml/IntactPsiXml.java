package uk.ac.ebi.intact.dataexchange.psimi.xml;

import psidev.psi.mi.jami.commons.MIFileType;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.ComplexType;
import psidev.psi.mi.jami.model.InteractionCategory;
import psidev.psi.mi.jami.xml.PsiXmlType;
import psidev.psi.mi.jami.xml.model.extension.factory.options.PsiXmlWriterOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a central access to basic methods in psi-jami.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>03/07/13</pre>
 */

public class IntactPsiXml {

    public static void initialiseAllIntactXmlWriters() {
        InteractionWriterFactory writerFactory = InteractionWriterFactory.getInstance();

        Map<String, Object> supportedOptions4 = createXMLInteractionWriterOptions(null, null, null, null, null);
        writerFactory.registerDataSourceWriter(DefaultIntactXmlWriter.class, supportedOptions4);
    }

    private static Map<String, Object> createXMLInteractionWriterOptions(InteractionCategory interactionCategory, ComplexType complexType,
                                                                         PsiXmlType type,
                                                                         Boolean extended, Boolean writeNames) {
        Map<String, Object> supportedOptions4 = new HashMap<String, Object>(14);
        supportedOptions4.put(InteractionWriterOptions.OUTPUT_FORMAT_OPTION_KEY, MIFileType.psimi_xml.toString());
        supportedOptions4.put(InteractionWriterOptions.INTERACTION_CATEGORY_OPTION_KEY, interactionCategory);
        supportedOptions4.put(InteractionWriterOptions.COMPLEX_TYPE_OPTION_KEY, complexType);
        supportedOptions4.put(PsiXmlWriterOptions.XML_TYPE_OPTION, type);
        supportedOptions4.put(PsiXmlWriterOptions.XML_EXTENDED_OPTION, extended);
        supportedOptions4.put(PsiXmlWriterOptions.XML_NAMES_OPTION, writeNames);
        supportedOptions4.put(InteractionWriterOptions.OUTPUT_OPTION_KEY, null);
        supportedOptions4.put(PsiXmlWriterOptions.COMPACT_XML_EXPERIMENT_SET_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.COMPACT_XML_INTERACTOR_SET_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.COMPACT_XML_AVAILABILITY_SET_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.ELEMENT_WITH_ID_CACHE_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.XML_INTERACTION_SET_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.DEFAULT_RELEASE_DATE_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.DEFAULT_SOURCE_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.XML_ENTRY_ATTRIBUTES_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.WRITE_COMPLEX_AS_INTERACTOR_OPTION, null);
        supportedOptions4.put(PsiXmlWriterOptions.XML_VERSION_OPTION, null);
        return supportedOptions4;
    }
}
