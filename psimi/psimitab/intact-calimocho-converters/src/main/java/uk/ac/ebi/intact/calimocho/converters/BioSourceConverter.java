package uk.ac.ebi.intact.calimocho.converters;

import org.hupo.psi.calimocho.key.CalimochoKeys;
import org.hupo.psi.calimocho.model.DefaultField;
import org.hupo.psi.calimocho.model.Field;
import uk.ac.ebi.intact.calimocho.comparator.OrganismComparator;
import uk.ac.ebi.intact.core.context.IntactContext;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.BioSourceAlias;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.CvObjectXref;
import uk.ac.ebi.intact.model.util.XrefUtils;

import java.util.*;

/**
 * Biosource converter
 *
 * @author Marine Dumousseau (marine@ebi.ac.uk)
 * @version $Id$
 * @since <pre>25/05/12</pre>
 */

public class BioSourceConverter {

    public static String TAXID = "taxid";
    private OrganismComparator organismComparator;

    public BioSourceConverter(){
        this.organismComparator = new OrganismComparator();
    }

    public Collection<Field> intactToCalimocho(BioSource organism){
        if (organism != null && organism.getTaxId() != null){
            Collection<Field> fields = new ArrayList<Field>(2);

            Field common = new DefaultField();

            String name = organism.getShortLabel();
            String fullName = organism.getFullName();
            String taxId = organism.getTaxId();

            common.set(CalimochoKeys.KEY, TAXID);
            common.set(CalimochoKeys.DB, TAXID);
            common.set(CalimochoKeys.VALUE, taxId);

            if (name != null){
                common.set(CalimochoKeys.TEXT, name);
            }

            fields.add(common);

            if (fullName != null){
                Field scientific = new DefaultField();
                scientific.set(CalimochoKeys.KEY, TAXID);
                scientific.set(CalimochoKeys.DB, TAXID);
                scientific.set(CalimochoKeys.VALUE, taxId);

                scientific.set(CalimochoKeys.TEXT, fullName);

                fields.add(scientific);
            }

            return fields;
        }

        return Collections.EMPTY_LIST;
    }

    /**
     *
     * @param organisms
     * @return the converted biosources
     */
    public Collection<BioSource> calimochoToIntact(Collection<Field> organisms){

        if (organisms != null && organisms.isEmpty()){
            Collection<BioSource> biosources = new ArrayList<BioSource>(organisms.size());

            List<Field> sortedOrganisms = new ArrayList<Field>(organisms);
            Collections.sort(sortedOrganisms, organismComparator);
            Iterator<Field> organismIterator = sortedOrganisms.iterator();

            Field firstOrganism = organismIterator.next();

            BioSource firstBiosource = new BioSource(firstOrganism.get(CalimochoKeys.TEXT), firstOrganism.get(CalimochoKeys.VALUE));
            biosources.add(firstBiosource);

            BioSource currentBiosource = firstBiosource;

            // we have more information for this biosource or other biosources based on taxid info
            while (organismIterator.hasNext()){
                Field organism = organismIterator.next();
                String taxId = organism.get(CalimochoKeys.VALUE);
                String text = organism.get(CalimochoKeys.TEXT);

                // we have a different organism
                if (currentBiosource.getTaxId() != null && taxId != null && !currentBiosource.getTaxId().equalsIgnoreCase(taxId)){
                    BioSource biosource = new BioSource(text, taxId);
                    biosources.add(firstBiosource);

                    currentBiosource = biosource;
                }

                // we have the same taxid, we just add more information to the current biosource
                while (currentBiosource.getTaxId() != null && taxId != null && currentBiosource.getTaxId().equalsIgnoreCase(taxId) && organismIterator.hasNext()){

                    if (currentBiosource.getShortLabel() == null){
                        currentBiosource.setShortLabel(text);
                        organism = organismIterator.next();
                    }
                    else if (currentBiosource.getShortLabel().equalsIgnoreCase(text)){
                        organism = organismIterator.next();
                    }
                    else if (currentBiosource.getFullName() == null){
                        currentBiosource.setFullName(text);
                        organism = organismIterator.next();
                    }
                    else if (currentBiosource.getFullName().equalsIgnoreCase(text)){
                        organism = organismIterator.next();
                    }
                    else {
                        BioSourceAlias alias = new BioSourceAlias();

                        alias.setName(text);

                        CvAliasType aliasType = new CvAliasType(IntactContext.getCurrentInstance().getInstitution(), AliasConverter.SYNONYM);

                        aliasType.setIdentifier(AliasConverter.SYNONYM_MI);
                        CvObjectXref psiRef = XrefUtils.createIdentityXrefPsiMi(aliasType, AliasConverter.SYNONYM_MI);
                        aliasType.addXref(psiRef);

                        if (!currentBiosource.getAliases().contains(alias)){
                            currentBiosource.addAlias(alias);
                        }
                    }
                }
            }

            return biosources;
        }

        return Collections.EMPTY_LIST;
    }
}
