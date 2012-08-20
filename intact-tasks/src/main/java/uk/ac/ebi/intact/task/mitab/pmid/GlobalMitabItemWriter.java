package uk.ac.ebi.intact.task.mitab.pmid;

import org.springframework.batch.item.file.FlatFileItemWriter;

/**
 * GlobalMitabItemWriter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>20/08/12</pre>
 */

public class GlobalMitabItemWriter extends FlatFileItemWriter<String> {

    private SimpleLineAggregator simpleLineAggregator;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (simpleLineAggregator == null){
            this.simpleLineAggregator = new SimpleLineAggregator();
            setLineAggregator(this.simpleLineAggregator);
        }

        super.afterPropertiesSet();

        // we don't want transactional
        setTransactional(false);
    }

    public SimpleLineAggregator getSimpleLineAggregator() {
        return simpleLineAggregator;
    }

    public void setSimpleLineAggregator(SimpleLineAggregator simpleLineAggregator) {
        this.simpleLineAggregator = simpleLineAggregator;
        setLineAggregator(simpleLineAggregator);
    }
}
