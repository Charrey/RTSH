package thesis;

import com.charrey.Configuration;
import com.charrey.graph.generation.succeed.ScriptieSucceedDirectedTestCaseGenerator;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.util.LinkedList;
import java.util.List;

import static thesis.Util.comparitiveTest;

public class RefuseLongerPathsPerformance extends SystemTest {

    private static final List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting()              .withContraction().withoutPruning().get(),  new SettingsBuilder().withKPathRouting()              .withContraction().withoutPruning().allowingLongerPaths().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting()         .withContraction().withoutPruning().get(), new SettingsBuilder().withInplaceDFSRouting()         .withContraction().withoutPruning().allowingLongerPaths().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       , new SettingsBuilder().withControlPointRouting()       .withContraction().withoutPruning().get(), new SettingsBuilder().withControlPointRouting()       .withContraction().withoutPruning().allowingLongerPaths().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withContraction().withoutPruning().get(), new SettingsBuilder().withInplaceOldGreedyDFSRouting().withContraction().withoutPruning().allowingLongerPaths().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withContraction().withoutPruning().get(), new SettingsBuilder().withInplaceNewGreedyDFSRouting().withContraction().withoutPruning().allowingLongerPaths().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting()    .withContraction().withoutPruning().get(),new SettingsBuilder().withCachedGreedyDFSRouting()    .withContraction().withoutPruning().allowingLongerPaths().get()));
    }

    @Test
    public void testLittleBigger() throws InterruptedException { //crashes x = 6 case 816 CP
        comparitiveTest(configurations, 0d, 0d, 0d, true, (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 1.5, (int)seed).init(1).getNext(), 30*60*1000L, false, false);
    }

    @Test
    public void testMoreBigger() throws InterruptedException { //crashes x = 6 case 816 CP
        comparitiveTest(configurations, 0d, 0d, 0d, true, (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 3.0, (int)seed).init(1).getNext(), 30*60*1000L, false, false);
    }

    @Test
    public void testMuchBigger() throws InterruptedException { //crashes x = 7 case 445 CP
        comparitiveTest(configurations, 0d, 0d, 0d, true, (vs, es, vt, et, seed, labels) -> new ScriptieSucceedDirectedTestCaseGenerator(vs, 5.0, (int)seed).init(1).getNext(), 30*60*1000L, true, false);
    }


}
