package scriptie;

import com.charrey.graph.generation.TestCase;

public interface TestCaseProvider {

    TestCase get(int vs, int es, int vt, int et, long seed, boolean labels);
}
