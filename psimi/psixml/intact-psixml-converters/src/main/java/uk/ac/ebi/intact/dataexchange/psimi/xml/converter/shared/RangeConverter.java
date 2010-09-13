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

import psidev.psi.mi.xml.model.*;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.AbstractIntactPsiConverter;
import uk.ac.ebi.intact.dataexchange.psimi.xml.converter.PsiConversionException;
import uk.ac.ebi.intact.model.CvFuzzyType;
import uk.ac.ebi.intact.model.Institution;
import uk.ac.ebi.intact.model.Range;
import uk.ac.ebi.intact.model.util.FeatureUtils;

/**
 * Range Converter.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class RangeConverter extends AbstractIntactPsiConverter<Range, psidev.psi.mi.xml.model.Range> {

    public static final String CERTAIN_MI_REF = "MI:0335";
    public static final String MORE_THAN_MI_REF = "MI:0336";
    public static final String RANGE_MI_REF = "MI:0338";
    public static final String LESS_THAN_MI_REF = "MI:0337";
    public static final String UNDETERMINED_MI_REF = "MI:0339";
    public static final String CTERMINAL_MI_REF = "MI:0334";
    public static final String NTERMINAL_MI_REF = "MI:0340";
    public static final String RAGGED_NTERMINUS_MI_REF = "MI:0341";

    public static final String CERTAIN = "certain";
    public static final String MORE_THAN = "greater-than";
    public static final String RANGE = "range";
    public static final String LESS_THAN = "less-than";
    public static final String UNDETERMINED = "undetermined";
    public static final String CTERMINAL = "c-terminal";
    public static final String NTERMINAL = "n-terminal";
    public static final String RAGGED_NTERMINUS = "ragged n-terminus";

    public RangeConverter(Institution institution) {
        super(institution);
    }

    public Range psiToIntact(psidev.psi.mi.xml.model.Range psiObject) {
        Integer beginIntervalFrom = null;
        Integer beginIntervalTo = null;
        Integer endIntervalFrom = null;
        Integer endIntervalTo = null;

        // collect the range positions
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

        // collect the range CvFuzzyTypes
        CvObjectConverter<CvFuzzyType,RangeStatus> fuzzyTypeConverter =
                new CvObjectConverter<CvFuzzyType,RangeStatus>(getInstitution(), CvFuzzyType.class, RangeStatus.class);

        final RangeStatus startStatus = psiObject.getStartStatus();

        CvFuzzyType fromFuzzyType = null;
        if (startStatus != null) {
            fromFuzzyType = fuzzyTypeConverter.psiToIntact(startStatus);
        }

        final RangeStatus endStatus = psiObject.getEndStatus();

        CvFuzzyType toFuzzyType = null;
        if (endStatus != null) {
            toFuzzyType = fuzzyTypeConverter.psiToIntact(endStatus);
        }
        String seq = null;

        // if the positions are null but the range status is 'range', 'less-than', 'greater-than', 'certain' or 'ragged n-terminus' it is an error
        if ((beginIntervalFrom == null || beginIntervalTo == null) && (isRange(psiObject.getStartStatus())
                || isMoreThan(psiObject.getStartStatus()) || isLessThan( psiObject.getStartStatus())
                || isCertain( psiObject.getStartStatus()) || isRaggedNTerminal( psiObject.getStartStatus()))){
            throw new PsiConversionException( "Cannot convert a range start position of type range, less-than, more-than or certain without specific location (begin, end) to the IntAct data model." );
        }
        else if ((endIntervalFrom == null || endIntervalTo == null) && (isRange(psiObject.getEndStatus())
                || isMoreThan(psiObject.getEndStatus()) || isLessThan( psiObject.getEndStatus())
                || isCertain( psiObject.getEndStatus()) || isRaggedNTerminal( psiObject.getEndStatus() ))){
            throw new PsiConversionException( "Cannot convert a range end position of type range, less-than, more-than or certain without specific location (begin, end) to the IntAct data model." );
        }
        else if ( beginIntervalFrom == null || endIntervalTo == null ) {

            // set the values to 0 as this means undertermined in the IntAct model
            beginIntervalFrom = 0;
            beginIntervalTo = 0;
            endIntervalFrom = 0;
            endIntervalTo = 0;
        }

        // create the range
        Range range = new Range(getInstitution(), beginIntervalFrom, beginIntervalTo, endIntervalFrom, endIntervalTo, seq);
        range.setFromCvFuzzyType(fromFuzzyType);
        range.setToCvFuzzyType(toFuzzyType);

        // check possible errors
        if (fromFuzzyType.isUndetermined() && (beginIntervalFrom > 0 || beginIntervalTo > 0 || beginIntervalFrom != beginIntervalTo)){
            throw new PsiConversionException( "Cannot convert the range " + range.toString() + ". The start position is undetermined and we should have a position null" +
                    " or equal to 0 instead of "+beginIntervalFrom+". A position interval is not allowed for this status.");
        }
        else if (fromFuzzyType.isNTerminal() && (beginIntervalFrom > 1 || beginIntervalTo > 1 || beginIntervalFrom != beginIntervalTo) ){
            throw new PsiConversionException( "Cannot convert the range " + range.toString() + ". The start position is n-terminal and we should have a position equal to 1" +
                    " instead of "+beginIntervalFrom+". A position interval is not allowed for this status.");
        }
        else if (fromFuzzyType.isCTerminal() && (beginIntervalFrom < 0 || beginIntervalTo < 0 || beginIntervalFrom != beginIntervalTo) ){
            throw new PsiConversionException( "Cannot convert the range " + range.toString() + ". The start position is c-terminal and we should have a position equal to the sequence length" +
                    " (or 0 if we don't know the sequence length) instead of "+beginIntervalFrom+". A position interval is not allowed for this status.");
        }

        if (toFuzzyType.isUndetermined() && (endIntervalFrom > 0 || endIntervalTo > 0 || endIntervalFrom != endIntervalTo)){
            throw new PsiConversionException( "Cannot convert the range " + range.toString() + ". The end position is undetermined and we should have a position null" +
                    " or equal to 0. A position interval is not allowed for this status.");
        }
        else if (toFuzzyType.isNTerminal() && (endIntervalFrom > 1 || endIntervalTo > 1 || endIntervalFrom != endIntervalTo) ){
            throw new PsiConversionException( "Cannot convert the range " + range.toString() + ". The end position is n-terminal and we should have a position equal to 1" +
                    " instead of "+endIntervalTo+". A position interval is not allowed for this status.");
        }
        else if (toFuzzyType.isCTerminal() && (endIntervalFrom < 0 || endIntervalTo < 0 || endIntervalFrom != endIntervalTo) ){
            throw new PsiConversionException( "Cannot convert the range " + range.toString() + ". The start position is c-terminal and we should have a position equal to the sequence length" +
                    " (or 0 if we don't know the sequence length) instead of "+endIntervalTo+". A position interval is not allowed for this status.");
        }

        // correct positions for undetermined, n-terminal or c-terminal
        FeatureUtils.correctRangePositionsAccordingToType(range, seq);

        // check if it is a bad range
        if (FeatureUtils.isABadRange(range, seq)){
            throw new PsiConversionException( "Cannot convert the range " + range.toString() + "." + FeatureUtils.getBadRangeInfo(range, seq) );
        }

        return range;
    }

    private boolean isRange( RangeStatus rangeStatus ) {
        if (rangeStatus != null){
            if ( isStatusOfType( rangeStatus, RANGE, RANGE_MI_REF ) ) {
                return true;
            }
        }

        return false;
    }

    private boolean isLessThan( RangeStatus rangeStatus ) {

        if ( isStatusOfType( rangeStatus, LESS_THAN, LESS_THAN_MI_REF ) ) {
            return true;
        }
        return false;
    }

    private boolean isMoreThan( RangeStatus rangeStatus ) {
        if ( isStatusOfType( rangeStatus, MORE_THAN, MORE_THAN_MI_REF ) ) {
            return true;
        }
        return false;
    }

    private boolean isCertain( RangeStatus rangeStatus ) {
        if ( isStatusOfType( rangeStatus, CERTAIN, CERTAIN_MI_REF ) ) {
            return true;
        }
        return false;
    }

    private boolean isUndetermined( RangeStatus rangeStatus ) {
        if ( isStatusOfType( rangeStatus, UNDETERMINED, UNDETERMINED_MI_REF ) ) {
            return true;
        }
        return false;
    }

    private boolean isCTerminal( RangeStatus rangeStatus ) {
        if ( isStatusOfType( rangeStatus, CTERMINAL, CTERMINAL_MI_REF ) ) {
            return true;
        }
        return false;
    }

    private boolean isNTerminal( RangeStatus rangeStatus ) {
        if ( isStatusOfType( rangeStatus, NTERMINAL, NTERMINAL_MI_REF ) ) {
            return true;
        }
        return false;
    }

    private boolean isRaggedNTerminal( RangeStatus rangeStatus ) {
        if ( isStatusOfType( rangeStatus, RAGGED_NTERMINUS, RAGGED_NTERMINUS ) ) {
            return true;
        }
        return false;
    }

    private boolean isStatusOfType( RangeStatus status, String psimiName, String psimiIdentifier ) {
        if ( status.getXref() != null ) {
            final DbReference ref = status.getXref().getPrimaryRef();
            return psimiIdentifier.equalsIgnoreCase( ref.getId() );
        }
        else if (status.getNames() != null){
            final Names names = status.getNames();

            return psimiName.equalsIgnoreCase(names.getShortLabel()) || psimiName.equalsIgnoreCase(names.getFullName());
        }
        return false;
    }

    public psidev.psi.mi.xml.model.Range intactToPsi(Range intactObject) {
        psidev.psi.mi.xml.model.Range psiRange = new psidev.psi.mi.xml.model.Range();

        // get the positions
        long beginIntervalFrom = intactObject.getFromIntervalStart();
        long beginIntervalTo = intactObject.getFromIntervalEnd();
        long endIntervalFrom = intactObject.getToIntervalStart();
        long endIntervalTo = intactObject.getToIntervalEnd();

        if( beginIntervalFrom != 0 ) {
            psiRange.setBegin(new Position(beginIntervalFrom));
        }

        if( endIntervalTo != 0 ) {
            psiRange.setEnd(new Position(endIntervalTo));
        }

        if ( beginIntervalTo > beginIntervalFrom && beginIntervalTo != 0 ) {
            Interval beginInterval = new Interval(beginIntervalFrom, beginIntervalTo);
            psiRange.setBeginInterval(beginInterval);
        }

        if ( endIntervalTo > endIntervalFrom && endIntervalTo != 0 ) {
            Interval endInterval = new Interval(endIntervalFrom, endIntervalTo);
            psiRange.setEndInterval(endInterval);
        }

        // set the range status
        CvObjectConverter<CvFuzzyType,RangeStatus> fuzzyTypeConverter =
                new CvObjectConverter<CvFuzzyType,RangeStatus>( getInstitution(),
                        CvFuzzyType.class,
                        RangeStatus.class );

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