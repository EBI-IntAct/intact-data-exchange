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
package uk.ac.ebi.intact.dataexchange.cvutils.model;

import org.obo.datamodel.impl.TermCategoryImpl;

/**
 * A wrapper class for filtering the OBO file  for subsets
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class OboCategory extends TermCategoryImpl {

    protected java.lang.String name;
    protected java.lang.String desc;

    public static final String DRUGABLE = "Drugable";
    public static final String PSI_MI_SLIM = "PSI-MI slim";
    //add more 


    public OboCategory(String name){
     super.setName( name );
    }

    public OboCategory(String name, String desc){
     super.setName( name );
     super.setDesc( desc );
       
    }
}
