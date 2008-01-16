package uk.ac.ebi.intact.dataexchange.imex.repository;

import java.io.File;

import uk.ac.ebi.intact.dataexchange.imex.repository.dao.RepoEntryService;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.RepoEntry;
import uk.ac.ebi.intact.dataexchange.imex.repository.model.Message;
import com.google.common.collect.Multiset;
import com.google.common.collect.HashMultiset;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PlaygroundStats {

    public static void main(String[] args) throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "myRepo-mintdip/");

        Repository repo = ImexRepositoryContext.openRepository(tempDir.getAbsolutePath());

        Multiset<String> totalCount = new HashMultiset<String>();
        Multiset<String> importableCount = new HashMultiset<String>();
        Multiset<String> incorrectPubmedBibref = new HashMultiset<String>();
        Multiset<String> noParticipantDetMethods = new HashMultiset<String>();
        Multiset<String> participantWithoutInteractor = new HashMultiset<String>();
        Multiset<String> multipleParticipantDetMethods = new HashMultiset<String>();
        Multiset<String> xmlErrors = new HashMultiset<String>();

        RepoEntryService repoEntryService = ImexRepositoryContext.getInstance().getImexServiceProvider().getRepoEntryService();
        for (RepoEntry repoEntry : repoEntryService.findAllRepoEntries()) {
            final String provider = repoEntry.getRepoEntrySet().getProvider().getName();
            totalCount.add(provider);

            if (repoEntry.isImportable()) importableCount.add(provider);

            if (!repoEntry.isValid()) {
                if (!repoEntry.getMessages().isEmpty()) {
                    Message error = repoEntry.getMessages().iterator().next();


                    if (error.getStackTrace() != null) {
                        final String errorLine = error.getStackTrace().split("\t")[0];

                        if (errorLine.contains("Bibref in ExperimentDescription")) {
                            incorrectPubmedBibref.add(provider);
                        }
                        else if (errorLine.contains("that does not have participant detection")) {
                            noParticipantDetMethods.add(provider);
                        } else if (errorLine.contains("Participant without interactor")) {
                            participantWithoutInteractor.add(provider);
                        } else if (errorLine.contains("Experiment without CvIdentification")) {
                            multipleParticipantDetMethods.add(provider);
                        }
                        else {
                            System.out.println(errorLine);
                        }
                    } else {
                        if (error.getText().contains("Error:(")) {
                            xmlErrors.add(provider);
                        } else {
                            System.out.println("\tError: " + error.getText());
                        }
                    }
                }
            }
        }

        System.out.println("Total: "+ totalCount);
        System.out.println("Importable: "+importableCount);
        System.out.println("Entries with XML errors: "+xmlErrors);
        System.out.println("Bibref to Pubmed incorrect: "+incorrectPubmedBibref);

        System.out.println("\nNo participant det methods: "+noParticipantDetMethods);
        System.out.println("Multiple participant det methods: "+multipleParticipantDetMethods);
        System.out.println("Participant without interactor: "+participantWithoutInteractor);
    }
}
