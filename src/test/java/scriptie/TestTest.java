package scriptie;

import com.charrey.Configuration;
import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.util.LinkedList;
import java.util.List;

import static scriptie.Util.comparitiveTest;

public class TestTest extends SystemTest  {

    @Test
    public void testTest() throws InterruptedException { //cr
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
                new SettingsBuilder().withControlPointRouting().withAllDifferentPruning().withCachedPruning().withNeighbourReachabilityFiltering().get(),
                null));
//        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
//                new SettingsBuilder().withCachedDFSRouting().withAllDifferentPruning().withCachedPruning().withNeighbourReachabilityFiltering().get(),
//                null));
//        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
//                new SettingsBuilder().withInplaceDFSRouting().withAllDifferentPruning().withCachedPruning().withNeighbourReachabilityFiltering().get(),
//                null));
//        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
//                new SettingsBuilder().withKPathRouting().withAllDifferentPruning().withCachedPruning().withNeighbourReachabilityFiltering().get(),
//                null));
//        configurations.add(new Configuration("x",        "red"   , "DFS"      ,
//                new SettingsBuilder().withCachedGreedyDFSRouting().withAllDifferentPruning().withCachedPruning().withNeighbourReachabilityFiltering().get(),
//                null));
        comparitiveTest(configurations, 2.429, 1.5, 3.425, false,  Util::getRandomSuccessDirectedTestCase, 10*60*1000L, false, false);
    }


}
