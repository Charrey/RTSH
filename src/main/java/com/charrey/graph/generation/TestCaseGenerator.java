package com.charrey.graph.generation;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class TestCaseGenerator {

    private final Deque<TestCase> testCases = new ArrayDeque<>();

    public TestCase getNext() {
        return testCases.pop();
    }

    public boolean hasNext() {
        return !testCases.isEmpty();
    }

    public void init(int amount, boolean print) {
        testCases.clear();
        if (print) {
            System.out.println("Generating graphs..");
        }
        for (int i = 0; i < amount; i++) {
            testCases.add(getRandom());
            if (print) {
                System.out.println(i + "/" + amount);
            }
        }
    }

    public abstract void makeHarder();

    protected abstract TestCase getRandom();
}
