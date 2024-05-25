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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * A BootableList is a List implementation that can be used in the
 * build process of JNode.
 * Using this class, instead of e.g. ArrayList, will avoid class incompatibilities
 * between the JNode java.util implementation and Sun's implementation.
 *
 * @author epr
 */
public class BootableArrayList<T> extends VmSystemObject implements List<T>, RandomAccess {

    private ArrayList<T> listCache;
    private T[] array;
    private int hashCode;
    private transient boolean locked;

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public BootableArrayList() {
        hashCode = super.hashCode();
    }

    /**
     * Constructs a list containing the elements of the specified collection,
     * in the order they are returned by the collection's iterator.
     *
     * @param c the c
     */
    public BootableArrayList(Collection<? extends T> c) {
        addAll(c);
    }

    /**
     * Constructs an empty list with an initial capacity of ten.
     *
     * @param initialCapacity the initialCapacity
     */
    public BootableArrayList(int initialCapacity) {
        listCache = new ArrayList<>(initialCapacity);
        hashCode = listCache.hashCode();
    }

    /**
     * Gets (an if needed reload) the arraylist.
     *
     * @return the cache
     */
    private ArrayList<T> getListCache() {
        if (locked) {
            throw new RuntimeException("Cannot change a locked BootableArrayList");
        }
        if (listCache == null) {
            listCache = new ArrayList<>();
            if (array != null) {
                listCache.addAll(Arrays.asList(array));
            }
            array = null;
        }
        return listCache;
    }

    @Override
    public void add(int index, T o) {
        getListCache().add(index, o);
    }

    @Override
    public boolean add(T o) {
        return getListCache().add(o);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return getListCache().addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return getListCache().addAll(index, c);
    }

    @Override
    public void clear() {
        getListCache().clear();
    }

    @Override
    public boolean contains(Object o) {
        return getListCache().contains(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        return getListCache().containsAll(c);
    }

    /**
     * @param minCapacity the minCapacity
     */
    public void ensureCapacity(int minCapacity) {
        getListCache().ensureCapacity(minCapacity);
    }

    @Override
    public boolean equals(Object obj) {
        return getListCache().equals(obj);
    }

    @Override
    public int hashCode() {
        if (listCache != null) {
            return getListCache().hashCode();
        } else {
            return hashCode;
        }
    }

    @Override
    public int indexOf(Object o) {
        return getListCache().indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        return getListCache().isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return getListCache().iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return getListCache().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return getListCache().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return getListCache().listIterator(index);
    }

    @Override
    public T remove(int index) {
        return getListCache().remove(index);
    }

    @Override
    public boolean remove(Object o) {
        return getListCache().remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return getListCache().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return getListCache().retainAll(c);
    }

    @Override
    public T set(int index, T o) {
        return getListCache().set(index, o);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return getListCache().subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return getListCache().toArray();
    }

    @Override
    public <E> E[] toArray(E[] a) {
        return getListCache().toArray(a);
    }

    @Override
    public String toString() {
        if (listCache != null) {
            return getListCache().toString();
        } else {
            return super.toString();
        }
    }

    /** */
    public void trimToSize() {
        getListCache().trimToSize();
    }

    @Override
    public T get(int index) {
        return getListCache().get(index);
    }

    @Override
    public int size() {
        return getListCache().size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();
        if (listCache != null) {
            array = (T[]) listCache.toArray();
            hashCode = listCache.hashCode();
        } else {
            array = null;
        }
        listCache = null;
        locked = true;
    }
}
