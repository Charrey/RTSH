package com.charrey.algorithms;


import com.charrey.graph.Vertex;
import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.ValueIterator;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.IntStream;

public class AllDifferent {

    private static final Settings settings = new DefaultSettings();
    private final Set<Map<Vertex, Set<Vertex>>> cacheYesA = new HashSet<>();
    private final Set<Map<Vertex, Set<Vertex>>> cacheNoA = new HashSet<>();
    public boolean get(Map<Vertex, Set<Vertex>> allDifferentMap) {
        if (cacheYesA.contains(allDifferentMap)) {
            return true;
        } else if (cacheNoA.contains(allDifferentMap)) {
            return false;
        } else {
            final Map<Vertex, int[]> domains = new HashMap<>();
            if (allDifferentMap.values().stream().anyMatch(x -> x.size() == 0)) {
                return false;
            }
            allDifferentMap.forEach((key, value) -> domains.put(key, value.stream().mapToInt(Vertex::data).toArray()));
            Model model = new Model(settings);
            List<Vertex> ordered = new ArrayList<>(allDifferentMap.keySet());
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

    public boolean get(List<Set<Vertex>> compatibility) {
        Model model = new Model(settings);
        IntVar[] variables = IntStream.range(0, compatibility.size())
                .boxed()
                .map(i -> model.intVar(String.valueOf(i), compatibility.get(i).stream().mapToInt(Vertex::data).toArray()))
                .toArray(IntVar[]::new);
        model.allDifferent(variables).post();
        return model.getSolver().solve();
    }


    public static Set<Pair<Integer, Integer>> checkAll(Map<Integer, Set<Integer>> allDifferentMap) {
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
        for (int i = 0; i < variables.length; i++) {
            ValueIterator iterator = variables[i].getValueIterator(false);
            iterator.bottomUpInit();
            while (iterator.hasNext()) {
                int value = iterator.next();
                Constraint bind = model.allEqual(variables[i], model.intVar(value));
                bind.post();
                if (!model.getSolver().solve()) {
                    res.add(new Pair<>(i, value));
                }
                model.unpost(bind);
                model.getSolver().reset();
            }
        }
        return res;
    }

    private static Set<Pair<Integer, Integer>> constructSetOfPairs(Map<Integer, Set<Integer>> allDifferentMap) {
        Set<Pair<Integer, Integer>> toRemove = new HashSet<>();
        for (Map.Entry<Integer, Set<Integer>> entry : allDifferentMap.entrySet()) {
            for (Integer target : entry.getValue()) {
                toRemove.add(new Pair<>(entry.getKey(), target));
            }
        }
        return toRemove;
    }
}
