/**
 * Copyright 2007 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import uk.ac.ebi.intact.model.ComponentConfidence;
import uk.ac.ebi.intact.model.Institution;


/**
 * TODO comment that class header
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since TODO specify the maven artifact version
 *        <pre>
 *               06-Dec-2007
 *               </pre>
 */
public class ParticipantConfidenceConverter extends AbstractConfidenceConverter<ComponentConfidence> {

    public ParticipantConfidenceConverter(Institution institution) {
        super( institution );
    }

    @Override
    public ComponentConfidence newConfidenceInstance(String value) {
        return new ComponentConfidence(value);
    }

}
