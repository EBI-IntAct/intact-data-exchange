package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.PublicationFileFilter;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Iterator for species without splitting the results in chunks
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>18/10/11</pre>
 */

public class SpeciesClassificationIterator implements Iterator<SpeciesFileUnit> {

    private static final Log log = LogFactory.getLog(SpeciesClassificationIterator.class);

    private File speciesFile;
    private SpeciesFileUnit lastUnit;

    private Iterator<String> lineIterator;
    private String nextLine;
    private String currentFileInfo;
    private String taxidSeparator;
    private int currentTaxid;
    private String speciesName;
    private int currentNumberInteractions;

    private Set<File> positiveXmlInputs;
    private Set<File> negativeXmlInputs;

    private PublicationFileFilter publicationFilter;
    private FileNameGenerator fileNameGenerator;
    private File pmidFolder;
    private String fileExtension;

    public SpeciesClassificationIterator(File speciesFile, String taxidSeparator, String speciesName,
                                         Set<File> positiveXmlInputs, Set<File> negativeXmlInputs,
                                         PublicationFileFilter fileFilter, FileNameGenerator fileNameGenerator,
                                         File pmidFolder, String fileExtension) throws IOException {

        if (speciesFile == null){
            throw new IllegalArgumentException("The species file of a SpeciesClassificationIterator must be not null");
        }
        if (speciesName == null){
            throw new IllegalArgumentException("The species name of a SpeciesClassificationIterator must be not null");
        }
        if (positiveXmlInputs == null){
            throw new IllegalArgumentException("The set of files containing positive interactions must be not null");
        }
        if (negativeXmlInputs == null){
            throw new IllegalArgumentException("The set of files containing negative interactions must be not null");
        }
        if (taxidSeparator == null){
            throw new IllegalArgumentException("The taxid separator of a SpeciesClassificationIterator must be not null");
        }
        if (fileFilter == null){
            throw new IllegalArgumentException("The publication File filter of a SpeciesClassificationIterator must be not null");
        }
        if (fileNameGenerator == null){
            throw new IllegalArgumentException("The publication File name generator of a SpeciesClassificationIterator must be not null");
        }
        if (pmidFolder == null){
            throw new IllegalArgumentException("The pmid parent folder of a SpeciesClassificationIterator must be not null");
        }
        if (fileExtension == null){
            throw new IllegalArgumentException("The file extension of a SpeciesClassificationIterator must be not null");
        }

        this.speciesFile = speciesFile;
        this.taxidSeparator = taxidSeparator;
        this.speciesName = speciesName;
        this.positiveXmlInputs = positiveXmlInputs;
        this.negativeXmlInputs = negativeXmlInputs;
        this.publicationFilter = fileFilter;
        this.fileNameGenerator = fileNameGenerator;
        this.pmidFolder = pmidFolder;
        this.fileExtension = fileExtension;

        initializeLineIterator(speciesFile);

        if (this.lineIterator.hasNext()){
            this.nextLine = this.lineIterator.next();
            readNextSpeciesUnits();
        }
        else {
            this.nextLine = null;
            this.lastUnit = null;
        }
    }

    @Override
    public boolean hasNext() {
        return lastUnit != null;
    }

    @Override
    public SpeciesFileUnit next() {
        if (lastUnit == null){
            throw new NoSuchElementException("Iterator contains no elements");
        }

        SpeciesFileUnit releaseUnit = lastUnit;

        readNextSpeciesUnits();

        return releaseUnit;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from this iterator");
    }

    private boolean readLine(String line){
        String[] info = extractValuesFrom(line);
        if (info != null){
            String nextFileInfo = info[0];
            String nextTaxid = info[1];
            String nextInteraction = info[2];

            try {
                currentTaxid = Integer.parseInt(nextTaxid);
            }
            catch (NumberFormatException n){
                log.error(speciesFile.getName());
                log.error(" : skip the bad formatted line. " + info[1] + " is not a valid number.");
                log.error(line);
                log.error("\n");

                currentTaxid = 0;
            }

            try {
                currentNumberInteractions = Integer.parseInt(nextInteraction);
            }
            catch (NumberFormatException n){
                log.error(speciesFile.getName());
                log.error(" : skip the bad formatted line. " + info[2] + " is not a valid number.");
                log.error(line);
                log.error("\n");

                currentNumberInteractions = 0;
            }

            currentFileInfo = nextFileInfo;
            return true;
        }
        else{
            currentFileInfo = null;
            currentTaxid = 0;
        }

        return false;
    }

    private String [] extractValuesFrom (String line){
        String [] value = null;

        if (line.contains(taxidSeparator)){
            String [] lineInfo = line.split(taxidSeparator);

            if (lineInfo.length == 3){
                value = lineInfo;
            }
            else {
                log.error(speciesFile.getName());
                log.error(" : skip the bad formatted line. " + line + " does not contain info following the format fileInfo" + taxidSeparator + "taxid"+taxidSeparator+"number interactions");
                log.error(line);
                log.error("\n");
            }
        }
        else {
            log.error(speciesFile.getName());
            log.error(" : skip the bad formatted line. " + line + " does not contain info following the format fileInfo" + taxidSeparator + "taxid"+taxidSeparator+"number interactions");
            log.error(line);
            log.error("\n");
        }

        return value;
    }

    private void initializeLineIterator(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        LinkedList<String> lines = new LinkedList<String>();

        String line = reader.readLine();

        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }

        this.lineIterator = lines.iterator();
    }

    private void readNextSpeciesUnits(){
        this.positiveXmlInputs.clear();
        this.negativeXmlInputs.clear();

        if (nextLine != null){

            // skip lines that are not valid
            while (!readLine(nextLine) && lineIterator.hasNext()){
                nextLine = lineIterator.next();
            }

            if (currentFileInfo == null){
                this.nextLine = null;
                this.lastUnit = null;
            }
            else {
                this.lastUnit = createSpeciesFileUnit();

                if (lineIterator.hasNext()){
                   nextLine = lineIterator.next();
                }
                else{
                    nextLine = null;
                }
            }
        }
        else {
            this.lastUnit = null;
        }

        this.currentFileInfo = null;
    }

    private SpeciesFileUnit createSpeciesFileUnit() {
        // init interaction iterators if not done yet
        SpeciesFileUnit fileUnit = new SpeciesFileUnit();
        fileUnit.setSpecies(this.speciesName);
        fileUnit.setTaxid(this.currentTaxid);
        fileUnit.setNumberInteractions(this.currentNumberInteractions);

        extractInputFilesForLine(this.currentFileInfo);

        // collect positive entries
        if (!positiveXmlInputs.isEmpty()){

            fileUnit.getPositiveIndexedEntries().addAll(positiveXmlInputs);
        }
        // collect negative entries
        if (!negativeXmlInputs.isEmpty()){

            fileUnit.getNegativeIndexedEntries().addAll(negativeXmlInputs);
        }
        if (positiveXmlInputs.isEmpty() && negativeXmlInputs.isEmpty()) {
            log.error(speciesName);
            log.error(" : Impossible to retrieve files for " + speciesName + ", negative xml inputs " + negativeXmlInputs.size()
                    + ", positive xml inputs " + positiveXmlInputs.size());
            log.error("\n");
        }

        log.info("Read species " +  speciesName +", negative xml inputs " + negativeXmlInputs.size() + ", positive xml inputs "
                + positiveXmlInputs.size());

        return fileUnit;
    }

    private void extractInputFilesForLine(String fileInfo) {
        // we have the year folder where to look for the files
        if (fileInfo.contains(File.separator)){
            String year = null;
            String pubmedId = null;

            String[] fileDescription = fileInfo.split(File.separator);
            year = fileDescription[0];
            pubmedId = fileDescription[1];

            publicationFilter.setPublicationId(fileNameGenerator.replaceBadCharactersFor(pubmedId));

            File yearDirectory = new File(this.pmidFolder, year);
            if (yearDirectory.exists() && yearDirectory.canRead()){
                File[] matches = yearDirectory.listFiles(publicationFilter);

                for (File match : matches){
                    if (match.getName().endsWith(this.fileNameGenerator.getNegativeTag() + fileExtension)){
                        negativeXmlInputs.add(match);
                    }
                    // we exclude large scale experiment files
                    else {
                        positiveXmlInputs.add(match);
                    }
                }
            }
            else {
                log.error(fileInfo);
                log.error(":");
                log.error(" impossible to retrieve or read these files");
            }
        }
        // no year, we will try to look in the current directory
        else {
            File[] matches = pmidFolder.listFiles(publicationFilter);

            for (File match : matches){
                if (match.getName().endsWith(this.fileNameGenerator.getNegativeTag() + fileExtension)){
                    negativeXmlInputs.add(match);
                }
                // we exclude large scale experiment files
                else {
                    positiveXmlInputs.add(match);
                }
            }
        }
    }
}
