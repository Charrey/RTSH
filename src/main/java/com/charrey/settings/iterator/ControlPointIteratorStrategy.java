package com.charrey.settings.iterator;

import com.charrey.settings.PathIterationConstants;

import java.util.Objects;

public final class ControlPointIteratorStrategy extends IteratorSettings {

    private final int maxControlPoints;

    public ControlPointIteratorStrategy(int maxControlPoints) {
        super(PathIterationConstants.CONTROL_POINT);
        this.maxControlPoints = maxControlPoints;
    }

    @Override
    public int serialized() {
        return maxControlPoints * 100 + PathIterationConstants.CONTROL_POINT.ordinal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ControlPointIteratorStrategy that = (ControlPointIteratorStrategy) o;
        return maxControlPoints == that.maxControlPoints;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxControlPoints);
    }

    @Override
    public String toString() {
        return "Control points ";
    }

    public int getMaxControlpoints() {
        return maxControlPoints;
    }
}
