package scriptie;

import com.charrey.settings.SettingsBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import system.SystemTest;

import java.util.*;

import static scriptie.Util.*;

public class ContractionPerformance extends SystemTest {

    private static final List<Configuration> configurations = new LinkedList<>();

    @BeforeAll
    public static void init() {
        configurations.add(new Configuration("*",        "blue"  , "K-Path"   , new SettingsBuilder().withKPathRouting()              .withContraction().get(),  new SettingsBuilder().withKPathRouting()              .withoutContraction().get()));
        configurations.add(new Configuration("x",        "red"   , "DFS"      , new SettingsBuilder().withInplaceDFSRouting()         .withContraction().get(), new SettingsBuilder().withInplaceDFSRouting()         .withoutContraction().get()));
        configurations.add(new Configuration("+",        "green" , "CP"       , new SettingsBuilder().withControlPointRouting()       .withContraction().get(), new SettingsBuilder().withControlPointRouting()       .withoutContraction().get()));
        configurations.add(new Configuration("o",        "purple", "GDFS O IP", new SettingsBuilder().withInplaceOldGreedyDFSRouting().withContraction().get(), new SettingsBuilder().withInplaceOldGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("asterisk", "yellow", "GDFS A IP", new SettingsBuilder().withInplaceNewGreedyDFSRouting().withContraction().get(), new SettingsBuilder().withInplaceNewGreedyDFSRouting().withoutContraction().get()));
        configurations.add(new Configuration("star",     "gray"  , "GDFS C"   , new SettingsBuilder().withCachedGreedyDFSRouting()    .withContraction().get(),new SettingsBuilder().withCachedGreedyDFSRouting()    .withoutContraction().get()));
    }

    @Test
    public void testLittleBigger() throws InterruptedException { //crashes x = 6 case 816 CP
        comparitiveTest(configurations, 3.0, 1.5, 4.0, false, Util::getRandomSuccessDirectedTestCase, 10*60*1000L, true, false);
    }

    @Test
    public void testMuchBigger() throws InterruptedException { //crashes x = 7 case 445 CP
        comparitiveTest(configurations, 3.0, 5.0, 4.0, false, Util::getRandomSuccessDirectedTestCase, 10*60*1000L, true, false);
    }


}
