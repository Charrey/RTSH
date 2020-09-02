package scriptie;

import com.charrey.graph.generation.TestCase;
import com.charrey.graph.generation.TestCaseGenerator;
import com.charrey.graph.generation.succeed.RandomSucceedDirectedTestCaseGenerator2;
import com.charrey.result.HomeomorphismResult;
import com.charrey.settings.Settings;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.Test;
import system.SystemTest;

public class TestTest extends SystemTest  {

    @Test
    public void test() {
        TestCaseGenerator gen = new RandomSucceedDirectedTestCaseGenerator2(4, 12, 6, 24, -792178600897337516L);
        gen.init(1);
        TestCase tc = gen.getNext();
        Settings settings = new SettingsBuilder().withKPathRouting().withCachedPruning().withAllDifferentPruning().get();
        HomeomorphismResult result = testWithoutExpectation(tc, 24*60*60*1000, settings);
        System.out.println(result);
    }
}
