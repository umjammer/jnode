/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.vm.objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author epr
 */
public class BootableHashMap<K, V> extends VmSystemObject implements Map<K, V> {

    private HashMap<K, V> mapCache;
    private Entry<K, V>[] entryArray;
    private int hashCode;
    private transient boolean locked;

    /**
     * Constructs an empty HashMap.
     * @see java.util.HashMap#HashMap()
     */
    public BootableHashMap() {
        this.hashCode = super.hashCode();
    }

    /**
     * Constructs an empty HashMap.
     * @see java.util.HashMap#HashMap(int)
     *
     * @param initialCapacity the initialCapacity
     */
    public BootableHashMap(int initialCapacity) {
        mapCache = new HashMap<>(initialCapacity);
        this.hashCode = mapCache.hashCode();
    }

    @Override
    public int hashCode() {
        if (mapCache != null) {
            return getMapCache().hashCode();
        } else {
            return hashCode;
        }
    }

    @Override
    public String toString() {
        if (mapCache != null) {
            return getMapCache().toString();
        } else {
            return super.toString();
        }
    }

    @Override
    public Collection<V> values() {
        return getMapCache().values();
    }

    @Override
    public Set<K> keySet() {
        return getMapCache().keySet();
    }

    @Override
    public V get(Object key) {
        return getMapCache().get(key);
    }

    @Override
    public void clear() {
        getMapCache().clear();
    }

    @Override
    public int size() {
        return getMapCache().size();
    }

    @Override
    public V put(K key, V value) {
        return getMapCache().put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        getMapCache().putAll(m);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return getMapCache().entrySet();
    }

    @Override
    public boolean containsKey(Object key) {
        return getMapCache().containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return getMapCache().isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return getMapCache().equals(obj);
    }

    @Override
    public V remove(Object o) {
        return getMapCache().remove(o);
    }

    @Override
    public boolean containsValue(Object value) {
        return getMapCache().containsValue(value);
    }

    static final class Entry<eK, eV> extends VmSystemObject {
        private final eK key;
        private final eV value;

        public Entry(Map.Entry<eK, eV> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        /**
         * Gets the key
         *
         * @return Object
         */
        public eK getKey() {
            return key;
        }

        /**
         * Gets the value
         *
         * @return Object
         */
        public eV getValue() {
            return value;
        }
    }

    /**
     * Gets the hashmap
     *
     * @return the cache
     */
    private HashMap<K, V> getMapCache() {
        if (locked) {
            throw new RuntimeException("Cannot change a locked BootableHashMap");
        }
        if (mapCache == null) {
            if (entryArray != null) {
                final int max = entryArray.length;

                mapCache = new HashMap<>(max);
                for (final Entry<K, V> e : entryArray) {
                    mapCache.put(e.getKey(), e.getValue());
                }

                entryArray = null;
            } else {
                mapCache = new HashMap<>();
            }
        }
        return mapCache;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();

        if (mapCache != null) {
            entryArray = new Entry[mapCache.size()];
            int index = 0;
            for (Map.Entry<K, V> entry : mapCache.entrySet()) {
                entryArray[index++] = new Entry<>(entry);
            }
            hashCode = mapCache.hashCode();
            mapCache = null;
        }
        locked = true;
    }

    public boolean isLocked() {
        return locked;
    }
}
