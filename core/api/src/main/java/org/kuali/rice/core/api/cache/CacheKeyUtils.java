/**
 * Copyright 2005-2011 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.core.api.cache;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This is a utility class that generates cache keys for compound objects.
 */
public final class CacheKeyUtils {

    private CacheKeyUtils() {
        throw new UnsupportedOperationException("do not call.");
    }

    /**
     * Create a String key value out of a Map.  The Map should really just contain
     * simple types.
     * @param map the map.  if null will return ""
     * @param <K> the map key type
     * @return the map as a string value
     */
    public static <K extends Comparable<K>> String key(Map<K, ?> map) {
        if (map == null) {
            return "";
        }

        final SortedMap<K, ?> sorted = new TreeMap<K, Object>(map);
        final StringBuilder b = new StringBuilder("[");
        for (Map.Entry<K, ?> entry : sorted.entrySet()) {
            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                b.append(entry.getKey());
                b.append(":");
                b.append(entry.getValue());
                b.append(",");
            }
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Create a String key value out of a Collection.  The Collection should really just contain
     * simple types.
     * @param col the collection.  if null will return ""
     * @param <K> the col type
     * @return the collection as a string value
     */
    public static <K extends Comparable<K>> String key(Collection<K> col) {
        if (col == null) {
            return "";
        }

        final List<K> sorted = new ArrayList<K>(col);
        Collections.sort(sorted);
        final StringBuilder b = new StringBuilder("[");
        for (K entry : sorted) {
            if (entry != null) {
                b.append(entry);
                b.append(",");
            }
        }
        b.append("]");
        return b.toString();
    }

}
