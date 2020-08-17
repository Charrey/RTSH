package com.charrey.settings.iterator;

import com.charrey.settings.pathiteration.PathIteration;

public final class ControlPointIteratorStrategy extends IteratorSettings {

    private int maxControlPoints;

    public ControlPointIteratorStrategy(int maxControlPoints) {
        super(PathIteration.CONTROL_POINT);
        this.maxControlPoints = maxControlPoints;
    }


    @Override
    public String toString() {
        return "Control points ";
    }

    public int getMaxControlpoints() {
        return maxControlPoints;
    }

    @Override
    public Object clone() {
        ControlPointIteratorStrategy toReturn = (ControlPointIteratorStrategy) super.clone();
        toReturn.maxControlPoints = maxControlPoints;
        return toReturn;
    }
}
