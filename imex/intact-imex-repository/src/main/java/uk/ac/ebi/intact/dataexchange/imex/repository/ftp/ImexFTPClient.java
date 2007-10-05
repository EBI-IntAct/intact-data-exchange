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
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexFTPClient {

    /**
     * Sets up a logger for that class.
     */
    private static final Log log = LogFactory.getLog(ImexFTPClient.class);

    private FTPClient ftpClient;
    private String host;
    private String folder;

    protected ImexFTPClient(String host, String folder) {
        this.host = host;
        this.folder = normalizeFolder(folder);

        ftpClient = new FTPClient();
    }

    private static String normalizeFolder(String folder) {
        if (!folder.startsWith("/")) {
            folder = "/"+folder;
        }

        if (!folder.endsWith("/")) {
            folder = folder+"/";
        }

        return folder;
    }

    public void connect() throws IOException {
        if (log.isDebugEnabled()) log.debug("Connecting to: " + host);

        ftpClient.connect(host);
        if (log.isDebugEnabled()) log.debug("\tReply: " + ftpClient.getReplyString());

        if (log.isDebugEnabled()) log.debug("Login as anonymous");
        ftpClient.login("anonymous", "");
        if (log.isDebugEnabled()) log.debug("\tReply: " + ftpClient.getReplyString());
    }

    public List<ImexFTPFile> listFiles() throws IOException {
        checkConnected();

        if (log.isDebugEnabled()) log.debug("Listing files of folder: "+folder);

        FTPFile[] baseFiles = ftpClient.listFiles(folder);

        List<ImexFTPFile> allFiles = new ArrayList<ImexFTPFile>();

        for (FTPFile file : baseFiles) {
            if (file.isDirectory() && file.getName().matches("\\d{4}")) {
                allFiles.addAll(listFilesByYear(Integer.valueOf(file.getName())));
            }
        }

        return allFiles;
    }

    public List<ImexFTPFile> listFilesByYear(int year) throws IOException {
        checkConnected();

        if (log.isDebugEnabled()) log.debug("Listing files for year: "+year);
        
        FTPFile[] ftpFiles = ftpClient.listFiles(folder+"/"+year);

        List<ImexFTPFile> files = new ArrayList<ImexFTPFile>();

        for (FTPFile ftpFile : ftpFiles) {
            ImexFTPFile imexFtpFile = new ImexFTPFile(ftpFile, host, folder, year);
            if (imexFtpFile.isFile() && imexFtpFile.getName().endsWith(".xml.gz")) {
                files.add(imexFtpFile);
            }
        }

        return files;
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    public void disconnect() throws IOException {
        ftpClient.disconnect();
    }

    protected void checkConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("FTPClient is not connected to the FTP host. Call FTPClient.connect() first");
        }
    }

    protected String getFolder() {
        return folder;
    }

    protected FTPClient getFtpClient() {
        return ftpClient;
    }

    protected String getHost() {
        return host;
    }
}