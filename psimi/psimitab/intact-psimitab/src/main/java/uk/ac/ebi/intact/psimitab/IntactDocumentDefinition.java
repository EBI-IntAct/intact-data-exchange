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
package uk.ac.ebi.intact.psimitab;

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.*;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class IntactDocumentDefinition extends MitabDocumentDefinition {

    public static final int EXPERIMENTAL_ROLE_A = 15;
    public static final int EXPERIMENTAL_ROLE_B = 16;
    public static final int BIOLOGICAL_ROLE_A = 17;
    public static final int BIOLOGICAL_ROLE_B = 18;
    public static final int PROPERTIES_A = 19;
    public static final int PROPERTIES_B = 20;
    public static final int INTERACTOR_TYPE_A = 21;
    public static final int INTERACTOR_TYPE_B = 22;
    public static final int HOST_ORGANISM = 23;
    public static final int EXPANSION_METHOD = 24;
    public static final int DATASET = 25;

    public IntactDocumentDefinition() {
        super();
        addColumnDefinition(new ColumnDefinition("Experimental role(s) interactor A", "experimentalRoleA", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Experimental role(s) interactor B", "experimentalRoleB", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Biological role(s) interactor A", "biologicalRoleA", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Biological role(s) interactor B", "biologicalRoleB", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Properties interactor A", "propertiesA", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Properties interactor B", "propertiesB", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Type(s) interactor A", "typeA", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Type(s) interactor B", "typeB", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("HostOrganism(s)", "hostOrganism", new CrossReferenceFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Expansion method(s)", "expansion", new PlainTextFieldBuilder()));
        addColumnDefinition(new ColumnDefinition("Dataset name(s)", "dataset", new PlainTextFieldBuilder()));
    }

    public InteractionRowConverter<BinaryInteraction> createInteractionRowConverter() {
        return new IntactInteractionRowConverter();
    }
}
