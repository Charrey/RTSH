package com.charrey.occupation;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.runtimecheck.DomainChecker;
import com.charrey.runtimecheck.DomainCheckerException;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OccupationTransaction extends AbstractOccupation {

    private final BitSet routingBits;
    private final BitSet vertexBits;
    private final DomainChecker domainChecker;
    private final GlobalOccupation parent;

    private LinkedList<TransactionElement> waiting = new LinkedList<>();
    private boolean locked = false;

    OccupationTransaction(BitSet routingBits, BitSet vertexBits, DomainChecker domainChecker, GlobalOccupation parent) {
        this.routingBits = routingBits;
        this.vertexBits = vertexBits;
        this.domainChecker = domainChecker;
        this.parent = parent;
    }

    public void occupyRoutingAndCheck(int verticesPlaced, @NotNull Vertex v) throws DomainCheckerException {
        assert !routingBits.get(v.data());
        routingBits.set(v.data());
        String previous = null;
        try {
            previous = domainChecker.toString();
            domainChecker.afterOccupyEdge(verticesPlaced, v);
            waiting.add(new TransactionElement(verticesPlaced, v));
        } catch (DomainCheckerException e) {
            assertEquals(previous, domainChecker.toString());
            routingBits.clear(v.data());
            throw e;
        }
    }

    public void occupyRoutingAndCheck(int verticesPlaced, @NotNull Path p) throws DomainCheckerException {
        for (int i = 0; i < p.intermediate().size(); i++) {
            try {
                occupyRoutingAndCheck(verticesPlaced, p.intermediate().get(i));
            } catch (DomainCheckerException e) {
                for (int j = i - 1; j >= 0; j--) {
                    releaseRouting(verticesPlaced, p.intermediate().get(j));
                }
                throw e;
            }
        }
    }

    public void releaseRouting(int verticesPlaced, @NotNull Vertex v) {
        assert isOccupiedRouting(v);
        routingBits.clear(v.data());
        domainChecker.afterReleaseEdge(verticesPlaced, v);
        waiting.remove(new TransactionElement(verticesPlaced, v));
    }

    private boolean isOccupiedRouting(@NotNull Vertex v) {
        return routingBits.get(v.data());
    }

    private boolean isOccupiedVertex(@NotNull Vertex v) {
        return vertexBits.get(v.data());
    }

    public boolean isOccupied(@NotNull Vertex v) {
        return isOccupiedRouting(v) || isOccupiedVertex(v);
    }


    public void uncommit() {
        for (int i = waiting.size() - 1; i >= 0; i--) {
            TransactionElement transactionElement = waiting.get(i);
            parent.releaseRouting(transactionElement.verticesPlaced, transactionElement.added);
        }
        locked = false;
    }

    public void commit() {
        if (locked) {
            throw new IllegalStateException("You must uncommit before committing.");
        }
        for (TransactionElement transactionElement : waiting) {
            try {
                parent.occupyRoutingAndCheck(transactionElement.verticesPlaced, transactionElement.added);
            } catch (DomainCheckerException e) {
                assert false;
            }
        }
        locked = true;
    }

    private static class TransactionElement {
        private final int verticesPlaced;
        private final Vertex added;
        private final long time;

        TransactionElement(int verticesPlaced, Vertex added) {
            this.verticesPlaced = verticesPlaced;
            this.added = added;
            this.time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TransactionElement that = (TransactionElement) o;
            return verticesPlaced == that.verticesPlaced &&
                    added.equals(that.added);
        }

        @Override
        public int hashCode() {
            return Objects.hash(verticesPlaced, added);
        }
    }
}
