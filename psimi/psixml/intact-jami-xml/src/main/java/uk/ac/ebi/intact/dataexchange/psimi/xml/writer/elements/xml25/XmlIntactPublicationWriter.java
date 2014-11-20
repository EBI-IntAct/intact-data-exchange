package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml25;

import psidev.psi.mi.jami.exception.MIIOException;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CurationDepth;
import psidev.psi.mi.jami.model.Publication;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.xml25.XmlPublicationWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Xml25 writer for publications (bibref objects)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>11/11/13</pre>
 */

public class XmlIntactPublicationWriter extends XmlPublicationWriter {

    public XmlIntactPublicationWriter(XMLStreamWriter writer){
        super(writer);
    }

    @Override
    public void writeAllPublicationAttributes(Publication object) {
        try{
            Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(object.getAnnotations(),
                    null, "no-export");
            Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(object.getAnnotations());
            exportAnnotations.removeAll(noExportAnnotations);
            boolean hasTitle = object.getTitle() != null;
            boolean hasJournal = object.getJournal() != null;
            boolean hasPublicationDate = object.getPublicationDate() != null;
            boolean hasCurationDepth = !CurationDepth.undefined.equals(object.getCurationDepth());
            boolean hasAuthors = !object.getAuthors().isEmpty();
            boolean hasAttributes = !exportAnnotations.isEmpty();
            // write attributes if no identifiers available
            if (hasTitle || hasJournal || hasPublicationDate || hasCurationDepth || hasAuthors || hasAttributes){
                // write start attribute list
                getStreamWriter().writeStartElement("attributeList");
                // write publication properties such as title, journal, etc..
                writePublicationPropertiesAsAttributes(object, hasTitle, hasJournal, hasPublicationDate, hasCurationDepth, hasAuthors);
                // write normal attributes
                if (hasAttributes){
                    Iterator<Annotation> annotIterator = exportAnnotations.iterator();
                    while (annotIterator.hasNext()){
                        Annotation ann = annotIterator.next();
                        getAttributeWriter().write(ann);
                    }
                }
                // write end attributeList
                getStreamWriter().writeEndElement();
            }
        } catch (XMLStreamException e) {
            throw new MIIOException("Impossible to write the publication attributes for : "+object.toString(), e);
        }
    }
}
