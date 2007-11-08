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
import org.apache.xerces.parsers.SAXParser;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Source;
import uk.ac.ebi.intact.business.IntactException;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterContext;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.persister.standard.EntryPersister;
import uk.ac.ebi.intact.core.persister.standard.InteractionPersister;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InstitutionConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InteractionConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.ConversionCache;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Imports/exports data between an IntAct-model database and PSI XML
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiExchange {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(PsiExchange.class);

    /**
     * Number of Interactions persisted per batch
     */
    private static final int IMPORT_BATCH_SIZE = 100;

    private PsiExchange() {
    }

    /**
     * Imports a stream containing PSI XML
     *
     * @param psiXmlStream the stream to read and import
     * @param dryRun       if true, don't modify the database (but simulate the upload and return the report)
     *
     * @return report of the import
     *
     * @throws PersisterException thrown if there are problems parsing the stream or persisting the data in the intact-model database
     */
    public static void importIntoIntact(InputStream psiXmlStream, boolean dryRun) throws PersisterException {
        PsimiXmlReader reader = new PsimiXmlReader();
        EntrySet entrySet = null;
        try {
            entrySet = reader.read(psiXmlStream);
        } catch (Exception e) {
            throw new PersisterException("Exception reading the PSI XML from an InputStream", e);
        }

        importIntoIntact(entrySet, dryRun);
    }

    /**
     * Imports an EntrySet into intact
     *
     * @param entrySet the psi set of entries to import
     * @param dryRun   if true, don't modify the database (but simulate the upload and return the report)
     *
     * @return report of the import
     *
     * @throws PersisterException thrown if there are problems persisting the data in the intact-model database
     */
    public static void importIntoIntact(EntrySet entrySet, boolean dryRun) throws PersisterException {
        IntactContext context = IntactContext.getCurrentInstance();

        if (dryRun) {
            PersisterContext.getInstance().setDryRun(dryRun);
        }

        // check if the transaction is active
        if (context.getDataContext().isTransactionActive()) {
            throw new IllegalStateException("To import to intact, the current transaction when calling the method must be inactive");
        }

        // some time for stats
        long startTime = System.currentTimeMillis();

        // this will count the interactions and will be used to flush in batches
        int interactionCount = 0;

        beginTransaction();

        // the persister of interactions instance
        InteractionPersister interactionPersister = InteractionPersister.getInstance();

        for (Entry entry : entrySet.getEntries()) {
            InstitutionConverter institutionConverter = new InstitutionConverter();
            Institution institution;

            if (entry.getSource() != null) {
                institution = institutionConverter.psiToIntact(entry.getSource());
            } else {
                institution = context.getInstitution();
            }

            // instead of converting/processing the whole Entry, we process the interactions to avoid memory exceptions
            InteractionConverter interactionConverter = new InteractionConverter(institution);

            // We use the following list to store the labels of the interactions between commits (when
            // the data is actually saved in the database).
            // If when processing a label, the label is already found in the list, a commit will be forced
            // so the interaction can be properly synced and get the corresponding prefix XXX-2
            List<String> interactionLabelsToCommit = new ArrayList<String>();

            for (psidev.psi.mi.xml.model.Interaction psiInteraction : entry.getInteractions()) {
                Interaction interaction = null;
                try {
                    interaction = interactionConverter.psiToIntact(psiInteraction);
                } catch (PsiConversionException e) {
                    throw new PersisterException("Problem converting PSI interaction: "+psiInteraction, e);
                } 

                if (interactionLabelsToCommit.contains(interaction.getShortLabel())) {
                    // commit the persistence
                    interactionPersister.commit();

                    // restart a transaction
                    commitTransaction();

                    if (log.isDebugEnabled()) {
                        log.debug("Forced commit due to a label already existing in this commit page: "+interaction.getShortLabel());
                    }

                    beginTransaction();

                    interactionLabelsToCommit.clear();
                }

                // mark the interaction to save or update
                if (log.isDebugEnabled()) log.debug("Marking to save or update: "+interaction.getShortLabel());
                interactionPersister.saveOrUpdate(interaction);

                interactionLabelsToCommit.add(interaction.getShortLabel());

                // commit the interactions in batches into the database
                if (interactionCount > 0 && interactionCount % IMPORT_BATCH_SIZE == 0) {

                    // commit the persistence
                    interactionPersister.commit();

                    commitTransaction();

                    // restart a transaction
                    beginTransaction();

                    interactionLabelsToCommit.clear();
                }

                interactionCount++;
            }
        }

        ConversionCache.clear();

        // final commit for the last batch
        interactionPersister.commit();
        commitTransaction();

        if (log.isDebugEnabled()) {
            log.debug("Imported: " + interactionCount + " interactions (less if duplicates were found) in " + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    private static void beginTransaction() {
        IntactContext.getCurrentInstance().getDataContext().beginTransaction();
    }

    private static void commitTransaction() throws PersisterException {
        try {
            IntactContext.getCurrentInstance().getDataContext().commitTransaction();
        } catch (IntactTransactionException e) {
            throw new PersisterException(e);
        }
    }

    /**
     * Imports an IntactEntry into intact
     *
     * @param entry  the intact entry to import
     * @param dryRun if true, don't modify the database (but simulate the upload and return the report)
     *
     * @return report of the import
     *
     * @throws PersisterException thrown if there are problems persisting the data in the intact-model database
     */
    public static void importIntoIntact(IntactEntry entry, boolean dryRun) throws PersisterException {
        EntryPersister entryPersister = EntryPersister.getInstance(dryRun);
        entryPersister.saveOrUpdate(entry);
        entryPersister.commit();
    }


    /**
     * Export to PSI XML
     *
     * @param intactEntries the entries to export
     *
     * @return an outputstream with the xml
     */
    public static OutputStream exportToPsiXml(IntactEntry... intactEntries) {
        OutputStream os = new ByteArrayOutputStream();
        EntrySet entrySet = exportToEntrySet(intactEntries);

        PsimiXmlWriter writer = new PsimiXmlWriter();

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
    public static void exportToPsiXml(Writer writer, IntactEntry... intactEntries) {
        EntrySet entrySet = exportToEntrySet(intactEntries);

        PsimiXmlWriter psimiXmlWriter = new PsimiXmlWriter();

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
    public static void exportToPsiXml(File file, IntactEntry... intactEntries) {
        try {
            exportToPsiXml(new FileWriter(file), intactEntries);
        } catch (IOException e) {
            throw new ExportException("Exception creating PSI XML", e);
        }
    }

    public static EntrySet exportToEntrySet(IntactEntry... intactEntries) {
        Collection<Entry> psiEntries = new ArrayList<Entry>();

        EntryConverter entryConverter = new EntryConverter();

        for (IntactEntry intactEntry : intactEntries) {
            Entry psiEntry = entryConverter.intactToPsi(intactEntry);
            psiEntries.add(psiEntry);
        }

        return createEntrySet(psiEntries);
    }

    private static EntrySet createEntrySet(Collection<Entry> entry) {
        return new EntrySet(entry, 2, 5, 3);
    }


    /**
     * Gets the release dates from a PSI-MI XML file
     * @param xmlFile
     * @return
     */
    public static List<DateTime> getReleaseDates(File xmlFile) throws IOException {
        return getReleaseDates(new FileInputStream(xmlFile));
    }

    /**
     * Gets the release dates from a PSI-MI XML InputStream
     * @param is
     * @return
     */
    public static List<DateTime> getReleaseDates(InputStream is) throws IOException {
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

        SAXParser parser = new SAXParser();
        parser.setContentHandler(handler);

        try {
            parser.parse(new InputSource(is));
        } catch (SAXException e) {
            throw new IntactException(e);
        }

        return releaseDates;
    }

    public static DateTime getReleaseDate(Entry entry) {
        DateTime releaseDate = null;
        Source source = entry.getSource();

        if (source != null) {
            releaseDate = new DateTime(source.getReleaseDate());
        }

        return releaseDate;
    }

    protected static DateTime toDateTime(String dateStr) {
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