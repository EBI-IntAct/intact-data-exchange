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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.shared;

import psidev.psi.mi.xml.model.Interval;
import psidev.psi.mi.xml.model.Position;
import psidev.psi.mi.xml.model.RangeStatus;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.model.CvFuzzyType;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.Range;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RangeConverter extends AbstractIntactPsiConverter<Range, psidev.psi.mi.xml.model.Range> {

    public RangeConverter(Institution institution) {
        super(institution);
    }


    public Range psiToIntact(psidev.psi.mi.xml.model.Range psiObject) {
        int from = Long.valueOf(psiObject.getBegin().getPosition()).intValue();
        int to = Long.valueOf(psiObject.getEnd().getPosition()).intValue();
        String seq = null;

        Range range = new Range(getInstitution(), from, to, seq);

        Interval beginInterval = psiObject.getBeginInterval();
        if (beginInterval != null) {
            int intervalFrom = Long.valueOf(beginInterval.getBegin()).intValue();
            int intervalTo = Long.valueOf(beginInterval.getEnd()).intValue();
            range.setFromIntervalStart(intervalFrom);
            range.setFromIntervalEnd(intervalTo);
        }

        Interval endInterval = psiObject.getEndInterval();
        if (endInterval != null) {
            int intervalFrom = Long.valueOf(endInterval.getBegin()).intValue();
            int intervalTo = Long.valueOf(endInterval.getEnd()).intValue();
            range.setToIntervalStart(intervalFrom);
            range.setToIntervalEnd(intervalTo);
        }

        CvObjectConverter<CvFuzzyType,RangeStatus> fuzzyTypeConverter =
                new CvObjectConverter<CvFuzzyType,RangeStatus>(getInstitution(), CvFuzzyType.class, RangeStatus.class);

        CvFuzzyType fromFuzzyType = fuzzyTypeConverter.psiToIntact(psiObject.getStartStatus());
        range.setFromCvFuzzyType(fromFuzzyType);

        CvFuzzyType toFuzzyType = fuzzyTypeConverter.psiToIntact(psiObject.getEndStatus());
        range.setToCvFuzzyType(toFuzzyType);

        return range;
    }

    public psidev.psi.mi.xml.model.Range intactToPsi(Range intactObject) {
        psidev.psi.mi.xml.model.Range psiRange = new psidev.psi.mi.xml.model.Range();

        long beginIntervalFrom = intactObject.getFromIntervalStart();
        long beginIntervalTo = intactObject.getFromIntervalEnd();
        long endIntervalFrom = intactObject.getToIntervalStart();
        long endIntervalTo = intactObject.getToIntervalEnd();

        psiRange.setBegin(new Position(beginIntervalFrom));
        psiRange.setEnd(new Position(endIntervalTo));

        if (beginIntervalTo > beginIntervalFrom) {
            Interval beginInterval = new Interval(beginIntervalFrom, beginIntervalTo);
            psiRange.setBeginInterval(beginInterval);
        }

        if (endIntervalTo > endIntervalFrom) {
            Interval endInterval = new Interval(endIntervalFrom, endIntervalTo);
            psiRange.setEndInterval(endInterval);
        }

        CvObjectConverter<CvFuzzyType,RangeStatus> fuzzyTypeConverter =
                        new CvObjectConverter<CvFuzzyType,RangeStatus>(getInstitution(), CvFuzzyType.class, RangeStatus.class);

        RangeStatus startStatus = fuzzyTypeConverter.intactToPsi(intactObject.getFromCvFuzzyType());
        psiRange.setStartStatus(startStatus);

        RangeStatus endStatus = fuzzyTypeConverter.intactToPsi(intactObject.getToCvFuzzyType());
        psiRange.setEndStatus(endStatus);


        return psiRange;
    }
}