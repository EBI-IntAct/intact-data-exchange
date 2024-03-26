
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
package uk.ac.ebi.intact.psimitab.converters.util;
import psidev.psi.mi.jami.model.Annotation;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Position;
import psidev.psi.mi.jami.model.Range;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Tools to manage PSIMITAB files
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class PsimitabTools {

    /**
     * Do not instantiate PsimitabTools.
     */
    private PsimitabTools() {
    }

    /**
     * Alters the order of interactorA and interactorB of a binary interaction, according to a comparator.
     * @param binaryInteraction the binary interaction
     * @param comparator the comparator to use
     */
    public static void reorderInteractors(BinaryInteraction binaryInteraction, Comparator<Interactor> comparator) {
        if (binaryInteraction == null) throw new NullPointerException("binaryInteraction is null");
        if (comparator == null) throw new NullPointerException("comparator is null");

        Interactor interactorA = binaryInteraction.getInteractorA();
        Interactor interactorB = binaryInteraction.getInteractorB();

        if (comparator.compare(interactorA, interactorB) < 0) {
            binaryInteraction.setInteractorA(interactorB);
            binaryInteraction.setInteractorB(interactorA);
        }
    }

    /**
     * Finds an Annotations with a topic that has an MI or label equal to the value provided
     *
     * @param annotations The annotations to search in
     * @param miOrLabel   The MI (use it when possible) or the shortLabel
     * @return The annotation with that CvTopic. Null if no annotation for that CV is found
     * @since 1.8.0
     */
    public static Annotation findAnnotationByTopicMiOrLabel(Collection<Annotation> annotations, String miOrLabel) {
        for (Annotation annotation : annotations) {
            final CvTerm topic = annotation.getTopic();
            if (topic != null && (miOrLabel.equals(topic.getMIIdentifier()) || miOrLabel.equals(topic.getShortName()))) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * @param range : the range to convert
     * @return the range as a String
     *         If the range is invalid, will return fromIntervalStart-toIntervalEnd
     */
    public static String convertRangeIntoString(Range range) {
        if (range == null) {
            throw new IllegalArgumentException("The range cannot be null.");
        }

        if (isABadRange(range, null)) {
            return range.getStart().getStart() + "-" + range.getEnd().getEnd();
            //throw new IllegalRangeException(getBadRangeInfo(range, null));
        }

        String startPosition = positionToString(range.getStart().getStatus(), range.getStart().getStart(), range.getStart().getEnd());
        String endPosition = positionToString(range.getEnd().getStatus(), range.getEnd().getStart(), range.getEnd().getEnd());

        return startPosition + "-" + endPosition;
    }

    /**
     * @param range    : the range to check
     * @param sequence : the sequence of the protein
     * @return true if the range is within the sequence, coherent with its fuzzy type and not overlapping
     */
    public static boolean isABadRange(Range range, String sequence) {
        return (getBadRangeInfo(range, sequence) != null);
    }

    /**
     * @param range    : the range to check
     * @param sequence : the sequence of the protein
     * @return true if the range is within the sequence, coherent with its fuzzy type and not overlapping
     */
    public static String getBadRangeInfo(Range range, String sequence) {

        // a range null is not a valid range for a feature
        if (range == null) {
            return "Range is null";
        }

        // get the start and end status of the range
        final Position start = range.getStart();
        final Position end = range.getEnd();

        if (start == null || start.getStatus() == null) {
            return "The start status of the range is null and it is mandatory for PSI-MI.";
        }
        if (end == null || end.getStatus() == null) {
            return "The end status of the range is null and it is mandatory for PSI-MI.";
        }
        final CvTerm startStatus = start.getStatus();
        final CvTerm endStatus = end.getStatus();

        // If the range is the start status and the begin position (s) are not consistent, or the end status and the end position (s) are not consistent
        // or the start status is not consistent with the end status, the range is not valid
        final long fromIntervalStart = start.getStart();
        final long fromIntervalEnd = start.getEnd();
        final long toIntervalStart = end.getStart();
        final long toIntervalEnd = end.getEnd();

        String areRangePositionsAccordingToTypeOkStart = getRangePositionsAccordingToRangeTypeErrorMessage(startStatus, fromIntervalStart, fromIntervalEnd, sequence);

        String areRangePositionsAccordingToTypeOkEnd = getRangePositionsAccordingToRangeTypeErrorMessage(endStatus, toIntervalStart, toIntervalEnd, sequence);

        if (areRangePositionsAccordingToTypeOkStart != null) {
            return areRangePositionsAccordingToTypeOkStart;
        }
        if (areRangePositionsAccordingToTypeOkEnd != null) {
            return areRangePositionsAccordingToTypeOkEnd;
        }
        if (areRangeStatusInconsistent(startStatus, endStatus)) {
            return "Start status " + startStatus.getShortName() + " and end status " + endStatus.getShortName() + " are inconsistent";
        }

        // if the range has not a position undetermined, C terminal region or N-terminal region, we check if the range positions are not overlapping
        if (!(startStatus.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI) ||
                startStatus.getMIIdentifier().equals(Position.UNDETERMINED_MI) ||
                startStatus.getMIIdentifier().equals(Position.N_TERMINAL_RANGE_MI)) &&
                !(endStatus.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI) ||
                        endStatus.getMIIdentifier().equals(Position.UNDETERMINED_MI) ||
                        endStatus.getMIIdentifier().equals(Position.N_TERMINAL_RANGE_MI)) &&
                areRangePositionsOverlapping(range)) {
            return "The range positions overlap : " + startStatus.getShortName() + ":" + fromIntervalStart + "-" + fromIntervalEnd + "," + endStatus.getShortName() + ":" + toIntervalStart + "-" + toIntervalEnd;
        }

        return null;
    }

    /**
     * @param rangeType : the status of the position
     * @param start     : the start of the position
     * @param end       : the end of the position (equal to start if the range position is a single position and not an interval)
     * @param sequence  : the sequence of the protein
     * @return message with the error. Null otherwise
     */
    public static String getRangePositionsAccordingToRangeTypeErrorMessage(CvTerm rangeType, long start, long end, String sequence) {

        if (rangeType == null) {
            throw new IllegalArgumentException("It is not possible to check if the range status is compliant with the range positions because it is null and mandatory.");
        }

        // the sequence length is 0 if the sequence is null
        int sequenceLength = 0;

        if (sequence != null) {
            sequenceLength = sequence.length();
        }

        // the position status is defined
        // undetermined position, we expect to have a position equal to 0 for both the start and the end
        if (rangeType.getMIIdentifier().equals(Position.UNDETERMINED_MI) ||
                rangeType.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI) ||
                rangeType.getMIIdentifier().equals(Position.N_TERMINAL_RANGE_MI)) {
            if (start != 0 || end != 0) {
                return "Undetermined positions (undetermined, N-terminal region, C-terminal region) must always be 0. Actual positions : " + start + "-" + end;
            }
        }
        // n-terminal position : we expect to have a position equal to 1 for both the start and the end
        else if (rangeType.getMIIdentifier().equals(Position.N_TERMINAL_MI)) {
            if (start != 1 || end != 1) {
                return "N-terminal positions must always be 1. Actual positions : " + start + "-" + end;
            }
        }
        // c-terminal position : we expect to have a position equal to the sequence length (0 if the sequence is null) for both the start and the end
        else if (rangeType.getMIIdentifier().equals(Position.C_TERMINAL_MI)) {
            if (sequenceLength == 0 && (start < 0 || end < 0 || start != end)) {
                return "C-terminal positions must always be superior to 0. Actual positions : " + start + "-" + end;
            } else if ((start != sequenceLength || end != sequenceLength) && sequenceLength > 0) {
                return "C-terminal positions must always be equal to the length of the protein sequence. Actual positions : " + start + "-" + end + ", sequence length " + sequenceLength;
            }
        }
        // greater than position : we don't expect an interval for this position so the start should be equal to the end
        else if (rangeType.getMIIdentifier().equals(Position.GREATER_THAN_MI)) {
            if (start != end) {
                return "Greater than positions must always be a single position and here it is an interval. Actual positions : " + start + "-" + end;
            }

            // The sequence is null, all we can expect is at least a start superior to 0.
            if (sequenceLength == 0) {
                if (start <= 0) {
                    return "Greater than positions must always be strictly superior to 0. Actual positions : " + start + "-" + end;
                }
            }
            // The sequence is not null, we expect to have positions superior to 0 and STRICTLY inferior to the sequence length
            else {
                if (start >= sequenceLength || start <= 0) {
                    return "Greater than positions must always be strictly superior to 0 and strictly inferior to the protein sequence length. Actual positions : " + start + "-" + end + ", sequence length " + sequenceLength;
                }
            }
        }
        // less than position : we don't expect an interval for this position so the start should be equal to the end
        else if (rangeType.getMIIdentifier().equals(Position.LESS_THAN_MI)) {
            if (start != end) {
                return "Less than positions must always be a single position and here it is an interval. Actual positions : " + start + "-" + end;
            }
            // The sequence is null, all we can expect is at least a start STRICTLY superior to 1.
            if (sequenceLength == 0) {
                if (start <= 1) {
                    return "Less than positions must always be strictly superior to 1. Actual positions : " + start + "-" + end;
                }
            }
            // The sequence is not null, we expect to have positions STRICTLY superior to 1 and inferior or equal to the sequence length
            else {
                if (start <= 1 || start > sequenceLength) {
                    return "Less than positions must always be strictly superior to 1 and inferior or equal to the protein sequence length. Actual positions : " + start + "-" + end + ", sequence length " + sequenceLength;
                }
            }
        }
        // if the range position is certain or ragged-n-terminus, we expect to have the positions superior to 0 and inferior or
        // equal to the sequence length (only possible to check if the sequence is not null)
        // We don't expect any interval for this position so the start should be equal to the end
        else if (rangeType.getMIIdentifier().equals(Position.CERTAIN_MI) ||
                rangeType.getMIIdentifier().equals(Position.RAGGED_N_TERMINAL_MI)) {
            if (start != end) {
                return "Certain and ragged-n-terminus positions must always be a single position and here it is an interval. Actual positions : " + start + "-" + end;
            }

            if (sequenceLength == 0) {
                if (start <= 0) {
                    return "Certain and ragged-n-terminus positions must always be strictly superior to 0. Actual positions : " + start + "-" + end;
                }
            } else {
                if (areRangePositionsOutOfBounds(start, end, sequenceLength)) {
                    return "Certain and ragged-n-terminus positions must always be strictly superior to 0 and inferior or equal to the protein sequence length. Actual positions : " + start + "-" + end + ", sequence length " + sequenceLength;
                }
            }
        }
        // the range status is not well known, so we allow the position to be an interval, we just check that the start and end are superior to 0 and inferior to the sequence
        // length (only possible to check if the sequence is not null)
        else {
            if (sequenceLength == 0) {
                if (areRangePositionsInvalid(start, end) || start <= 0 || end <= 0) {
                    return rangeType.getShortName() + " positions must always be strictly superior to 0 and the end must be superior or equal to the start. Actual positions : " + start + "-" + end;
                }
            } else {
                if (areRangePositionsInvalid(start, end) || start <= 0 || end <= 0) {
                    return rangeType.getShortName() + " positions must always have an end superior or equal to the start. Actual positions : " + start + "-" + end;
                } else if (areRangePositionsOutOfBounds(start, end, sequenceLength)) {
                    return rangeType.getShortName() + " positions must always be strictly superior to 0 and inferior or equal to the sequence length. Actual positions : " + start + "-" + end + ", sequence length " + sequenceLength;
                }
            }
        }
        return null;
    }

    /**
     * A position is out of bound if inferior or equal to 0 or superior to the sequence length.
     *
     * @param start          : the start position of the interval
     * @param end            : the end position of the interval
     * @param sequenceLength : the length of the sequence, 0 if the sequence is null
     * @return true if the start or the end is inferior or equal to 0 and if the start or the end is superior to the sequence length
     */
    public static boolean areRangePositionsOutOfBounds(long start, long end, int sequenceLength) {
        return start <= 0 || end <= 0 || start > sequenceLength || end > sequenceLength;
    }

    /**
     * A range interval is invalid if the start is after the end
     *
     * @param start : the start position of the interval
     * @param end   : the end position of the interval
     * @return true if the start is after the end
     */
    public static boolean areRangePositionsInvalid(long start, long end) {

        if (start > end) {
            return true;
        }
        return false;
    }

    /**
     * @param startStatus : the status of the start position
     * @param endStatus   : the status of the end position
     * @return true if the range status are inconsistent (n-terminal is the end, c-terminal is the beginning)
     */
    public static boolean areRangeStatusInconsistent(CvTerm startStatus, CvTerm endStatus) {

        if (startStatus == null) {
            throw new IllegalArgumentException("It is not possible to check if the start range status is compliant with the range positions because it is null and mandatory.");
        }

        if (endStatus == null) {
            throw new IllegalArgumentException("It is not possible to check if the end range status is compliant with the range positions because it is null and mandatory.");
        }

        // both status are not null
        // the start position is C-terminal but the end position is different from C-terminal
        if (startStatus.getMIIdentifier().equals(Position.C_TERMINAL_MI) &&
                endStatus.getMIIdentifier().equals(Position.C_TERMINAL_MI)) {
            return true;
        }
        // the end position is N-terminal but the start position is different from N-terminal
        else if (startStatus.getMIIdentifier().equals(Position.N_TERMINAL_MI) &&
                endStatus.getMIIdentifier().equals(Position.N_TERMINAL_MI)) {
            return true;
        }
        // the end status is C terminal region, the start status can only be C-terminal region or C-terminal
        else if (startStatus.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI) &&
                !(endStatus.getMIIdentifier().equals(Position.C_TERMINAL_MI) ||
                        endStatus.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI))) {
            return true;
        }
        // the start status is N terminal region, the end status can only be N-terminal region or N-terminal
        else if (startStatus.getMIIdentifier().equals(Position.N_TERMINAL_MI) &&
                !(endStatus.getMIIdentifier().equals(Position.N_TERMINAL_MI) ||
                        endStatus.getMIIdentifier().equals(Position.N_TERMINAL_RANGE_MI))) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the interval positions of the range are overlapping
     *
     * @param range
     * @return true if the range intervals are overlapping
     */
    public static boolean areRangePositionsOverlapping(Range range) {
        // get the range status
        final Position start = range.getStart();
        final Position end = range.getEnd();

        if (start == null || start.getStatus() == null) {
            throw new IllegalArgumentException("It is not possible to check if the start range status is compliant with the range positions because it is null and mandatory.");
        }
        if (end == null || end.getStatus() == null) {
            throw new IllegalArgumentException("It is not possible to check if the end range status is compliant with the range positions because it is null and mandatory.");
        }
        final CvTerm startStatus = start.getStatus();
        final CvTerm endStatus = end.getStatus();

        // both the end and the start have a specific status
        // in the specific case where the start is superior to a position and the end is inferior to another position, we need to check that the
        // range is not invalid because 'greater than' and 'less than' are both exclusive
        if (startStatus.getMIIdentifier().equals(Position.GREATER_THAN_MI) &&
                endStatus.getMIIdentifier().equals(Position.LESS_THAN_MI) &&
                end.getEnd() - start.getStart() < 2) {
            return true;
        }
        // we have a greater than start position and the end position is equal to the start position
        else if (startStatus.getMIIdentifier().equals(Position.GREATER_THAN_MI) &&
                !endStatus.getMIIdentifier().equals(Position.GREATER_THAN_MI) &&
                start.getStart() == end.getStart()) {
            return true;
        }
        // we have a less than end position and the start position is equal to the start position
        else if (!startStatus.getMIIdentifier().equals(Position.LESS_THAN_MI) &&
                endStatus.getMIIdentifier().equals(Position.LESS_THAN_MI) &&
                start.getEnd() == end.getEnd()) {
            return true;
        }
        // As the range positions are 0 when the status is undetermined, we can only check if the ranges are not overlapping when both start and end are not undetermined
        if (!(startStatus.getMIIdentifier().equals(Position.UNDETERMINED_MI) ||
                startStatus.getMIIdentifier().equals(Position.N_TERMINAL_RANGE_MI) ||
                startStatus.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI)) &&
                !(endStatus.getMIIdentifier().equals(Position.UNDETERMINED_MI) ||
                        endStatus.getMIIdentifier().equals(Position.N_TERMINAL_RANGE_MI) ||
                        endStatus.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI))) {
            return arePositionsOverlapping(start.getStart(), start.getEnd(), end.getStart(), end.getEnd());
        }

        return false;
    }

    private static String positionToString(CvTerm status, long start, long end) {
        String position;

        if (status.getMIIdentifier().equals(Position.UNDETERMINED_MI)) {
            position = "?";
        } else if (status.getMIIdentifier().equals(Position.C_TERMINAL_RANGE_MI)) {
            position = "c";
        } else if (status.getMIIdentifier().equals(Position.N_TERMINAL_RANGE_MI)) {
            position = "n";
        } else if (status.getMIIdentifier().equals(Position.GREATER_THAN_MI)) {
            position = ">" + start;
        } else if (status.getMIIdentifier().equals(Position.LESS_THAN_MI)) {
            position = "<" + start;
        } else if (status.getMIIdentifier().equals(Position.RANGE_MI)) {
            position = start + ".." + end;
        } else {
            position = Long.toString(start);
        }

        return position;
    }

    /**
     * Checks if the interval positions are overlapping
     *
     * @param fromStart
     * @param fromEnd
     * @param toStart
     * @param toEnd
     * @return true if the range intervals are overlapping
     */
    public static boolean arePositionsOverlapping(long fromStart, long fromEnd, long toStart, long toEnd) {

        if (fromStart > toStart || fromEnd > toStart || fromStart > toEnd || fromEnd > toEnd) {
            return true;
        }
        return false;
    }

    public static Collection<Annotation> getPublicAnnotations(Collection<Annotation> annotations) {

        final Collection<Annotation> publicAnnotations = new ArrayList<>(annotations.size());
        final Iterator<Annotation> i = annotations.iterator();
        while (i.hasNext()) {
            Annotation annotation = i.next();
            if (isCvTopicPublic(annotation.getTopic())) {
                publicAnnotations.add(annotation);
            }
        }

        return publicAnnotations;
    }

    private static boolean isCvTopicPublic(CvTerm cvTopic) {
        for (Annotation annotation : cvTopic.getAnnotations()) {
            if(annotation.getTopic() != null){
                if (annotation.getTopic().getShortName().equals("hidden") || annotation.getTopic().getShortName().equals("no-export")) {
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }
}
