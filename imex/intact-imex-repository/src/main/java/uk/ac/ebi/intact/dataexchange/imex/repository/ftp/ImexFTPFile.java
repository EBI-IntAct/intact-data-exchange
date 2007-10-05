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

import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ImexFTPFile {

    private FTPFile ftpFile;
    private URL url;

    protected ImexFTPFile(FTPFile ftpFile, String host, String folder, int year) {
        this.ftpFile = ftpFile;

        try {
            this.url = new URL("ftp://"+host+folder+year+"/"+ftpFile.getName());
        } catch (MalformedURLException e) {
            throw new ImexFTPException(e);
        }
    }

    public URL getUrl() {
        return url;
    }

    public InputStream openStream() throws IOException {
        return new GZIPInputStream(url.openStream());
    }

    public File toFile() throws IOException {
        File tempFile = File.createTempFile(ftpFile.getName()+"-", ".xml");
        FileWriter writer = new FileWriter(tempFile);

        BufferedReader in = new BufferedReader(new InputStreamReader(openStream()));
        String line;
        while ((line = in.readLine()) != null) {
            writer.write(line + "\n");
        }

        writer.close();

        return tempFile;
    }

    ////////////////////////////////////
    // Delegated methods from FTPFile

    public String getGroup() {
        return ftpFile.getGroup();
    }

    public int getHardLinkCount() {
        return ftpFile.getHardLinkCount();
    }

    public String getLink() {
        return ftpFile.getLink();
    }

    public String getName() {
        return ftpFile.getName();
    }

    public String getRawListing() {
        return ftpFile.getRawListing();
    }

    public long getSize() {
        return ftpFile.getSize();
    }

    public Calendar getTimestamp() {
        return ftpFile.getTimestamp();
    }

    public int getType() {
        return ftpFile.getType();
    }

    public String getUser() {
        return ftpFile.getUser();
    }

    public boolean hasPermission(int i, int i1) {
        return ftpFile.hasPermission(i, i1);
    }

    public boolean isDirectory() {
        return ftpFile.isDirectory();
    }

    public boolean isFile() {
        return ftpFile.isFile();
    }

    public boolean isSymbolicLink() {
        return ftpFile.isSymbolicLink();
    }

    public boolean isUnknown() {
        return ftpFile.isUnknown();
    }

    public void setGroup(String s) {
        ftpFile.setGroup(s);
    }

    public void setHardLinkCount(int i) {
        ftpFile.setHardLinkCount(i);
    }

    public void setLink(String s) {
        ftpFile.setLink(s);
    }

    public void setName(String s) {
        ftpFile.setName(s);
    }

    public void setPermission(int i, int i1, boolean b) {
        ftpFile.setPermission(i, i1, b);
    }

    public void setRawListing(String s) {
        ftpFile.setRawListing(s);
    }

    public void setSize(long l) {
        ftpFile.setSize(l);
    }

    public void setTimestamp(Calendar calendar) {
        ftpFile.setTimestamp(calendar);
    }

    public void setType(int i) {
        ftpFile.setType(i);
    }

    public void setUser(String s) {
        ftpFile.setUser(s);
    }

    @Override
    public String toString() {
        return ftpFile.toString();
    }
}