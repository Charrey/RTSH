package com.charrey.pruning;

import com.charrey.algorithms.UtilityData;
import com.charrey.matching.VertexMatching;
import com.charrey.occupation.GlobalOccupation;
import com.charrey.pruning.cached.*;
import com.charrey.pruning.parallel.ParallelPruner;
import com.charrey.pruning.serial.SerialAllDifferentPruner;
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
        switch (settings.getPruningMethod()) {
            case NONE:
                return new NoPruner();
            case ZERODOMAIN:
                switch (settings.getWhenToApply()) {
                    case CACHED:
                        if (settings.getFiltering() instanceof MReachabilityFiltering) {
                            return new MReachCachedZeroDomainPruner(settings, data.getPatternGraph(), data.getTargetGraph(), occupation, vertexMatching);
                        } else {
                            return new CachedZeroDomainPruner(data, settings, occupation);
                        }

                    case SERIAL:
                        return new SerialZeroDomainPruner(settings, data.getPatternGraph(), data.getTargetGraph(), occupation, vertexMatching);
                    default:
                        throw new UnsupportedOperationException();
                }
            case ALLDIFFERENT:
                if (settings.getWhenToApply() == WhenToApply.SERIAL) {
                    return new SerialAllDifferentPruner(settings, data.getPatternGraph(), data.getTargetGraph(), occupation, vertexMatching);
                } else {
                    if (settings.getFiltering() instanceof NReachabilityFiltering || settings.getFiltering() instanceof MReachabilityFiltering || settings.getFiltering() instanceof UnmatchedDegreesFiltering) {
                        return new MReachCachedAllDifferentPruner(settings, data.getPatternGraph(), data.getTargetGraph(), occupation, vertexMatching);
                    } else {
                        return new AllDifferentPruner(data, settings, occupation);
                    }
                }
            default:
                throw new UnsupportedOperationException();
        }
    }


}
