package com.charrey;

import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import org.jetbrains.annotations.NotNull;

public interface HomeomorphismSolver {

    HomeomorphismResult getHomeomorphism(@NotNull TestCase testcase, long timeout, String name, boolean monitorSpace);

}
