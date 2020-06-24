package com.charrey.graph.generation;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The type Test case generator.
 */
public abstract class TestCaseGenerator {



    private final Deque<TestCase> testCases = new ArrayDeque<>();

    /**
     * Returns the next Testcase from this generator.
     *
     * @return the next
     */
    public TestCase getNext() {
        return testCases.pop();
    }

    /**
     * Returns whether the next graph is available for usage. This can happen using the init() call.
     *
     * @return the boolean
     */
    public boolean hasNext() {
        return !testCases.isEmpty();
    }

    /**
     * Pregenerate a specific number of test cases.
     *
     * @param amount the number of test cases to generate.
     * @param print  whether the progress of generation should be logged to the console.
     */
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

    /**
     * Adjusts the settings of this generator to generate graphs for which it is more difficult to find homeomorphisms. This
     * could, for example, entail increasing the graph sizes or connectedness.
     */
    public abstract void makeHarder();

    /**
     * Returns a random test case.
     *
     * @return a random test case.
     */
    protected abstract TestCase getRandom();
}
