package system;

import com.charrey.IsoFinder;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.TimeoutResult;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class SystemTest {

    @NotNull
    HomeomorphismResult testSucceed(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) throws IOException {
        HomeomorphismResult morph = new IsoFinder().getHomeomorphism(testCase, settings, timeout, "SYSTEMTEST");
        if (!(morph instanceof TimeoutResult) && !morph.succeed) {
            //openInBrowser(testCase.getSourceGraph().toString(), testCase.getTargetGraph().toString());
            fail();
        }
        return morph;
    }

    @NotNull
    protected static HomeomorphismResult testWithoutExpectation(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) {
        return new IsoFinder().getHomeomorphism(testCase, settings, timeout, "SYSTEMTEST");
    }


}
