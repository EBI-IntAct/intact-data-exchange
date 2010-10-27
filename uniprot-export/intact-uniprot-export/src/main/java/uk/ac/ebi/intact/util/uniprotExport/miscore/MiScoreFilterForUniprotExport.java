package uk.ac.ebi.intact.util.uniprotExport.miscore;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extractor.IntactQueryProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * This class will process uniprot-export of binary interactions (both spoke expanded and truly binary interactions)
 * The rules are simpler than for the CCline export :
 * - Starts with the MITAB file generated during the release. It contains all the released binary interactions in IntAct (contains spoke expanded as well)
 * - Computes the mi cluster score for all the interactions passing the dr-export constraint at the level of the experiment and which are composed of two uniprot proteins.
 * - Excludes the binary interactions with a score lower than the EXPORT_THRESHOLD
 * - Export only binary interactions containing at least one truly binary interaction in IntAct (not only spoke expanded)
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Oct-2010</pre>
 */

public class MiScoreFilterForUniprotExport {

    private IntactQueryProvider queryProvider;
    private Set<String> trueBinaryInteractions;
    private Set<String> eligibleInteractionsForUniprotExport;
    private Set<Integer> interactionsToBeExported;
    private IntactPsimiTabReader mitabReader;
    private static final double EXPORT_THRESHOLD = 0.4;
    private static final String CONFIDENCE_NAME = "intactPsiscore";
    private static final String INTACT = "intact";
    /**
     * the interaction cluster score
     */
    private IntActInteractionClusterScore interactionClusterScore;

    public MiScoreFilterForUniprotExport(){
        queryProvider = new IntactQueryProvider();
        trueBinaryInteractions = new HashSet<String>();
        eligibleInteractionsForUniprotExport = new HashSet<String>();
        interactionsToBeExported = new HashSet<Integer>();
        interactionClusterScore = new IntActInteractionClusterScore();
        mitabReader = new IntactPsimiTabReader(true);

        eligibleInteractionsForUniprotExport.addAll(this.queryProvider.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport());
    }

    private void computeMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        this.trueBinaryInteractions.clear();

        File mitab = new File(mitabFile);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(mitab));

        List<BinaryInteraction> interactionToProcess = new ArrayList<BinaryInteraction>();

        while (iterator.hasNext()){
            interactionToProcess.clear();
            while (interactionToProcess.size() < 200 && iterator.hasNext()){
                IntactBinaryInteraction interaction = (IntactBinaryInteraction) iterator.next();
                String intactAc = null;

                for (CrossReference ref : interaction.getInteractionAcs()){
                    if (ref.getDatabase().equalsIgnoreCase(INTACT)){
                        intactAc = ref.getIdentifier();
                        break;
                    }
                }

                ExtendedInteractor interactorA = interaction.getInteractorA();
                String uniprotA = null;
                ExtendedInteractor interactorB = interaction.getInteractorB();
                String uniprotB = null;

                if (interactorA != null){
                    for (CrossReference refA : interactorA.getIdentifiers()){
                        if (refA.getDatabase().equalsIgnoreCase("uniprotkb")){
                            uniprotA = refA.getIdentifier();
                            break;
                        }
                    }
                }
                if (interactorB != null){
                    for (CrossReference refB : interactorB.getIdentifiers()){
                        if (refB.getDatabase().equalsIgnoreCase("uniprotkb")){
                            uniprotB = refB.getIdentifier();
                            break;
                        }
                    }
                }

                if (intactAc != null && uniprotA != null && uniprotB != null){

                    if (this.eligibleInteractionsForUniprotExport.contains(intactAc)){
                        interactionToProcess.add(interaction);
                        if (interaction.getExpansionMethods().isEmpty()){
                            this.trueBinaryInteractions.add(intactAc);
                        }
                    }
                }
            }
            this.interactionClusterScore.setBinaryInteractionList(interactionToProcess);
            this.interactionClusterScore.runService();
        }
    }

    private void extractInteractionsForUniprotExport() throws UniprotExportException {
        this.interactionsToBeExported.clear();

        if (this.interactionClusterScore.getInteractionMapping() == null){
            throw new UniprotExportException("Before exporting interactions in Uniprot, it is mandatory to compute the mi score of the interactions possible to export. " +
                    "Currently no mi cluster score has been computed.");
        }
        else if (this.interactionClusterScore.getInteractionMapping().isEmpty()){
            throw new UniprotExportException("Before exporting interactions in Uniprot, it is mandatory to compute the mi score of the interactions possible to export. " +
                    "Currently no mi cluster score has been computed.");
        }

        for (Map.Entry<Integer, EncoreInteraction> entry : this.interactionClusterScore.getInteractionMapping().entrySet()){
            EncoreInteraction interaction = entry.getValue();

            double score = getMiClusterScoreFor(interaction);

            if (score >= EXPORT_THRESHOLD){

                if (interaction.getExperimentToDatabase() == null){
                    throw new UniprotExportException("The interaction " + entry.getKey() + ":" + interaction.getInteractorA() + "-" + interaction.getInteractorB() +" doesn't have any references to IntAct.");
                }
                List<String> intactInteractions = interaction.getExperimentToDatabase().get(INTACT);

                if (intactInteractions.isEmpty()){
                    throw new UniprotExportException("The interaction " + entry.getKey() + ":" + interaction.getInteractorA() + "-" + interaction.getInteractorB() +" doesn't have any references to IntAct.");
                }

                for (String ac : intactInteractions){
                    if (trueBinaryInteractions.contains(ac)){
                        interactionsToBeExported.add(entry.getKey());
                    }
                }
            }
        }
    }

    private double getMiClusterScoreFor(EncoreInteraction interaction){
        List<Confidence> confidenceValues = interaction.getConfidenceValues();
        double score = 0;
        for(Confidence confidenceValue:confidenceValues){
            if(confidenceValue.getType().equalsIgnoreCase(CONFIDENCE_NAME)){
                score = Double.parseDouble(confidenceValue.getValue());
            }
        }

        return score;
    }

    public void processUniprotExport(String mitab, String fileExport) throws UniprotExportException {
        try {
            computeMiScoreInteractionEligibleUniprotExport(mitab);
            extractInteractionsForUniprotExport();

            this.interactionClusterScore.saveScoresForSpecificInteractions(fileExport, this.interactionsToBeExported);
        } catch (IOException e) {
            throw new UniprotExportException("It was not possible to convert the data in the mitab file " + mitab + " in an InputStream", e);
        } catch (ConverterException e) {
            throw new UniprotExportException("It was not possible to iterate the binary interactions in the mitab file " + mitab, e);
        }
    }

    public Set<String> getTrueBinaryInteractions() {
        return trueBinaryInteractions;
    }

    public Set<String> getEligibleInteractionsForUniprotExport() {
        return eligibleInteractionsForUniprotExport;
    }

    public IntactQueryProvider getQueryProvider() {
        return queryProvider;
    }
}
