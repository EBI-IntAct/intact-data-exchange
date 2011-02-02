package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.xml.converter.ConverterException;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.util.uniprotExport.miscore.UniprotExportException;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.QueryFactory;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.IntActInteractionClusterScore;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiScoreResults;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * This filter is selecting binary interactions eligible for uniprot export from a mitab file and
 * will compute the mi score of each binary interaction
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>22-Oct-2010</pre>
 */

public class MitabFilter implements InteractionFilter{

    private QueryFactory queryProvider;
    private Set<String> eligibleInteractionsForUniprotExport;
    private IntactPsimiTabReader mitabReader;
    private static final String CONFIDENCE_NAME = "intactPsiscore";
    private static final String INTACT = "intact";
    private static final String UNIPROT = "uniprotkb";
    private final static String FEATURE_CHAIN = "-PRO_";

    private InteractionExporter exporter;

    private String mitab;

    public MitabFilter(InteractionExporter exporter, String mitab){
        queryProvider = new QueryFactory();
        eligibleInteractionsForUniprotExport = new HashSet<String>();
        mitabReader = new IntactPsimiTabReader(true);
        this.exporter = exporter;

        this.mitab = mitab;

        eligibleInteractionsForUniprotExport.addAll(this.queryProvider.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport());
    }

    public MitabFilter(InteractionExporter exporter){
        queryProvider = new QueryFactory();
        eligibleInteractionsForUniprotExport = new HashSet<String>();
        mitabReader = new IntactPsimiTabReader(true);
        this.exporter = exporter;

        this.mitab = null;

        eligibleInteractionsForUniprotExport.addAll(this.queryProvider.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport());
    }

    private MiScoreResults computeMiScoreInteractionEligibleUniprotExport(String mitabFile) throws IOException, ConverterException {
        IntActInteractionClusterScore clusterScore = new IntActInteractionClusterScore();
        MiClusterContext context = new MiClusterContext();

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
                        if (UNIPROT.equalsIgnoreCase(refA.getDatabase())){
                            uniprotA = refA.getIdentifier();
                            //if (uniprotA.contains(FEATURE_CHAIN)){
                                //uniprotA = refA.getIdentifier().substring(0, uniprotA.indexOf(FEATURE_CHAIN));
                            //}

                            break;
                        }
                    }
                }
                if (interactorB != null){
                    for (CrossReference refB : interactorB.getIdentifiers()){
                        if (UNIPROT.equalsIgnoreCase(refB.getDatabase())){
                            uniprotB = refB.getIdentifier();

                            //if (uniprotB.contains(FEATURE_CHAIN)){
                                //uniprotB = refB.getIdentifier().substring(0, uniprotB.indexOf(FEATURE_CHAIN));
                            //}

                            break;
                        }
                    }
                }

                if (intactAc != null && uniprotA != null && uniprotB != null){

                    if (this.eligibleInteractionsForUniprotExport.contains(intactAc)){
                        interactionToProcess.add(interaction);

                        FilterUtils.processGeneNames(interactorA, uniprotA, interactorB, uniprotB, context);
                        processMiTerms(interaction, context);

                        List<InteractionDetectionMethod> detectionMethods = interaction.getDetectionMethods();
                        String detectionMI = detectionMethods.iterator().next().getIdentifier();

                        List<InteractionType> interactionTypes = interaction.getInteractionTypes();
                        String typeMi = interactionTypes.iterator().next().getIdentifier();

                        Map.Entry<String, String> entry = new DefaultMapEntry(detectionMI, typeMi);
                        context.getInteractionToMethod_type().put(intactAc, entry);

                        if (!interaction.getExpansionMethods().isEmpty()){

                            context.getSpokeExpandedInteractions().add(intactAc);
                        }
                    }
                }
            }
            clusterScore.setBinaryInteractionList(interactionToProcess);
            clusterScore.runService();
        }

        return new MiScoreResults(clusterScore, context);
    }

    private void processMiTerms(BinaryInteraction interaction, MiClusterContext context){
        List<InteractionDetectionMethod> detectionMethods = interaction.getDetectionMethods();

        Map<String, String> miTerms = context.getMiTerms();

        for (InteractionDetectionMethod method : detectionMethods){
            if (!miTerms.containsKey(method.getIdentifier())){
                String methodName = method.getText() != null ? method.getText() : "-";
                miTerms.put(method.getIdentifier(), methodName);
            }
        }

        List<InteractionType> types = interaction.getInteractionTypes();

        for (InteractionType type : types){
            if (!miTerms.containsKey(type.getIdentifier())){
                String methodName = type.getText() != null ? type.getText() : "-";
                miTerms.put(type.getIdentifier(), methodName);
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

    public MiScoreResults exportInteractionsFrom(String mitab) throws UniprotExportException {
        try {

            MiScoreResults clusterResults = computeMiScoreInteractionEligibleUniprotExport(mitab);
            exporter.exportInteractionsFrom(clusterResults);

            //this.interactionClusterScore.saveScoresForSpecificInteractions(fileExport, this.interactionsToBeExported);

            return clusterResults;
        } catch (IOException e) {
            throw new UniprotExportException("It was not possible to convert the data in the mitab file " + mitab + " in an InputStream", e);
        } catch (ConverterException e) {
            throw new UniprotExportException("It was not possible to iterate the binary interactions in the mitab file " + mitab, e);
        }
    }

    public Set<String> getEligibleInteractionsForUniprotExport() {
        return eligibleInteractionsForUniprotExport;
    }

    public QueryFactory getQueryProvider() {
        return queryProvider;
    }

    @Override
    public MiScoreResults exportInteractions() throws UniprotExportException {
        return exportInteractionsFrom(mitab);
    }

    @Override
    public InteractionExporter getInteractionExporter() {
        return this.exporter;
    }

    @Override
    public void setInteractionExporter(InteractionExporter exporter) {
        this.exporter = exporter;
    }

    public String getMitab() {
        return mitab;
    }

    public void setMitab(String mitab) {
        this.mitab = mitab;
    }
}
