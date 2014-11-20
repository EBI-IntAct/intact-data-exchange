package uk.ac.ebi.intact.dataexchange.psimi.mitab.writer;

import psidev.psi.mi.jami.tab.io.writer.Mitab25BinaryEvidenceWriter;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.writer.feeder.MitabIntactInteractionEvidenceFeeder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Intact Mitab 2.5 writer for binary interaction evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>19/06/13</pre>
 */

public class Mitab25IntactBinaryEvidenceWriter extends Mitab25BinaryEvidenceWriter {

    public Mitab25IntactBinaryEvidenceWriter() {
        super();
    }

    public Mitab25IntactBinaryEvidenceWriter(File file) throws IOException {
        super(file);
    }

    public Mitab25IntactBinaryEvidenceWriter(OutputStream output) {
        super(output);
    }

    public Mitab25IntactBinaryEvidenceWriter(Writer writer) {
        super(writer);
    }

    @Override
    protected void initialiseColumnFeeder() {
        setColumnFeeder(new MitabIntactInteractionEvidenceFeeder(getWriter()));
    }

}
