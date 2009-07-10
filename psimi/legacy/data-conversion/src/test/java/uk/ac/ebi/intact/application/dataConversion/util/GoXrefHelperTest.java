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
package uk.ac.ebi.intact.application.dataConversion.util;

import org.junit.Test;
import org.junit.Assert;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class GoXrefHelperTest {

    @Test
    public void test_default() throws Exception {
        //C
        GoXrefHelper goXrefHelper = new GoXrefHelper("GO:0005737");
        Assert.assertEquals("component", goXrefHelper.getQualifier());
        Assert.assertEquals("C:cytoplasm", goXrefHelper.getSecondaryId());

        //F
        goXrefHelper = new GoXrefHelper("GO:0005520");
        Assert.assertEquals("function", goXrefHelper.getQualifier());
        Assert.assertEquals("F:insulin-like growth factor binding", goXrefHelper.getSecondaryId());

        //P
        goXrefHelper = new GoXrefHelper("GO:0045663");
        Assert.assertEquals("process", goXrefHelper.getQualifier());
        Assert.assertEquals("P:positive regulation of myoblast differentiation", goXrefHelper.getSecondaryId());

        goXrefHelper = new GoXrefHelper("GO:0005856");
        Assert.assertEquals("component", goXrefHelper.getQualifier());
        Assert.assertEquals("C:cytoskeleton", goXrefHelper.getSecondaryId());
    }
}
