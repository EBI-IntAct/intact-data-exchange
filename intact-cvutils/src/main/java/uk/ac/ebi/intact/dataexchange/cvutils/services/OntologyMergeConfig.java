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
package uk.ac.ebi.intact.dataexchange.cvutils.services;

/**
 * TODO comment that class header
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
public class OntologyMergeConfig {


    private String sourceOntologyTerm;
    private String targetOntologyTerm;
    private boolean recursive;
    private boolean includeSourceOntologyTerm;


    public String getSourceOntologyTerm() {
        return sourceOntologyTerm;
    }

    public void setSourceOntologyTerm( String sourceOntologyTerm ) {
        this.sourceOntologyTerm = sourceOntologyTerm;
    }

    public String getTargetOntologyTerm() {
        return targetOntologyTerm;
    }

    public void setTargetOntologyTerm( String targetOntologyTerm ) {
        this.targetOntologyTerm = targetOntologyTerm;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive( boolean recursive ) {
        this.recursive = recursive;
    }

    public boolean isIncludeSourceOntologyTerm() {
        return includeSourceOntologyTerm;
    }

    public void setIncludeSourceOntologyTerm( boolean includeSourceOntologyTerm ) {
        this.includeSourceOntologyTerm = includeSourceOntologyTerm;
    }
}
