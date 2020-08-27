package system;

import com.charrey.IsoFinder;
import com.charrey.graph.MyGraph;
import com.charrey.graph.generation.TestCase;
import com.charrey.result.HomeomorphismResult;
import com.charrey.result.TimeoutResult;
import com.charrey.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.alg.util.Pair;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.charrey.util.DOTViewer.openInBrowser;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class SystemTest {

    private final Object challengeLock = new Object();

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
    HomeomorphismResult testWithoutExpectation(@NotNull TestCase testCase, long timeout, @NotNull Settings settings) {
        return new IsoFinder().getHomeomorphism(testCase, settings, timeout, "SYSTEMTEST");
    }

    private void writeChallenge(Pair<MyGraph, MyGraph> pair) throws IOException {
        synchronized (challengeLock) {
            File file = new File("challenges/challenge-" + new SimpleDateFormat("yyyyMMddHHmm'.txt'").format(new Date()) + ".txt");
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(pair);
        }
    }


    @NotNull
    @SuppressWarnings("unchecked")
    protected List<Challenge> readChallenges() {
        synchronized (challengeLock) {
            List<Challenge> res = new ArrayList<>();
            File folder = new File("challenges");
            File[] listOfFiles = folder.listFiles();
            assert listOfFiles != null;
            for (File listOfFile : listOfFiles) {
                try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(listOfFile))) {
                    Pair<MyGraph, MyGraph> pair = (Pair<MyGraph, MyGraph>) oos.readObject();
                    res.add(new Challenge(listOfFile, pair.getFirst(), pair.getSecond()));
                } catch (@NotNull IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            res.sort((o1, o2) -> {
                int targetSizeCompare = Integer.compare(o1.targetGraph.vertexSet().size(), o2.targetGraph.vertexSet().size());
                if (targetSizeCompare == 0) {
                    return Integer.compare(o1.sourceGraph.vertexSet().size(), o2.sourceGraph.vertexSet().size());
                } else {
                    return targetSizeCompare;
                }
            });
            return res;
        }

    }




    protected static class Challenge {
        final MyGraph sourceGraph;
        final MyGraph targetGraph;
        private final File file;

        Challenge(File file, MyGraph sourceGraph, MyGraph targetGraph) {
            this.file = file;
            this.targetGraph = targetGraph;
            this.sourceGraph = sourceGraph;
        }

        public File getFile() {
            return file;
        }
    }
}
