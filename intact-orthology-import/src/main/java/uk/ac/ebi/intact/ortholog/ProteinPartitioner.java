package uk.ac.ebi.intact.ortholog;

import lombok.extern.log4j.Log4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j
public class ProteinPartitioner implements Partitioner {

    private final OrthologsProteinAssociation orthologsProteinAssociation;

    public ProteinPartitioner(OrthologsProteinAssociation orthologsProteinAssociation) {
        this.orthologsProteinAssociation = orthologsProteinAssociation;
    }

    @Override
    public Map<String, ExecutionContext> partition(int partSize) {
        List<Integer> proteinAcs = orthologsProteinAssociation.getProteinAcs();

        log.info("Starting new partitions");
        log.info("Number of partitions: " + partSize);
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        int totalCount = proteinAcs.size();
        int targetSize = (int) Math.ceil((double) totalCount / partSize);
        int startingIndex = 0;
        int endingIndex = targetSize;
        int number = 0;

        log.info("Proteins per partitions: " + targetSize);

        while (startingIndex < totalCount) {
            ExecutionContext ctxMap = new ExecutionContext();
            partitionMap.put("Thread:-" + number, ctxMap);

            if (endingIndex > totalCount) {
                endingIndex = totalCount;
            }

            ctxMap.putInt("startAc", proteinAcs.get(startingIndex));
            ctxMap.putInt("endAc", proteinAcs.get(endingIndex - 1));

            // Next start index is the previous end index
            // Next end index is increased by target size (number of proteins per partition)
            startingIndex = endingIndex;
            endingIndex += targetSize;

            number++;
        }
        log.info("END: Created " + partitionMap.size() + " partitions");
        return partitionMap;
    }
}
