package uk.ac.ebi.intact.dataexchange.psimi.exporter.pmid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The SinglePublicationInteractionComposite an ItemStream and ItemWriter which writes for each PublicationFileEntry a psi file.
 *
 * It will have a list of delegate writers
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22/09/11</pre>
 */

public class SinglePublicationInteractionCompositeWriter implements ItemWriter<Collection<PublicationFileEntry>> {

    private static final Log log = LogFactory.getLog(SinglePublicationInteractionCompositeWriter.class);

    private List<SinglePublicationInteractionWriter> delegates;

    public SinglePublicationInteractionCompositeWriter(){
        delegates = new ArrayList<SinglePublicationInteractionWriter>();
    }

    @Override
    public void write(List<? extends Collection<PublicationFileEntry>> items) throws Exception {

        for (SinglePublicationInteractionWriter delegate : this.delegates){
            delegate.write(items);
        }
    }

    public List<SinglePublicationInteractionWriter> getDelegates() {
        return delegates;
    }

    public void setDelegates(List<SinglePublicationInteractionWriter> delegates) {
        if (delegates == null){
            this.delegates = new ArrayList<SinglePublicationInteractionWriter>();
        }
        this.delegates = delegates;
    }
}
