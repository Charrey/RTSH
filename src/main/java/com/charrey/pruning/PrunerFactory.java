package com.charrey.pruning;

import com.charrey.algorithms.UtilityData;
import com.charrey.graph.MyGraph;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.cached.*;
import com.charrey.pruning.parallel.ParallelPruner;
import com.charrey.pruning.serial.SerialZeroDomainPruner;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import com.charrey.settings.pruning.WhenToApply;
import com.charrey.settings.pruning.domainfilter.MReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.NReachabilityFiltering;
import com.charrey.settings.pruning.domainfilter.UnmatchedDegreesFiltering;

public class PrunerFactory {

    public static Pruner get(Settings settings, VertexMatching vertexMatching, UtilityData data, GlobalOccupation occupation) {
        if (settings.getWhenToApply() == WhenToApply.PARALLEL) {
            return new ParallelPruner(get(new SettingsBuilder(settings).withSerialPruning().get(), vertexMatching, data, occupation),
                    settings, data.getPatternGraph(), data.getTargetGraph());
        }
        return switch (settings.getPruningMethod()) {
            case NONE -> new NoPruner();
            case ZERODOMAIN ->
                switch (settings.getWhenToApply()) {
                    case CACHED -> {
                        if (settings.getFiltering() instanceof MReachabilityFiltering)
                            yield new MReachCachedZeroDomainPruner(settings, data.getPatternGraph(), data.getTargetGraph(), occupation, vertexMatching, 4);
                        yield new CachedZeroDomainPruner(data, settings, occupation, vertexMatching);
                    }
                    case SERIAL -> new SerialZeroDomainPruner(settings, data.getPatternGraph(), data.getTargetGraph(), occupation, vertexMatching);
                    default -> throw new UnsupportedOperationException();
                };
            case ALLDIFFERENT -> {
                if (settings.getWhenToApply() == WhenToApply.SERIAL) {
                    throw new IllegalArgumentException("AllDifferent cannot be run serially without caching. Choose CACHED execution or PARALLEL. Note that PARALLEL uses quadratic space.");
                } else {
                    if (settings.getFiltering() instanceof NReachabilityFiltering || settings.getFiltering() instanceof MReachabilityFiltering || settings.getFiltering() instanceof UnmatchedDegreesFiltering) {
                        yield new MReachCachedAllDifferentPruner(settings, data.getPatternGraph(), data.getTargetGraph(), occupation, vertexMatching, settings.getFiltering() instanceof  NReachabilityFiltering ? ((NReachabilityFiltering)settings.getFiltering()).getLevel() : 0);
                    }
                    yield new AllDifferentPruner(data, settings, occupation, vertexMatching);
                }
            }
        };
    }


}
