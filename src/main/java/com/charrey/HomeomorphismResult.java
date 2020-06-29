package com.charrey;

import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import org.jetbrains.annotations.NotNull;

/**
 * The result from a subgraph homeomorphism search
 */
public class HomeomorphismResult {

    /**
     * Result provided if during the initial domain filtering we could already conclude there does not exist a subgraph
     * homeomorphism
     */
    static final HomeomorphismResult COMPATIBILITY_FAIL = new HomeomorphismResult(true, null, null, 0);
    private final VertexMatching matching;
    private final EdgeMatching edgeMatching;
    /**
     * Whether a homeomorphism was found (true = not found, false = found).
     */
    public final boolean failed;
    /**
     * The number of iterations the algorithm took to find the homeomorphism. An iteration is one of the following:
     * - Add a vertex matching
     * - Replace a vertex matching by a new one
     * - Remove a vertex matching and all its edge matchings
     * - Add an edge matching
     * - Replace an edge matching by a new one
     * - Remove an edge matching
     */
    public final long iterations;

    private HomeomorphismResult(boolean failed, VertexMatching matching, EdgeMatching edgeMatching, long iterations) {
        this.failed = failed;
        this.matching = matching;
        this.edgeMatching = edgeMatching;
        this.iterations = iterations;
    }

    /**
     * Construction method of a result where no homeomorphism was found.
     *
     * @param iterations iterations performed before stopping the search
     * @return result object providing relevant information (it failed with x iterations)
     */
    @NotNull
    static HomeomorphismResult ofFailed(long iterations) {
        return new HomeomorphismResult(true, null, null, iterations);
    }

    /**
     * Construction method of a result where a homeomorphism was found.
     *
     * @param vertexMatching the vertex-to-vertex matching of the homeomorphism.
     * @param edgeMatching   the edge-to-path matching of the homeomorphism.
     * @param iterations     the number of iterations performed before finding the homeomorphism
     * @return result object providing relevant information
     */
    @NotNull
    static HomeomorphismResult ofSucceed(VertexMatching vertexMatching, EdgeMatching edgeMatching, long iterations) {
        return new HomeomorphismResult(false, vertexMatching, edgeMatching, iterations);
    }

    @NotNull
    public String toString() {
        return failed ? "FAILED!" : matching.toString() + "\n" + edgeMatching.toString();
    }

    public int[] getPlacement() {
        return failed ? new int[0] : matching.getPlacement().stream().mapToInt(x -> x).toArray();
    }
}
