package uk.ac.ebi.intact.dataexchange.psimi.exporter.species;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemStreamException;

/**
 * The SpeciesClassificationChunkReader is an ItemStrem and ItemReader which can read a set of files starting with a common publication id and extract interactions containing
 * species of interest.
 *
 * The reader returns a SpeciesFileUnit when it can gather all the files of a specific publication and species and it truncates files if too many interactions are involved.
 *
 * Some properties can be customized :
 * - the large scale value which is the maximum number of interactions allowed for one single file.
 * - the species folder name which is the name of the parent directory which contains the index files to read (not recursively however)
 * - the pmidFolderPath is the name of the pmid folder where to find the xml files to read
 * - the extension is the extension of the species files (by default is txt)
 * - the experimentSeparator is the separator of the experiment infos in the index files (by default is :)
 * - the error log name which is the name of the file where to log the error messages
 * - the publication filter which allows to filter files in a directory using file names (initialized by default)
 * - the fileNameGenerator and fileExtension which allows to recompose file names and allows to retrieve xml files (initialized by default)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/09/11</pre>
 */

public class SpeciesClassificationChunkReader extends SpeciesClassificationReader {
    private static final Log log = LogFactory.getLog(SpeciesClassificationChunkReader.class);

    /**
     * Maximum limit of interactions per entry
     */
    private int largeScale = 2000;

    private SpeciesFileUnit nextUnit=null;

    public SpeciesClassificationChunkReader(){
        super();
    }

    @Override
    protected SpeciesInteractionUnit createNewSpeciesInteractionUnit() {
        SpeciesInteractionUnit interactionUnit = new SpeciesInteractionUnit();
        interactionUnit.setSpecies(getCurrentSpecies());
        interactionUnit.setNegativeInteractionIterator(new InteractionEvidenceChunkIterator(this.largeScale, getCurrentNegativeInteractionIterator()));
        interactionUnit.setPositiveInteractionIterator(new InteractionEvidenceChunkIterator(this.largeScale,getCurrentPositiveInteractionIterator()));
        return interactionUnit;
    }

    public int getLargeScale() {
        return largeScale;
    }

    public void setLargeScale(int largeScale) {
        this.largeScale = largeScale;
    }

    @Override
    /**
     * It will read several species units
     */
    protected void readNextLine(){

        if (nextUnit != null){
            setCurrentSpeciesUnit(nextUnit);
        }
        else {
            setCurrentSpeciesUnit(getSpeciesIterator().next());
        }
        getCurrentSpeciesUnit().setDataSourceOptions(getDataSourceOptions());

        nextUnit = null;

        int numberInteractions = getCurrentSpeciesUnit().getNumberInteractions();
        setCurrentLine(getCurrentLine()+1);
        while (numberInteractions < largeScale && getSpeciesIterator().hasNext()){
            nextUnit = getSpeciesIterator().next();
            if (nextUnit.getNumberInteractions()+numberInteractions <= largeScale){
                // merge different publications in same chunk
                numberInteractions+=nextUnit.getNumberInteractions();
                getCurrentSpeciesUnit().getPositiveIndexedEntries().addAll(nextUnit.getPositiveIndexedEntries());
                getCurrentSpeciesUnit().getNegativeIndexedEntries().addAll(nextUnit.getNegativeIndexedEntries());
                setCurrentLine(getCurrentLine()+1);
            }
            else{
                break;
            }
        }

        setCurrentNegativeInteractionIterator(getCurrentSpeciesUnit().getNegativeInteractionIterator());
        setCurrentPositiveInteractionIterator(getCurrentSpeciesUnit().getPositiveInteractionIterator());
    }

    @Override
    public void close() throws ItemStreamException {
        this.nextUnit = null;
        super.close();
    }
}
