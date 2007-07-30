/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.enricher;

import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.standard.IntactEntryEnricher;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.model.IntactEntry;

import java.io.*;

/**
 * Main utility to enrich a Psi file with additional information from the intact database and external sources
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiEnricher {

    public static void enrichPsiXml(File sourcePsiFile, File destinationPsiFile, EnricherConfig config) throws IOException {
        EntrySet entrySet = readEntrySet(new FileInputStream(sourcePsiFile));
        entrySet = enrichEntrySet(entrySet, config);

        FileWriter writer = new FileWriter(destinationPsiFile);
        writeEntrySet(entrySet, writer);
        writer.close();
    }

    public static void enrichPsiXml(InputStream sourcePsi, Writer enrichedPsiWriter, EnricherConfig config) throws IOException {
        EntrySet entrySet = readEntrySet(sourcePsi);
        entrySet = enrichEntrySet(entrySet, config);
        writeEntrySet(entrySet, enrichedPsiWriter);
    }

    public static EntrySet enrichEntrySet(EntrySet entrySet, EnricherConfig config) {
        EntrySet enrichedSet = new EntrySet();
        enrichedSet.setLevel(entrySet.getLevel());
        enrichedSet.setVersion(enrichedSet.getVersion());
        enrichedSet.setMinorVersion(entrySet.getMinorVersion());

        for (Entry entry : entrySet.getEntries()) {
            Entry enrichedEntry = enrichEntry(entry, config);
            enrichedSet.getEntries().add(enrichedEntry);
        }

        return enrichedSet;
    }

    public static Entry enrichEntry(Entry entry, EnricherConfig config) {

        EntryConverter converter = new EntryConverter(null);
        IntactEntry intactEntry = converter.psiToIntact(entry);

        EnricherContext.getInstance().setConfig(config);

        IntactEntryEnricher enricher = IntactEntryEnricher.getInstance();
        enricher.enrich(intactEntry);

        Entry enrichedEntry = converter.intactToPsi(intactEntry);
        enrichedEntry.setSource(entry.getSource());
        
        return enrichedEntry;

    }

    private static EntrySet readEntrySet(InputStream is) throws IOException {
        PsimiXmlReader reader = new PsimiXmlReader();

        try {
            return reader.read(is);
        } catch (Exception e) {
            throw new PsiEnricherException(e);
        }
    }

    private static void writeEntrySet(EntrySet entrySet, Writer destWriter) throws IOException {
        PsimiXmlWriter writer = new PsimiXmlWriter();

        try {
            writer.write(entrySet, destWriter);
        } catch (Exception e) {
            throw new PsiEnricherException(e);
        }
    }

}