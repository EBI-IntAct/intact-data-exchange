package uk.ac.ebi.intact.dataexchange.psimi.exporter.pmid;

import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * The CompositePublicationFileEntryWriter is ItemWriter which use several delegate writers to write each PublicationFileEntry a psi file.
 *
 * It will have a list of delegate writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22/09/11</pre>
 */

public class CompositePublicationFileEntryWriter implements ItemWriter<SortedSet<PublicationFileEntry>> {

    private List<ItemWriter<SortedSet<PublicationFileEntry>>> delegates;

    public CompositePublicationFileEntryWriter(){
        delegates = new ArrayList<ItemWriter<SortedSet<PublicationFileEntry>>>();
    }

    @Override
    public void write(List<? extends SortedSet<PublicationFileEntry>> items) throws Exception {

        for (ItemWriter<SortedSet<PublicationFileEntry>> delegate : this.delegates){
            delegate.write(items);
        }
    }

    public List<ItemWriter<SortedSet<PublicationFileEntry>>> getDelegates() {
        return delegates;
    }

    public void setDelegates(List<ItemWriter<SortedSet<PublicationFileEntry>>> delegates) {
        if (delegates == null){
            this.delegates = new ArrayList<ItemWriter<SortedSet<PublicationFileEntry>>>();
        }
        this.delegates = delegates;
    }
}
