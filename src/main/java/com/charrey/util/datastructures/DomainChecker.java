package com.charrey.util.datastructures;

import com.charrey.graph.Vertex;
import com.charrey.util.UtilityData;

import java.util.Arrays;

public class DomainChecker {

    private final Vertex[][] reverseDomain;
    private final int[] domainSizes;


    public DomainChecker(UtilityData data) {
        this.reverseDomain = data.getReverseCompatibility();
        this.domainSizes = Arrays.stream(data.getCompatibility()).mapToInt(value -> value.length).toArray();
    }


    public void afterRelease(int verticesPlaced, Vertex v) {
        for (int i = reverseDomain[v.data()].length - 1; i >= 0; i--) {
            int candidate = reverseDomain[v.data()][i].data();
            if (candidate < verticesPlaced) {
                return;
            } else {
                domainSizes[candidate]++;
            }
        }
    }

    public void afterOccupy(int verticesPlaced, Vertex v) {
        boolean bad = false;
        int revertFrom = reverseDomain[v.data()].length;
        for (int i = reverseDomain[v.data()].length - 1; i >= 0; i--) {
            int candidate = reverseDomain[v.data()][i].data();
            if (candidate < verticesPlaced) {
                return;
            } else {
                domainSizes[candidate]--;
                if (domainSizes[candidate] == 0) {
                    revertFrom = i;
                    bad = true;
                    break;
                }
            }
        }
        for (int i = revertFrom; i < reverseDomain[v.data()].length; i++) {
            domainSizes[reverseDomain[v.data()][i].data()]++;
        }
        if (bad) {
            //throw new EmptyDomainException();
        }
    }

//    public static class EmptyDomainException extends Throwable {
//    }
}
