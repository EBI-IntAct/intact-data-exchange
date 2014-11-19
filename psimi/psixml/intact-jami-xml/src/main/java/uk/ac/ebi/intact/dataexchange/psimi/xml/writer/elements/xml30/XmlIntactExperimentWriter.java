package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.xml30;

import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.xml30.XmlExperimentWriter;
import psidev.psi.mi.jami.xml.utils.PsiXmlUtils;
import uk.ac.ebi.intact.jami.ApplicationContextProvider;
import uk.ac.ebi.intact.jami.context.IntactConfiguration;
import uk.ac.ebi.intact.jami.model.extension.IntactExperiment;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Collections;
import java.util.Iterator;

/**
 * XML 3.0 intact experiment writer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>13/11/13</pre>
 */

public class XmlIntactExperimentWriter extends XmlExperimentWriter {

    public XmlIntactExperimentWriter(XMLStreamWriter writer, PsiXmlObjectCache objectIndex) {
        super(writer, objectIndex);
    }

    @Override
    protected void writeNames(Experiment object) throws XMLStreamException {
        if (object instanceof IntactExperiment){
            IntactExperiment xmlExperiment = (IntactExperiment) object;
            // write names
            PsiXmlUtils.writeCompleteNamesElement(xmlExperiment.getShortLabel(),
                    xmlExperiment.getPublication() != null ? xmlExperiment.getPublication().getTitle() : null,
                    Collections.EMPTY_LIST,
                    getStreamWriter(),
                    null);
        }
        else{
            super.writeNames(object);
        }
    }

    @Override
    protected void writeExperimentXrefs(Experiment object, String imexId) throws XMLStreamException {
        if (object instanceof IntactExperiment){
            IntactExperiment intactExperiment = (IntactExperiment)object;
            // write xrefs
            if (!intactExperiment.getXrefs().isEmpty() || imexId != null || intactExperiment.getAc() != null){
                // write start xref
                getStreamWriter().writeStartElement("xref");
                if (intactExperiment.getAc() != null){
                    writeIntactAc("primaryRef", intactExperiment.getAc());
                    if (!object.getXrefs().isEmpty()){
                        writeXrefFromIntactExperimentXrefs(intactExperiment, imexId);
                    }
                }
                else if (!object.getXrefs().isEmpty()){
                    writeXrefFromExperimentXrefs(intactExperiment, imexId);
                }

                // write end xref
                getStreamWriter().writeEndElement();
            }
        }
        else{
            super.writeExperimentXrefs(object, imexId);
        }
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

    protected void writeXrefFromIntactExperimentXrefs(Experiment object, String imexId) throws XMLStreamException {
        Iterator<Xref> refIterator = object.getXrefs().iterator();
        // default qualifier is null as we are not processing identifiers
        getXrefWriter().setDefaultRefType(null);
        getXrefWriter().setDefaultRefTypeAc(null);

        while (refIterator.hasNext()){
            Xref ref = refIterator.next();
            // write secondaryref
            getXrefWriter().write(ref,"secondaryRef");

        }
    }
}
