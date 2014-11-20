package uk.ac.ebi.intact.dataexchange.psimi.mitab.writer;

import psidev.psi.mi.jami.tab.io.writer.Mitab27BinaryEvidenceWriter;
import uk.ac.ebi.intact.dataexchange.psimi.mitab.writer.feeder.MitabIntactInteractionEvidenceFeeder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Mitab 2.7 writer for binary interaction evidences
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/06/13</pre>
 */

public class Mitab27IntactBinaryEvidenceWriter extends Mitab27BinaryEvidenceWriter {

    public Mitab27IntactBinaryEvidenceWriter() {
        super();
    }

    public Mitab27IntactBinaryEvidenceWriter(File file) throws IOException {
        super(file);
    }

    public Mitab27IntactBinaryEvidenceWriter(OutputStream output){
        super(output);
    }

    public Mitab27IntactBinaryEvidenceWriter(Writer writer){
        super(writer);
    }

    @Override
    protected void initialiseColumnFeeder() {
        setColumnFeeder(new MitabIntactInteractionEvidenceFeeder(getWriter()));
    }
}
