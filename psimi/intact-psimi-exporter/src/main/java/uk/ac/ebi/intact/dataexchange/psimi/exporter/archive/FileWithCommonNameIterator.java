package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import org.apache.commons.io.FileUtils;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileUnit;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.NameTruncation;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.ReleaseUnitIterator;

import java.io.File;
import java.util.*;

/**
 * This iterator will iterate through files with common names
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/10/11</pre>
 */

public class FileWithCommonNameIterator implements ReleaseUnitIterator<FileUnit> {

    protected NameTruncation fileNameTruncation;
    protected String[] extensions;
    protected File directory;
    protected boolean recursive;

    protected Iterator<File> fileIterator;
    private File lastProcessedFile;

    protected FileUnit lastProcessedFileUnit;

    public FileWithCommonNameIterator(File directory, NameTruncation nameTruncation, String[] extensions, boolean recursive){
        this.recursive = recursive;
        this.extensions = extensions;
        this.fileNameTruncation = nameTruncation;

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

            List<File> files = new ArrayList<File>(FileUtils.listFiles(directory, extensions, recursive));
            Collections.sort(files);

            // initialises the iterator
            this.fileIterator = files.iterator();

            if (this.fileIterator.hasNext()){
                this.lastProcessedFile = this.fileIterator.next();
                readNextFileUnit();
            }
            else {
                this.lastProcessedFileUnit = null;
            }
        }
    }

    @Override
    public boolean hasNext() {

        return lastProcessedFileUnit != null;
    }

    protected FileUnit createFileUnit(String commonName){
        return new FileUnit(commonName);
    }

    @Override
    public FileUnit next() {

        if (fileIterator == null || lastProcessedFileUnit == null){
            throw new NoSuchElementException("Iterator contains no elements");
        }

        FileUnit fileUnit = lastProcessedFileUnit;

        readNextFileUnit();

        return fileUnit;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from this iterator");
    }

    protected String extractCommonName(File firstFile) {
        String commonName;

        if (fileNameTruncation == null){
            commonName = firstFile.getName().substring(0, firstFile.getName().lastIndexOf("."));
        }
        else {
            commonName = fileNameTruncation.truncate(firstFile.getName().substring(0, firstFile.getName().lastIndexOf(".")));
        }

        return commonName;
    }

    protected void readNextFileUnit(){
        if (lastProcessedFile != null){
            File firstFile = lastProcessedFile;

            String commonName = extractCommonName(firstFile);

            lastProcessedFileUnit = createFileUnit(commonName);

            lastProcessedFileUnit.getEntities().add(firstFile);

            boolean needToProcessNextLine = true;

            if (!fileIterator.hasNext()){
                lastProcessedFile = null;
                needToProcessNextLine = false;
            }

            while (fileIterator.hasNext() && needToProcessNextLine){
                File secondFile = fileIterator.next();

                String name = extractCommonName(secondFile);

                if (name.equalsIgnoreCase(commonName)){
                    lastProcessedFile = null;
                    lastProcessedFileUnit.getEntities().add(secondFile);
                }
                else {
                    lastProcessedFile = secondFile;
                    needToProcessNextLine = false;
                }
            }
        }
        else {
            lastProcessedFileUnit = null;
        }
    }
}
