package system;

import com.charrey.Homeomorphism;
import com.charrey.IsoFinder;
import com.charrey.graph.generation.GraphGeneration;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.BeforeAll;

import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public abstract class SystemTest {

    @BeforeAll
    public static void init() {
        Logger.getLogger("IsoFinder").addHandler(new LogHandler());
    }

    protected void testSucceed(Pair<GraphGeneration, GraphGeneration> pair) {
        Optional<Homeomorphism> morph = IsoFinder.getHomeomorphism(pair.getFirst(), pair.getSecond());
        if (!morph.isPresent()) {
            System.out.println(pair.getFirst());
            System.out.println(pair.getSecond());
        }
        assert morph.isPresent();
        System.out.println(morph);
    }

    private static class LogHandler extends Handler {
        @Override
        public void publish(LogRecord record) {
            System.out.println(record.getMessage());
        }

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }
    }

}
