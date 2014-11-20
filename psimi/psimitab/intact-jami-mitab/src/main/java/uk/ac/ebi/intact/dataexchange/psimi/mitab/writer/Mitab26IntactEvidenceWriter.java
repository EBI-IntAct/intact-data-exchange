package uk.ac.ebi.intact.dataexchange.psimi.mitab.writer;

import psidev.psi.mi.jami.binary.BinaryInteractionEvidence;
import psidev.psi.mi.jami.binary.expansion.ComplexExpansionMethod;
import psidev.psi.mi.jami.model.InteractionEvidence;
import psidev.psi.mi.jami.tab.io.writer.Mitab26BinaryEvidenceWriter;
import psidev.psi.mi.jami.tab.io.writer.Mitab26EvidenceWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Mitab 2.6 writer for interaction evidence
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/06/13</pre>
 */

public class Mitab26IntactEvidenceWriter extends Mitab26EvidenceWriter {

    public Mitab26IntactEvidenceWriter() {
        super();
    }

    public Mitab26IntactEvidenceWriter(File file) throws IOException {
        super(file);
    }

    public Mitab26IntactEvidenceWriter(OutputStream output) {
        super(output);
    }

    public Mitab26IntactEvidenceWriter(Writer writer) {
        super(writer);
    }

    public Mitab26IntactEvidenceWriter(OutputStream output, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) {
        super(output, expansionMethod);
    }

    public Mitab26IntactEvidenceWriter(File file, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) throws IOException {
        super(file, expansionMethod);
    }

    public Mitab26IntactEvidenceWriter(Writer writer, ComplexExpansionMethod<InteractionEvidence, BinaryInteractionEvidence> expansionMethod) {
        super(writer, expansionMethod);
    }

    @Override
    protected void initialiseWriter(Writer writer){
        setBinaryWriter(new Mitab26IntactBinaryEvidenceWriter(writer));
    }

    @Override
    protected void initialiseOutputStream(OutputStream output) {
        setBinaryWriter(new Mitab26IntactBinaryEvidenceWriter(output));
    }

    @Override
    protected void initialiseFile(File file) throws IOException {
        setBinaryWriter(new Mitab26IntactBinaryEvidenceWriter(file));
    }
}
