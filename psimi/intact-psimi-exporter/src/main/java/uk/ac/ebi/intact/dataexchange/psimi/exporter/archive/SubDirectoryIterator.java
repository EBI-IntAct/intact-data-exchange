package uk.ac.ebi.intact.dataexchange.psimi.exporter.archive;

import java.io.File;
import java.util.*;

/**
 * This iterator will iterate the subdirectories of a folder
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/10/11</pre>
 */

public class SubDirectoryIterator implements ReleaseUnitIterator<FileUnit>{
    protected File directory;
    protected boolean recursive;

    protected Iterator<File> fileIterator;

    protected FileUnit lastProcessedDirectory;

    public SubDirectoryIterator(File directory, boolean recursive){
        this.recursive = recursive;

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

            List<File> files = Arrays.asList(directory.listFiles());
            Collections.sort(files);

            // initialises the iterator
            this.fileIterator = files.iterator();

            readNextFileUnit();
        }
    }

    @Override
    public boolean hasNext() {

        return lastProcessedDirectory != null;
    }

    protected FileUnit createFileUnit(String commonName){
        return new FileUnit(commonName);
    }

    @Override
    public FileUnit next() {

        if (fileIterator == null || lastProcessedDirectory == null){
            throw new NoSuchElementException("Iterator contains no elements");
        }

        FileUnit fileUnit = lastProcessedDirectory;

        readNextFileUnit();

        return fileUnit;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from this iterator");
    }

    private void readNextFileUnit(){
        if (fileIterator.hasNext()){
            File firstFile = fileIterator.next();

            // we keep reading the dataset directory until we have a subDirectory or we finished to read all the files of the dataset directory
            while (fileIterator.hasNext() && !firstFile.isDirectory()){
                firstFile = fileIterator.next();
            }

            if (firstFile.isDirectory()){
                lastProcessedDirectory = createFileUnit(firstFile.getAbsolutePath());
                lastProcessedDirectory.getEntities().add(firstFile);
            }
            else {
                lastProcessedDirectory = null;
            }
        }
        else {
            this.lastProcessedDirectory = null;
        }
    }
}
