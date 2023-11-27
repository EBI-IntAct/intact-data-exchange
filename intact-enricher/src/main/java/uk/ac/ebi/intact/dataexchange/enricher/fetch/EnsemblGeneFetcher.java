package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;

@Component("intactEnsemblGeneFetcher")
@Lazy
public class EnsemblGeneFetcher extends psidev.psi.mi.jami.bridges.ensembl.EnsemblGeneFetcher {
    public EnsemblGeneFetcher() throws BridgeFailedException {
    }
}
