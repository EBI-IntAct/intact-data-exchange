package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.model.Gene;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;

@Component("intactEnsemblGeneFetcher")
@Lazy
public class EnsemblGeneFetcher extends psidev.psi.mi.jami.bridges.ensembl.EnsemblGeneFetcher {

    @Autowired
    private EnricherContext enricherContext;

    public EnsemblGeneFetcher() throws BridgeFailedException {
    }

    @Override
    public Collection<Gene> fetchByIdentifier(String identifier) throws BridgeFailedException {
        EnricherCache ensembleGeneCache = enricherContext.getCacheManager().getCache("EnsembleGene");

        if (ensembleGeneCache.isKeyInCache(identifier)) {
            return (Collection<Gene>) ensembleGeneCache.get(identifier);
        }
        else{
            Collection<Gene> terms = super.fetchByIdentifier(identifier);
            ensembleGeneCache.put(identifier, terms);
            return terms;
        }
    }
}
