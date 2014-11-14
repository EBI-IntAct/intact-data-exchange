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
import psidev.psi.mi.jami.bridges.obo.OboSourceFetcher;
import psidev.psi.mi.jami.commons.MIFileUtils;
import psidev.psi.mi.jami.model.Source;
import psidev.psi.mi.jami.utils.CvTermUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * intact source fetcher
 *
 */
@Component(value = "intactSourceFetcher")
@Lazy
public class SourceFetcher extends AbstractCvObjectFetcher<Source>{


    public SourceFetcher() {
    }

    @Override
    protected void initialiseDefaultFetcher() throws BridgeFailedException {
        String urlString = getEnricherContext().getConfig().getOboUrl();
        InputStream stream = null;
        File tempFile = null;
        try {
            URL url = new URL(urlString);
            stream = url.openStream();
            tempFile = MIFileUtils.storeAsTemporaryFile(stream, "cvFetcher_" + System.currentTimeMillis(), ".obo");

            super.setOboFetcher(new OboSourceFetcher(CvTermUtils.createPsiMiDatabase(), tempFile.getAbsolutePath()));

        } catch (MalformedURLException e) {
            throw new BridgeFailedException("Cannot open URL "+urlString, e);
        } catch (IOException e) {
            throw new BridgeFailedException("Cannot read URL "+urlString, e);
        } finally {
            if (stream != null){
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (tempFile != null){
                tempFile.delete();
            }
        }
    }
}
