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
package uk.ac.ebi.intact.dataexchange.psimi.xml.exchange;

import uk.ac.ebi.intact.config.impl.CustomCoreDataConfig;
import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.IntactEnvironment;
import uk.ac.ebi.intact.context.IntactSession;
import uk.ac.ebi.intact.context.impl.StandaloneSession;
import uk.ac.ebi.intact.core.unit.IntactUnit;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.enricher.PsiEnricher;
import uk.ac.ebi.intact.util.DebugUtil;

import java.io.*;
import java.util.Properties;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(IntactEnvironment.INSTITUTION_LABEL.getFqn(), "ebi");
        props.put(IntactEnvironment.AC_PREFIX_PARAM_NAME.getFqn(), "EBI");

        IntactSession intactSession = new StandaloneSession();
        
        File hibernateFile = new File(Playground.class.getResource("/playground-hibernate.cfg.xml").getFile());

        CustomCoreDataConfig dataConfig = new CustomCoreDataConfig("custom", hibernateFile, intactSession);

        IntactContext.initContext(dataConfig, intactSession);

        IntactUnit iu = new IntactUnit();
        iu.createSchema();

        for (File file : getPsiMiFilesInFolder(new File("/ebi/sp/pro6/intact/local/data/curation-material/bantscheff-2007/hela"))) {

            System.out.println("Importing: "+file);

            InputStream is = new FileInputStream(file);

             StringWriter writer = new StringWriter();

            PsiEnricher.enrichPsiXml(is, writer, new EnricherConfig());

            writer.flush();

            InputStream enricherInput = new ByteArrayInputStream(writer.toString().getBytes());

            PsiExchange.importIntoIntact(enricherInput, false);
        }

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        System.out.println("Interactions DB: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractionDao().countAll() +" - "+ DebugUtil.labelList(
        IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractionDao().getAll()));
        System.out.println("Experiments DB: " + IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getExperimentDao().countAll() +" - "+ IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getExperimentDao().getAll());

        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }

    private static File[] getPsiMiFilesInFolder(File folder) throws IOException {
        File[] files = folder.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });


        // modify entrySet line
        for (File file : files) {
            file = modifyRoot(file);
        }

        return files;
    }

    private static File modifyRoot(File file) throws IOException {
        StringBuilder sb = new StringBuilder();

        BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

        String line = null;

        while ((line = bufReader.readLine()) != null) {

            if (line.startsWith("<entrySet")) {
                line = "<entrySet level=\"2\" minorVersion=\"3\" version=\"5\" xmlns=\"net:sf:psidev:mi\"" +
                       "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                       "          xsi:schemaLocation=\"net:sf:psidev:mi http://psidev.sourceforge.net/mi/rel25/src/MIF253.xsd\">";
            }

            sb.append(line).append("\n");
        }

        bufReader.close();

        FileWriter writer = new FileWriter(file);
        writer.write(sb.toString());
        writer.close();

        return file;
    }

}