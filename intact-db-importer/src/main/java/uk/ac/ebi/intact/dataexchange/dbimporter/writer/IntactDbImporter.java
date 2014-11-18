package uk.ac.ebi.intact.dataexchange.dbimporter.writer;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.intact.jami.service.IntactService;

import java.util.List;
import java.util.logging.Logger;

/**
 * IntAct JAMI db importer
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>17/11/14</pre>
 */

public class IntactDbImporter<I> extends AbstractIntactDbImporter<I>{

    private IntactService<I> intactService;

    private Logger log = Logger.getLogger(IntactDbImporter.class.getName());

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);

        if (intactService == null){
            throw new IllegalStateException("The interaction service must be provided. ");
        }
    }

    @Override
    @Transactional(value = "jamiTransactionManager", propagation = Propagation.REQUIRED)
    public void write(List<? extends I> is) throws Exception {
        if (this.intactService == null){
            throw new IllegalStateException("The writer must have a non null interaction service");
        }
        if (getSynchronizerListener() == null){
            throw new IllegalStateException("The writer cannot write before calling the method open as it is not initialised");
        }

        this.intactService.saveOrUpdate(is);
    }

    public IntactService<I> getIntactService() {
        return intactService;
    }

    public void setIntactService(IntactService<I> intactService) {
        this.intactService = intactService;
    }
}
