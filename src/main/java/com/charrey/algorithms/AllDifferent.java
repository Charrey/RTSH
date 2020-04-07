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



    public boolean get(Map<Vertex, Set<Vertex>> allDifferentMap) {
        final Map<Vertex, int[]> domains = new HashMap<>();
        allDifferentMap.forEach((key, value) -> domains.put(key, value.stream().mapToInt(Vertex::getIntId).toArray()));
        Model model = new Model("Foo");
        IntVar[] variables = new IntVar[allDifferentMap.size()];
        List<Vertex> ordered = new ArrayList<>(allDifferentMap.keySet());
        for (int i = 0; i < allDifferentMap.size(); i++) {
            variables[i] = model.intVar("foo", domains.get(ordered.get(i)));
        }
        model.allDifferent(variables).post();
        return model.getSolver().solve();
    }


    public static Set<Pair<Integer, Integer>> checkAll(Map<Integer, Set<Integer>> allDifferentMap) {
        Set<Pair<Integer, Integer>> res = new HashSet<>();
        final int[][] domains = new int[allDifferentMap.size()][];
        allDifferentMap.forEach((key, value) -> domains[key] = (value.stream().mapToInt(x -> x).toArray()));

        Model model = new Model("Foo");
        IntVar[] variables = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
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
}
