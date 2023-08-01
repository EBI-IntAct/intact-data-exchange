package uk.ac.ebi.intact.dataexchange.psimi.xml;

import psidev.psi.mi.jami.factory.options.InteractionWriterOptions;
import psidev.psi.mi.jami.model.ComplexType;
import psidev.psi.mi.jami.model.InteractionCategory;
import psidev.psi.mi.jami.xml.PsiXmlType;
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import psidev.psi.mi.jami.xml.io.writer.DefaultXmlWriter;
import psidev.psi.mi.jami.xml.model.extension.factory.options.PsiXmlWriterOptions;

import java.util.Map;

/**
 * Generic writer for PSI-MI XML
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/05/14</pre>
 */

public class DefaultIntactXmlWriter extends DefaultXmlWriter {

    public void initialiseContext(Map options) {
        IntactPsiXmlWriterFactory factory = IntactPsiXmlWriterFactory.getInstance();
        InteractionCategory category = InteractionCategory.mixed;
        ComplexType type = ComplexType.n_ary;
        PsiXmlVersion version = PsiXmlVersion.v2_5_4;
        PsiXmlType xmlType = PsiXmlType.expanded;

        boolean extended = false;
        boolean named = true;

        if (options == null || !options.containsKey(PsiXmlWriterOptions.OUTPUT_OPTION_KEY)){
            throw new IllegalStateException("The PSI-MI XML interaction writer has not been initialised. The options for the interaction writer " +
                    "should contain at least "+ InteractionWriterOptions.OUTPUT_OPTION_KEY + " to know where to write the interactions.");
        }

        if (options.containsKey(PsiXmlWriterOptions.INTERACTION_CATEGORY_OPTION_KEY)){
            Object value = options.get(PsiXmlWriterOptions.INTERACTION_CATEGORY_OPTION_KEY);
            if (value instanceof InteractionCategory){
                category = (InteractionCategory)value;
            }
        }
        if (options.containsKey(PsiXmlWriterOptions.COMPLEX_TYPE_OPTION_KEY)){
            Object value = options.get(PsiXmlWriterOptions.COMPLEX_TYPE_OPTION_KEY);
            if (value instanceof ComplexType){
                type = (ComplexType)value;
            }
        }
        if (options.containsKey(PsiXmlWriterOptions.XML_VERSION_OPTION)){
            Object value = options.get(PsiXmlWriterOptions.XML_VERSION_OPTION);
            if (value instanceof PsiXmlVersion){
                version = (PsiXmlVersion)value;
            }
        }
        if (options.containsKey(PsiXmlWriterOptions.XML_EXTENDED_OPTION)){
            Object value = options.get(PsiXmlWriterOptions.XML_EXTENDED_OPTION);
            if (value != null){
                extended = (Boolean)value;
            }
        }
        if (options.containsKey(PsiXmlWriterOptions.XML_NAMES_OPTION)){
            Object value = options.get(PsiXmlWriterOptions.XML_NAMES_OPTION);
            if (value != null){
                named = (Boolean)value;
            }
        }
        if (options.containsKey(PsiXmlWriterOptions.XML_TYPE_OPTION)){
            Object value = options.get(PsiXmlWriterOptions.XML_TYPE_OPTION);
            if (value instanceof PsiXmlType){
                xmlType = (PsiXmlType)value;
            }
        }

        setDelegate(factory.createPsiXmlWriter(category, version, type, xmlType, extended, named));
        getDelegate().initialiseContext(options);
    }
}
