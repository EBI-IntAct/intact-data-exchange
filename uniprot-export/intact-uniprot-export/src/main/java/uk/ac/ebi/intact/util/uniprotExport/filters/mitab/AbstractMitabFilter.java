package uk.ac.ebi.intact.util.uniprotExport.filters.mitab;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.InteractionDetectionMethod;
import psidev.psi.mi.tab.model.InteractionType;
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
    protected Set<String> eligibleInteractionsForUniprotExport = new HashSet<String>();
    //protected List<String> eligibleInteractionsNotInMitab = new ArrayList<String>();

    protected static final String INTACT = "intact";
    protected static final String UNIPROT = "uniprotkb";

    protected String mitab;

    public AbstractMitabFilter(InteractionExporter exporter, String mitab){
        super(exporter);
        this.mitab = mitab;

        eligibleInteractionsForUniprotExport.addAll(this.queryFactory.getReleasedInteractionAcsPassingFilters());
        //eligibleInteractionsNotInMitab.addAll(this.queryFactory.getReleasedSelfInteractionAcsPassingFilters());
        //eligibleInteractionsNotInMitab.addAll(this.queryFactory.getNegativeInteractionsPassingFilter());
    }

    public AbstractMitabFilter(InteractionExporter exporter){
        super(exporter);

        this.mitab = null;

        eligibleInteractionsForUniprotExport.addAll(this.queryFactory.getReleasedInteractionAcsPassingFilters());
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
