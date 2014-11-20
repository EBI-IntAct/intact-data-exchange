package uk.ac.ebi.intact.dataexchange.psimi.mitab.writer;

import psidev.psi.mi.jami.tab.io.writer.Mitab26BinaryEvidenceWriter;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.writer.feeder.MitabIntactInteractionEvidenceFeeder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Intact Mitab 2.6 writer for binary interaction evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/06/13</pre>
 */

public class Mitab26IntactBinaryEvidenceWriter extends Mitab26BinaryEvidenceWriter {

    public Mitab26IntactBinaryEvidenceWriter() {
        super();
    }

    public Mitab26IntactBinaryEvidenceWriter(File file) throws IOException {
        super(file);
    }

    public Mitab26IntactBinaryEvidenceWriter(OutputStream output) {
        super(output);
    }

    public Mitab26IntactBinaryEvidenceWriter(Writer writer) {
        super(writer);
    }

    @Override
    protected void initialiseColumnFeeder() {
        setColumnFeeder(new MitabIntactInteractionEvidenceFeeder(getWriter()));
    }
}
