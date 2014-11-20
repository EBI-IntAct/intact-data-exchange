package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.FeatureEvidence;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.xml25.XmlFeatureEvidenceWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * XML 2.5 writer for a feature evidence (with feature detection method)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13/11/13</pre>
 */

public class XmlIntactFeatureEvidenceWriter extends XmlFeatureEvidenceWriter {

    public XmlIntactFeatureEvidenceWriter(XMLStreamWriter writer, PsiXmlObjectCache objectIndex) {
        super(writer, objectIndex);
    }

    @Override
    protected void writeAttributes(FeatureEvidence object) throws XMLStreamException {
        Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(object.getAnnotations(),
                null, "no-export");
        Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(object.getAnnotations());
        exportAnnotations.removeAll(noExportAnnotations);
        // write attributes
        if (!exportAnnotations.isEmpty()){
            // write start attribute list
            getStreamWriter().writeStartElement("attributeList");
            for (Object ann : exportAnnotations){
                getAttributeWriter().write((Annotation)ann);
            }
            // write interaction dependency
            writeOtherAttributes(object, false);

            // write end attributeList
            getStreamWriter().writeEndElement();
        }
        // write interaction dependency
        else{
            // write role and participant ref attribute if not null
            writeOtherAttributes(object, true);
        }
    }
}
