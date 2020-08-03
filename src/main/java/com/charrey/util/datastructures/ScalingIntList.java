package com.charrey.util.datastructures;

import gnu.trove.list.linked.TIntLinkedList;

public class ScalingIntList {


    private TIntLinkedList inner = new TIntLinkedList();

    public ScalingIntList(ScalingIntList copy) {
        inner = new TIntLinkedList(copy.inner);
    }

    public ScalingIntList() {

    }

    public int[] toArray() {
        return inner.toArray();
    }

    public int size() {
        return inner.size();
    }

    public void add(int i) {
        inner.add(i);
    }

    public int get(int index) {
        while (index >= inner.size()) {
            add(0);
        }
        return inner.get(index);
    }

    public void set(int index, int value) {
        while (index >= inner.size()) {
            add(0);
        }
        inner.set(index, value);
    }

    public void removeLast() {
        inner.removeAt(inner.size() - 1);
    }
}
