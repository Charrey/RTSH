package system;

import com.charrey.HomeomorphismResult;
import com.charrey.IsoFinder;
import com.charrey.graph.generation.GraphGeneration;
import com.charrey.graph.generation.TestCase;
import org.jgrapht.alg.util.Pair;
import org.junit.jupiter.api.BeforeAll;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static com.charrey.util.DOTViewer.openInBrowser;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SystemTest {

    private final Object challengeLock = new Object();

    @BeforeAll
    public static void init() {
        Logger.getLogger("IsoFinder").addHandler(new LogHandler());
    }

    protected HomeomorphismResult testSucceed(TestCase testCase, boolean writeChallenge) throws IOException {
        HomeomorphismResult morph = IsoFinder.getHomeomorphism(testCase);
        if (morph.failed) {
            openInBrowser(testCase.source.toString(), testCase.target.toString());
            if (writeChallenge) {
                writeChallenge(new Pair<>(testCase.source, testCase.target));
            }
            fail();
        }
        return morph;
    }

    private void writeChallenge(Pair<GraphGeneration, GraphGeneration> pair) throws IOException {
        synchronized (challengeLock) {
            File file = new File("challenges/challenge-" + new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date()) + ".txt");
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(pair);
        }
    }


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

        public File getFile() {
            return file;
        }
    }
}
