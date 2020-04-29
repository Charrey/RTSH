package system;

import com.charrey.Homeomorphism;
import com.charrey.IsoFinder;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.RandomTestCaseGenerator;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.BeforeAll;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.charrey.util.DOTViewer.openInBrowser;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SystemTest {

    private final Object challengeLock = new Object();

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

    protected Homeomorphism testSucceed(RandomTestCaseGenerator.TestCase testCase, boolean writeChallenge) throws IOException {
        Optional<Homeomorphism> morph = IsoFinder.getHomeomorphism(testCase);
        if (morph.isEmpty()) {
            openInBrowser(testCase.source.toString(), testCase.target.toString());
            if (writeChallenge) {
                writeChallenge(new Pair<>(testCase.source, testCase.target));
            }
            fail();
        }
        return morph.get();
    }

    private void writeChallenge(Pair<GraphGeneration, GraphGeneration> pair) throws IOException {
        synchronized (challengeLock) {
            File file = new File("challenges/challenge-" + new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date()) + ".txt");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(pair);
        }
    }

//    protected Pair<GraphGeneration, GraphGeneration> readChallenge() throws IOException, ClassNotFoundException {
//        try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(new File("challenge.txt")))) {
//            //noinspection unchecked
//            return (Pair<GraphGeneration, GraphGeneration>) oos.readObject();
//        } catch (FileNotFoundException e) {
//            return null;
//        }
//    }

    @SuppressWarnings("unchecked")
    protected List<Challenge> readChallenges() {
        synchronized (challengeLock) {
            List<Challenge> res = new ArrayList<>();
            File folder = new File("challenges");
            File[] listOfFiles = folder.listFiles();
            for (File listOfFile : listOfFiles) {
                try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(listOfFile))) {
                    Pair<GraphGeneration, GraphGeneration> pair = (Pair<GraphGeneration, GraphGeneration>) oos.readObject();
                    res.add(new Challenge(listOfFile, pair.getFirst(), pair.getSecond()));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            res.sort((o1, o2) -> {
                int targetSizeCompare = Integer.compare(o1.target.getGraph().vertexSet().size(), o2.target.getGraph().vertexSet().size());
                if (targetSizeCompare == 0) {
                    return Integer.compare(o1.source.getGraph().vertexSet().size(), o2.source.getGraph().vertexSet().size());
                } else {
                    return targetSizeCompare;
                }
            });
            return res;
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

    protected class Challenge {
        public final GraphGeneration source;
        public final GraphGeneration target;
        private final File file;

        public Challenge(File file, GraphGeneration source, GraphGeneration target) {
            this.file = file;
            this.target = target;
            this.source = source;
        }

        public void delete() {
            file.delete();
        }
    }
}
