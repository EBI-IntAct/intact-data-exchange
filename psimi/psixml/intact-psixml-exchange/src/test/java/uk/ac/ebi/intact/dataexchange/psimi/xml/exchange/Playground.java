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

import uk.ac.ebi.intact.context.IntactContext;
import uk.ac.ebi.intact.context.IntactEnvironment;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherContext;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher.PsiEnricher;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;

import java.io.*;
import java.util.Properties;
import java.util.Iterator;

import org.joda.time.Interval;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Playground {

    public static void main(String[] args) throws Exception {
        File fileToImport = new File("/ebi/sp/pro6/intact/local/data/curation-material/Sato-2007/sato.xml");
        //File fileToImport = new File("/tmp/titz_trepa_interactions_psi_mi.xml");

        Properties props = new Properties();
        props.put(IntactEnvironment.INSTITUTION_LABEL.getFqn(), "ebi");
        props.put(IntactEnvironment.AC_PREFIX_PARAM_NAME.getFqn(), "EBI");

        File hibernateFile = new File(Playground.class.getResource("/d003-hibernate.cfg.xml").getFile());

        IntactContext.initStandaloneContext(hibernateFile);
        IntactContext.getCurrentInstance().getUserContext().setUserId("DAVET");

        long startTime = System.currentTimeMillis();

        File tempFile = enrich(fileToImport);
        //File tempFile = new File("/tmp", "psiToImport-9971.xml");  // titz

        long timeAfterEnrich = System.currentTimeMillis();

        importIntoIntact(tempFile);

        long endTime = System.currentTimeMillis();

        System.out.println("Imported file: "+tempFile);

        Interval enrichingTime = new Interval(startTime, timeAfterEnrich);
        Interval importTime = new Interval(timeAfterEnrich, endTime);
        Interval totalTime = new Interval(startTime, endTime);

        System.out.println("\nEnriching time: "+enrichingTime.toDurationMillis()+"ms");
        System.out.println("Import time: "+importTime.toDurationMillis()+"ms");
        System.out.println("Total time: "+totalTime.toDurationMillis()+"ms");


    }

    private static File enrich(File fileToImport) throws IOException {
        EnricherConfig enricherConfig = new EnricherConfig();
        enricherConfig.setUpdateInteractionShortLabels(true);

        EnricherContext.getInstance().getConfig().setUpdateInteractionShortLabels(true);

        System.out.println("Importing: " + fileToImport);

        InputStream is = new FileInputStream(fileToImport);

        File tempFile = File.createTempFile("psiToImport-", ".xml");
        Writer writer = new FileWriter(tempFile);

        PsiEnricher.enrichPsiXml(is, writer, enricherConfig);
        return tempFile;
    }

    private static void importIntoIntact(File tempFile) throws FileNotFoundException {
        InputStream enricherInput = new FileInputStream(tempFile);

        int interactionsBefore = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractionDao().countAll();
        int experimentsBefore = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getExperimentDao().countAll();

        PersisterStatistics stats = PsiExchange.importIntoIntact(enricherInput);

        int interactionsAfter = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getInteractionDao().countAll();
        int experimentsAfter = IntactContext.getCurrentInstance().getDataContext().getDaoFactory()
                .getExperimentDao().countAll();

        System.out.println("Real Interactions Created: " + (interactionsAfter-interactionsBefore));
        System.out.println("Real Experiments Created: " + (experimentsAfter-experimentsBefore)+"\n");

        System.out.println("Interactions Created: " + (stats.getPersistedCount(InteractionImpl.class, false)));
        System.out.println("Experiments Created: " + (stats.getPersistedCount(Experiment.class, false)));
        System.out.println("Proteins Created: " + (stats.getPersistedCount(ProteinImpl.class, false)));
        System.out.println("Small Molecule Created: " + (stats.getPersistedCount(SmallMoleculeImpl.class, false)));
        System.out.println("CvObjects Created: " + (stats.getPersistedCount(CvObject.class, true)));
        System.out.println("BioSources Created: " + (stats.getPersistedCount(BioSource.class, false)));
        System.out.println("Features Created: " + (stats.getPersistedCount(Feature.class, false)));
        System.out.println("Components Created: " + (stats.getPersistedCount(Component.class, false)));

        //  TODO: print duplicate interactions
        System.out.println("\nDuplicated interactions: "+stats.getDuplicates(InteractionImpl.class, false));
    }


}