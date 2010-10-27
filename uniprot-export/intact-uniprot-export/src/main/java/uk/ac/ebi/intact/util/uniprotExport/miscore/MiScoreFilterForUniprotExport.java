package uk.ac.ebi.intact.util.uniprotExport.miscore;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extension.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.extractor.IntactQueryProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Oct-2010</pre>
 */

public class MiScoreFilterForUniprotExport {

    private IntactQueryProvider queryProvider;
    private Set<String> spokeExpandedInteractions;
    private Set<String> eligibleInteractionsForUniprotExport;
    private IntactPsimiTabReader mitabReader;
    /**
     * the interaction cluster score
     */
    private IntActInteractionClusterScore interactionClusterScore;

    public MiScoreFilterForUniprotExport(){
        queryProvider = new IntactQueryProvider();
        spokeExpandedInteractions = new HashSet<String>();
        eligibleInteractionsForUniprotExport = new HashSet<String>();
        interactionClusterScore = new IntActInteractionClusterScore();
        mitabReader = new IntactPsimiTabReader(true);

        eligibleInteractionsForUniprotExport.addAll(this.queryProvider.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport());
    }

    public void computeMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        this.spokeExpandedInteractions.clear();

        File mitab = new File(mitabFile);
        Iterator<BinaryInteraction> iterator = mitabReader.iterate(new FileInputStream(mitab));

        List<BinaryInteraction> interactionToProcess = new ArrayList<BinaryInteraction>();

        while (iterator.hasNext()){
            interactionToProcess.clear();
            while (interactionToProcess.size() < 200 && iterator.hasNext()){
                IntactBinaryInteraction interaction = (IntactBinaryInteraction) iterator.next();
                String intactAc = null;

                for (CrossReference ref : interaction.getInteractionAcs()){
                    if (ref.getDatabase().equalsIgnoreCase("intact")){
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
                        if (!interaction.getExpansionMethods().isEmpty()){
                            this.spokeExpandedInteractions.add(intactAc);
                        }
                    }
                }
            }
            this.interactionClusterScore.setBinaryInteractionList(interactionToProcess);
            this.interactionClusterScore.runService();
        }
    }
    
    public Set<String> getSpokeExpandedInteractions() {
        return spokeExpandedInteractions;
    }

    public Set<String> getEligibleInteractionsForUniprotExport() {
        return eligibleInteractionsForUniprotExport;
    }

    public IntactQueryProvider getQueryProvider() {
        return queryProvider;
    }
}
