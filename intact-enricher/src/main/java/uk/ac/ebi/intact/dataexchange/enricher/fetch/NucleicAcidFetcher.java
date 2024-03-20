package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.model.NucleicAcid;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.enricher.cache.EnricherCache;

import java.util.Collection;

@Component("intactNucleicAcidFetcher")
@Lazy
public class NucleicAcidFetcher extends psidev.psi.mi.jami.bridges.rna.central.RNACentralFetcher {

    @Autowired
    private EnricherContext enricherContext;

    public NucleicAcidFetcher() throws BridgeFailedException {
    }

    @Override
    public Collection<NucleicAcid> fetchByIdentifier(String identifier) throws BridgeFailedException {
        EnricherCache nucleicAcidCache = enricherContext.getCacheManager().getCache("NucleicAcid");

        if (nucleicAcidCache.isKeyInCache(identifier)) {
            return (Collection<NucleicAcid>) nucleicAcidCache.get(identifier);
        }
        else{
            Collection<NucleicAcid> terms = super.fetchByIdentifier(identifier);
            nucleicAcidCache.put(identifier, terms);
            return terms;
        }
    }
}
