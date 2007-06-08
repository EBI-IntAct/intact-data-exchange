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
package uk.ac.ebi.intact.dataexchange.psimi.xml.tools;

import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.validator.ValidationReport;
import uk.ac.ebi.intact.dataexchange.psimi.xml.tools.validator.ValidationReportImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsiProcessReport {

    private ValidationReport validationReport;
    private List<PsiProcessReport> childReports;

    public PsiProcessReport(ValidationReport validationReport) {
        this.validationReport = validationReport;

        childReports = new ArrayList<PsiProcessReport>();
    }

    public ValidationReport getValidationReport() {
        if (validationReport == null) {
            return new ValidationReportImpl();
        }
        return validationReport;
    }

    public void mergeWith(PsiProcessReport subReport) {
        if (subReport == null) {
            throw new NullPointerException("Cannot merge with null subReport");
        }

        childReports.add(subReport);
    }

    public List<PsiProcessReport> getChildReports() {
        return childReports;
    }
}