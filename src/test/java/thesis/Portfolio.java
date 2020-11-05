package thesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {


    private final List<Map<Integer, Double>> results = new ArrayList<>();


    private long lastPrint = 0;
    public synchronized void register(int x, int attempt, double timeTaken, boolean correctNano) {
        while (results.size() <= x) {
            results.add(new HashMap<>());
        }
        if (correctNano) {
            timeTaken = timeTaken / 1_000_000_000d;
        }

        double newValue = results.get(x).containsKey(attempt) ?
                Math.min(timeTaken, results.get(x).get(attempt)) : timeTaken;
        results.get(x).put(attempt, newValue);
        if (System.currentTimeMillis() - lastPrint > 10*60_000) {
            System.out.println(this);
            lastPrint = System.currentTimeMillis();
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("\\addplot[\n" +
                "        mark=square,\n" +
                "        orange,\n" +
                "    ] plot coordinates {\n");
        int initialX = 0;
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).isEmpty()) {
                initialX = i;
                break;
            }
        }
        List<Map<Integer, Double>> listView = results.subList(initialX, results.size());
        for (int i = 0; i < listView.size(); i++) {
            double toAdd = listView.get(i).values().stream().mapToDouble(y -> y).average().orElse(-1);
            res.append("        (").append(i + initialX).append(",").append(toAdd).append(")\n");
        }
        res.append("};\n    \\addlegendentry{portfolio}\n\n");
        return res.toString();
    }
}
