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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import psidev.psi.mi.xml.*;
import psidev.psi.mi.xml.xmlindex.IndexedEntry;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Source;
import uk.ac.ebi.intact.core.IntactException;
 
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.persister.PersisterHelper;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InstitutionConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InteractionConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher.PsiEnricherException;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;
import uk.ac.ebi.intact.model.InteractionImpl;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

/**
 * Imports/exports data between an IntAct-model database and PSI XML.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiExchangeImpl implements PsiExchange {

    private PersisterHelper persisterHelper;
    private IntactContext intactContext;
    private PsimiXmlForm xmlForm;
    private PsimiXmlVersion psiVersion;

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(PsiExchangeImpl.class);

    public PsiExchangeImpl(IntactContext intactContext) {
        this.intactContext = intactContext;
        this.persisterHelper = intactContext.getPersisterHelper();
        this.xmlForm = PsimiXmlForm.FORM_COMPACT;
        this.psiVersion = PsimiXmlVersion.VERSION_254;
    }

    /////////////////////
    // PsiExchange

    public PsimiXmlForm getXmlForm() {
        return xmlForm;
    }

    public void setXmlForm(PsimiXmlForm xmlForm) {
        if (xmlForm == null) {
            throw new IllegalArgumentException("You must give a non null xmlForm");
        }
        this.xmlForm = xmlForm;
    }

    public PsimiXmlVersion getPsiVersion() {
        return psiVersion;
    }

    public void setPsiVersion(PsimiXmlVersion psiVersion) {
        if (psiVersion == null) {
            throw new IllegalArgumentException("You must give a non null psiVersion");
        }
        this.psiVersion = psiVersion;
    }

    /**
     * Imports a stream containing PSI XML
     *
     * @param psiXmlStream the stream to read and import
     * @return report of the import
     *
     * @throws PersisterException thrown if there are problems parsing the stream or persisting the data in the intact-model database
     */
    public PersisterStatistics importIntoIntact(InputStream psiXmlStream) throws PersisterException {
        final List<IndexedEntry> indexedEntries;
        try {
            PsimiXmlLightweightReader reader = new PsimiXmlLightweightReader( psiXmlStream );
            indexedEntries = reader.getIndexedEntries();
        } catch (PsimiXmlReaderException e) {
            throw new PsiEnricherException("Problem reading source PSI", e);
        }

        PersisterStatistics stats = new PersisterStatistics();

        for (IndexedEntry indexedEntry : indexedEntries) {
            PersisterStatistics localStats = importIntoIntact(indexedEntry);
            stats = merge(stats, localStats);
        }

        return stats;
    }

    @Transactional
    public PersisterStatistics importIntoIntact(IndexedEntry entry) throws ImportException {
        long startTime = System.currentTimeMillis();

        ConversionCache.clear();

        PersisterStatistics importStats = new PersisterStatistics();

        final Institution institution;
        try {
            final Source source = entry.unmarshallSource();
            InstitutionConverter institutionConverter = new InstitutionConverter();
            institution = institutionConverter.psiToIntact(source);
        } catch (PsimiXmlReaderException e) {
            throw new ImportException("Problem unmarshalling IndexedEntry source", e);
        }

        InteractionConverter interactionConverter = new InteractionConverter(institution);

        final Iterator<psidev.psi.mi.xml.model.Interaction> iterator;
        try {
            iterator = entry.unmarshallInteractionIterator();
        } catch (PsimiXmlReaderException e) {
            throw new ImportException("Problem unmarshalling psi interactions from IndexedEntry", e);
        }

        int interactionCount = 0;

        while (iterator.hasNext()) {
            psidev.psi.mi.xml.model.Interaction psiInteraction = iterator.next();

            Interaction interaction = interactionConverter.psiToIntact(psiInteraction);
            ConversionCache.clear();

            // mark the interaction to save or update
            if (log.isDebugEnabled()) log.debug("Persisting: "+interaction.getShortLabel());
            PersisterStatistics localImportStats = importIntoIntact(interaction);

            importStats = merge(importStats, localImportStats);

            interactionCount++;
        }

        if (log.isDebugEnabled()) {
            log.debug("On the fly import done. Processed " + interactionCount+" interactions ("+importStats.getPersistedCount(InteractionImpl.class, false) + " persisted, "+importStats.getDuplicatesCount(InteractionImpl.class, false)+" duplicated (ignored)) in "+
                    (System.currentTimeMillis() - startTime) + "ms");
        }

        return importStats;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PersisterStatistics importIntoIntact(Interaction interaction) {
        return persisterHelper.save(interaction);
    }

    /**
     * Imports an EntrySet into intact
     *
     * @param entrySet the psi set of entries to import
     * @return report of the import
     *
     * @throws PersisterException thrown if there are problems persisting the data in the intact-model database
     */
    public PersisterStatistics importIntoIntact(EntrySet entrySet) throws ImportException {

        PersisterStatistics importStats = new PersisterStatistics();

        // some time for stats
        long startTime = System.currentTimeMillis();

        // this will count the interactions and will be used to flush in batches
        int interactionCount = 0;

        for (Entry entry : entrySet.getEntries()) {
            InstitutionConverter institutionConverter = new InstitutionConverter();
            Institution institution;

            if (entry.getSource() != null) {
                institution = institutionConverter.psiToIntact(entry.getSource());
            } else {
                institution = intactContext.getInstitution();
            }

            ConversionCache.clear();

            // instead of converting/processing the whole Entry, we process the interactions to avoid memory exceptions
            InteractionConverter interactionConverter = new InteractionConverter(institution);

            for (psidev.psi.mi.xml.model.Interaction psiInteraction : entry.getInteractions()) {


                Interaction interaction = null;
                try {
                    interaction = interactionConverter.psiToIntact(psiInteraction);
                } catch (PsiConversionException e) {
                    throw new PersisterException("Problem converting PSI interaction: " + psiInteraction, e);
                }

                // mark the interaction to save or update
                if (log.isDebugEnabled()) log.debug("Persisting: " + interaction.getShortLabel());

                // commit the persistence
                PersisterStatistics localImportStats = importIntoIntact(interaction);

                importStats = merge(importStats, localImportStats);

                ConversionCache.clear();

                interactionCount++;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Processed " + interactionCount + " interactions (" + importStats.getPersistedCount(InteractionImpl.class, false) + " persisted, " + importStats.getDuplicatesCount(InteractionImpl.class, false) + " duplicated (ignored)) in " +
                      (System.currentTimeMillis() - startTime) + "ms");
        }

        return importStats;
    }

    private PersisterStatistics merge(PersisterStatistics stats1, PersisterStatistics stats2) {
        PersisterStatistics mergedStats = new PersisterStatistics();
        mergedStats.getDuplicatesMap().putAll(stats1.getDuplicatesMap());
        mergedStats.getDuplicatesMap().putAll(stats2.getDuplicatesMap());
        mergedStats.getPersistedMap().putAll(stats1.getPersistedMap());
        mergedStats.getPersistedMap().putAll(stats2.getPersistedMap());
        mergedStats.getMergedMap().putAll(stats1.getMergedMap());
        mergedStats.getMergedMap().putAll(stats2.getMergedMap());
        mergedStats.getTransientMap().putAll(stats1.getTransientMap());
        mergedStats.getTransientMap().putAll(stats2.getTransientMap());

        return mergedStats;
    }

    /**
     * Imports an IntactEntry into intact
     *
     * @param entry  the intact entry to import
     * 
     * @return report of the import
     *
     * @throws PersisterException thrown if there are problems persisting the data in the intact-model database
     */
    public void importIntoIntact(IntactEntry entry) throws PersisterException {
        persisterHelper.save(entry);
    }

    /**
     * Export to PSI XML
     *
     * @param intactEntries the entries to export
     *
     * @return an outputstream with the xml
     */
    public OutputStream exportToPsiXml(IntactEntry... intactEntries) {
        OutputStream os = new ByteArrayOutputStream();
        EntrySet entrySet = exportToEntrySet(intactEntries);

        PsimiXmlWriter writer = new PsimiXmlWriter(psiVersion, xmlForm);

        try {
            writer.write(entrySet, os);
        } catch (Exception e) {
            throw new ExportException("Exception creating PSI XML", e);
        }

        return os;
    }

    /**
     * Export to PSI XML
     *
     * @param intactEntries the entries to export
     * @param writer        the writer to use
     */
    public void exportToPsiXml(Writer writer, IntactEntry... intactEntries) {
        EntrySet entrySet = exportToEntrySet(intactEntries);

        PsimiXmlWriter psimiXmlWriter = new PsimiXmlWriter(psiVersion, xmlForm);

        try {
            psimiXmlWriter.write(entrySet, writer);
        } catch (Exception e) {
            throw new ExportException("Exception creating PSI XML", e);
        }
    }

    /**
     * Export to PSI XML
     *
     * @param intactEntries the entries to export
     * @param file          the file that will contain the xml
     */
    public void exportToPsiXml(File file, IntactEntry... intactEntries) {
        try {
            exportToPsiXml(new FileWriter(file), intactEntries);
        } catch (IOException e) {
            throw new ExportException("Exception creating PSI XML", e);
        }
    }

    public EntrySet exportToEntrySet(IntactEntry... intactEntries) {
        Collection<Entry> psiEntries = new ArrayList<Entry>();

        EntryConverter entryConverter = new EntryConverter();

        for (IntactEntry intactEntry : intactEntries) {
            Entry psiEntry = entryConverter.intactToPsi(intactEntry);
            psiEntries.add(psiEntry);
        }

        return createEntrySet(psiEntries);
    }

    private EntrySet createEntrySet(Collection<Entry> entry) {
        return new EntrySet(entry, 2, 5, 3);
    }

    /**
     * Gets the release dates from a PSI-MI XML file
     * @param xmlFile
     * @return
     */
    public List<DateTime> getReleaseDates(File xmlFile) throws IOException {
        return getReleaseDates(new FileInputStream(xmlFile));
    }

    /**
     * Gets the release dates from a PSI-MI XML InputStream
     * @param is
     * @return
     */
    public List<DateTime> getReleaseDates(InputStream is) throws IOException {
        final List<DateTime> releaseDates = new ArrayList<DateTime>();

        DefaultHandler handler = new DefaultHandler() {

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if (localName.equals("source")) {
                    final String releaseDateStr = attributes.getValue("releaseDate");
                    DateTime releaseDate = toDateTime(releaseDateStr);
                    releaseDates.add(releaseDate);
                }
            }
        };


        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(is), handler);
        } catch (Exception e) {
            throw new IntactException(e);
        }

        return releaseDates;
    }

    public DateTime getReleaseDate(Entry entry) {
        DateTime releaseDate = null;
        Source source = entry.getSource();

        if (source != null) {
            releaseDate = new DateTime(source.getReleaseDate());
        }

        return releaseDate;
    }

    protected DateTime toDateTime(String dateStr) {
        DateTime dateTime;
        try {
            if (dateStr.length() > 10) {
                //e.g. Wed Sep 20 11:54:49 PDT 2006
                String dateWithoutTimezone = dateStr.replaceAll("PDT ", "");
                DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE MMM dd hh:mm:ss yyyy");
                dateTime = fmt.parseDateTime(dateWithoutTimezone);
            } else {
                //e.g. 2007-12-27
                dateTime = new DateTime(dateStr);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalDateFormat("Illegal date format: "+dateStr, e);
        }

        return dateTime;
    }
}