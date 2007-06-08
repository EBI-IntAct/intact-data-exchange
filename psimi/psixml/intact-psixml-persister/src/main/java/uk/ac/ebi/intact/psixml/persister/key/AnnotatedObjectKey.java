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
package uk.ac.ebi.intact.psixml.persister.key;

import net.sf.ehcache.Element;
import uk.ac.ebi.intact.model.AnnotatedObject;

/**
 * TODO comment this
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class AnnotatedObjectKey<A extends AnnotatedObject> implements Key {

    private Element element;

    public AnnotatedObjectKey(A annotatedObject) {
        String key = generateKey(annotatedObject);

        this.element = new Element(key, annotatedObject);
    }

    protected String generateKey(A annotatedObject) {
        String key = annotatedObject.getShortLabel();

        if (key == null) {
            throw new RuntimeException("To generate a key from an annotatedObject the shortLabel is needed: " + annotatedObject);
        }

        return key;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotatedObjectKey that = (AnnotatedObjectKey) o;

        if (element != null ? !element.equals(that.element) : that.element != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (element != null ? element.hashCode() : 0);
    }
}