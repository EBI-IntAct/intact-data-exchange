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
package uk.ac.ebi.intact.psixml.generator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class StubProject extends MavenProject {


    @Override
    public Set getArtifacts() {

        //File artFile = new File(System.getProperty("user.home"), "/.m2/repository/psidev/psi/mi/psi25-xml/1.0-beta4-SNAPSHOT/psi25-xml-1.0-beta4-SNAPSHOT.jar");
        File artFile = new File(StubProject.class.getResource("/psi25-xml-1.0-beta4-SNAPSHOT.jar").getFile());

        Set<Artifact> artifacts = new HashSet<Artifact>();
        artifacts.add(new StubArtifact("", "", "", artFile));

        return artifacts;
    }

    private class StubArtifact implements Artifact {

        private String groupId;
        private String artifactId;
        private String version;
        private File file;

        public StubArtifact(String groupId, String artifactId, String version, File file) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.file = file;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getScope() {
            throw new UnsupportedOperationException();
        }

        public String getType() {
            throw new UnsupportedOperationException();
        }

        public String getClassifier() {
            throw new UnsupportedOperationException();
        }// only providing this since classifier is *very* optional...

        public boolean hasClassifier() {
            throw new UnsupportedOperationException();
        }

        public File getFile() {
            return file;
        }

        public void setFile(File destination) {
        }

        public String getBaseVersion() {
            throw new UnsupportedOperationException();
        }

        public void setBaseVersion(String baseVersion) {
        }

        public String getId() {
            throw new UnsupportedOperationException();
        }

        public String getDependencyConflictId() {
            throw new UnsupportedOperationException();
        }

        public void addMetadata(ArtifactMetadata metadata) {
        }

        public Collection getMetadataList() {
            throw new UnsupportedOperationException();
        }

        public void setRepository(ArtifactRepository remoteRepository) {
        }

        public ArtifactRepository getRepository() {
            throw new UnsupportedOperationException();
        }

        public void updateVersion(String version, ArtifactRepository localRepository) {
        }

        public String getDownloadUrl() {
            throw new UnsupportedOperationException();
        }

        public void setDownloadUrl(String downloadUrl) {
        }

        public ArtifactFilter getDependencyFilter() {
            throw new UnsupportedOperationException();
        }

        public void setDependencyFilter(ArtifactFilter artifactFilter) {
        }

        public ArtifactHandler getArtifactHandler() {
            throw new UnsupportedOperationException();
        }

        public List getDependencyTrail() {
            throw new UnsupportedOperationException();
        }

        public void setDependencyTrail(List dependencyTrail) {
        }

        public void setScope(String scope) {
        }

        public VersionRange getVersionRange() {
            throw new UnsupportedOperationException();
        }

        public void setVersionRange(VersionRange newRange) {
        }

        public void selectVersion(String version) {
        }

        public void setGroupId(String groupId) {
        }

        public void setArtifactId(String artifactId) {
        }

        public boolean isSnapshot() {
            throw new UnsupportedOperationException();
        }

        public void setResolved(boolean resolved) {
        }

        public boolean isResolved() {
            throw new UnsupportedOperationException();
        }

        public void setResolvedVersion(String version) {
        }

        public void setArtifactHandler(ArtifactHandler handler) {
        }

        public boolean isRelease() {
            throw new UnsupportedOperationException();
        }

        public void setRelease(boolean release) {
        }

        public List getAvailableVersions() {
            throw new UnsupportedOperationException();
        }

        public void setAvailableVersions(List versions) {
        }

        public boolean isOptional() {
            throw new UnsupportedOperationException();
        }

        public void setOptional(boolean optional) {
        }

        public ArtifactVersion getSelectedVersion() throws OverConstrainedVersionException {
            throw new UnsupportedOperationException();
        }

        public boolean isSelectedVersionKnown() throws OverConstrainedVersionException {
            throw new UnsupportedOperationException();
        }

        public int compareTo(Object o) {
            throw new UnsupportedOperationException();
        }
    }
}