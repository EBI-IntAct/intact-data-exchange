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
package uk.ac.ebi.intact.dataexchange.cvutils;

import uk.ac.ebi.intact.dataexchange.cvutils.model.CvTerm;
import uk.ac.ebi.intact.dataexchange.cvutils.model.IntactOntology;

import java.util.*;

import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class CvUtils {

    private CvUtils() {
    }

    /**
     * Returns the id of the parent which is common to the IDs provided. If none is found, it returns null.
     * @param ontology An ontology of CvTerms
     * @param ids the children IDs
     * @return The id of the parent
     */
    public static String findLowestCommonAncestor(IntactOntology ontology, String... ids) {
        if (ids.length < 2) {
            throw new IllegalArgumentException("At least two IDs have to be provided to find a common parent");
        }

        Collection<CvTerm> children = new ArrayList<CvTerm>(ids.length);

        for (String id : ids) {
            CvTerm term = ontology.search(id);

            if (term == null) {
                throw new IllegalArgumentException("Term with id '"+id+"' was not found in the ontology provided");
            }

            children.add(term);
        }

        CvTerm parent = findLowestCommonAncestor(children.toArray(new CvTerm[children.size()]));

        if (parent != null) {
            return parent.getId();
        }

        return null;
    }

    /**
     * Returns the id of the parent which is common to the children provided. If none is found, it returns null.
     * @param children A CvTerms with a common parent
     * @return the common parent for the children
     */
    public static CvTerm findLowestCommonAncestor(CvTerm ... children) {
        if (children.length < 2) {
            throw new IllegalArgumentException("At least two children have to be provided to find a common parent");
        }

        Multimap<String,String> cvMap = new LinkedListMultimap<String,String>();

        // get all the parents for each child
        for (CvTerm child : children) {
            cvMap.put(child.getId(), child.getId());
            cvMap.putAll(child.getId(), findAllParentsForTerm(child));
        }

        // calculate the common parents by interesecting all the collections of parents for each child
        List<String> commonParents = null;

        for (Collection<String> parents : cvMap.asMap().values()) {
            List<String> parentsList = new LinkedList<String>(parents);
            if (commonParents == null) {
                commonParents = parentsList;
            } else {
                commonParents = ListUtils.intersection(commonParents, parentsList);
            }

        }

        // the first child of the commonParents collection is the lowest common ancestor
        if (!commonParents.isEmpty()) {
            String parentId = commonParents.iterator().next();
            return findParentById(children[0], parentId);
        }

        return null;
    }

    private static List<String> findAllParentsForTerm(CvTerm child) {
        Set<String> parents = new LinkedHashSet<String>();

        // creates a set of IDs from the parents. If the parent id is found,
        // the id is removed from the set and added to the end
        for (CvTerm parent : child.getParents()) {
            if (parents.contains(parent.getId())) {
                parents.remove(parent.getId());
            }
            parents.add(parent.getId());

            for (String parentId : findAllParentsForTerm(parent)) {
                if (parents.contains(parentId)) {
                    parents.remove(parentId);
                }
                parents.add(parentId);
            }
        }

        return new LinkedList(parents);
    }

    private static CvTerm findParentById(CvTerm child, String parentId) {
        if (parentId.equals(child.getId())) {
            return child;
        }

        for (CvTerm parent : child.getParents()) {
            if (parentId.equals(parent.getId())) {
                return parent;
            }

            CvTerm candidate = findParentById(parent, parentId);

            if (candidate != null) {
                return candidate;
            }
        }

        return null;
    }

}