package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.compact.xml25;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.ModelledParticipant;
import psidev.psi.mi.jami.model.Stoichiometry;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.CompactPsiXmlElementWriter;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.compact.xml25.XmlModelledParticipantWriter;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.IntactConfiguration;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Compact XML 2.5 writer for a intact modelled participant.
 * It ignores experimental details.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>14/11/13</pre>
 */

public class XmlIntactModelledParticipantWriter extends XmlModelledParticipantWriter implements CompactPsiXmlElementWriter<ModelledParticipant> {
    public XmlIntactModelledParticipantWriter(XMLStreamWriter writer, PsiXmlObjectCache objectIndex) {
        super(writer, objectIndex);
    }

    @Override
    protected void writeXref(ModelledParticipant object) throws XMLStreamException {
        if (object instanceof IntactModelledParticipant){
            IntactModelledParticipant intactParticipant = (IntactModelledParticipant)object;
            if (intactParticipant.getAc() != null){
                writeIntactAc("primaryRef", intactParticipant.getAc());
                if (!object.getXrefs().isEmpty()){
                    writeXrefFromIntactParticipantXrefs(object);
                }
            }
            else if (!object.getXrefs().isEmpty()){
                writeXrefFromParticipantXrefs(object);
            }
        }
        else{
            super.writeXref(object);
        }
    }

    protected void writeXrefFromIntactParticipantXrefs(ModelledParticipant object) throws XMLStreamException {
        Iterator<Xref> refIterator = object.getXrefs().iterator();
        // default qualifier is null as we are not processing identifiers
        getXrefWriter().setDefaultRefType(null);
        getXrefWriter().setDefaultRefTypeAc(null);
        // write start xref
        getStreamWriter().writeStartElement("xref");

        while (refIterator.hasNext()){
            Xref ref = refIterator.next();
            // write secondaryref
            getXrefWriter().write(ref,"secondaryRef");
        }

        // write end xref
        getStreamWriter().writeEndElement();
    }

    protected void writeIntactAc(String nodeName, String ac) throws XMLStreamException {

        IntactConfiguration intactConfig = ApplicationContextProvider.getBean("intactJamiConfiguration");
        String dbName = "intact";
        String dbAc = "MI:0469";
        if (intactConfig != null){
            dbName = intactConfig.getDefaultInstitution().getShortName();
            dbAc = intactConfig.getDefaultInstitution().getMIIdentifier();
        }
        // write start
        getStreamWriter().writeStartElement(nodeName);
        // write database
        getStreamWriter().writeAttribute("db", dbName);
        if (dbAc != null){
            getStreamWriter().writeAttribute("dbAc", dbAc);
        }
        // write id
        getStreamWriter().writeAttribute("id", ac);
        // write qualifier
        getStreamWriter().writeAttribute("refType", Xref.IDENTITY);
        getStreamWriter().writeAttribute("refTypeAc", Xref.IDENTITY_MI);
        // write end db ref
        getStreamWriter().writeEndElement();
    }

    @Override
    protected void writeAttributes(ModelledParticipant object) throws XMLStreamException {
        // write attributes
        Stoichiometry stc = object.getStoichiometry();
        Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(object.getAnnotations(),
                null, "no-export");
        Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(object.getAnnotations());
        exportAnnotations.removeAll(noExportAnnotations);
        if (!exportAnnotations.isEmpty()){
            // write start attribute list
            getStreamWriter().writeStartElement("attributeList");
            for (Object ann : exportAnnotations){
                getAttributeWriter().write((Annotation)ann);
            }
            // write stoichiometry attribute if not null
            writeOtherAttributes(object, false);
            // write end attributeList
            getStreamWriter().writeEndElement();
        }
        // write stoichiometry attribute if not null
        else {
            writeOtherAttributes(object, true);
        }
    }
}
