package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionType;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.intact.psimitab.IntactPsimiTabReader;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.miscore.exporter.QueryFactory;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract filter from a mitab file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public abstract class AbstractMitabFilter {
    protected QueryFactory queryProvider;
    protected Set<String> eligibleInteractionsForUniprotExport;
    protected IntactPsimiTabReader mitabReader;
    protected static final String CONFIDENCE_NAME = "intactPsiscore";
    protected static final String INTACT = "intact";
    protected static final String UNIPROT = "uniprotkb";

    protected InteractionExporter exporter;

    protected String mitab;

    public AbstractMitabFilter(InteractionExporter exporter, String mitab){
        queryProvider = new QueryFactory();
        mitabReader = new IntactPsimiTabReader(true);
        this.exporter = exporter;

        this.mitab = mitab;

        eligibleInteractionsForUniprotExport.addAll(this.queryProvider.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport());
    }

    public AbstractMitabFilter(InteractionExporter exporter){
        queryProvider = new QueryFactory();
        eligibleInteractionsForUniprotExport = new HashSet<String>();
        mitabReader = new IntactPsimiTabReader(true);
        this.exporter = exporter;

        this.mitab = null;

        eligibleInteractionsForUniprotExport.addAll(this.queryProvider.getInteractionAcsFromReleasedExperimentsContainingNoUniprotProteinsToBeProcessedForUniprotExport());
    }

    protected void processMiTerms(BinaryInteraction interaction, MiClusterContext context){
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

    protected double getMiClusterScoreFor(EncoreInteraction interaction){
        List<Confidence> confidenceValues = interaction.getConfidenceValues();
        double score = 0;
        for(Confidence confidenceValue:confidenceValues){
            if(confidenceValue.getType().equalsIgnoreCase(CONFIDENCE_NAME)){
                score = Double.parseDouble(confidenceValue.getValue());
            }
        }

        return score;
    }

    public Set<String> getEligibleInteractionsForUniprotExport() {
        return eligibleInteractionsForUniprotExport;
    }

    public QueryFactory getQueryProvider() {
        return queryProvider;
    }

    public String getMitab() {
        return mitab;
    }

    public void setMitab(String mitab) {
        this.mitab = mitab;
    }
}
