package com.charrey.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.util.Pair;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * Class to encapsulate the different settings under which homeomorphisms may be found. Each setting influences the
 * behaviour of the search in some way.
 */
public class Settings {


    /**
     * A comparator to lexicographically sort Settings objects
     */
    public static final Comparator<Settings> comparator = Comparator.comparingInt((ToIntFunction<Settings>) value -> value.initialNeighbourhoodFiltering ? 1 : 0)
            .thenComparingInt(o2 -> o2.initialGlobalAllDifferent ? 1 : 0)
            .thenComparingInt(o2 -> o2.refuseLongerPaths ? 1 : 0)
            .thenComparingInt(o2 -> o2.pruningMethod)
            .thenComparingInt(o2 -> o2.pathIteration);
    /**
     * Whether to apply domain reduction by filtering for compatible neighbourhoods.
     */
    public final boolean initialNeighbourhoodFiltering;
    /**
     * Whether AllDifferent needs to be applied for each possible matching at the start to reduce the domains.
     */
    public final boolean initialGlobalAllDifferent;
    /**
     * Whether to refuse paths that take up unnecessarily many resources.
     */
    public final boolean refuseLongerPaths;
    /**
     * Which pruning method to use (select from PruningConstants.java)
     */
    public int pruningMethod;
    /**
     * Which method to iterate paths is used (select from PathIterationConstants.java)
     */
    public int pathIteration;

    /**
     * Instantiates a new Settings.
     *
     * @param initialNeighbourhoodFiltering Whether to apply domain reduction by filtering for compatible neighbourhoods.
     * @param initialGlobalAllDifferent     Whether AllDifferent needs to be applied for each possible matching at the start                                      to reduce the domains.
     * @param refuseLongerPaths             Whether to refuse paths that take up unnecessarily many resources.
     * @param pruningMethod                 Which pruning method to use (select from PruningConstants.java)
     * @param pathIteration                 Which method to iterate paths is used (select from PathIterationConstants.java)
     */
    public Settings(boolean initialNeighbourhoodFiltering,
                    boolean initialGlobalAllDifferent,
                    boolean refuseLongerPaths,
                    int pruningMethod, int pathIteration) {
        this.initialNeighbourhoodFiltering = initialNeighbourhoodFiltering;
        this.initialGlobalAllDifferent = initialGlobalAllDifferent;
        this.refuseLongerPaths = refuseLongerPaths;
        this.pruningMethod = pruningMethod;
        this.pathIteration = pathIteration;
    }

    /**
     * Reads a pair of Settings and a performance measure from a String. This may be useful for
     * Comparing the performance of different settings on the same graph. This is the counterpart of writeString().
     *
     * @param serialized the serialized pair of Settings and performance metric
     * @return a Settings object and the performance value
     */
    @NotNull
    public static Pair<Settings, Long> readString(@NotNull String serialized) {
        String[] separated = serialized.split(",");
        Settings res = new Settings(
                separated[0].equals("1"),
                separated[1].equals("1"),
                separated[2].equals("1"),
                Integer.parseInt(separated[3]),
                Integer.parseInt(separated[4])
        );
        return new Pair<>(res, Long.parseLong(separated[5]));
    }

    /**
     * Serializes a Settings object (and a long indicating performance) into a String. This may be useful for
     * Comparing the performance of different settings on the same graph. This is the counterpart of readString().
     *
     * @param settings    the settings to serialize
     * @param performance the performance metric to serialize
     * @return a serialized string
     */
    @NotNull
    public static String writeString(@NotNull Settings settings, long performance) {
        return (settings.initialNeighbourhoodFiltering ? "1" : "0") + "," +
                (settings.initialGlobalAllDifferent ? "1" : "0") + "," +
                (settings.refuseLongerPaths ? "1" : "0") + "," +
                settings.pruningMethod + "," +
                settings.pathIteration + "," + performance;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settings settings = (Settings) o;
        return initialNeighbourhoodFiltering == settings.initialNeighbourhoodFiltering &&
                initialGlobalAllDifferent == settings.initialGlobalAllDifferent &&
                refuseLongerPaths == settings.refuseLongerPaths &&
                pruningMethod == settings.pruningMethod &&
                pathIteration == settings.pathIteration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialNeighbourhoodFiltering, initialGlobalAllDifferent, refuseLongerPaths, pruningMethod, pathIteration);
    }

    /**
     * Gets an option as integer by the index of that option. Useful for serialization.
     *
     * @param index index of the option in this settings object
     * @return integer value of that option (where booleans are converted to one (true) and zero (false).
     * @throws IndexOutOfBoundsException thrown when no option has that index.
     */
    public int getByIndex(int index) {
        switch (index) {
            case 0:
                return initialNeighbourhoodFiltering ? 1 : 0;
            case 1:
                return initialGlobalAllDifferent ? 1 : 0;
            case 2:
                return refuseLongerPaths ? 1 : 0;
            case 3:
                return pruningMethod;
            case 4:
                return pathIteration;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
