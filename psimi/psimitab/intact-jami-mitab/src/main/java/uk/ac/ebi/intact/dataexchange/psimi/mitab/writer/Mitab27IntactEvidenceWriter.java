package uk.ac.ebi.intact.dataexchange.psimi.mitab.writer;

import psidev.psi.mi.jami.binary.BinaryInteractionEvidence;
import psidev.psi.mi.jami.binary.expansion.ComplexExpansionMethod;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.tab.io.writer.Mitab27BinaryEvidenceWriter;
import psidev.psi.mi.jami.tab.io.writer.Mitab27EvidenceWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Mitab 2.7 writer for interaction evidence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/06/13</pre>
 */

public class Mitab27IntactEvidenceWriter extends Mitab27EvidenceWriter {

    public Mitab27IntactEvidenceWriter() {
    super();
}

    public Mitab27IntactEvidenceWriter(File file) throws IOException {
        super(file);
    }

    public Mitab27IntactEvidenceWriter(OutputStream output) {
        super(output);
    }

    public Mitab27IntactEvidenceWriter(Writer writer) {
        super(writer);
    }

    public Mitab27IntactEvidenceWriter(OutputStream output, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) {
        super(output, expansionMethod);
    }

    public Mitab27IntactEvidenceWriter(File file, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) throws IOException {
        super(file, expansionMethod);
    }

    public Mitab27IntactEvidenceWriter(Writer writer, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) {
        super(writer, expansionMethod);
    }

    @Override
    protected void initialiseWriter(Writer writer){
        setBinaryWriter(new Mitab27IntactBinaryEvidenceWriter(writer));
    }

    @Override
    protected void initialiseOutputStream(OutputStream output) {
        setBinaryWriter(new Mitab27IntactBinaryEvidenceWriter(output));
    }

    @Override
    protected void initialiseFile(File file) throws IOException {
        setBinaryWriter(new Mitab27IntactBinaryEvidenceWriter(file));
    }
}