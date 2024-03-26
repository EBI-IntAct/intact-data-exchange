package uk.ac.ebi.intact.psimitab.converters.converters;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.AliasImpl;
import uk.ac.ebi.intact.jami.model.extension.AbstractIntactAlias;

import java.util.Arrays;
import java.util.List;

/**
 * This class allows to convert a Intact Alias with a MITAB Alias
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>26/07/12</pre>
 */

public class AliasConverter {

    private static final List<String> uniprotKeys;
    public static final String UNIPROT = "uniprotkb";
    public static final String INTACT = "intact";
    public static final String UNKNOWN = "unknown";

    static {
        uniprotKeys = Arrays.asList(
                psidev.psi.mi.jami.model.Alias.GENE_NAME_MI,
                psidev.psi.mi.jami.model.Alias.GENE_NAME_SYNONYM_MI,
                psidev.psi.mi.jami.model.Alias.ISOFORM_SYNONYM_MI,
                psidev.psi.mi.jami.model.Alias.LOCUS_NAME_MI,
                psidev.psi.mi.jami.model.Alias.ORF_NAME_MI);
    }

    public Alias intactToMitab(AbstractIntactAlias intactAlias){

        if (intactAlias != null && intactAlias.getName() != null){

            Alias alias = new AliasImpl();

            if (intactAlias.getType() != null && intactAlias.getType().getShortName() != null){
                String type = intactAlias.getType().getShortName();
                String typeMI = intactAlias.getType().getMIIdentifier();

                if (uniprotKeys.contains(typeMI)){
                    alias.setDbSource(UNIPROT);
                }
                else {
                    alias.setDbSource(INTACT);
                }

                alias.setAliasType(type);
            }
            else {
                alias.setDbSource(UNKNOWN);
            }

            alias.setName(intactAlias.getName());

            return alias;
        }

        return null;
    }
}
