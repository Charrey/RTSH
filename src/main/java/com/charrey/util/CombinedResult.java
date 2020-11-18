package com.charrey.util;

public class CombinedResult {
    public final boolean firstWon;
    public final boolean secondWon;
    public final double value;

    public CombinedResult(boolean firstWon, boolean secondWon, double value) {
        this.firstWon = firstWon;
        this.secondWon = secondWon;
        this.value = value;
    }
}