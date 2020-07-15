package com.charrey.pruning;

public class PruningThread implements Runnable {

    private final ParallelPruner pruner;
    private final Pruner inner;

    public PruningThread(ParallelPruner parallelPruner, Pruner inner) {
        this.pruner = parallelPruner;
        this.inner = inner;
    }

    @Override
    public void run() {
        while (true) {
            //retrieve current matching
            PartialMatching partialMatching = pruner.getCurrentMatching();
            try {
                pruner.checkPartial(partialMatching);
            } catch (DomainCheckerException e) {
                signalPrune();
            }
            synchronized (pruner) {
                if (pruner.isInPruningState) {
                    try {
                        pruner.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


            //todo;
            throw new UnsupportedOperationException();
        }
    }

    private void waitForDonePruning() {
    }

    private void signalPrune() {
    }
}
