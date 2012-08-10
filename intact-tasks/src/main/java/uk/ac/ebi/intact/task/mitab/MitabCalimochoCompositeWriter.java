package uk.ac.ebi.intact.task.mitab;

import org.hupo.psi.calimocho.model.Row;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Composite writer using calimocho
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>10/08/12</pre>
 */

public class MitabCalimochoCompositeWriter implements ItemWriter<Collection<? extends Row>> {

    private List<ItemWriter<Row>> delegates;

    public void write(List<? extends Collection<? extends Row>> items) throws Exception {
        for (Collection<? extends Row> binaryInteraction : items) {
            for (ItemWriter<Row> delegate : delegates) {
                delegate.write(new ArrayList<Row>(binaryInteraction));
            }
        }
    }

    public void setDelegates(List<ItemWriter<Row>> delegates) {
        this.delegates = delegates;
    }
}
