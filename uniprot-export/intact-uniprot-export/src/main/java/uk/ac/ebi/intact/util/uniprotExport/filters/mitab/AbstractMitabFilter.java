package uk.ac.ebi.intact.util.uniprotExport.filters.mitab;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.enfin.mi.cluster.MethodTypePair;
import uk.ac.ebi.intact.util.uniprotExport.exporters.InteractionExporter;
import uk.ac.ebi.intact.util.uniprotExport.filters.IntactFilter;
import uk.ac.ebi.intact.util.uniprotExport.results.contexts.MiClusterContext;

import java.util.*;

/**
 * Abstract filter from a mitab file
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>08/02/11</pre>
 */

public abstract class AbstractMitabFilter extends IntactFilter{
    //protected List<String> eligibleInteractionsNotInMitab = new ArrayList<String>();

    protected static final String INTACT = "intact";
    protected static final String UNIPROT = "uniprotkb";

    protected String mitab;

    public AbstractMitabFilter(InteractionExporter exporter, String mitab){
        super(exporter);
        this.mitab = mitab;

        //eligibleInteractionsNotInMitab.addAll(this.queryFactory.getReleasedSelfInteractionAcsPassingFilters());
    }

    public AbstractMitabFilter(InteractionExporter exporter){
        super(exporter);

        this.mitab = null;

        eligibleInteractionsForUniprotExport.addAll(this.queryFactory.getReleasedInteractionAcsPassingFilters());
    }

    protected void processMiTerms(BinaryInteraction interaction, MiClusterContext context, String intactAc){
        List<CrossReference> detectionMethods = interaction.getDetectionMethods();

        Map<String, String> miTerms = context.getMiTerms();

        for (CrossReference method : detectionMethods){
            if (!miTerms.containsKey(method.getIdentifier())){
                String methodName = method.getText() != null ? method.getText() : "-";
                miTerms.put(method.getIdentifier(), methodName);
            }
        }

        List<CrossReference> types = interaction.getInteractionTypes();

        for (CrossReference type : types){
            if (!miTerms.containsKey(type.getIdentifier())){
                String methodName = type.getText() != null ? type.getText() : "-";
                miTerms.put(type.getIdentifier(), methodName);
            }
        }

        String detectionMI = detectionMethods.iterator().next().getIdentifier();
        String typeMi = types.iterator().next().getIdentifier();

        MethodTypePair entry = new MethodTypePair(detectionMI, typeMi);
        context.getInteractionToMethod_type().put(intactAc, entry);
    }

    public Set<String> getEligibleInteractionsForUniprotExport() {
        return eligibleInteractionsForUniprotExport;
    }

    public String getMitab() {
        return mitab;
    }

    public void setMitab(String mitab) {
        this.mitab = mitab;
    }
}
