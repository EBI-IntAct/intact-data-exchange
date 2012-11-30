/*
 * Copyright 2001-2008 The European Bioinformatics Institute.
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
package uk.ac.ebi.intact.dataexchange.cvutils.model;

/**
 * Contains the additional information for a CV, from an external resource
 * 
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotationInfo {

    private String shortLabel;
    private String fullName;
    private String type;
    private String mi;
    private String topicShortLabel;
    private String reason;
    private boolean applyToChildren;

    public AnnotationInfo(String shortLabel, String fullName, String type, String mi, String topicShortLabel, String reason, boolean applyToChildren) {
        this.shortLabel = shortLabel;
        this.fullName = fullName;
        this.type = type;
        this.mi = mi;
        this.topicShortLabel = topicShortLabel;
        this.reason = reason;
        this.applyToChildren = applyToChildren;
    }

    public boolean isApplyToChildren() {
        return applyToChildren;
    }

    public void setApplyToChildren(boolean applyToChildren) {
        this.applyToChildren = applyToChildren;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMi() {
        return mi;
    }

    public void setMi(String mi) {
        this.mi = mi;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public String getTopicShortLabel() {
        return topicShortLabel;
    }

    public void setTopicShortLabel(String topicShortLabel) {
        this.topicShortLabel = topicShortLabel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}