package com.charrey.algorithms;

import com.charrey.graph.MyGraph;
import gnu.trove.TIntCollection;
import org.jgrapht.Graphs;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class RefuseLongerPaths {

    public static boolean canReachThroughIsolatedPath(MyGraph targetGraph, Integer from, TIntCollection to, int butNotThrough) {
        Deque<Integer> frontier = new LinkedList<>();
        Set<Integer> explored = new HashSet<>();
        frontier.add(from);
        explored.add(butNotThrough);

        while (!frontier.isEmpty()) {
            Integer considering = frontier.pollFirst();
            explored.add(considering);
            if (Graphs.successorListOf(targetGraph, considering).stream().anyMatch(to::contains)) {
                return true;
            } else if (Graphs.successorListOf(targetGraph, considering).size() == 1 && !explored.contains(Graphs.successorListOf(targetGraph, considering).iterator().next())) {
                frontier.add(Graphs.successorListOf(targetGraph, considering).iterator().next());
            }
        }
        return false;
    }

    public static boolean canBeReachedThroughIsolatedPath(MyGraph targetGraph, TIntCollection from, Integer to, int butNotThrough) {
        Deque<Integer> frontier = new LinkedList<>();
        Set<Integer> explored = new HashSet<>();
        frontier.add(to);
        explored.add(butNotThrough);

        while (!frontier.isEmpty()) {
            Integer considering = frontier.pollFirst();
            explored.add(considering);
            if (Graphs.predecessorListOf(targetGraph, considering).stream().anyMatch(from::contains)) {
                return true;
            } else if (Graphs.predecessorListOf(targetGraph, considering).size() == 1 && !explored.contains(Graphs.predecessorListOf(targetGraph, considering).iterator().next())) {
                frontier.add(Graphs.predecessorListOf(targetGraph, considering).iterator().next());
            }
        }
        return false;
    }




}
