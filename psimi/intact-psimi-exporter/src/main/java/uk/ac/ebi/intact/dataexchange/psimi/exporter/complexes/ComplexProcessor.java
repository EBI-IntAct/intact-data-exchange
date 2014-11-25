package uk.ac.ebi.intact.dataexchange.psimi.exporter.complexes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemProcessor;
import psidev.psi.mi.jami.model.Complex;
import uk.ac.ebi.intact.dataexchange.psimi.exporter.util.FileNameGenerator;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;

/**
 * Processor which will convert a complex entry into ComplexFileEntry.
 *
 * The processor will use an entry converter for psi xml 254, compact.
 *
 * It will give a unique id for each processed object for the all step
 *
 * Some properties can be customized :
 * - fileName generator which generates the proper file name (initialized by default)
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>21/09/11</pre>
 */

public class ComplexProcessor implements ItemProcessor<IntactComplex, ComplexFileEntry> {

    /**
     * The fileName generator
     */
    private FileNameGenerator speciesNameGenerator;

    private static final Log log = LogFactory.getLog(ComplexProcessor.class);


    public ComplexProcessor(){
        speciesNameGenerator = new FileNameGenerator();
    }

    @Override
    public ComplexFileEntry process(IntactComplex item) throws Exception {

        log.info("Start processing complex : " + item.getShortName());

        ComplexFileEntry speciesFileEntry = null;

        // the names of the species
        String speciesName = this.speciesNameGenerator.createSpeciesName(item.getOrganism().getScientificName(), null, false);

        log.info("Process complex " + item.getShortName());

        // we can flush the current intact entry as the last experiment has been processed
        log.info("Create final file for " + item.getShortName());
        // create the species entry
        speciesFileEntry = flushIntactEntry(item.getShortName(), speciesName, item);

        return speciesFileEntry;
    }

    /**
     * Flush the current intact entry in a speciesFileEntry
     * @param interactionLabel
     * @param speciesName
     */
    private ComplexFileEntry flushIntactEntry(String interactionLabel, String speciesName, IntactComplex intactEntry){

        // name of the entry = interaction shortname
        String finalEntryName = speciesNameGenerator.createPublicationName(interactionLabel, null, false);

        // flush the current intact entry and start a new one
        return createSpeciesEntry(speciesName, finalEntryName, intactEntry);
    }

    private ComplexFileEntry createSpeciesEntry(String speciesName, String entryName, Complex intactEntry) {
        log.info("create complex entry : " + speciesName);
        log.info("Intact complex : " + entryName);

        // create a publication entry
        ComplexFileEntry speciesEntry = new ComplexFileEntry(speciesName, entryName, intactEntry);

        log.info("Finished complex entry : " + entryName);

        return speciesEntry;
    }

    public FileNameGenerator getSpeciesNameGenerator() {
        return speciesNameGenerator;
    }

    public void setSpeciesNameGenerator(FileNameGenerator speciesNameGenerator) {
        this.speciesNameGenerator = speciesNameGenerator;
    }
}
