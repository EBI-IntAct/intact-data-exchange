package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.xml25.XmlSourceWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Writer of a source in an 2.5 entry.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/11/13</pre>
 */

public class XmlIntactSourceWriter extends XmlSourceWriter {

    public XmlIntactSourceWriter(XMLStreamWriter writer){
        super(writer);
    }

    @Override
    protected void writeAttributes(Source object) throws XMLStreamException {
        if (!object.getAnnotations().isEmpty()){
            Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(object.getAnnotations(),
                    null, "no-export");
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
