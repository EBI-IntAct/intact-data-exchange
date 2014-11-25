package uk.ac.ebi.intact.dataexchange.psimi.exporter.complexes;

import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * The CompositeComplexWriter is ItemWriter which use several delegate writers to write each ComplexFileEntry a psi file.
 *
 * It will have a list of delegate writers
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22/09/11</pre>
 */

public class CompositeComplexWriter implements ItemWriter<ComplexFileEntry> {

    private List<ComplexWriter> delegates;

    public CompositeComplexWriter(){
        delegates = new ArrayList<ComplexWriter>();
    }

    @Override
    public void write(List<? extends ComplexFileEntry> items) throws Exception {

        for (ComplexWriter delegate : this.delegates){
            delegate.write(items);
        }
    }

    public List<ComplexWriter> getDelegates() {
        return delegates;
    }

    public void setDelegates(List<ComplexWriter> delegates) {
        if (delegates == null){
            this.delegates = new ArrayList<ComplexWriter>();
        }
        this.delegates = delegates;
    }
}
