package uk.ac.ebi.intact.dataexchange.psimi.mitab.writer;

import psidev.psi.mi.jami.binary.BinaryInteractionEvidence;
import psidev.psi.mi.jami.binary.expansion.ComplexExpansionMethod;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.tab.io.writer.Mitab25BinaryEvidenceWriter;
import psidev.psi.mi.jami.tab.io.writer.Mitab25EvidenceWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * The mitab 2.5 writer for interaction evidence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/06/13</pre>
 */

public class Mitab25IntactEvidenceWriter extends Mitab25EvidenceWriter {

    public Mitab25IntactEvidenceWriter() {
        super();
    }

    public Mitab25IntactEvidenceWriter(File file) throws IOException {
        super(file);
    }

    public Mitab25IntactEvidenceWriter(OutputStream output) {
        super(output);
    }

    public Mitab25IntactEvidenceWriter(Writer writer) {
        super(writer);
    }

    public Mitab25IntactEvidenceWriter(OutputStream output, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) {
        super(output, expansionMethod);
    }

    public Mitab25IntactEvidenceWriter(File file, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) throws IOException {
        super(file, expansionMethod);
    }

    public Mitab25IntactEvidenceWriter(Writer writer, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) {
        super(writer, expansionMethod);
    }

    @Override
    protected void initialiseWriter(Writer writer) {
        setBinaryWriter(new Mitab25IntactBinaryEvidenceWriter(writer));
    }

    @Override
    protected void initialiseOutputStream(OutputStream output) {
        setBinaryWriter(new Mitab25IntactBinaryEvidenceWriter(output));
    }

    @Override
    protected void initialiseFile(File file) throws IOException {
        setBinaryWriter(new Mitab25IntactBinaryEvidenceWriter(file));
    }
}
