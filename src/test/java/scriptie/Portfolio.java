package scriptie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {


    private volatile List<Map<Integer, Double>> results = new ArrayList<>();


    public synchronized void register(int x, int attempt, double timeTaken) {
        while (results.size() <= x) {
            results.add(new HashMap<>());
        }

        double newValue = results.get(x).containsKey(attempt) ? Math.min(timeTaken / 1_000_000_000d, results.get(x).get(attempt)) : timeTaken / 1_000_000_000d;
        results.get(x).put(attempt, newValue);
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("\\addplot[\n" +
                "        mark=square,\n" +
                "        orange,\n" +
                "    ] plot coordinates {\n");
        int initialIndex = 0;
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).isEmpty()) {
                initialIndex = i;
                break;
            }
        }
        List<Map<Integer, Double>> listView = results.subList(initialIndex, results.size());
        for (int i = 0; i < listView.size(); i++) {
            double toAdd = listView.get(i).values().stream().mapToDouble(y -> y).average().orElse(-1);
            res.append("        (").append(i + initialIndex).append(",").append(toAdd).append(")\n");
        }
        res.append("};\n    \\addlegendentry{portfolio}\n\n");
        return res.toString();
    }
}
