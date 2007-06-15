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

import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.PsimiXmlWriter;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.core.persister.PersisterException;
import uk.ac.ebi.intact.core.persister.standard.EntryPersister;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared.EntryConverter;
import uk.ac.ebi.intact.model.IntactEntry;

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

        EntryConverter converter = new EntryConverter(context.getInstitution());

        for (Entry entry : entrySet.getEntries()) {
            IntactEntry intactEntry = converter.psiToIntact(entry);
            importIntoIntact(intactEntry, dryRun);

            context.getDataContext().flushSession();
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
        IntactContext context = IntactContext.getCurrentInstance();

        EntryPersister entryPersister = EntryPersister.getInstance(dryRun);
        entryPersister.saveOrUpdate(entry);
    }

    /**
     * Export to PSI XML
     * @param intactEntries the entries to export
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
     * @param intactEntries the entries to export
     * @param writer the writer to use
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
     * @param intactEntries the entries to export
     * @param file the file that will contain the xml
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