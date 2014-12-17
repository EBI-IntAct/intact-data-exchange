package uk.ac.ebi.intact.dataexchange.psimi.exporter.dataset;

import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

/**
 *  The CompositePublicationDatasetWriter is ItemWriter which use several delegate writers to write each PublicationDatasetUnit a psi file.
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/09/11</pre>
 */

public class CompositePublicationDatasetWriter implements ItemWriter<PublicationDatasetUnit> {

    private List<ItemWriter<PublicationDatasetUnit>> delegates;

    public CompositePublicationDatasetWriter(){
        delegates = new ArrayList<ItemWriter<PublicationDatasetUnit>>();
    }

    @Override
    public void write(List<? extends PublicationDatasetUnit> items) throws Exception {

        for (ItemWriter<PublicationDatasetUnit> delegate : this.delegates){
            delegate.write(items);
        }
    }

    public List<ItemWriter<PublicationDatasetUnit>> getDelegates() {
        return delegates;
    }

    public void setDelegates(List<ItemWriter<PublicationDatasetUnit>> delegates) {
        if (delegates == null){
            this.delegates = new ArrayList<ItemWriter<PublicationDatasetUnit>>();
        }
        this.delegates = delegates;
    }
}
