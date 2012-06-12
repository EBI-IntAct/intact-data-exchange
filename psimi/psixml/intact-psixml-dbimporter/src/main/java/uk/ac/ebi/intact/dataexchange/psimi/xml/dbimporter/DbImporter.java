package uk.ac.ebi.intact.dataexchange.psimi.xml.dbimporter;


import org.joda.time.Interval;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.core.persistence.dao.DaoFactory;
import uk.ac.ebi.intact.core.persister.stats.PersisterStatistics;
import uk.ac.ebi.intact.core.persister.stats.StatsUnit;
import uk.ac.ebi.intact.dataexchange.enricher.EnricherConfig;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.PsiExchange;
import uk.ac.ebi.intact.dataexchange.psimi.xml.exchange.enricher.PsiEnricher;
import uk.ac.ebi.intact.model.*;
import uk.ac.ebi.intact.model.user.User;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Import PSI-MI XML2.5 from the command line.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 * @since 2.1.2
 */
public class DbImporter {

    public static void main(String[] args) throws Exception {
        String file = null;
        String curatorName = null;

        if (args.length < 2) {
            System.err.println("Usage: DbImporter <input.file> <curator.login>");
            System.exit(1);
        } else {
            file = args[0];
            curatorName = args[1];
        }

        List<File> filesToImport = new ArrayList<File>();

        filesToImport.add(new File(file));

        System.out.println("--------------------------");
        System.out.println("FILE: "+file);
        System.out.println("USER NAME: "+curatorName);
        System.out.println("--------------------------");

        if (!new File(file).exists()) {
            System.err.println("File does not exist or has wrong permissions: "+file);
            System.exit(-1);
        }

        IntactContext.initContext(new String[] { "classpath*:/META-INF/intact.spring.xml",
                                                 "/META-INF/intact.spring.xml"});

        User user = IntactContext.getCurrentInstance().getDaoFactory().getUserDao().getByLogin(curatorName.toLowerCase());

        IntactContext.getCurrentInstance().getUserContext().setUserId(curatorName);

        if (user == null){
            throw new IllegalArgumentException("User with login " + curatorName + " does not exist in the database and so the db import is aborted.");
        }
        else {
            IntactContext.getCurrentInstance().getUserContext().setUser(user);
        }

        for (File fileToImport : filesToImport) {

            long startTime = System.currentTimeMillis();

            File tempFile = enrich(fileToImport);

            System.out.println("\n---------------------------------------------------------");
            System.out.println("\nFile: " + fileToImport+"\n");

            long timeAfterEnrich = System.currentTimeMillis();

            importIntoIntact(tempFile);

            long endTime = System.currentTimeMillis();

            System.out.println("\nEnriched file: " + tempFile+ "(Original: "+fileToImport+"\n");

            Interval enrichingTime = new Interval(startTime, timeAfterEnrich);
            Interval importTime = new Interval(timeAfterEnrich, endTime);
            Interval totalTime = new Interval(startTime, endTime);

            System.out.println("\nEnriching time: " + enrichingTime.toDurationMillis() + "ms");
            System.out.println("Import time: " + importTime.toDurationMillis() + "ms");
            System.out.println("Total time: " + totalTime.toDurationMillis() + "ms");
            System.out.println("\n---------------------------------------------------------\n");
        }
    }

    public static File enrich(File fileToImport) throws IOException {
        EnricherConfig enricherConfig = new EnricherConfig();
        enricherConfig.setUpdateInteractionShortLabels(true);
        enricherConfig.setUpdateExperiments(true);

        //EnricherContext.getInstance().getConfig().setUpdateInteractionShortLabels(true);

        System.out.println("Importing: " + fileToImport);

        InputStream is = new FileInputStream(fileToImport);
        String name = fileToImport.getName().replaceAll( ".xml", "" );
        File tempFile = new File(name + "-enriched-"+System.currentTimeMillis()+".xml");
        try{

            System.out.println( "Enriching file in: " + tempFile.getAbsolutePath() );

            Writer writer = new BufferedWriter(new FileWriter(tempFile));

            try{
                PsiEnricher psiEnricher = (PsiEnricher) IntactContext.getCurrentInstance().getSpringContext().getBean("psiEnricher");
                psiEnricher.enrichPsiXml(is, writer, enricherConfig);
            }
            finally {
                writer.close();
            }
        }
        finally {
            is.close();
        }

        return tempFile;
    }

    private static void importIntoIntact(File tempFile) throws IOException {
        InputStream enricherInput = new FileInputStream(tempFile);

        try{
            final DaoFactory daoFactory = IntactContext.getCurrentInstance().getDataContext().getDaoFactory();
            int interactionsBefore = daoFactory.getInteractionDao().countAll();
            int experimentsBefore = daoFactory.getExperimentDao().countAll();

            PsiExchange psiExchange = (PsiExchange) IntactContext.getCurrentInstance().getSpringContext().getBean("psiExchange");
            PersisterStatistics stats = psiExchange.importIntoIntact(enricherInput);

            int interactionsAfter = daoFactory.getInteractionDao().countAll();
            int experimentsAfter = daoFactory.getExperimentDao().countAll();

            System.out.println("Real Interactions Created: " + (interactionsAfter - interactionsBefore));
            System.out.println("Real Experiments Created: " + (experimentsAfter - experimentsBefore) + "\n");

            printStats( stats, System.out );

            System.out.println("New prots: " + stats.getPersisted(Protein.class, true));
        }
        finally {
            enricherInput.close();
        }
    }

    public static void printStats(PersisterStatistics stats, PrintStream ps) {

        ps.println("Interactions Created: " + (stats.getPersistedCount(InteractionImpl.class, false)));
        ps.println("Experiments Created: " + (stats.getPersistedCount(Experiment.class, false)));
        ps.println("Proteins Created: " + (stats.getPersistedCount(ProteinImpl.class, false)));
        ps.println("Small Molecule Created: " + (stats.getPersistedCount(SmallMoleculeImpl.class, false)));
        ps.println("CvObjects Created: " + (stats.getPersistedCount(CvObject.class, true)));
        ps.println("BioSources Created: " + (stats.getPersistedCount(BioSource.class, false)));
        ps.println("Features Created: " + (stats.getPersistedCount(Feature.class, false)));
        ps.println("Components Created: " + (stats.getPersistedCount(Component.class, false)));

        final Collection<StatsUnit> interactionDuplicates = stats.getDuplicates(InteractionImpl.class, false);
        ps.println("\nDuplicated interactions ("+interactionDuplicates.size()+"): "+ interactionDuplicates);
    }
}

