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
package uk.ac.ebi.intact.dataexchange.psimi.xml.converter.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.map.IdentityMap;

import java.util.Map;
import java.util.HashMap;

import psidev.psi.mi.xml.model.HasId;

/**
 * Stores a cache of elements with id
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class ConversionCache {

    private static final Log log = LogFactory.getLog(ConversionCache.class);

    private Map objectIdentityMap;
    private Map<String,Object> psiIdMap;

    public ConversionCache() {
        this.objectIdentityMap = new IdentityMap();
        this.psiIdMap = new HashMap<String,Object>();
    }

    public static ThreadLocal<ConversionCache> instance = new ThreadLocal<ConversionCache>() {
        @Override
        protected ConversionCache initialValue() {
            return new ConversionCache();
        }
    };

    public static Object getElement(Object id) {
        if (id == null) return null;

        Object cachedObject = instance.get().objectIdentityMap.get(id);

        if (cachedObject == null) {
            if (id instanceof HasId) {
                String psiIdKey = createPsiIdKey(id);
                cachedObject = instance.get().psiIdMap.get(psiIdKey);
            }
        }

        return cachedObject;
    }

    public static void putElement(Object key, Object element) {
        if (key != null) {
            instance.get().objectIdentityMap.put(key, element);

            if (key instanceof HasId) {
                String psiIdKey = createPsiIdKey(key);
                instance.get().psiIdMap.put(psiIdKey, element);
            }
        }
    }

    private static String createPsiIdKey(Object key) {
        int psiIdKey = ((HasId)key).getId();
        return key.getClass().getName()+":"+psiIdKey;
    }

    public static void clear() {
        if (log.isDebugEnabled()) log.debug("Clearing Conversion cache");
        
        instance.get().objectIdentityMap.clear();
        instance.get().psiIdMap.clear();
    }
}