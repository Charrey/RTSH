package com.charrey.algorithms;



import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.charrey.graph.Vertex;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.iterators.ValueIterator;
import org.jgrapht.alg.util.Pair;

public class AllDifferent {

    private Set<Map<Vertex, Set<Vertex>>> cacheYes = new HashSet<>();
    private Set<Map<Vertex, Set<Vertex>>> cacheNo = new HashSet<>();
    public boolean get(Map<Vertex, Set<Vertex>> allDifferentMap) {
        if (cacheYes.contains(allDifferentMap)) {
            return true;
        } else if (cacheNo.contains(allDifferentMap)) {
            return false;
        } else {
            final Map<Vertex, int[]> domains = new HashMap<>();
            allDifferentMap.forEach((key, value) -> domains.put(key, value.stream().mapToInt(Vertex::intData).toArray()));
            if (domains.values().stream().anyMatch(x -> x.length == 0)) {
                return false;
            }
            Model model = new Model("Foo");
            IntVar[] variables = new IntVar[allDifferentMap.size()];
            List<Vertex> ordered = new ArrayList<>(allDifferentMap.keySet());
            for (int i = 0; i < allDifferentMap.size(); i++) {
                variables[i] = model.intVar("foo", domains.get(ordered.get(i)));
            }
            model.allDifferent(variables).post();
            boolean result = model.getSolver().solve();
            if (result) {
                cacheYes.add(allDifferentMap);
            } else {
                cacheNo.add(allDifferentMap);
            }
            return result;
        }
    }


    public static Set<Pair<Integer, Integer>> checkAll(Map<Integer, Set<Integer>> allDifferentMap) {
        Set<Pair<Integer, Integer>> res = new HashSet<>();
        final int[][] domains = new int[allDifferentMap.size()][];
        allDifferentMap.forEach((key, value) -> domains[key] = (value.stream().mapToInt(x -> x).toArray()));

        Model model = new Model("Foo");
        IntVar[] variables = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            if (domains[i].length == 0) {
                return constructSetOfPairs(allDifferentMap);
            }
            variables[i] = model.intVar("foo", domains[i]);
        }
        model.allDifferent(variables).post();
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
        //return model.getSolver().solve();
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
