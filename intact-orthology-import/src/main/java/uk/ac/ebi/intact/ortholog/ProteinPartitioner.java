package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import uk.ac.ebi.intact.jami.model.extension.IntactProtein;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j
public class ProteinPartitioner implements Partitioner {

    private final OrthologsProteinAssociation orthologsProteinAssociation;
//    private final Collection<IntactProtein> intactProteins;

    public ProteinPartitioner(OrthologsProteinAssociation orthologsProteinAssociation) {
        this.orthologsProteinAssociation = orthologsProteinAssociation;
    }

    @Override
    public Map<String, ExecutionContext> partition(int partSize) {
//        Collection<IntactProtein> intactProteins = orthologsProteinAssociation.getIntactProtein();

        List<Integer> proteinAcs = orthologsProteinAssociation.getProteinAcs();

        log.info("Starting new partition");
        Map<String,ExecutionContext> partitionMap = new HashMap<String,ExecutionContext>();

        int totalCount = proteinAcs.size();
        long targetSize=(long)Math.ceil((double)totalCount/partSize);
        int startingIndex=0;
        int endingIndex=99;
        int number=0;


        while(startingIndex<=(totalCount-1)){
            ExecutionContext ctxMap = new ExecutionContext();
            partitionMap.put("Thread:-"+number, ctxMap);

            if(endingIndex>=(totalCount-1)){
                endingIndex=(totalCount-1);
            }

            ctxMap.putInt("startAc",proteinAcs.get((int)startingIndex));
            ctxMap.putInt("endAc",proteinAcs.get((int)endingIndex));
            startingIndex+= (int) targetSize;
            endingIndex+= (int) targetSize;

            number++;
        }
        log.debug("END: Created Partitions of size: "+ partitionMap.size());
        return partitionMap;
    }
}
