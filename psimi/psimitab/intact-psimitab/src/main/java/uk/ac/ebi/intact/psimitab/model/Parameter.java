/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 * limitations under the License.
 */
package uk.ac.ebi.intact.psimitab.model;

import java.io.Serializable;

/**
 * TODO comment that class header
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class Parameter implements Serializable {

    private String type;
    private String value;
    private String unit;



    public Parameter( String type,String value, String unit ) {
        this.type = type;
        this.value = value;
        this.unit = unit;
    }

    public String getType() {
        return type;
    }


    public String getUnit() {
        return unit;
    }

    public String getValue() {
        return value;
    }
}
