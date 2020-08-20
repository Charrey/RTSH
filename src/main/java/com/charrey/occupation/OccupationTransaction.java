package com.charrey.occupation;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.Path;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.PartialMatching;
import com.charrey.pruning.Pruner;
import com.charrey.util.MyLinkedList;
import gnu.trove.set.TIntSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class very similar to GlobalOccupation, but used exclusively for registration of intermediate vertices.
 * This is linked to a GlobalOccupation object that stores other occupations. All changes can be pushed to the
 * GlobalOccupation object or made undone in single method calls.
 */
public class OccupationTransaction implements AbstractOccupation {

    private final TIntSet routingOccupied;
    private final TIntSet vertexOccupied;
    private final Pruner domainChecker;
    private final GlobalOccupation parent;

    private final MyLinkedList<TransactionElement> waiting = new MyLinkedList<>();
    private final UtilityData data;
    private boolean inCommittedState = false;

    /**
     * Instantiates a new OccupationTransaction and initializes it.
     *
     * @param routingOccupied initial vertices occupied for routing
     * @param vertexOccupied  initial vertices occupied for vertex-on-vertex
     * @param domainChecker   domain checker for pruning the search space
     * @param data            utility data for cached computations
     * @param parent          GlobalOccupation to which changes may be committed
     */
    OccupationTransaction(TIntSet routingOccupied, TIntSet vertexOccupied, Pruner domainChecker, UtilityData data, GlobalOccupation parent) {
        this.routingOccupied = routingOccupied;
        this.vertexOccupied = vertexOccupied;
        this.domainChecker = domainChecker;
        this.parent = parent;
        this.data = data;
    }

    /**
     * Occupies a vertex in the target graph and marks it as being used as 'intermediate' vertex.
     *
     * @param vertexPlacementSize the number of source graph vertices placed
     * @param vertex              the vertex being occupied for routing purposes
     * @throws DomainCheckerException thrown when this occupation would result in a dead end in the search.                                If this is thrown, this class remains unchanged.
     */
    public void occupyRoutingAndCheck(int vertexPlacementSize, int vertex, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        if (routingOccupied.contains(vertex)) {
            throw new IllegalStateException("Vertex was already occupied for routing!");
        }
        routingOccupied.add(vertex);
        try {
            domainChecker.afterOccupyEdge(vertexPlacementSize, vertex, partialMatching);
            waiting.add(new TransactionElement(vertexPlacementSize, vertex));
        } catch (DomainCheckerException e) {
            routingOccupied.remove(vertex);
            throw e;
        }
    }

    /**
     * Occupies all vertex along a target graph and marks them as being used as 'intermediate' vertices.
     *
     * @param vertexPlacementSize the number of source graph vertices placed
     * @param path                the path whose vertices to occupy
     * @throws DomainCheckerException thrown when this occupation would result in a dead end in the search.                                If this is thrown, this class remains unchanged.
     */
    public void occupyRoutingAndCheck(int vertexPlacementSize, @NotNull Path path, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        for (int i = 0; i < path.intermediate().length(); i++) {
            try {
                occupyRoutingAndCheck(vertexPlacementSize, path.intermediate().get(i), partialMatching);
            } catch (DomainCheckerException e) {
                for (int j = i - 1; j >= 0; j--) {
                    releaseRouting(vertexPlacementSize, path.intermediate().get(j));
                }
                throw e;
            }
        }
    }

    /**
     * Unregister a vertex that was initially marked as used as intermediate vertex.
     *
     * @param vertexPlacementSize the number of source graph vertices placed
     * @param vertex              the vertex that is being unregistered
     * @throws IllegalArgumentException thrown when the vertex was not occupied for routing
     */
    public void releaseRouting(int vertexPlacementSize, int vertex) {
        if (!isOccupiedRouting(vertex)) {
            throw new IllegalArgumentException("Cannot release a vertex that was never occupied (for routing purposes): " + vertex);
        }
        routingOccupied.remove(vertex);
        domainChecker.afterReleaseEdge(vertexPlacementSize, vertex);
        waiting.removeFromBack(new TransactionElement(vertexPlacementSize, vertex));
    }

    private boolean isOccupiedRouting(int v) {
        return routingOccupied.contains(v);
    }

    private boolean isOccupiedVertex(int v) {
        return vertexOccupied.contains(v);
    }

    public boolean isOccupied(int vertex) {
        return isOccupiedRouting(vertex) || isOccupiedVertex(vertex);
    }


    /**
     * Remove the changes of this transaction from the globalOccupation. For example, if this transaction was used
     * to mark vertex 16 as occupied and commit() was called, the occupation would be visible everywhere in this
     * program. By calling uncommit(), the occupation becomes hidden again.
     */
    public void uncommit(int verticesPlaced) {
        if (!inCommittedState) {
            return;
        }
        Set<Integer> totalCover = new HashSet<>();
        for (TransactionElement transactionElement : waiting) {
            totalCover.addAll(data.unconfigurableCover(transactionElement.added));
        }
        for (TransactionElement transactionElement : waiting) {
            totalCover.remove(transactionElement.added);
        }
        for (int i = waiting.size() - 1; i >= 0; i--) {
            TransactionElement transactionElement = waiting.get(i);
            parent.releaseRouting(transactionElement.verticesPlaced, transactionElement.added);
        }
        for (int coverElement : totalCover) {
            parent.releaseRouting(verticesPlaced, coverElement);
        }
        inCommittedState = false;
    }

    /**
     * Make the changes in this transaction visible to the rest of this program.
     */
    public void commit(int vertexPlacementSize, PartialMatchingProvider partialMatching) throws DomainCheckerException {
        if (inCommittedState) {
            throw new IllegalStateException("You must uncommit before committing.");
        }
        Set<TransactionElement> committed = new HashSet<>();
        Set<Integer> totalCover = new HashSet<>();
        for (TransactionElement transactionElement : waiting) {
            totalCover.addAll(data.unconfigurableCover(transactionElement.added));
        }
        for (TransactionElement transactionElement : waiting) {
            totalCover.remove(transactionElement.added);
        }
        for (TransactionElement transactionElement : waiting) {
            parent.occupyRoutingWithoutCheck(transactionElement.verticesPlaced, transactionElement.added);
            committed.add(transactionElement);
        }
        for (int coverElement : totalCover) {
            try {
                parent.occupyRoutingAndCheck(vertexPlacementSize, coverElement, partialMatching);
                committed.add(new TransactionElement(vertexPlacementSize, coverElement));
            } catch (DomainCheckerException e) {
                committed.forEach(x -> parent.releaseRouting(x.verticesPlaced, x.added));
                throw e;
            }
        }
        inCommittedState = true;
    }

    private static class TransactionElement {
        private final int verticesPlaced;
        private final int added;

        /**
         * Instantiates a new Transaction element.
         *
         * @param verticesPlaced the vertices placed
         * @param added          the added
         */
        TransactionElement(int verticesPlaced, int added) {
            this.verticesPlaced = verticesPlaced;
            this.added = added;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!o.getClass().equals(getClass())) {
                return false;
            }
            TransactionElement that = (TransactionElement) o;
            return verticesPlaced == that.verticesPlaced &&
                    added == that.added;
        }

        @Override
        public int hashCode() {
            return Objects.hash(verticesPlaced, added);
        }
    }
}
