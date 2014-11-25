package uk.ac.ebi.intact.dataexchange.psimi.exporter.pmid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(CompositePublicationFileEntryWriter.class);

    private List<PublicationFileEntryWriter> delegates;

    public CompositePublicationFileEntryWriter(){
        delegates = new ArrayList<PublicationFileEntryWriter>();
    }

    @Override
    public void write(List<? extends SortedSet<PublicationFileEntry>> items) throws Exception {

        for (PublicationFileEntryWriter delegate : this.delegates){
            delegate.write(items);
        }
    }

    public List<PublicationFileEntryWriter> getDelegates() {
        return delegates;
    }

    public void setDelegates(List<PublicationFileEntryWriter> delegates) {
        if (delegates == null){
            this.delegates = new ArrayList<PublicationFileEntryWriter>();
        }
        this.delegates = delegates;
    }
}
