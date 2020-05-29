package com.charrey.settings;

import org.jgrapht.alg.util.Pair;

import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
import java.util.function.ToIntFunction;

public class Settings {




    public static final Settings instance = new Settings(
            true,
            true,
            true,
            RunTimeCheck.ALL_DIFFERENT,
            PathIterationStrategy.YEN,
            new Random(300));




    public boolean initialLocalizedAllDifferent;
    public boolean initialGlobalAllDifferent;
    public boolean refuseLongerPaths;
    public int runTimeCheck;
    public int pathIteration;
    public Random random;

    public Settings(boolean initialLocalizedAllDifferent,
                    boolean initialGlobalAllDifferent,
                    boolean refuseLongerPaths,
                    int runTimeCheck, int pathIteration, Random random) {
        this.initialLocalizedAllDifferent = initialLocalizedAllDifferent;
        this.initialGlobalAllDifferent = initialGlobalAllDifferent;
        this.refuseLongerPaths = refuseLongerPaths;
        this.runTimeCheck = runTimeCheck;
        this.pathIteration = pathIteration;
        this.random = random;
    }

    public static Pair<Settings, Long> readString(String content) {
        String[] separated = content.split(",");
        Settings res = new Settings(
                separated[0].equals("1"),
                separated[1].equals("1"),
                separated[2].equals("1"),
                Integer.parseInt(separated[3]),
                Integer.parseInt(separated[4]),
                new Random(300)
                );
        return new Pair<>(res, Long.parseLong(separated[5]));
    }

    public static String writeString(Pair<Settings, Long> x) {
        return (x.getFirst().initialLocalizedAllDifferent ? "1" : "0") + "," +
                (x.getFirst().initialGlobalAllDifferent ? "1" : "0") + "," +
                (x.getFirst().refuseLongerPaths ? "1" : "0") + "," +
                x.getFirst().runTimeCheck + "," +
                x.getFirst().pathIteration+ "," + x.getSecond();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return initialLocalizedAllDifferent == settings.initialLocalizedAllDifferent &&
                initialGlobalAllDifferent == settings.initialGlobalAllDifferent &&
                refuseLongerPaths == settings.refuseLongerPaths &&
                runTimeCheck == settings.runTimeCheck &&
                pathIteration == settings.pathIteration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialLocalizedAllDifferent, initialGlobalAllDifferent, refuseLongerPaths, runTimeCheck, pathIteration, random);
    }

    public static Comparator<Settings> comparator = Comparator.comparingInt((ToIntFunction<Settings>) value -> value.initialLocalizedAllDifferent ? 1 : 0)
            .thenComparingInt(o2 -> o2.initialGlobalAllDifferent ? 1 : 0)
            .thenComparingInt(o2 -> o2.refuseLongerPaths ? 1 : 0)
            .thenComparingInt(o2 -> o2.runTimeCheck)
            .thenComparingInt(o2 -> o2.pathIteration);

    public int getByIndex(int index) {
        switch (index) {
            case 0:
                return initialLocalizedAllDifferent ? 1 : 0;
            case 1:
                return initialGlobalAllDifferent ? 1 : 0;
            case 2:
                return refuseLongerPaths ? 1 : 0;
            case 3:
                return runTimeCheck;
            case 4:
                return pathIteration;
            default:
                return -1;
        }
    }
}
