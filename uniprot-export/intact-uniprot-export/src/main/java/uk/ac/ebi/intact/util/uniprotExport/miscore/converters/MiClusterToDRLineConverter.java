package uk.ac.ebi.intact.util.uniprotExport.miscore.converters;

import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.parameters.DRParameters;
import uk.ac.ebi.intact.util.uniprotExport.writers.DRLineWriter;
import uk.ac.ebi.intact.util.uniprotExport.writers.DRLineWriterImpl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>28/01/11</pre>
 */

public class MiClusterToDRLineConverter extends AbstractConverter {

    private DRLineWriter writer;
    public MiClusterToDRLineConverter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        super(clusterScore, fileName);
        this.writer = new DRLineWriterImpl(fileName);
    }

    public void write() throws IOException {
        Set<String> interactors = new HashSet(this.clusterScore.getInteractorMapping().keySet());
        Set<String> processedInteractors = new HashSet<String>();

        while (!interactors.isEmpty()){
            processedInteractors.clear();
            Iterator<String> interactorIterator = interactors.iterator();

            String interactorAc = interactorIterator.next();
            processedInteractors.add(interactorAc);

            String parent = interactorAc;
            if (interactorAc.contains("-")){
                parent = interactorAc.substring(0, interactorAc.indexOf("-"));
            }

            int numberInteractions = this.clusterScore.getInteractorMapping().get(interactorAc).size();

            while (interactorIterator.hasNext()){
                String nextInteractor = interactorIterator.next();

                if (nextInteractor.startsWith(parent)){
                    processedInteractors.add(nextInteractor);
                    numberInteractions += this.clusterScore.getInteractorMapping().get(nextInteractor).size();
                }
            }

            DRParameters parameter = new DRParameters(parent, numberInteractions);
            writer.writeDRLine(parameter);
            interactors.removeAll(processedInteractors);
        }
    }
}
