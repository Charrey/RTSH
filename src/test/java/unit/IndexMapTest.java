package unit;

import com.charrey.graph.Vertex;
import com.charrey.util.datastructures.IndexMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexMapTest {

    private HashMap<Vertex, Integer> referenceMap;
    private IndexMap<Integer> indexMap;
    private static List<Vertex> vertices;

    @BeforeAll
    public static void beforeInit() {
        vertices = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            vertices.add(new Vertex(i));
        }
        vertices = Collections.unmodifiableList(vertices);
    }

    @BeforeEach
    public void init() {
        referenceMap = new HashMap<>();
        indexMap = new IndexMap<>(100);
    }


    @Test
    public void testSize() {
        for (int i = 0; i < 100; i++) {
            referenceMap.put(vertices.get(i), i * 2 + 1);
            indexMap.put(vertices.get(i), i * 2 + 1);
            assertEquals(referenceMap.size(), indexMap.size());
        }
        for (int i = 0; i < 100; i++) {
            referenceMap.remove(vertices.get(i));
            indexMap.remove(vertices.get(i));
            assertEquals(referenceMap.size(), indexMap.size());
        }
    }
}
