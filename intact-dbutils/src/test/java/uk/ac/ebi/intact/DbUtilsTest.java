/*
 * Copyright 2006 The Apache Software Foundation.
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
package uk.ac.ebi.intact;

import uk.ac.ebi.intact.dbutil.DbUtils;
import uk.ac.ebi.intact.util.filter.XmlUrlFilter;
import uk.ac.ebi.intact.context.IntactContext;

import java.net.URL;

/**
 * TODO: comment this!
 *
 * @author Bruno Aranda (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class DbUtilsTest
{
    public static void main(String[] args) throws Exception
    {
        //DbUtils.createOrUpdateCvObjects();

        URL url = new URL("ftp://ftp.ebi.ac.uk/pub/databases/intact/current/psi1/pmid/2006");

        IntactContext.getCurrentInstance().getDataContext().beginTransaction();

        DbUtils.importPsi1XmlFromFolderUrl(url, new XmlUrlFilter(), true);

        IntactContext.getCurrentInstance().getDataContext().commitTransaction();
    }
}
