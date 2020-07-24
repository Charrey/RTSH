package com.charrey.pruning;

import java.util.LinkedList;
import java.util.List;

public class PruningThread implements Runnable {

    private final ParallelPruner pruner;
    private final Pruner inner;
    private final List<ParallelPruner> subscribers;
    private boolean done = false;

    PruningThread(ParallelPruner parallelPruner, Pruner inner) {
        this.pruner = parallelPruner;
        this.inner = inner;
        this.subscribers = new LinkedList<>();
        subscribers.add(parallelPruner);
    }

    private void signalPrune() {
        subscribers.forEach(parallelPruner -> parallelPruner.isInPruningState = true);
    }

    @Override
    public void run() {
        try {
            while (!done) {
                //retrieve current matching
                while (pruner.isInPruningState) {
                    Thread.sleep(1);
                }
                PartialMatching partialMatching = pruner.getCurrentMatching();
                try {
                    pruner.checkPartial(partialMatching);
                } catch (DomainCheckerException e) {
                    signalPrune();
                }
                synchronized (pruner) {
                    if (pruner.isInPruningState) {
                        pruner.wait();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    void setDone() {
        done = true;
    }
}
