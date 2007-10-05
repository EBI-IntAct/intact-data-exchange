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
package uk.ac.ebi.intact.dataexchange.imex.repository.ftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexMintFTPClient extends ImexFTPClient {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ImexMintFTPClient.class);

    public ImexMintFTPClient(String host, String folder) {
        super(host, folder);
    }

    @Override
    public List<ImexFTPFile> listFiles() throws IOException {
        checkConnected();

        if (log.isDebugEnabled()) log.debug("Listing files of folder: "+getFolder());

        FTPFile[] baseFiles = getFtpClient().listFiles(getFolder());

        Set<Integer> availableYears = new HashSet<Integer>();

        for (FTPFile file : baseFiles) {
            final String filename = file.getName();
            if (filename.matches("\\d{4}.*")) {
                availableYears.add(Integer.valueOf(filename.substring(0,4)));
            }
        }

        List<ImexFTPFile> allFiles = new ArrayList<ImexFTPFile>();

        for (int year : availableYears) {
            allFiles.addAll(listFilesByYear(year));
        }

        return allFiles;
    }

    @Override
    public List<ImexFTPFile> listFilesByYear(int year) throws IOException {
        checkConnected();

        if (log.isDebugEnabled()) log.debug("Listing files for year: "+year);

        FTPFile[] baseFiles = getFtpClient().listFiles(getFolder());

        List<ImexFTPFile> files = new ArrayList<ImexFTPFile>();

        for (FTPFile ftpFile : baseFiles) {
            final String filename = ftpFile.getName();
            if (filename.startsWith(String.valueOf(year))) {
                ImexFTPFile imexFtpFile = new ImexFTPFile(ftpFile, getHost(), getFolder(), year);

                if (imexFtpFile.isFile() && imexFtpFile.getName().endsWith(".xml.gz") ) {
                    files.add(imexFtpFile);
                }
            }
        }

        return files;
    }
}