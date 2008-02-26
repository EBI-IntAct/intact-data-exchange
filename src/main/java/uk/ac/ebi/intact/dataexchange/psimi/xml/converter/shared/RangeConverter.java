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
        Integer beginIntervalFrom = null;
        Integer beginIntervalTo = null;
        Integer endIntervalFrom = null;
        Integer endIntervalTo = null;

        if (psiObject.getBegin() != null) {
            final int begin = Long.valueOf(psiObject.getBegin().getPosition()).intValue();
            beginIntervalFrom = begin;
            beginIntervalTo = begin;
        }
        if (psiObject.getEnd() != null) {
            final int end = Long.valueOf(psiObject.getEnd().getPosition()).intValue();
            endIntervalFrom = end;
            endIntervalTo = end;
        }

        Interval beginInterval = psiObject.getBeginInterval();
        if (beginInterval != null) {
            beginIntervalFrom = Long.valueOf(beginInterval.getBegin()).intValue();
            beginIntervalTo = Long.valueOf(beginInterval.getEnd()).intValue();
        }

        Interval endInterval = psiObject.getEndInterval();
        if (endInterval != null) {
            endIntervalFrom = Long.valueOf(endInterval.getBegin()).intValue();
            endIntervalTo = Long.valueOf(endInterval.getEnd()).intValue();
        }

        String seq = null;

        Range range = new Range(getInstitution(), beginIntervalFrom, endIntervalTo, seq);
        range.setFromIntervalStart(beginIntervalFrom);
        range.setFromIntervalEnd(beginIntervalTo);
        range.setToIntervalStart(endIntervalFrom);
        range.setToIntervalEnd(endIntervalTo);

        CvObjectConverter<CvFuzzyType,RangeStatus> fuzzyTypeConverter =
                new CvObjectConverter<CvFuzzyType,RangeStatus>(getInstitution(), CvFuzzyType.class, RangeStatus.class);

        final RangeStatus startStatus = psiObject.getStartStatus();

        if (startStatus != null) {
            CvFuzzyType fromFuzzyType = fuzzyTypeConverter.psiToIntact(startStatus);
            range.setFromCvFuzzyType(fromFuzzyType);
        }

        final RangeStatus endStatus = psiObject.getEndStatus();

        if (endStatus != null) {
            CvFuzzyType toFuzzyType = fuzzyTypeConverter.psiToIntact(endStatus);
            range.setToCvFuzzyType(toFuzzyType);
        }

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

        final CvFuzzyType fromFuzzyType = intactObject.getFromCvFuzzyType();

        if (fromFuzzyType != null) {
            RangeStatus startStatus = fuzzyTypeConverter.intactToPsi(fromFuzzyType);
            psiRange.setStartStatus(startStatus);
        }

        final CvFuzzyType toFuzzyType = intactObject.getToCvFuzzyType();

        if (toFuzzyType != null) {
            RangeStatus endStatus = fuzzyTypeConverter.intactToPsi(toFuzzyType);
            psiRange.setEndStatus(endStatus);
        }


        return psiRange;
    }
}