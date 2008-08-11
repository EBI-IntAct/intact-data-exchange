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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.psimitab;

import uk.ac.ebi.intact.util.ols.OlsClient;
import uk.ac.ebi.ook.web.services.Query;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * The InterproNameHandler gets the Information from a OLS-Service or a Map.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0
 */
public class GoTermHandler {

    private Map<String, String> goMap;

    private Query query;

    public GoTermHandler() {
        query = new OlsClient().getOntologyQuery();
        goMap = new HashMap<String, String>();
    }

    public String getNameById( String goTerm ) throws RemoteException {
        String result;

        if (goMap.containsKey(goTerm)) {
            result = goMap.get(goTerm);
        } else {
            result = query.getTermById( goTerm, "GO" );

            if (result != null) {
                goMap.put(goTerm, result);
            }
        }

        return result;
    }
}
