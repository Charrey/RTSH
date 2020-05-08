package com.charrey.algorithms;


import com.charrey.graph.Vertex;
import com.charrey.util.datastructures.IndexMap;
import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.ValueIterator;
import org.jgrapht.alg.util.Pair;

import java.util.*;

public class AllDifferent {

    private static final Settings settings = new DefaultSettings();

    private final Set<Map<Vertex, Set<Vertex>>> cacheYesA = new HashSet<>();
    private final Set<Map<Vertex, Set<Vertex>>> cacheNoA = new HashSet<>();
    public boolean get(int patternGraphSize, Map<Vertex, Set<Vertex>> allDifferentMap) {
        if (cacheYesA.contains(allDifferentMap)) {
            return true;
        } else if (cacheNoA.contains(allDifferentMap)) {
            return false;
        } else {
            final Map<Vertex, int[]> domains = new IndexMap<>(patternGraphSize);
            allDifferentMap.forEach((key, value) -> domains.put(key, value.stream().mapToInt(Vertex::data).toArray()));
            if (domains.values().stream().anyMatch(x -> x.length == 0)) {
                return false;
            }
            Model model = new Model(settings);
            IntVar[] variables = new IntVar[allDifferentMap.size()];
            List<Vertex> ordered = new ArrayList<>(allDifferentMap.keySet());
            for (int i = 0; i < allDifferentMap.size(); i++) {
                variables[i] = model.intVar(String.valueOf(i), domains.get(ordered.get(i)));
            }
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

    //private final Set<List<Set<Vertex>>> cacheYesB = new HashSet<>();
    //private final Set<List<Set<Vertex>>> cacheNoB = new HashSet<>();
    public boolean get(List<Set<Vertex>> compatibility) {
//        if (cacheYesB.contains(compatibility)) {
//            return true;
//        } else if (cacheNoB.contains(compatibility)) {
//            return false;
//        }
        Model model = new Model(settings);
        IntVar[] variables = new IntVar[compatibility.size()];
        for (int i = 0; i < compatibility.size(); i++) {
            variables[i] = model.intVar(String.valueOf(i), compatibility.get(i).stream().mapToInt(Vertex::data).toArray());
        }
        model.allDifferent(variables).post();
        boolean result = model.getSolver().solve();
//        if (result) {
//            cacheYesB.add(new ArrayList<>(compatibility));
//        } else {
//            cacheNoB.add(new ArrayList<>(compatibility));
//        }
        return result;
    }



        public static Set<Pair<Integer, Integer>> checkAll(Map<Integer, Set<Integer>> allDifferentMap) {
        Set<Pair<Integer, Integer>> res = new HashSet<>();
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
