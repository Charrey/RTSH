package com.charrey.algorithms;


import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.THashSet;
import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.ValueIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * The AllDifferent algorithm
 */
public class AllDifferent {

    private static final Settings settings = new DefaultSettings();
    private final Set<TIntObjectMap<TIntSet>> cacheYesA = new HashSet<>();
    private final Set<TIntObjectMap<TIntSet>> cacheNoA = new HashSet<>();

    /**
     * Given a domain map, runs AllDifferent after each possible single mapping. This method returns a set of mappings that lead to contradictions,
     * i.e. that need to be removed.
     *
     * @param allDifferentMap a map that indicates for each source vertex its domain in target graph vertices
     * @return a set of pairs such that the key of the pair may not be mapped to the value of the pair.
     */
    @NotNull
    static Set<int[]> checkAll(@NotNull TIntObjectMap<TIntSet> allDifferentMap, int iteration, String name) {
        final int[][] domains = new int[allDifferentMap.size()][];

        allDifferentMap.forEachEntry((key, value) -> {
            domains[key] = value.toArray();
            return true;
        });

        Model model = new Model(settings);
        IntVar[] variables = new IntVar[domains.length];
        for (int i = 0; i < domains.length; i++) {
            if (domains[i].length == 0) {
                return constructSetOfPairs(allDifferentMap);
            }
            variables[i] = model.intVar("foo", domains[i]);
        }
        model.allDifferent(variables).post();
        Set<int[]> res = new HashSet<>();

        long toProcess = 0L;
        Collection<TIntSet> values = allDifferentMap.valueCollection();
        for (TIntSet value : values) {
            toProcess = toProcess + value.size();
        }

        long lastTimePrinted = System.currentTimeMillis();


        for (int i = 0; i < variables.length; i++) {
            ValueIterator iterator = variables[i].getValueIterator(false);
            iterator.bottomUpInit();
            while (iterator.hasNext()) {
                if (System.currentTimeMillis() - lastTimePrinted > 1000) {
                    lastTimePrinted = System.currentTimeMillis();
                }
                int value = iterator.next();
                Constraint bind = model.allEqual(variables[i], model.intVar(value));
                bind.post();
                if (!model.getSolver().solve()) {
                    res.add(new int[]{i, value});
                }
                model.unpost(bind);
                model.getSolver().reset();
            }
        }
        return res;
    }

    @NotNull
    private static Set<int[]> constructSetOfPairs(@NotNull TIntObjectMap<TIntSet> allDifferentMap) {
        Set<int[]> toRemove = new THashSet<>();

        allDifferentMap.forEachEntry((key, values) -> {
            values.forEach(value -> {
                toRemove.add(new int[]{key, value});
                return true;
            });
            return true;
        });
        return toRemove;
    }

    /**
     * Returns whether the domain satisfies an AllDifferent constraint. This is needed for a node disjoint subgraph homeomorphism.
     *
     * @param allDifferentMap a map that indicates for each source vertex its domain in target graph vertices
     * @return whether the constraint is satisfied.
     */
    public boolean get(@NotNull TIntObjectMap<TIntSet> allDifferentMap) {
        if (cacheYesA.contains(allDifferentMap)) {
            return true;
        } else if (cacheNoA.contains(allDifferentMap)) {
            return false;
        } else {
            final TIntObjectMap<int[]> domains = new TIntObjectHashMap<>();

            final boolean[] succeeded = {true};
            allDifferentMap.forEachEntry((i, tIntSet) -> {
                if (tIntSet.isEmpty()) {
                    succeeded[0] = false;
                    return false;
                }
                return true;
            });
            if (!succeeded[0]) {
                return false;
            }

            allDifferentMap.forEachEntry((key, values) -> {
                domains.put(key, values.toArray());
                return true;
            });

            Model model = new Model(settings);
            TIntList ordered = new TIntArrayList(allDifferentMap.keySet());
            IntVar[] variables = new IntVar[ordered.size()];
            final int[] counter = {0};
            ordered.forEach(x -> {
                variables[counter[0]] = model.intVar(String.valueOf(ordered.indexOf(x)), domains.get(x));
                counter[0]++;
                return true;
            });

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
    public boolean get(@NotNull List<TIntSet> compatibility) {
        Model model = new Model(settings);
        IntVar[] variables = IntStream.range(0, compatibility.size())
                .boxed()
                .map(i -> model.intVar(String.valueOf(i), compatibility.get(i).toArray()))
                .toArray(IntVar[]::new);
        model.allDifferent(variables).post();
        return model.getSolver().solve();
    }
}
