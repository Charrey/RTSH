package com.charrey.util;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;

import java.util.HashSet;
import java.util.List;

public class Util {



    public static ConflictReport conflicted(VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        return conflictedVertex(vertexMatching, edgeMatching).and(conflictedPath(vertexMatching, edgeMatching));
    }

    private static ConflictReport conflictedVertex(VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        boolean vertexCompatible =  vertexMatching.getPlacement().size() == new HashSet<>(vertexMatching.getPlacement()).size();
        if (vertexMatching.getPlacement().isEmpty()) {
            return ConflictReport.OK;
        }
        Vertex culprit = vertexMatching.getPlacement().get(vertexMatching.getPlacement().size()-1);
        if (!vertexCompatible) {
            return new ConflictReport(culprit);
        }
        boolean edgeCompatible   = edgeMatching
                .allPaths()
                .stream()
                .map(Path::intermediate)
                .noneMatch(x -> x.contains(vertexMatching.getPlacement().get(vertexMatching.getPlacement().size()-1)));
        if (!edgeCompatible) {
            return new ConflictReport(culprit);
        } else {
            return ConflictReport.OK;
        }
    }

    private static ConflictReport conflictedPath(VertexMatching vertexMatching, EdgeMatching edgeMatching) {
        if (vertexMatching.getPlacement().isEmpty() || edgeMatching.paths.get(vertexMatching.getPlacement().size() - 1).isEmpty()) {
            return ConflictReport.OK;
        }
        Path lastPath  = edgeMatching.paths.get(vertexMatching.getPlacement().size() - 1).getLast();
        for (List<Path> paths : edgeMatching.paths) {
            for (Path path : paths) {
                if (path != lastPath && path.intermediate().stream().anyMatch(x -> lastPath.intermediate().contains(x))) {
                    return new ConflictReport(lastPath, path);
                }
            }
        }
        boolean vertexCompatible =  lastPath
                .intermediate()
                .stream()
                .noneMatch(x -> vertexMatching.getPlacement().contains(x));
        if (!vertexCompatible) {
            return new ConflictReport(lastPath);
        } else {
            return ConflictReport.OK;
        }
    }




}
