package uk.ac.ebi.intact.util.uniprotExport.miscore.filter;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.CrossReference;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.psimitab.model.ExtendedInteractor;
import uk.ac.ebi.intact.util.uniprotExport.miscore.results.MiClusterContext;
import uk.ac.ebi.intact.util.uniprotExport.writers.WriterUtils;

import java.util.Collection;
import java.util.Map;

/**
 * TODO comment this
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>01/02/11</pre>
 */

public class FilterUtils {
    private static final String UNIPROT = "uniprotkb";

    public static void processGeneNames(ExtendedInteractor interactorA, String intactA, ExtendedInteractor interactorB, String intactB, MiClusterContext context) {
        String geneNameA = retrieveInteractorGeneName(interactorA);
        String geneNameB = retrieveInteractorGeneName(interactorB);

        Map<String, String> geneNames = context.getGeneNames();

        if (!geneNames.containsKey(geneNameA)){
            context.getGeneNames().put(intactA, geneNameA);
        }
        if (!geneNames.containsKey(geneNameB)){
            context.getGeneNames().put(intactB, geneNameB);
        }
    }

    public static String retrieveInteractorGeneName(ExtendedInteractor interactor){
        Collection<Alias> aliases = interactor.getAliases();
        String geneName = null;

        if (aliases.isEmpty()) {

            Collection<CrossReference> otherIdentifiers = interactor.getAlternativeIdentifiers();
            // then look for locus
            String locusName = null;
            String orf = null;

            for (CrossReference ref : otherIdentifiers){
                if (UNIPROT.equalsIgnoreCase(ref.getDatabase())){
                    if (CvAliasType.LOCUS_NAME.equalsIgnoreCase(ref.getText())){
                        locusName = ref.getIdentifier();
                    }
                    else if (CvAliasType.ORF_NAME.equalsIgnoreCase(ref.getText())){
                        orf = ref.getIdentifier();
                    }
                }
            }

            geneName = locusName != null ? locusName : orf;

        } else {
            geneName = aliases.iterator().next().getName();
        }

        if( geneName == null ) {
            geneName = "-";
        }

        return geneName;
    }

    public static String [] extractUniprotAndIntactAcFromAccs(Map<String, String> interactorAccs){
        String interactorAcc = null;
        String intactAc = null;
        for(Map.Entry<String, String> entry : interactorAccs.entrySet()){
            if(WriterUtils.INTACT.equalsIgnoreCase(entry.getKey())){
                intactAc =  entry.getValue();
            }
            else if(WriterUtils.UNIPROT.equalsIgnoreCase(entry.getKey())){
                interactorAcc =  entry.getValue();
            }
        }

        return new String [] {interactorAcc, intactAc};
    }
}
