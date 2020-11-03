package thesis;

import com.charrey.Configuration;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.util.*;
import java.util.function.Consumer;

import static thesis.Util.*;

public class ContractionPerformance extends SystemTest {

    private static final List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       , new SettingsBuilder().withControlPointRouting().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting().get()));

        configurations.forEach(configuration -> {
            configuration.setFirst(new SettingsBuilder(configuration.getFirst()).withoutPruning().get());
            configuration.setFirst(new SettingsBuilder(configuration.getFirst()).withContraction().get());
            configuration.setSecond(new SettingsBuilder(configuration.getFirst()).withoutContraction().get());
        });
    }

    @Test
    public void testLittleBigger() throws InterruptedException {
        comparitiveTest(configurations, 0d, 0d, 0d, true, (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext(), 30*60*1000L, false, false);
    }

    @Test
    public void testMoreBigger() throws InterruptedException {
        comparitiveTest(configurations, 0d, 0d, 0d, true, (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 3.0, (int)seed).init(1).getNext(), 30*60*1000L, false, false);
    }

    @Test
    public void testMuchBigger() throws InterruptedException {
        comparitiveTest(configurations, 0d, 0d, 0d, true, (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 5.0, (int)seed).init(1).getNext(), 30*60*1000L, true, false);
    }


}
