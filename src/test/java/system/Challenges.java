package system;

import com.charrey.graph.generation.TestCase;
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
    //challenge-202004292044.txt.txt
    //0--3
    //1--1 (1--3)
    //2--0 (0--3) (0--1)
    //3--2 (2--3) (0--10--2)
    //4--5 (2--5) (3--5)
    //5--6 (0--6) (5--6) (1--11--6)
    //6--7 (3--9--7) (5--7) (6--7)
    //7--8 (0--8) (3--8) (7--8)
    //8--4 (1--4) (2--4) (7--4)
    //challenge-202004301006.txt.txt
    //0--3
    //1--0
    //2--4
    //3--1
    //4--5
    //5--6
    //challenge-202004301351.txt.txt
    //0--4
    //1--3
    //2--0
    //3--2
    //4--1
    //5--5
    @Test
    public void challenges() throws IOException {
        List<SystemTest.Challenge> challenges = readChallenges();
        for (SystemTest.Challenge challenge : challenges) {
            System.out.println(challenge.getFile().getName());
            testSucceed(new TestCase(challenge.sourceGraph, challenge.targetGraph), false, 3600_000);
        }
    }

}
