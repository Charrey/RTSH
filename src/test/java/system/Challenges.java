package system;

import com.charrey.graph.generation.RandomTestCaseGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class Challenges extends SystemTest{


    //challenge-202004271640.txt.txt
    //0--7
    //1--4
    //2--6
    //3--5
    //4--1
    //5--0
    //6--3
    //7--2
    @Test
    public void challenges() throws IOException {
        List<SystemTest.Challenge> challenges = readChallenges();
        for (SystemTest.Challenge challenge : challenges) {
            testSucceed(new RandomTestCaseGenerator.TestCase(challenge.source, challenge.target), false);
        }
    }

}
