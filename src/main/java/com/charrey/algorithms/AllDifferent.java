package com.charrey.algorithms;


import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.ValueIterator;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.IntStream;

/**
 * The AllDifferent algorithm
 */
public class AllDifferent {

    private static final Settings settings = new DefaultSettings();
    private final Set<Map<Integer, Set<Integer>>> cacheYesA = new HashSet<>();
    private final Set<Map<Integer, Set<Integer>>> cacheNoA = new HashSet<>();

    /**
     * Returns whether the domain satisfies an AllDifferent constraint. This is needed for a node disjoint subgraph homeomorphism.
     *
     * @param allDifferentMap a map that indicates for each source vertex its domain in target graph vertices
     * @return whether the constraint is satisfied.
     */
    public boolean get(@NotNull Map<Integer, Set<Integer>> allDifferentMap) {
        if (cacheYesA.contains(allDifferentMap)) {
            return true;
        } else if (cacheNoA.contains(allDifferentMap)) {
            return false;
        } else {
            final Map<Integer, int[]> domains = new HashMap<>();
            if (allDifferentMap.values().stream().anyMatch(x -> x.size() == 0)) {
                return false;
            }
            allDifferentMap.forEach((key, value) -> domains.put(key, value.stream().mapToInt(x -> x).toArray()));
            Model model = new Model(settings);
            List<Integer> ordered = new ArrayList<>(allDifferentMap.keySet());
            IntVar[] variables = ordered.stream().map(x -> model.intVar(String.valueOf(ordered.indexOf(x)), domains.get(x))).toArray(IntVar[]::new);

            model.allDifferent(variables).post();
            boolean result = model.getSolver().solve();
            if (result) {
                cacheYesA.add(allDifferentMap);
            } else {
                cacheNoA.add(allDifferentMap);
            }
            return result;
        }
    }

    /**
     * Returns whether the domain satisfies an AllDifferent constraint. This is needed for a node disjoint subgraph homeomorphism.
     *
     * @param compatibility a map that indicates for each source vertex (as index of a list) its domain in target graph vertices
     * @return whether the constraint is satisfied.
     */
    public boolean get(@NotNull List<Set<Integer>> compatibility) {
        Model model = new Model(settings);
        IntVar[] variables = IntStream.range(0, compatibility.size())
                .boxed()
                .map(i -> model.intVar(String.valueOf(i), compatibility.get(i).stream().mapToInt(x -> x).toArray()))
                .toArray(IntVar[]::new);
        model.allDifferent(variables).post();
        return model.getSolver().solve();
    }


    /**
     * Given a domain map, runs AllDifferent after each possible single mapping. This method returns a set of mappings that lead to contradictions,
     * i.e. that need to be removed.
     *
     * @param allDifferentMap a map that indicates for each source vertex its domain in target graph vertices
     * @return a set of pairs such that the key of the pair may not be mapped to the value of the pair.
     */
    @NotNull
    static Set<Pair<Integer, Integer>> checkAll(@NotNull Map<Integer, Set<Integer>> allDifferentMap, int iteration, String name) {
        final int[][] domains = new int[allDifferentMap.size()][];
        allDifferentMap.forEach((key, value) -> domains[key] = (value.stream().mapToInt(x -> x).toArray()));
        Model model = new Model(settings);
        IntVar[] variables = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            if (domains[i].length == 0) {
                return constructSetOfPairs(allDifferentMap);
            }
            variables[i] = model.intVar("foo", domains[i]);
        }
        model.allDifferent(variables).post();
        Set<Pair<Integer, Integer>> res = new HashSet<>();

        long toProcess = 0L;
        Collection<Set<Integer>> values = allDifferentMap.values();
        for (Set<Integer> value : values) {
            toProcess = toProcess + value.size();
        }

        long lastTimePrinted = System.currentTimeMillis();
        long counter = 0;


        for (int i = 0; i < variables.length; i++) {
            ValueIterator iterator = variables[i].getValueIterator(false);
            iterator.bottomUpInit();
            while (iterator.hasNext()) {
                if (System.currentTimeMillis() - lastTimePrinted > 1000) {
                    System.out.println(name + " filtering AllDifferent at iteration " + iteration + ": " + 100 * counter / (double) toProcess + "%");
                    lastTimePrinted = System.currentTimeMillis();
                }
                int value = iterator.next();
                Constraint bind = model.allEqual(variables[i], model.intVar(value));
                bind.post();
                if (!model.getSolver().solve()) {
                    res.add(new Pair<>(i, value));
                }
                model.unpost(bind);
                model.getSolver().reset();
                counter++;
            }
        }
        return res;
    }

    @NotNull
    private static Set<Pair<Integer, Integer>> constructSetOfPairs(@NotNull Map<Integer, Set<Integer>> allDifferentMap) {
        Set<Pair<Integer, Integer>> toRemove = new HashSet<>();
        for (Map.Entry<Integer, Set<Integer>> entry : allDifferentMap.entrySet()) {
            for (Integer target : entry.getValue()) {
                toRemove.add(new Pair<>(entry.getKey(), target));
            }
        }
        return toRemove;
    }
}
