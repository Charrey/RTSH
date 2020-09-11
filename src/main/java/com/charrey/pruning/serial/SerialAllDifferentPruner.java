package com.charrey.pruning.serial;

import com.charrey.graph.MyGraph;
import com.charrey.matching.PartialMatchingProvider;
import com.charrey.occupation.ReadOnlyOccupation;
import com.charrey.pruning.DomainCheckerException;
import com.charrey.pruning.Pruner;
import com.charrey.settings.Settings;

public class SerialAllDifferentPruner extends DefaultSerialPruner {
    protected SerialAllDifferentPruner(Settings settings, MyGraph sourceGraph, MyGraph targetGraph, ReadOnlyOccupation occupation) {
        super(settings, sourceGraph, targetGraph, occupation);
    }

    @Override
    public Pruner copy() {
        return new SerialAllDifferentPruner(settings, sourceGraph, targetGraph, occupation);
    }

    @Override
    public void close() {

    }

    @Override
    public void checkPartial(PartialMatchingProvider partialMatching) throws DomainCheckerException {

    }
}
