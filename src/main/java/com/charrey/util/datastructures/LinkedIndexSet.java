package com.charrey.util.datastructures;

import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class LinkedIndexSet<T extends Indexable> implements Set<T> {

    private static final int ABSENT = -2;
    private static final int END = -1;
    private int[] pointersBack;
    private int[] pointersForward;
    private T[] data;
    private int oldest = END;
    private int newest = END;
    private int size = 0;


    @SuppressWarnings("unchecked")
    public LinkedIndexSet(int size, Class<T> clazz) {
        pointersBack = new int[size];
        pointersForward = new int[size];
        for (int i = 0; i < size; i++) {
            pointersBack[i] = ABSENT;
            pointersForward[i] = ABSENT;
        }
        data = (T[]) java.lang.reflect.Array.newInstance(clazz, size);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
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

    @Override
    public @NotNull <T1> T1[] toArray(@NotNull T1[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T t) {
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
        }
        if (next != END) {
            pointersBack[next] = previous;
        }
        pointersForward[data] = -2;
        pointersBack[data] = -2;
        this.data[data] = null;
        if (oldest == data) {
            oldest = END;
        }
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private class LinkedIndexSetIterator implements Iterator<T> {
        int cursor = oldest;

        @Override
        public boolean hasNext() {
            return cursor != END;
        }

        @Override
        public T next() {
            T nextElem = data[cursor];
            cursor = pointersForward[cursor];
            return nextElem;
        }

        @Override
        public void remove() {
            LinkedIndexSet.this.remove(pointersBack[cursor]);
        }
    }
}
