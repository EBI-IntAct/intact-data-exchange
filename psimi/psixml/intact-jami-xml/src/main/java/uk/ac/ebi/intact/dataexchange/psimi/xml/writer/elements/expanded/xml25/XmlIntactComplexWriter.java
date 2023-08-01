package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.expanded.xml25;

import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.Checksum;
import psidev.psi.mi.jami.model.Complex;
import psidev.psi.mi.jami.model.Experiment;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.ExpandedPsiXmlElementWriter;
import psidev.psi.mi.jami.xml.io.writer.elements.impl.expanded.xml25.XmlComplexWriter;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Expanded XML 2.5 writer for a biological complex (ignore experimental details).
 * It will write cooperative effects as attributes
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/11/13</pre>
 */

public class XmlIntactComplexWriter extends XmlComplexWriter
        implements ExpandedPsiXmlElementWriter<Complex> {

    public XmlIntactComplexWriter(PsiXmlVersion version, XMLStreamWriter writer, PsiXmlObjectCache objectIndex) {
        super(version, writer, objectIndex);
    }

    @Override
    public Experiment extractDefaultExperimentFrom(Complex interaction) {
        if (interaction instanceof IntactComplex){
            IntactComplex intactComplex = (IntactComplex)interaction;
            Experiment exp = !intactComplex.getExperiments().isEmpty()? intactComplex.getExperiments().iterator().next():null;

            if (exp != null){
                return exp;
            }
            else{
                return super.extractDefaultExperimentFrom(interaction);
            }
        }
        else{
            return super.extractDefaultExperimentFrom(interaction);
        }
    }

    @Override
    protected void writeAttributes(Complex object) throws XMLStreamException {
        Collection<Annotation> noExportAnnotations = AnnotationUtils.collectAllAnnotationsHavingTopic(object.getAnnotations(),
                null, "no-export");
        Collection<Annotation> exportAnnotations = new ArrayList<Annotation>(object.getAnnotations());
        exportAnnotations.removeAll(noExportAnnotations);
        // write attributes
        if (!exportAnnotations.isEmpty()){
            // write start attribute list
            getStreamWriter().writeStartElement("attributeList");
            // write existing attributes
            for (Object ann : exportAnnotations){
                getAttributeWriter().write((Annotation) ann);
            }
            for (Object c : object.getChecksums()){
                getChecksumWriter().write((Checksum)c);
            }
            // write cooperative effect
            // can only write the FIRST cooperative effect
            if (!object.getCooperativeEffects().isEmpty()){
                writeCooperativeEffect(object, false);
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
            // can only write the FIRST cooperative effect
            if (!object.getCooperativeEffects().isEmpty()){
                writeCooperativeEffect(object, false);
            }
            // write end attributeList
            getStreamWriter().writeEndElement();
        }
        // write cooperative effects
        else if (!object.getCooperativeEffects().isEmpty()){
            // write cooperative effects
            writeCooperativeEffect(object, true);
        }
    }
}
