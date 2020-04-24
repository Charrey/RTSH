package system;

import com.charrey.Homeomorphism;
import com.charrey.IsoFinder;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.RandomTestCaseGenerator;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.BeforeAll;

import java.io.*;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.charrey.util.DOTViewer.openInBrowser;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SystemTest {

    protected void printPercentage(int iterationsDone, int totalIterations) {
        System.out.println((100 * iterationsDone / (double) totalIterations) + "% done");
    }

    @BeforeAll
    public static void init() {
        Logger.getLogger("IsoFinder").addHandler(new LogHandler());
    }


    protected void printTime(long actualTime) {
        long hours = actualTime / 3600000;
        long minutes = (actualTime % 3600000) / 60000;
        double seconds = (actualTime % 60000) / 1000d;
        String positive = String.format(
                "%d:%02d:%09f", hours, minutes, seconds);
        System.out.println(positive);
    }

    protected void testSucceed(RandomTestCaseGenerator.TestCase testCase) throws IOException {
        Optional<Homeomorphism> morph = IsoFinder.getHomeomorphism(testCase);
        if (morph.isEmpty()) {
            openInBrowser(testCase.source.toString(), testCase.target.toString());
            writeChallenge(new Pair<>(testCase.source, testCase.target));
            fail();
        }
    }


    private void writeChallenge(Pair<GraphGeneration, GraphGeneration> pair) throws IOException {
        File file = new File("challenge.txt");
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(pair);
    }

    protected Pair<GraphGeneration, GraphGeneration> readChallenge() throws IOException, ClassNotFoundException {
        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File("challenge.txt")))) {
            //noinspection unchecked
            return (Pair<GraphGeneration, GraphGeneration>) oos.readObject();
        } catch (FileNotFoundException e) {
            return null;
        }
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
