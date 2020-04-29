package com.charrey.util.datastructures;

import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class LinkedIndexSet<T extends Indexable> implements Set<T> {

    private static final int ABSENT = -2;
    private static final int END = -1;
    private final int[] pointersBack;
    private final int[] pointersForward;
    private final T[] data;
    private int oldest = END;
    private int newest = END;
    private int size = 0;
    private final Class<T> clazz;



    @SuppressWarnings("unchecked")
    public LinkedIndexSet(int size, Class<T> clazz) {
        pointersBack = new int[size];
        pointersForward = new int[size];
        for (int i = 0; i < size; i++) {
            pointersBack[i] = ABSENT;
            pointersForward[i] = ABSENT;
        }
        data = (T[]) java.lang.reflect.Array.newInstance(clazz, size);
        this.clazz = clazz;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        T object;
        try {
            object = (T) o;
        } catch (ClassCastException e) {
            return false;
        }
        return data[object.data()] != null;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new LinkedIndexSetIterator();
    }

    @Override
    public @NotNull Object[] toArray() {
        Object[] res = new Object[size];
        Iterator<T> i = iterator();
        int counter = 0;
        while (i.hasNext()) {
            T next = i.next();
            res[counter] = next;
            counter++;
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T1> T1[] toArray(@NotNull T1[] a) {
        if (a.length < size) {
            try {
                a = (T1[]) java.lang.reflect.Array.newInstance(clazz, size);
            } catch (ClassCastException e) {
                throw new ArrayStoreException();
            }
        }
        Iterator<T> i = iterator();
        int counter = 0;
        while (i.hasNext()) {
            T next = i.next();
            try {
                a[counter] = (T1) next;
            } catch (ClassCastException e) {
                throw new ArrayStoreException();
            }
            counter++;
        }
        return a;

    }

    @Override
    public boolean add(T t) {
        assert t != null;
        int data = t.data();
        if (this.data[data] != null) {
            return false;
        }
        if (newest != END) {
            pointersForward[newest] = data;
        }
        if (oldest == END) {
            oldest = data;
        }
        pointersForward[data] = END;
        pointersBack[data] = newest;
        this.data[data] = t;
        newest = data;
        size++;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        T other;
        try {
            other = (T) o;
        } catch (ClassCastException e) {
            return false;
        }
        int data = other.data();
        if (this.data[data] == null) {
            return false;
        }
        int previous = pointersBack[data];
        int next = pointersForward[data];
        if (previous != END) {
            pointersForward[previous] = next;
        } else {
            oldest = pointersForward[data];
        }
        if (next != END) {
            pointersBack[next] = previous;
        } else {
            newest = previous;
        }
        pointersBack[data] = ABSENT;
        this.data[data] = null;
        pointersForward[data] = ABSENT;
        size--;
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object i : c) {
            if (!contains(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T i : c) {
            changed = add(i) || changed;
        }
        return changed;
    }


    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object i : c) {
            changed = remove(i) || changed;
        }
        return changed;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        boolean changed = false;
        for (Iterator<T> iterator = iterator(); iterator.hasNext();) {
            T item = iterator.next();
            if (!c.contains(item)) {
                remove(item);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.data.length; i++) {
            data[i] = null;
            pointersBack[i] = ABSENT;
            pointersForward[i] = ABSENT;
        }
        oldest = END;
        newest = END;
        size = 0;
    }

    private class LinkedIndexSetIterator implements Iterator<T> {
        int cursor = oldest;
        boolean calledNext;

        @Override
        public boolean hasNext() {
            return cursor != END;
        }

        @Override
        public T next() {
            T nextElem = data[cursor];
            cursor = pointersForward[cursor];
            calledNext = true;
            return nextElem;
        }

        @Override
        public void remove() {
            if (cursor != -1) {
                LinkedIndexSet.this.remove(pointersBack[cursor]);
            }
        }
    }
}
