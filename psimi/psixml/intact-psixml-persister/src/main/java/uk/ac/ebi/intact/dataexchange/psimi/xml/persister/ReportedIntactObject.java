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

import uk.ac.ebi.intact.model.IntactObject;
import uk.ac.ebi.intact.persistence.util.CgLibUtil;

import java.io.Serializable;

/**
 * TODO comment this
*
* @author Bruno Aranda (baranda@ebi.ac.uk)
* @version $Id$
*/
public class ReportedIntactObject implements Serializable {

    private Class objectClass;
    private String ac;

    public ReportedIntactObject(IntactObject intactObject) {
        this.ac = intactObject.getAc();
        this.objectClass = CgLibUtil.removeCglibEnhanced(intactObject.getClass());
    }

    public String getAc() {
        return ac;
    }

    public Class getObjectClass() {
        return objectClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportedIntactObject that = (ReportedIntactObject) o;

        if (ac != null ? !ac.equals(that.ac) : that.ac != null) return false;
        if (objectClass != null ? !objectClass.equals(that.objectClass) : that.objectClass != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (objectClass != null ? objectClass.hashCode() : 0);
        result = 31 * result + (ac != null ? ac.hashCode() : 0);
        return result;
    }
}