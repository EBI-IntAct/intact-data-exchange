package uk.ac.ebi.intact.task.mitab.pmid;

import org.springframework.batch.item.file.transform.LineAggregator;

/**
 * It is a simple aggregator that will just return the line
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */

public class SimpleLineAggregator implements LineAggregator<String> {

    @Override
    public String aggregate(String item) {

        if (item.contains("\n")){
            int indexOfLastLineBreak = item.lastIndexOf("\n");
            String newItem = item.substring(0, indexOfLastLineBreak);

            return newItem;
        }

        return item;
    }
}
