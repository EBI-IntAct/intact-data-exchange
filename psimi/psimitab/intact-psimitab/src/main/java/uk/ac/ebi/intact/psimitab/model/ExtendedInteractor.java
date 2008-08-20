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
package uk.ac.ebi.intact.psimitab.model;

import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.CrossReference;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * TODO comment that class header
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ExtendedInteractor extends Interactor {

    private List<CrossReference> experimentalRoles;

    private List<CrossReference> biologicalRoles;

    private List<CrossReference> properties;

    private CrossReference interactorType;

    private List<Annotation> annotations;

    private List<Parameter> parameters;

    public ExtendedInteractor() {
    }

    public ExtendedInteractor(Collection<CrossReference> identifiers) {
        super(identifiers);
    }

    public List<CrossReference> getExperimentalRoles() {
        if (experimentalRoles == null) {
            experimentalRoles = new ArrayList<CrossReference>(2);
        }
        return experimentalRoles;
    }

    public void setExperimentalRoles(List<CrossReference> experimentalRoles) {
        this.experimentalRoles = experimentalRoles;
    }

    public List<CrossReference> getBiologicalRoles() {
        if (experimentalRoles == null) {
            experimentalRoles = new ArrayList<CrossReference>(2);
        }

        return biologicalRoles;
    }

    public void setBiologicalRoles(List<CrossReference> biologicalRoles) {
        this.biologicalRoles = biologicalRoles;
    }

    public List<CrossReference> getProperties() {
        if (experimentalRoles == null) {
            experimentalRoles = new ArrayList<CrossReference>();
        }

        return properties;
    }

    public void setProperties(List<CrossReference> properties) {
        this.properties = properties;
    }

    public CrossReference getInteractorType() {
        return interactorType;
    }

    public void setInteractorType(CrossReference interactorType) {
        this.interactorType = interactorType;
    }

    public List<Annotation> getAnnotations() {
        if (annotations == null) {
            annotations = new ArrayList<Annotation>();
        }

        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public List<Parameter> getParameters() {
        if (parameters == null) {
            return new ArrayList<Parameter>();
        }
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public boolean hasExperimentalRoles() {
        return !( experimentalRoles == null || experimentalRoles.isEmpty() );
    }

    public boolean hasProperties() {
        return !( properties == null || properties.isEmpty() );
    }

    public boolean hasInteractorType() {
        return interactorType != null; 
    }

    public boolean hasBiologicalRoles() {
        return !( biologicalRoles == null || biologicalRoles.isEmpty() );
    }

    ////////////////////////
    // Object's override

    @Override
    public String toString() {
        return "ExtendedInteractor{" + super.toString() +", "+
               "experimentalRoles=" + experimentalRoles +
               ", biologicalRoles=" + biologicalRoles +
               ", properties=" + properties +
               ", interactorType=" + interactorType +
               ", annotations=" + annotations +
               '}';
    }
}
