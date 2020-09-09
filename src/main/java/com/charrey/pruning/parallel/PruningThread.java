package com.charrey.pruning.parallel;

import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.serial.PartialMatching;

import java.util.LinkedList;
import java.util.List;

public class PruningThread implements Runnable {

    private final ParallelPruner pruner;
    private final List<ParallelPruner> subscribers;
    private boolean done = false;

    PruningThread(ParallelPruner parallelPruner) {
        this.pruner = parallelPruner;
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
                check(partialMatching);
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

    private void check(PartialMatching partialMatching) {
        try {
            pruner.checkPartial(() -> partialMatching);
        } catch (DomainCheckerException e) {
            signalPrune();
        }
    }


    void setDone() {
        done = true;
    }
}
