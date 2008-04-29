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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.intact.model.CvXrefQualifier;
import uk.ac.ebi.intact.util.go.GoServerProxy;
import uk.ac.ebi.intact.util.go.GoTerm;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class GoXrefHelper {

    private static final Log log = LogFactory.getLog(GoXrefHelper.class);

    private GoServerProxy goServer;
    private GoTerm goResponse;
    private GoTerm category;
    private String qualifier = null;
    private String secondaryId = null;

    public GoXrefHelper(String goId) {
        if (goId == null) {
            throw new IllegalArgumentException("The goId shouldn't be null");
        }
        goServer = new GoServerProxy();
        try {
            goResponse = goServer.query(goId);
            category = goResponse.getCategory();
            qualifier = calculateQualifierMiFromCategory(category);
            secondaryId = goResponse.getName();

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("GO term with id " + goId + " couldn't not be found");
            }
        }
    }

    private String calculateQualifierMiFromCategory(GoTerm goCategoryTerm) {
        if ("GO:0008150".equals(goCategoryTerm.getId())) { // process
            return CvXrefQualifier.PROCESS;
        } else if ("GO:0003674".equals(goCategoryTerm.getId())) { // function
            return CvXrefQualifier.FUNCTION;
        } else if ("GO:0005575".equals(goCategoryTerm.getId())) {  // component
            return CvXrefQualifier.COMPONENT;
        } else {
            throw new IllegalStateException("Illegal category: "+category.getId());
        }
    }

    public String getQualifier() {
        return this.qualifier;
    }

    public String getSecondaryId() {
        String secondaryId = null;
        if (qualifier != null) {
            if (qualifier.equals(CvXrefQualifier.COMPONENT)) {
                secondaryId = "C:" + this.secondaryId;
            } else if (qualifier.equals(CvXrefQualifier.FUNCTION)) {
                secondaryId = "F:" + this.secondaryId;
            } else if (qualifier.equals(CvXrefQualifier.PROCESS)) {
                secondaryId = "P:" + this.secondaryId;
            }
        }

        return secondaryId;
    }

    public static void main(String[] args) {
        //C
        GoXrefHelper goXrefHelper = new GoXrefHelper("GO:0005737");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());
        //F
        goXrefHelper = new GoXrefHelper("GO:0005520");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());

        //P
        goXrefHelper = new GoXrefHelper("GO:0045663");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());

        goXrefHelper = new GoXrefHelper("GO:0005856");
        System.out.println(goXrefHelper.getQualifier());
        System.out.println(goXrefHelper.getSecondaryId());
    }

}
    
