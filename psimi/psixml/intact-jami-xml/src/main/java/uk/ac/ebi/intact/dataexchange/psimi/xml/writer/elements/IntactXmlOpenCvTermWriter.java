package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.XmlOpenCvTermWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Xml writer for open cv terms
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>12/11/13</pre>
 */

public class IntactXmlOpenCvTermWriter extends XmlOpenCvTermWriter {

    public IntactXmlOpenCvTermWriter(XMLStreamWriter writer) {
        super(writer);
    }

    @Override
    protected void writeOtherProperties(CvTerm object) throws XMLStreamException {
        // write attributes
        if (!object.getAnnotations().isEmpty()){
            Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(object.getAnnotations(),
                    null,"no-export");
            Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(object.getAnnotations());
            exportAnnotations.removeAll(noExportAnnotations);

            if (!exportAnnotations.isEmpty()){
                // write start attribute list
                getStreamWriter().writeStartElement("attributeList");
                for (Annotation ann : object.getAnnotations()){
                    getAttributeWriter().write(ann);
                }
                // write end attributeList
                getStreamWriter().writeEndElement();
            }
        }
    }
}
