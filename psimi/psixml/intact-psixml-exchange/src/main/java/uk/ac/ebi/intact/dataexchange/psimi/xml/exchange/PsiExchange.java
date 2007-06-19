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
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.business.IntactTransactionException;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersistenceContext;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.persister.standard.EntryPersister;
import uk.ac.ebi.intact.core.persister.standard.InteractionPersister;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.InteractionConverter;
import uk.ac.ebi.intact.model.IntactEntry;
import uk.ac.ebi.intact.model.Interaction;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

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
    private static final int IMPORT_BATCH_SIZE = 50;

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
            PersistenceContext.getInstance().setDryRun(dryRun);
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

        // instead of converting/processing the whole Entry, we process the interactions to avoid memory exceptions
        InteractionConverter interactionConverter = new InteractionConverter(context.getInstitution());

        // the persister of interactions instance
        InteractionPersister interactionPersister = InteractionPersister.getInstance();

        for (Entry entry : entrySet.getEntries()) {
            for (psidev.psi.mi.xml.model.Interaction psiInteraction : entry.getInteractions()) {
                Interaction interaction = interactionConverter.psiToIntact(psiInteraction);

                // mark the interaction to save or update
                interactionPersister.saveOrUpdate(interaction);

                // commit the interactions in batches into the database
                if (interactionCount > 0 && interactionCount % IMPORT_BATCH_SIZE == 0) {

                    // commit the persistence
                    interactionPersister.commit();

                    // restart a transaction
                    commitTransaction();
                    beginTransaction();
                }

                interactionCount++;
            }
        }

        // final commit for the last batch
        interactionPersister.commit();
        commitTransaction();

        if (log.isDebugEnabled()) {
            log.debug("Imported: " + interactionCount + " interactions in " + (System.currentTimeMillis() - startTime) + "ms");
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
        IntactContext context = IntactContext.getCurrentInstance();

        Collection<Entry> psiEntries = new ArrayList<Entry>();

        EntryConverter entryConverter = new EntryConverter(context.getInstitution());

        for (IntactEntry intactEntry : intactEntries) {
            Entry psiEntry = entryConverter.intactToPsi(intactEntry);
            psiEntries.add(psiEntry);
        }

        return createEntrySet(psiEntries);
    }

    private static EntrySet createEntrySet(Collection<Entry> entry) {
        return new EntrySet(entry, 2, 5, 3);
    }

}