package uk.ac.ebi.intact.dataexchange.psimi.xml.writer.compact;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.xml.PsiXmlType;
import psidev.psi.mi.jami.xml.cache.PsiXmlObjectCache;
import psidev.psi.mi.jami.xml.io.writer.compact.CompactXmlComplexWriter;
import psidev.psi.mi.jami.xml.io.writer.elements.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.writer.elements.IntactPsiXmlElementWriterFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Compact PSI-XML writer for named biological complexes (no experimental evidences).
 * Participants, features, experiments also have expanded names
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19/11/13</pre>
 */

public class IntactCompactXmlComplexWriter extends CompactXmlComplexWriter {

    public IntactCompactXmlComplexWriter() {
        super();
    }

    public IntactCompactXmlComplexWriter(File file) throws IOException, XMLStreamException {
        super(file);
    }

    public IntactCompactXmlComplexWriter(OutputStream output) throws XMLStreamException {
        super(output);
    }

    public IntactCompactXmlComplexWriter(Writer writer) throws XMLStreamException {
        super(writer);
    }

    public IntactCompactXmlComplexWriter(XMLStreamWriter streamWriter, PsiXmlObjectCache cache) {
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
        PsiXmlPublicationWriter publicationWriter = intactFactory.createPublicationWriter(getStreamWriter(), false,
                attributeWriter, xrefWriter, getVersion());
        // open cv
        PsiXmlVariableNameWriter<CvTerm> openCvWriter = intactFactory.createOpenCvWriter(getStreamWriter(), false, aliasWriter,
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
        PsiXmlElementWriter<Interactor> interactorWriter = intactFactory.createInteractorWriter(getStreamWriter(), false, getElementCache(),
                aliasWriter, attributeWriter, xrefWriter, cvWriter, organismWriter, checksumWriter);
        // experiment Writer
        PsiXmlExperimentWriter experimentWriter = intactFactory.createExperimentWriter(getStreamWriter(), false, getElementCache(),
                getVersion(), false, aliasWriter, attributeWriter, xrefWriter, publicationWriter, organismWriter, cvWriter,
                confidenceWriters[0]);
        // availability writer
        PsiXmlElementWriter<String> availabilityWriter = getSubWritersFactory().createAvailabilityWriter(getStreamWriter(), getElementCache());
        // initialise source
        setSourceWriter(intactFactory.createSourceWriter(getStreamWriter(), false, getVersion(), aliasWriter, attributeWriter,
                xrefWriter, publicationWriter));
        // initialise optional writers
        initialiseOptionalWriters(experimentWriter, availabilityWriter, interactorWriter);
        // initialise interaction
        PsiXmlInteractionWriter[] interactionWriters = intactFactory.createInteractionWritersFor(getStreamWriter(), getElementCache(),
                getVersion(), PsiXmlType.compact, InteractionCategory.complex, ComplexType.n_ary, false, false, aliasWriter, attributeWriter, xrefWriter,
                confidenceWriters[0], confidenceWriters[1], checksumWriter, cvWriter, openCvWriter, experimentWriter, availabilityWriter,
                interactorWriter, publicationWriter);
        setInteractionWriter(interactionWriters[0]);
        // initialise complex
        setComplexWriter(interactionWriters[1]);
        // initialise annotation writer
        setAnnotationsWriter(attributeWriter);
    }
}
