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
 * Configuration for the Ontology merger.
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1
 */
public class OntologyMergeConfig {

    /**
     * the root CV term identifier we are going to copy under the target term.
     */
    private String sourceOntologyTerm;

    /**
     * the root CV term identifier where the source term are going to be copied.
     */
    private String targetOntologyTerm;

    /**
     * if true, the merger should copy all children term of the source recursively.
     */
    private boolean recursive;

    /**
     * if true, the merger should include the source term directly unter the target term, otherwise only the children
     * terms of the source.
     */
    private boolean includeSourceOntologyTerm;

    public OntologyMergeConfig( String sourceOntologyTerm, String targetOntologyTerm, boolean recursive, boolean includeSourceOntologyTerm ) {
        setSourceOntologyTerm( sourceOntologyTerm );
        setTargetOntologyTerm(targetOntologyTerm );
        this.recursive = recursive;
        this.includeSourceOntologyTerm = includeSourceOntologyTerm;
    }

    public String getSourceOntologyTerm() {
        return sourceOntologyTerm;
    }

    public void setSourceOntologyTerm( String sourceOntologyTerm ) {
        if ( sourceOntologyTerm == null ) {
            throw new IllegalArgumentException( "You must give a non null sourceOntologyTerm" );
        }
        this.sourceOntologyTerm = sourceOntologyTerm;
    }

    public String getTargetOntologyTerm() {
        return targetOntologyTerm;
    }

    public void setTargetOntologyTerm( String targetOntologyTerm ) {
        if ( targetOntologyTerm == null ) {
            throw new IllegalArgumentException( "You must give a non null targetOntologyTerm" );
        }
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

    @Override
    public String toString() {
        return "OntologyMergeConfig{" +
               "sourceOntologyTerm='" + sourceOntologyTerm + '\'' +
               ", targetOntologyTerm='" + targetOntologyTerm + '\'' +
               ", recursive=" + recursive +
               ", includeSourceOntologyTerm=" + includeSourceOntologyTerm +
               '}';
    }
}
