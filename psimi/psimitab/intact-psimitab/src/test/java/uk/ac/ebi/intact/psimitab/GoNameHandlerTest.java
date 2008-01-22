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

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;

import uk.ac.ebi.intact.psimitab.exception.NameNotFoundException;

/**
 * GoTermHandler Tester.
 *
 * @author Nadin Neuhauser
 * @version $Id$
 * @since 2.0.0-Snapshot
 */
public class GoNameHandlerTest {

    @Test
    public void usingBaseTest() throws UnsupportedEncodingException, NameNotFoundException {
        GoTermHandler handler = new GoTermHandler( );
        String goTerm = handler.getNameById( "GO:0005515" );
        assertNotNull( goTerm );
        assertEquals( "protein binding", goTerm );
    }

    @Test
    public void usingPerformanceTest() throws UnsupportedEncodingException, NameNotFoundException {
        GoTermHandler handler = new GoTermHandler( );
        List<String> listOfGoTerms = new ArrayList<String>();
        // add 10 different GO-Terms
        listOfGoTerms.add("GO:0005515");listOfGoTerms.add("GO:0003697");
        listOfGoTerms.add("GO:0005662");listOfGoTerms.add("GO:0033205");
        listOfGoTerms.add("GO:0005634");listOfGoTerms.add("GO:0006302");
        listOfGoTerms.add("GO:0006261");listOfGoTerms.add("GO:0007131");
        listOfGoTerms.add("GO:0000150");listOfGoTerms.add("GO:0000502");
        listOfGoTerms.add("GO:0006289");listOfGoTerms.add("GO:0008233");
        listOfGoTerms.add("GO:0007001");listOfGoTerms.add("GO:0010332");
        listOfGoTerms.add("GO:0033600");listOfGoTerms.add("GO:0006508");
        listOfGoTerms.add("GO:0001833");listOfGoTerms.add("GO:0033593");
        listOfGoTerms.add("GO:0007141");listOfGoTerms.add("GO:0043142");

        // add again the same GO-Terms -> that means that it will take less time to get these GoNames
        listOfGoTerms.add("GO:0005515");listOfGoTerms.add("GO:0003697");
        listOfGoTerms.add("GO:0005662");listOfGoTerms.add("GO:0033205");
        listOfGoTerms.add("GO:0005634");listOfGoTerms.add("GO:0006302");
        listOfGoTerms.add("GO:0006261");listOfGoTerms.add("GO:0007131");
        listOfGoTerms.add("GO:0000150");listOfGoTerms.add("GO:0000502");
        listOfGoTerms.add("GO:0006289");listOfGoTerms.add("GO:0008233");
        listOfGoTerms.add("GO:0007001");listOfGoTerms.add("GO:0010332");
        listOfGoTerms.add("GO:0033600");listOfGoTerms.add("GO:0006508");
        listOfGoTerms.add("GO:0001833");listOfGoTerms.add("GO:0033593");
        listOfGoTerms.add("GO:0007141");listOfGoTerms.add("GO:0043142");

        for (String goTerm : listOfGoTerms){
            String goName = handler.getNameById( goTerm );
            assertNotNull(goName);
        }
    }

}
