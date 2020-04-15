package com.charrey.util;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.util.Solution;
import org.jgrapht.alg.util.Pair;

import static com.charrey.util.Solution.*;

public class ConflictReport {

    public static final ConflictReport OK = new ConflictReport();
    public final boolean ok;
    public final Solution solution;


    //duplicate vertex caused by vertex placement
    public ConflictReport(Vertex vertex) {
        ok = false;
        solution = RETRY_LAST_VERTEX;
    }

    private ConflictReport() {
        this.ok = true;
        solution = NOTHING;
    }

    //last path provided conflicts with another path
    public ConflictReport(Path lastPath, Path path) {
        ok = false;
        solution = RETRY_LAST_PATH;
    }

    //last path provided conflicts with placement
    public ConflictReport(Path lastPath) {
        ok = false;
        solution = RETRY_LAST_PATH;
    }

    public ConflictReport and(ConflictReport conflictedPath) {
        if (this.ok) {
            return conflictedPath;
        } else if (conflictedPath.ok) {
            return this;
        } else if (this.solution == conflictedPath.solution) {
            return this;
        } else {
            return this.solution == RETRY_LAST_PATH ? this : conflictedPath;
        }
    }
}
