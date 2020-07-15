package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.settings.pruning.domainfilter.FilteringSettings;

public class ParallelPruner extends DefaultSerialPruner {


    private final PruningThread pruningThread;
    private final Thread theThread;
    volatile boolean isInPruningState = false;
    private PartialMatching partialMatching;

    public ParallelPruner(Pruner inner, FilteringSettings filter, MyGraph sourceGraph, MyGraph targetGraph) {
        super(filter, sourceGraph, targetGraph, inner.occupation);
        pruningThread = new PruningThread(this, inner);
        theThread = new Thread(pruningThread);
        theThread.start();
    }

    private void donePruning() {
        synchronized (this) {
            isInPruningState = false;
            notifyAll();
        }
    }

    @Override
    public Pruner copy() {
        throw new UnsupportedOperationException(); //todo;
    }

    @Override
    public void checkPartial(PartialMatching partialMatching) throws DomainCheckerException {
        this.partialMatching = partialMatching;
        if (this.isInPruningState) {
            boolean pruned = false;

            //we have to serially prune here! If pruned, throw
            //todo

            if (pruned) {
                throw new DomainCheckerException("");
            } else {
                donePruning();
            }
            throw new UnsupportedOperationException();
        }
    }

    PartialMatching getCurrentMatching() {
        return partialMatching;
    }
}
