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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher;

import psidev.psi.mi.xml.*;
import psidev.psi.mi.xml.xmlindex.IndexedEntry;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Source;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InstitutionConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InteractionConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherException;
import uk.ac.ebi.intact.dataexchange.enricher.standard.IntactEntryEnricher;
import uk.ac.ebi.intact.dataexchange.enricher.standard.InstitutionEnricher;
import uk.ac.ebi.intact.dataexchange.enricher.standard.InteractionEnricher;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Institution;

import java.io.*;
import java.util.List;
import java.util.Iterator;

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

    /**
     * Enriches a PSI XML Stream in memory
     * @param sourcePsi The PSI XML data to enrich
     * @param enrichedPsiWriter The writer where
     * @param config configuration to use
     * @throws IOException
     */
    public static void enrichPsiXml(InputStream sourcePsi, Writer enrichedPsiWriter, EnricherConfig config) throws PsiEnricherException {
        final List<IndexedEntry> indexedEntries;
        try {
            PsimiXmlLightweightReader reader = new PsimiXmlLightweightReader( sourcePsi );
            indexedEntries = reader.getIndexedEntries();
        } catch (PsimiXmlReaderException e) {
            throw new PsiEnricherException("Problem reading source PSI", e);
        }

        EnricherContext.getInstance().setConfig(config);
        
        try {

            PsimiXmlLightweightWriter writer = new PsimiXmlLightweightWriter(enrichedPsiWriter);

            writer.writeStartDocument();

            for (Iterator<IndexedEntry> indexedEntryIterator = indexedEntries.iterator(); indexedEntryIterator.hasNext();) {
                IndexedEntry entry =  indexedEntryIterator.next();

                // TODO put the source conversion in a method
                final Source source = entry.unmarshallSource();
                InstitutionConverter institutionConverter = new InstitutionConverter();
                final Institution institution = institutionConverter.psiToIntact(source);
                InstitutionEnricher.getInstance().enrich(institution);
                Source enrichedSource = institutionConverter.intactToPsi(institution);
                if (source.getReleaseDate() != null) {
                    enrichedSource.setReleaseDate(source.getReleaseDate());
                }

                writer.writeStartEntry(enrichedSource, entry.unmarshallAvailabilityList());

                InteractionConverter interactionConverter = new InteractionConverter(institution);

                final Iterator<Interaction> iterator = entry.unmarshallInteractionIterator();
                while (iterator.hasNext()) {

                    Interaction interaction = iterator.next();

                    //TODO put this in a different method?
                    uk.ac.ebi.intact.model.Interaction intactInteraction = interactionConverter.psiToIntact(interaction);
                    InteractionEnricher.getInstance().enrich(intactInteraction);
                    Interaction enrichedInteraction = interactionConverter.intactToPsi(intactInteraction);

                    ConversionCache.clear();

                    writer.writeInteraction(enrichedInteraction);
                }

                writer.writeEndEntry(entry.unmarshallAttributeList());

            }
            writer.writeEndDocument();

        } catch (EnricherException ee) {
            throw new PsiEnricherException("Problem enriching data", ee);
        } catch (Exception e) {
            throw new PsiEnricherException("Problem writing output PSI", e);
        }
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
        EntryConverter converter = new EntryConverter();
        IntactEntry intactEntry = converter.psiToIntact(entry);

        EnricherContext.getInstance().setConfig(config);

        IntactEntryEnricher enricher = IntactEntryEnricher.getInstance();
        enricher.enrich(intactEntry);

        Entry enrichedEntry = converter.intactToPsi(intactEntry);
        enrichedEntry.getSource().setReleaseDate(entry.getSource().getReleaseDate());

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