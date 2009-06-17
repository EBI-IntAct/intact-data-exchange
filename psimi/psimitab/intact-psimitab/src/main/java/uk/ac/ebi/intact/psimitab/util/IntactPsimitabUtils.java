/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.psimitab.util;

import psidev.psi.mi.tab.model.builder.*;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.BinaryInteraction;

import java.util.Collection;
import java.util.ArrayList;

import uk.ac.ebi.intact.psimitab.IntactDocumentDefinition;
import uk.ac.ebi.intact.psimitab.IntactBinaryInteraction;
import uk.ac.ebi.intact.psimitab.IntactInteractionRowConverter;

/**
 * Utilities for psimitab specific to IntAct.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactPsimitabUtils {

    private IntactPsimitabUtils() {

    }

    /**
     * Gets the main name for the interactor A.
     * @param binaryInteraction the binary interaction
     * @return the name for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static Field getInteractorANameField(BinaryInteraction binaryInteraction) {
        InteractionRowConverter rowConverter = new MitabInteractionRowConverter();
        Row row = rowConverter.createRow(binaryInteraction);

        return getInteractorANameField(row);
    }

    /**
     * Gets the main name for the interactor A.
     * @param binaryInteraction the binary interaction
     * @return the name for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static String getInteractorAName(BinaryInteraction binaryInteraction) {
        Field interactorNameField = getInteractorANameField(binaryInteraction);

        if (interactorNameField != null) {
            return interactorNameField.getValue();
        }

        return null;
    }

    /**
     * Gets the main name for the interactor B.
     * @param binaryInteraction the binary interaction
     * @return the name for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static Field getInteractorBNameField(BinaryInteraction binaryInteraction) {
        InteractionRowConverter rowConverter = new MitabInteractionRowConverter();
        Row row = rowConverter.createRow(binaryInteraction);

        return getInteractorBNameField(row);
    }

    /**
     * Gets the main name for the interactor A.
     * @param binaryInteraction the binary interaction
     * @return the name for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static String getInteractorBName(BinaryInteraction binaryInteraction) {
        Field interactorNameField = getInteractorBNameField(binaryInteraction);

        if (interactorNameField != null) {
            return interactorNameField.getValue();
        }

        return null;
    }

    /**
     * Gets the main name for the interactor A, based on the values from the alias and altid columns.
     * @param row the row
     * @return the name for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static Field getInteractorANameField(Row row) {
        Column idCol = row.getColumnByIndex(IntactDocumentDefinition.ID_INTERACTOR_A);
        Column altidCol = row.getColumnByIndex(IntactDocumentDefinition.ALTID_INTERACTOR_A);
        Column aliasCol = row.getColumnByIndex(IntactDocumentDefinition.ALIAS_INTERACTOR_A);

        return getInteractorNameField(idCol, altidCol, aliasCol);
    }

    /**
     * Gets the main name for the interactor B, based on the values from the alias and altid columns.
     * @param row the row
     * @return the name for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static Field getInteractorBNameField(Row row) {
        Column idCol = row.getColumnByIndex(IntactDocumentDefinition.ID_INTERACTOR_B);
        Column altidCol = row.getColumnByIndex(IntactDocumentDefinition.ALTID_INTERACTOR_B);
        Column aliasCol = row.getColumnByIndex(IntactDocumentDefinition.ALIAS_INTERACTOR_B);

        return getInteractorNameField(idCol, altidCol, aliasCol);
    }

    /**
     * Gets the main name for an interactor, based on the values from the alias and altid columns.
     * @param idCol the id column
     * @param altidCol the alternative id column
     * @param aliasCol the alias column
     * @return the name field for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static Field getInteractorNameField(Column idCol, Column altidCol, Column aliasCol) {
        final Collection<Field> idFields = idCol.getFields();
        final Collection<Field> altidFields = altidCol.getFields();
        final Collection<Field> aliasFields = aliasCol.getFields();

        Collection<Field> fields = new ArrayList<Field>(idFields.size() + altidFields.size() + aliasFields.size());
        fields.addAll(idFields);
        fields.addAll(altidFields);
        fields.addAll(aliasFields);

        Field interactorNameField = getInteractorNameField(fields);

        if (interactorNameField == null || idFields.isEmpty()) {
             return idFields.iterator().next();
        }

        return interactorNameField;
    }

    /**
     * Gets the main name for an interactor, based on the values from the alias and altid columns.
     * @param idCol the id column
     * @param altidCol the alternative id column
     * @param aliasCol the alias column
     * @return the name for the interactor. Could be null if none of the expected types is found
     * and the id column does not contain fields.
     */
    public static String getInteractorName(Column idCol, Column altidCol, Column aliasCol) {
        Field interactorNameField = getInteractorNameField(idCol, altidCol, aliasCol);

        if (interactorNameField != null) {
            return interactorNameField.getValue();
        }

        return null;
    }

    /**
     * Gets the main name for an interactor, based on the fields passed.
     * @param fields
     * @return
     */
    public static Field getInteractorNameField(Collection<Field> fields) {

        String[] priorities = new String[] {"gene name",
                                              "gene name synonym",
                                              "commercial name",
                                              "go synonym",
                                              "locus name",
                                              "orf name",
                                              "shortlabel",
                                              "uniprotkb",
                                              "chebi",
                                              "intact"};
        for (String priority : priorities) {
            for (Field field : fields) {
                if (priority.equals(field.getType()) || priority.equals(field.getDescription())) {
                    return field;
                }
            }
        }

        return null;
    }

}
