package system;

import com.charrey.Homeomorphism;
import com.charrey.graph.generation.RandomTestCaseGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTimeout;

public class IterationsTest extends SystemTest {

//    public static void main(String[] args) throws IOException {
//        for (int i = 0; i < 300; i++) {
//            List<Integer> patternNodesDomain = new java.util.ArrayList<>(List.of(5, 6, 7, 8, 9, 10, 11, 12));
//            Collections.shuffle(patternNodesDomain);
//            int patternNodes = patternNodesDomain.get(0);
//            List<Integer> patternEdgesDomain = new ArrayList<>();
//            for (int j = 0; j < (patternNodes * (patternNodes - 1))/2; j++) {
//                patternEdgesDomain.add(j);
//            }
//            Collections.shuffle(patternEdgesDomain);
//            RandomTestCaseGenerator graphGen = new RandomTestCaseGenerator(patternNodes, patternEdgesDomain.get(0), 0.1, 2);
//            graphGen.init(1, false);
//            RandomTestCaseGenerator.TestCase testCase = graphGen.getNext();
//            File file = new File("performanceTests/5-12/" + i +".txt");
//            file.createNewFile();
//            FileOutputStream fos = new FileOutputStream(file);
//            ObjectOutputStream oos = new ObjectOutputStream(fos);
//            oos.writeObject(testCase);
//        }
//    }


    private final long iterations = 187292466; // 29-04-2020 15:19
    private final long bestTime = 70000; // 29-04-2020 15:39
    @Test
    public void testSmall() {
        List<RandomTestCaseGenerator.TestCase> res = new ArrayList<>();
        File folder = new File("performanceTests/5-12");
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        Arrays.sort(listOfFiles, Comparator.comparingInt(o -> Integer.parseInt(o.getName().split("\\.")[0])));
        Map<RandomTestCaseGenerator.TestCase, String> filenames = new HashMap<>();
        for (File listOfFile : listOfFiles) {
            try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(listOfFile))) {
                RandomTestCaseGenerator.TestCase tc = (RandomTestCaseGenerator.TestCase) oos.readObject();
                filenames.put(tc, listOfFile.getName());
                res.add(tc);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        long start = System.currentTimeMillis();
        its = 0;
        assertTimeout(Duration.ofMillis((long) (bestTime * 1.1)), () -> {
            for(RandomTestCaseGenerator.TestCase tc : res) {
                System.out.println(filenames.get(tc));
                Homeomorphism result = testSucceed(tc, false);
                addIterations(result.getIterations());
            }
        });

        Assertions.assertEquals(iterations, its);
        Assertions.assertEquals(bestTime, System.currentTimeMillis() - start);
    }

    long its;
    private void addIterations(long toAdd) {
        its += toAdd;
    }


}
