package uk.ac.ebi.intact.util.uniprotExport.miscore.writer;

import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;

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

public class DRLineWriter extends AbstractWriter{

    public DRLineWriter(IntActInteractionClusterScore clusterScore, String fileName) throws IOException {
        super(clusterScore, fileName);
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

            writeDRLine(parent, numberInteractions);
            interactors.removeAll(processedInteractors);
        }
    }

    private void writeDRLine(String uniprotAc, int interactions) throws IOException {
        StringBuffer sb = new StringBuffer();

        sb.append("DR   ");
        sb.append("IntAct; ");
        sb.append(uniprotAc).append("; ");
        sb.append(interactions+".");
        sb.append(NEW_LINE);

        writer.write(sb.toString());
        writer.flush();
    }
}
