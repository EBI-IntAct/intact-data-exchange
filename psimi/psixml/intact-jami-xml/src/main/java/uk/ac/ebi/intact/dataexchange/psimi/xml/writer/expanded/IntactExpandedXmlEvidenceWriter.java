package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.expanded;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.xml.PsiXmlType;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.elements.*;
import psidev.psi.mi.jami.xml.io.writer.expanded.ExpandedXmlEvidenceWriter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.IntactPsiXmlElementWriterFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Expanded PSI-XML writer for interaction evidences (full experimental evidences)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19/11/13</pre>
 */

public class IntactExpandedXmlEvidenceWriter extends ExpandedXmlEvidenceWriter {

    public IntactExpandedXmlEvidenceWriter() {
        super();
    }

    public IntactExpandedXmlEvidenceWriter(File file) throws IOException, XMLStreamException {
        super(file);
    }

    public IntactExpandedXmlEvidenceWriter(OutputStream output) throws XMLStreamException {
        super(output);
    }

    public IntactExpandedXmlEvidenceWriter(Writer writer) throws XMLStreamException {
        super(writer);
    }

    public IntactExpandedXmlEvidenceWriter(XMLStreamWriter streamWriter, PsiXmlObjectCache cache) {
        super(streamWriter, cache);
    }

    @Override
    protected void initialiseSubWriters() {
        IntactPsiXmlElementWriterFactory intactFactory = IntactPsiXmlElementWriterFactory.getInstance();

        // basic sub writers
        // aliases
        PsiXmlElementWriter<Alias> aliasWriter = getSubWritersFactory().createAliasWriter(getStreamWriter());
        // attributes
        PsiXmlElementWriter<Annotation> attributeWriter = getSubWritersFactory().createAnnotationWriter(getStreamWriter());
        // xref
        PsiXmlXrefWriter xrefWriter = getSubWritersFactory().createXrefWriter(getStreamWriter(), false, attributeWriter);
        // publication
        PsiXmlPublicationWriter publicationWriter = getSubWritersFactory().createPublicationWriter(getStreamWriter(), false,
                attributeWriter, xrefWriter, getVersion());
        // open cv
        PsiXmlVariableNameWriter<CvTerm> openCvWriter = getSubWritersFactory().createOpenCvWriter(getStreamWriter(), false, aliasWriter,
                attributeWriter, xrefWriter);
        // cv
        PsiXmlVariableNameWriter<CvTerm> cvWriter = getSubWritersFactory().createCvWriter(getStreamWriter(), false, aliasWriter, xrefWriter);
        // confidences
        PsiXmlElementWriter<Confidence>[] confidenceWriters = getSubWritersFactory().createConfidenceWriters(getStreamWriter(), false,
                getElementCache(), getVersion(), openCvWriter, publicationWriter);
        // organism writer
        PsiXmlElementWriter<Organism> organismWriter = getSubWritersFactory().createOrganismWriter(getStreamWriter(), false, aliasWriter,
                attributeWriter, xrefWriter, openCvWriter);
        // checksum writer
        PsiXmlElementWriter<Checksum> checksumWriter = getSubWritersFactory().createChecksumWriter(getStreamWriter());
        // interactor writer
        PsiXmlElementWriter<Interactor> interactorWriter = getSubWritersFactory().createInteractorWriter(getStreamWriter(), false, getElementCache(),
                aliasWriter, attributeWriter, xrefWriter, cvWriter, organismWriter, checksumWriter);
        // experiment Writer
        PsiXmlExperimentWriter experimentWriter = intactFactory.createExperimentWriter(getStreamWriter(), false, getElementCache(),
                getVersion(), false, aliasWriter, attributeWriter, xrefWriter, publicationWriter, organismWriter, cvWriter,
                confidenceWriters[0]);
        // availability writer
        PsiXmlElementWriter<String> availabilityWriter = getSubWritersFactory().createAvailabilityWriter(getStreamWriter(), getElementCache());
        // initialise source
        setSourceWriter(getSubWritersFactory().createSourceWriter(getStreamWriter(), false, getVersion(), aliasWriter, attributeWriter,
                xrefWriter, publicationWriter));
        // initialise optional writers
        initialiseOptionalWriters(experimentWriter, availabilityWriter, interactorWriter);
        // initialise interaction
        PsiXmlInteractionWriter[] interactionWriters = intactFactory.createInteractionWritersFor(getStreamWriter(), getElementCache(),
                getVersion(), PsiXmlType.expanded, InteractionCategory.evidence, ComplexType.n_ary, false, false, aliasWriter, attributeWriter, xrefWriter,
                confidenceWriters[0], confidenceWriters[1], checksumWriter, cvWriter, openCvWriter, experimentWriter, availabilityWriter,
                interactorWriter, publicationWriter);
        setInteractionWriter(interactionWriters[0]);
        // initialise complex
        setComplexWriter(interactionWriters[1]);
        // initialise annotation writer
        setAnnotationsWriter(attributeWriter);
    }
}

