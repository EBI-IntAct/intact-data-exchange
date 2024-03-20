package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.model.NucleicAcid;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;

@Component("intactEnsemblNucleicAcidFetcher")
@Lazy
public class EnsemblNucleicAcidFetcher extends psidev.psi.mi.jami.bridges.ensembl.EnsemblNucleicAcidFetcher {

    @Autowired
    private EnricherContext enricherContext;

    public EnsemblNucleicAcidFetcher() throws BridgeFailedException {
    }

    @Override
    public Collection<NucleicAcid> fetchByIdentifier(String identifier) throws BridgeFailedException {
        EnricherCache ensembleNucleicAcidCache = enricherContext.getCacheManager().getCache("EnsemblNucleicAcid");

        if (ensembleNucleicAcidCache.isKeyInCache(identifier)) {
            return (Collection<NucleicAcid>) ensembleNucleicAcidCache.get(identifier);
        }
        else{
            Collection<NucleicAcid> terms = super.fetchByIdentifier(identifier);
            ensembleNucleicAcidCache.put(identifier, terms);
            return terms;
        }
    }
}
