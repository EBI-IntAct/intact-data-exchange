/*
 * Copyright 2006 The European Bioinformatics Institute.
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
package uk.ac.ebi.intact.dbutil;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.ControlledVocabularyRepository;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.checker.EntrySetChecker;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.model.EntrySetTag;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.parser.EntrySetParser;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.persister.EntrySetPersister;
import uk.ac.ebi.intact.application.dataConversion.psiUpload.util.report.MessageHolder;
import uk.ac.ebi.intact.dbutil.cv.PsiLoaderException;
import uk.ac.ebi.intact.dbutil.cv.UpdateCVs;
import uk.ac.ebi.intact.dbutil.cv.UpdateCVsConfig;
import uk.ac.ebi.intact.dbutil.cv.UpdateCVsReport;
import uk.ac.ebi.intact.util.UrlUtils;
import uk.ac.ebi.intact.util.filter.UrlFilter;
import uk.ac.ebi.intact.util.protein.BioSourceFactory;
import uk.ac.ebi.intact.util.protein.UpdateProteins;
import uk.ac.ebi.intact.util.protein.UpdateProteinsI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * Utilities to setup an intact database
 *
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DbUtils {

    private static final Logger log = Logger.getLogger(DbUtils.class);

    /**
     * Creates or updates the CvObjects in a database from a default intact.obo file included in this package
     */
    public static UpdateCVsReport createOrUpdateCvObjects(PrintStream output) throws PsiLoaderException, IOException {
        return createOrUpdateCvObjects(DbUtils.class.getResourceAsStream("intact.obo"), output, new UpdateCVsConfig());
    }

    /**
     * Creates or updates the CvObjects in a database from the url provided
     *
     * @param oboFileUrl a URL pointing to an OBO file
     */
    public static UpdateCVsReport createOrUpdateCvObjects(URL oboFileUrl, PrintStream output, UpdateCVsConfig config) throws PsiLoaderException, IOException {
        return createOrUpdateCvObjects(oboFileUrl.openStream(), output, config);
    }

    /**
     * Creates or updates the CvObjects in a database from an input stream with OBO format
     *
     * @param inputStream
     */
    public static UpdateCVsReport createOrUpdateCvObjects(InputStream inputStream, PrintStream output, UpdateCVsConfig config) throws PsiLoaderException, IOException {
        return UpdateCVs.load(inputStream, output, config);
    }

    public static void importPsi1Xml(URL psiXmlUrl) throws SAXException, IOException {
        importPsi1Xml(psiXmlUrl.openStream());
    }

    public static List<URL> importPsi1XmlFromFolderUrl(URL folderUrl, UrlFilter filter, boolean recursive) throws SAXException, IOException {
        List<URL> urlsToImport = UrlUtils.listFilesFromFolderUrl(folderUrl, filter, recursive);

        for (URL url : urlsToImport) {
            importPsi1Xml(url.openStream());
        }

        return urlsToImport;
    }

    public static void importPsi1Xml(InputStream is) throws SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        MessageHolder messages = MessageHolder.getInstance();

        // Parse the PSI file
        DocumentBuilder builder = null;

        try {
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }

        Document document = builder.parse(is);
        Element rootElement = document.getDocumentElement();

        EntrySetParser entrySetParser = new EntrySetParser();
        EntrySetTag entrySet = entrySetParser.process(rootElement);

        UpdateProteinsI proteinFactory = new UpdateProteins();
        BioSourceFactory bioSourceFactory = new BioSourceFactory();

        ControlledVocabularyRepository.check();

        // check the parsed model
        EntrySetChecker.check(entrySet, proteinFactory, bioSourceFactory);

        if (messages.checkerMessageExists()) {
            // display checker messages.
            MessageHolder.getInstance().printCheckerReport(System.err);
        } else {
            EntrySetPersister.persist(entrySet);

            if (messages.checkerMessageExists()) {
                // display persister messages.
                MessageHolder.getInstance().printPersisterReport(System.err);
            } else {
                log.debug("The data have been successfully saved in your Intact node.");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        URL url = new URL("ftp://ftp.ebi.ac.uk/pub/databases/intact/current/psi1/pmid/2006/");
        //URLConnection conn = url.openConnection();

        InputStream is = url.openStream();
        //InputStream is = conn.getInputStream();

        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = r.readLine()) != null) {
            System.out.println(line + "\n");
        }
    }

}
