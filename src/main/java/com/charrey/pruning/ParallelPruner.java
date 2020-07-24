package com.charrey.pruning;

import com.charrey.graph.MyGraph;
import com.charrey.settings.Settings;

public class ParallelPruner extends DefaultSerialPruner {


    private final PruningThread pruningThread;
    private final Thread theThread;
    private final Pruner inner;
    volatile boolean isInPruningState = false;
    private PartialMatching partialMatching;

    public ParallelPruner(Pruner inner, Settings settings, MyGraph sourceGraph, MyGraph targetGraph) {
        super(settings, sourceGraph, targetGraph, inner.occupation);
        this.inner = inner;
        pruningThread = new PruningThread(this, inner);
        theThread = new Thread(pruningThread);
        theThread.start();
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private ParallelPruner(ParallelPruner parallelPruner) {
        super(parallelPruner.settings, parallelPruner.sourceGraph, parallelPruner.targetGraph, parallelPruner.occupation);
        synchronized (parallelPruner) {
            this.inner = parallelPruner.inner.copy();
            this.pruningThread = parallelPruner.pruningThread;
            this.theThread = parallelPruner.theThread;
            this.isInPruningState = parallelPruner.isInPruningState;
            this.partialMatching = parallelPruner.partialMatching;
        }
    }

    @Override
    public Pruner copy() {
        return new ParallelPruner(this);
    }

    @Override
    public void close() {
        pruningThread.setDone();
        try {
            theThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void checkPartial(PartialMatching partialMatching) throws DomainCheckerException {
        this.partialMatching = partialMatching;
        if (this.isInPruningState) {
            inner.checkPartial(partialMatching);
            synchronized (this) {
                isInPruningState = false;
                notifyAll();
            }
        }
    }

    PartialMatching getCurrentMatching() throws InterruptedException {
        PartialMatching toReturn = this.partialMatching;
        while (toReturn == null) {
            Thread.sleep(10);
            toReturn = this.partialMatching;
        }
        return partialMatching;
    }
}
