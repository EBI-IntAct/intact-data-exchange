package uk.ac.ebi.intact.psimitab.converters;

import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.AliasImpl;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.CvDatabase;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.util.InstitutionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static uk.ac.ebi.intact.model.CvAliasType.*;

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
        uniprotKeys = new ArrayList<String>( Arrays.asList(GENE_NAME_MI_REF, GENE_NAME_SYNONYM_MI_REF,
                ISOFORM_SYNONYM_MI_REF, LOCUS_NAME_MI_REF,
                ORF_NAME_MI_REF) );
    }

    public Alias intactToMitab(uk.ac.ebi.intact.model.Alias intactAlias){

        if (intactAlias != null && intactAlias.getName() != null){

            Alias alias = new AliasImpl();

            if (intactAlias.getCvAliasType() != null && intactAlias.getCvAliasType().getShortLabel() != null){
                String type = intactAlias.getCvAliasType().getShortLabel();
                String typeMI = intactAlias.getCvAliasType().getIdentifier();

                if (uniprotKeys.contains(typeMI)){
                    alias.setDbSource(UNIPROT);
                }
                else {
                    String institutionName = INTACT;
                    IntactContext context = IntactContext.getCurrentInstance();

                    if (context.getInstitution() != null){
                        Institution institution = context.getInstitution();

                        CvDatabase database = InstitutionUtils.retrieveCvDatabase(context, institution);

                        if (database != null && database.getShortLabel() != null){
                            institutionName = database.getShortLabel();
                        }
                    }

                    alias.setDbSource(institutionName);
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
