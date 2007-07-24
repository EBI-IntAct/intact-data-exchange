/*
 * Copyright 2001-2007 The European Bioinformatics Institute.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util.PsiMiPopulator;
import uk.ac.ebi.intact.model.Alias;
import uk.ac.ebi.intact.model.CvAliasType;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.util.CvObjectUtils;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AliasConverter<A extends Alias> extends AbstractIntactPsiConverter<A, psidev.psi.mi.xml.model.Alias> {

    private Class<A> aliasClass;

    public AliasConverter(Institution institution, Class<A> aliasType) {
        super(institution);
        this.aliasClass = aliasType;
    }

    public A psiToIntact(psidev.psi.mi.xml.model.Alias psiObject) {
        String name = psiObject.getValue();
        String aliasType = psiObject.getType();
        String aliasTypeAc = psiObject.getTypeAc();

        if (name.length() == 0) {
            throw new PsiConversionException("Value in Alias is empty: "+psiObject);
        }

        CvAliasType cvAliasType = null;

        if (aliasType != null) {
            cvAliasType = new CvAliasType(getInstitution(), aliasType);

            PsiMiPopulator psiMiPopulator = new PsiMiPopulator(getInstitution());
            psiMiPopulator.populateWithPsiMi(cvAliasType, aliasTypeAc);
        }

        A alias = newAliasInstance(aliasClass, cvAliasType, name);
        alias.setOwner(getInstitution());

        return alias;
    }

    public psidev.psi.mi.xml.model.Alias intactToPsi(Alias intactObject) {
        String name = intactObject.getName();

        psidev.psi.mi.xml.model.Alias psiAlias = new psidev.psi.mi.xml.model.Alias(name);

        CvAliasType cvAliasType = intactObject.getCvAliasType();

        if (cvAliasType != null) {
            String aliasType = cvAliasType.getShortLabel();
            String aliasTypeAc = CvObjectUtils.getPsiMiIdentityXref(cvAliasType).getPrimaryId();

            psiAlias.setType(aliasType);
            psiAlias.setTypeAc(aliasTypeAc);
        }

        return psiAlias;
    }

    private static <A extends Alias> A newAliasInstance(Class<A> aliasClass, CvAliasType aliasType, String name) {
        A alias = null;
        try {
            alias = aliasClass.newInstance();
            alias.setCvAliasType(aliasType);
            alias.setName(name);
        } catch (Exception e) {
            throw new PsiConversionException(e);
        }

        return alias;
    }


}