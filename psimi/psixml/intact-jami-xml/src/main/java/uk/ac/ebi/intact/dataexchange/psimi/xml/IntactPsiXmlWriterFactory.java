package uk.ac.ebi.intact.dataexchange.psimi.xml;

import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.model.ComplexType;
import psidev.psi.mi.jami.model.InteractionCategory;
import psidev.psi.mi.jami.xml.PsiXmlType;
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import psidev.psi.mi.jami.xml.io.writer.compact.*;
import psidev.psi.mi.jami.xml.io.writer.expanded.*;
import psidev.psi.mi.jami.xml.model.extension.factory.PsiXmlWriterFactory;
import uk.ac.ebi.intact.dataexchange.psimi.xml.writer.compact.IntactCompactXmlComplexWriter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.writer.compact.IntactCompactXmlEvidenceWriter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.writer.expanded.IntactExpandedXmlComplexWriter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.writer.expanded.IntactExpandedXmlEvidenceWriter;

/**
 * Factory for creating a intact PSI-XML writer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>02/05/14</pre>
 */

public class IntactPsiXmlWriterFactory {

    private static final IntactPsiXmlWriterFactory instance = new IntactPsiXmlWriterFactory();

    private IntactPsiXmlWriterFactory(){
    }

    public static IntactPsiXmlWriterFactory getInstance() {
        return instance;
    }

    public InteractionWriter createPsiXmlWriter(InteractionCategory interactionCategory,
                                                PsiXmlVersion version,
                                                ComplexType complexType,
                                                PsiXmlType type, boolean extended, boolean named){
        PsiXmlWriterFactory writerFactory = PsiXmlWriterFactory.getInstance();

        switch (complexType){
            case binary:
                return writerFactory.createPsiXmlBinaryWriter(interactionCategory, version, type, extended, named);
            default:
                return createPsiXmlWriter(interactionCategory, type, version, extended, named);
        }
    }

    public InteractionWriter createPsiXmlWriter(InteractionCategory interactionCategory, PsiXmlType type,
                                                PsiXmlVersion version,
                                                boolean extended, boolean named){
        if (interactionCategory == null){
            interactionCategory = InteractionCategory.mixed;
        }

        PsiXmlWriterFactory writerFactory = PsiXmlWriterFactory.getInstance();

        if (extended){
            return writerFactory.createPsiXmlWriter(interactionCategory, type, version, extended, named);
        }
        else if (named){
            return writerFactory.createPsiXmlWriter(interactionCategory, type, version, extended, named);
        }
        else{
            switch (type){
                case compact:
                    switch (interactionCategory){
                        case evidence:
                            return new IntactCompactXmlEvidenceWriter();
                        case modelled:
                            return new CompactXmlModelledWriter();
                        case basic:
                            return new LightCompactXmlWriter();
                        case complex:
                            return new IntactCompactXmlComplexWriter();
                        case mixed:
                            return new IntactCompactXmlEvidenceWriter();
                        default:
                            throw new IllegalArgumentException("Cannot find a XML writer for interaction category: "+interactionCategory);
                    }
                default:
                    switch (interactionCategory){
                        case evidence:
                            return new IntactExpandedXmlEvidenceWriter();
                        case modelled:
                            return new ExpandedXmlModelledWriter();
                        case basic:
                            return new LightExpandedXmlWriter();
                        case complex:
                            return new IntactExpandedXmlComplexWriter();
                        case mixed:
                            return new IntactExpandedXmlEvidenceWriter();
                        default:
                            throw new IllegalArgumentException("Cannot find a XML writer for interaction category: "+interactionCategory);
                    }
            }
        }
    }
}
