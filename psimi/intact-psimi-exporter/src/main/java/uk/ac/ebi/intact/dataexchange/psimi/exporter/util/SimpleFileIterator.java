package uk.ac.ebi.intact.dataexchange.psimi.exporter.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/10/11</pre>
 */

public class SimpleFileIterator implements ReleaseUnitIterator<FileUnit> {

    protected NameTruncation fileNameTruncation;
    protected String[] extensions;
    protected File directory;
    protected boolean recursive;

    protected Iterator<File> fileIterator;

    protected FileUnit lastProcessedFile;

    public SimpleFileIterator(File directory, String[] extensions, boolean recursive){
        this.recursive = recursive;
        this.extensions = extensions;

        if (directory == null){
            throw new IllegalArgumentException("The directory of a fileIterator must be not null");
        }
        else if (!directory.canRead()){
            throw new IllegalArgumentException("Impossible to read directory " + directory.getName());
        }
        else if (!directory.isDirectory()){
            this.directory = directory;
            fileIterator = new ArrayList<File>(0).iterator();
        }
        else {
            this.directory = directory;

            List<File> files = new ArrayList(FileUtils.listFiles(directory, extensions, recursive));
            Collections.sort(files);

            // initialises the iterator
            this.fileIterator = files.iterator();

            readNextFileUnit();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from this iterator");
    }

    private void readNextFileUnit(){
        if (this.fileIterator.hasNext()){
            File file = this.fileIterator.next();

            lastProcessedFile = new FileUnit(file.getName().substring(0, file.getName().lastIndexOf(".")));
            lastProcessedFile.getEntities().add(file);
        }
        else {
            this.lastProcessedFile = null;
        }
    }

    @Override
    public boolean hasNext() {

        return lastProcessedFile != null;
    }

    protected FileUnit createFileUnit(String commonName){
        return new FileUnit(commonName);
    }

    @Override
    public FileUnit next() {

        if (fileIterator == null || lastProcessedFile == null){
            throw new NoSuchElementException("Iterator contains no elements");
        }

        FileUnit result = lastProcessedFile;

        readNextFileUnit();

        return result;
    }
}
