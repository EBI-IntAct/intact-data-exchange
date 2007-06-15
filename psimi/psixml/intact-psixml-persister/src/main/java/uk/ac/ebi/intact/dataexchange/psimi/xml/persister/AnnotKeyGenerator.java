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
package uk.ac.ebi.intact.dataexchange.psimi.xml.persister;

import uk.ac.ebi.intact.model.AnnotatedObject;
import uk.ac.ebi.intact.model.BioSource;
import uk.ac.ebi.intact.model.Component;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotKeyGenerator {

    private AnnotKeyGenerator() {}

    public static String createKey(AnnotatedObject ao) {
        String key;

        if (ao instanceof Component) {
            Component comp = (Component)ao;
            String label = comp.getInteraction().getShortLabel()+"_"+comp.getInteractor().getShortLabel();
            key = comp.getClass().getSimpleName()+":"+label;
        } else if (ao instanceof BioSource) {
            key = ao.getClass().getSimpleName()+":"+((BioSource)ao).getTaxId();
        } else {
            key = ao.getClass().getSimpleName()+":"+ao.getShortLabel();
        }

        return key;
    }
}