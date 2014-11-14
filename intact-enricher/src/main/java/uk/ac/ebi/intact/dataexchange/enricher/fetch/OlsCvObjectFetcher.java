/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.enricher.fetch;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.bridges.exception.BridgeFailedException;
import psidev.psi.mi.jami.bridges.ols.OlsCvTermFetcher;
import psidev.psi.mi.jami.model.CvTerm;

/**
 * Intact ols fetcher
 *
 */
@Component(value = "intactOlsCvObjectFetcher")
@Lazy
public class OlsCvObjectFetcher extends AbstractCvObjectFetcher<CvTerm>{


    public OlsCvObjectFetcher() {
    }

    @Override
    protected void initialiseDefaultFetcher() throws BridgeFailedException {
        super.setOboFetcher(new OlsCvTermFetcher());
    }
}
