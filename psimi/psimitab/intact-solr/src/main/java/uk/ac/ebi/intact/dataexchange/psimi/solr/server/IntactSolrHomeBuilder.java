/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.solr.server;

import org.apache.commons.io.FileUtils;
import org.hupo.psi.mi.psicquic.model.server.SolrHomeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Creates a filesystem to host a solr home with IntAct configuration.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactSolrHomeBuilder extends SolrHomeBuilder {

    private static Logger log = LoggerFactory.getLogger(IntactSolrHomeBuilder.class);

    public IntactSolrHomeBuilder() {
    }

    public void install(File solrWorkingDir) throws IOException {
        if (log.isInfoEnabled()) log.info("Installing Intact SOLR Home at: "+solrWorkingDir);

        // copy resource directory containing solr-home and war file
        File solrHomeToCreate = new File(solrWorkingDir,"home");
        File solrWarToCreate = new File(solrWorkingDir,"solr.war");

        // only copy solr-home when solr-home does not exist
        if (!solrHomeToCreate.exists() && getSolrHomeDir() == null){
            solrHomeToCreate.mkdirs();

            File solrHomeToCopy = new File(IntactSolrHomeBuilder.class.getResource("/home").getFile());
            // is in the resources
            if (solrHomeToCopy.exists()){
                FileUtils.copyDirectory(solrHomeToCopy, solrHomeToCreate);

                if (!solrWarToCreate.exists() && getSolrWar() == null){
                    try (InputStream solrWarToCopy = IntactSolrHomeBuilder.class.getResourceAsStream("/solr.war")) {
                        FileUtils.copyInputStreamToFile(solrWarToCopy, solrWarToCreate);
                    }
                }
            }
            // is in the jar in the dependencies
            else {

                String originalName = IntactSolrHomeBuilder.class.getResource("/home").getFile();
                String jarFileName = originalName.substring(0, originalName.indexOf("!")).replace("file:", "");

                JarFile jarFile = new JarFile(jarFileName);
                Enumeration<JarEntry> jarEntries = jarFile.entries();

                // write
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();

                    // solr war file
                    if (entry.getName().endsWith("solr.war") && !solrWarToCreate.exists() && getSolrHomeDir() == null) {

                        InputStream inputStream = jarFile.getInputStream(entry);

                        try{
                            FileUtils.copyInputStreamToFile(inputStream, solrWarToCreate);
                        }
                        finally {
                            inputStream.close();
                        }
                    }
                    else if (entry.toString().startsWith("home")){
                        File fileToCreate = new File(solrWorkingDir, entry.toString());

                        if (entry.isDirectory()) {
                            fileToCreate.mkdirs();
                            continue;
                        }

                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            FileUtils.copyInputStreamToFile(inputStream, fileToCreate);
                        }
                    }
                }
            }

            setSolrHomeDir(solrHomeToCreate);
            setSolrWar(solrWarToCreate);
        }
        // only copy solr.war when solr.war does not exist
        else if (!solrWarToCreate.exists() && getSolrWar() == null){

            File solrHomeToCopy = new File(IntactSolrHomeBuilder.class.getResource("/home").getFile());
            // is in the resources
            if (solrHomeToCopy.exists()){
                try(InputStream solrWarToCopy = IntactSolrHomeBuilder.class.getResourceAsStream("/solr.war")) {
                    FileUtils.copyInputStreamToFile(solrWarToCopy, new File(solrWorkingDir + "/solr.war"));
                }
            }
            // is in the jar in the dependencies
            else {

                String originalName = IntactSolrHomeBuilder.class.getResource("/home").getFile();
                String jarFileName = originalName.substring(0, originalName.indexOf("!")).replace("file:", "");

                JarFile jarFile = new JarFile(jarFileName);
                Enumeration<JarEntry> jarEntries = jarFile.entries();

                // write
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();

                    // solr war file
                    if (entry.getName().endsWith("solr.war")) {
                        File fileToCreate = new File(solrWorkingDir, entry.toString());

                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            FileUtils.copyInputStreamToFile(inputStream, fileToCreate);
                        }
                    }
                }
            }

            setSolrHomeDir(solrHomeToCreate);
            setSolrWar(solrWarToCreate);
        }

        if (log.isDebugEnabled()) {
            log.debug("\nIntact Solr Home: {}\nSolr WAR: {}", getSolrHomeDir().toString(), getSolrWar().toString());
        }
    }
}
