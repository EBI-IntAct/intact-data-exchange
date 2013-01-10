package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher;

import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Entry;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public interface PsiEnricher {
    void enrichPsiXml(File sourcePsiFile, File destinationPsiFile, EnricherConfig config) throws IOException;

    void enrichPsiXml(InputStream sourcePsi, Writer enrichedPsiWriter, EnricherConfig config) throws PsiEnricherException;

    EntrySet enrichEntrySet(EntrySet entrySet, EnricherConfig config);

    Entry enrichEntry(Entry entry, EnricherConfig config);
}
