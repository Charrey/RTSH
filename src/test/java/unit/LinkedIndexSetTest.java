package unit;

import com.charrey.util.datastructures.Indexable;
import com.charrey.util.datastructures.LinkedIndexSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkedIndexSetTest {

    private HashSet<Indexable> hashSet;
    private LinkedIndexSet<IndexableImpl> linkedIndexSet;
    private int ITS;
    private List<IndexableImpl> elements;

    @BeforeEach
    public void init() {
        ITS = 1000000;
        hashSet = new HashSet<>((int) (ITS * 1.3), 1);
        linkedIndexSet = new LinkedIndexSet<>(ITS, IndexableImpl.class);


        assertEquals(0, hashSet.size());
        assertEquals(0, linkedIndexSet.size());

        elements = getIndexable(ITS);
        Collections.shuffle(elements);
    }

    @Test
    public void testInsert() {
        long start = System.currentTimeMillis();
        hashSet.addAll(elements);
        long duration = System.currentTimeMillis() - start;
        System.out.println("Insert HashSet        for " + ITS + " elements: " + duration/1000. + "s");
        assertEquals(ITS, hashSet.size());
        start = System.currentTimeMillis();
        linkedIndexSet.addAll(elements);
        duration = System.currentTimeMillis() - start;
        System.out.println("Insert LinkedIndexSet for " + ITS + " elements: " + duration/1000. + "s");
        assertEquals(ITS, linkedIndexSet.size());
    }

    @Test
    public void testRemove() {
        hashSet.addAll(elements);
        linkedIndexSet.addAll(elements);
        Collections.shuffle(elements);
        long start = System.currentTimeMillis();
        for (IndexableImpl element : elements) {
            hashSet.remove(element);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Remove HashSet        for " + ITS + " elements: " + duration/1000. + "s");
        assertEquals(0, hashSet.size());

        start = System.currentTimeMillis();
        for (IndexableImpl element : elements) {
            linkedIndexSet.remove(element);
        }
        duration = System.currentTimeMillis() - start;
        System.out.println("Remove LinkedIndexSet for " + ITS + " elements: " + duration/1000. + "s");
        assertEquals(0, linkedIndexSet.size());
    }

    @Test
    public void testIterate(){
        hashSet.addAll(elements);
        linkedIndexSet.addAll(elements);
        Collections.shuffle(elements);

        int count = 0;
        long start = System.currentTimeMillis();
        for (Indexable ignored : hashSet) {
            count++;
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Iterate HashSet        for " + ITS + " elements: " + duration/1000. + "s");
        assertEquals(ITS, count);

        count = 0;
        start = System.currentTimeMillis();
        for (IndexableImpl ignored : linkedIndexSet) {
            count++;
        }
        duration = System.currentTimeMillis() - start;
        System.out.println("Iterate LinkedIndexSet for " + ITS + " elements: " + duration/1000. + "s");
        assertEquals(ITS, count);
    }

    private static List<IndexableImpl> getIndexable(int size) {
        List<IndexableImpl> res = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            res.add(new IndexableImpl(i));
        }
        return res;
    }


    private static class IndexableImpl implements Indexable {
        private int data;

        public IndexableImpl(int data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return String.valueOf(data);
        }

        @Override
        public int data() {
            return data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IndexableImpl indexable = (IndexableImpl) o;
            return data == indexable.data;
        }

        @Override
        public int hashCode() {
            return data;
        }
    }
}
