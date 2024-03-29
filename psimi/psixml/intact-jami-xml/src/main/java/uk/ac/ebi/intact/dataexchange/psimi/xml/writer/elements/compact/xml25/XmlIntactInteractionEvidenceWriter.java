package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Checksum;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.compact.xml25.XmlInteractionEvidenceWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Compact XML 2.5 writer for an interaction evidence (with full experimental details).
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/11/13</pre>
 */

public class XmlIntactInteractionEvidenceWriter extends XmlInteractionEvidenceWriter {
    public XmlIntactInteractionEvidenceWriter(PsiXmlVersion version, XMLStreamWriter writer, PsiXmlObjectCache objectIndex) {
        super(version, writer, objectIndex);
    }

    @Override
    protected void writeAttributes(InteractionEvidence object) throws XMLStreamException {
        Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(object.getAnnotations(),
                null, "no-export");
        Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(object.getAnnotations());
        exportAnnotations.removeAll(noExportAnnotations);
        // write attributes
        if (!exportAnnotations.isEmpty()){
            // write start attribute list
            getStreamWriter().writeStartElement("attributeList");
            for (Object ann : object.getAnnotations()){
                getAttributeWriter().write((Annotation)ann);
            }
            for (Object c : object.getChecksums()){
                getChecksumWriter().write((Checksum)c);
            }
            // write end attributeList
            getStreamWriter().writeEndElement();
        }
        // write checksum
        else if (!object.getChecksums().isEmpty()){
            // write start attribute list
            getStreamWriter().writeStartElement("attributeList");
            for (Object c : object.getChecksums()){
                getChecksumWriter().write((Checksum)c);
            }
            // write end attributeList
            getStreamWriter().writeEndElement();
        }
    }
}
