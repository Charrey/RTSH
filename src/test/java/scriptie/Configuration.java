package scriptie;

import com.charrey.settings.Settings;

import java.util.List;



public class Configuration {
    private final String prefix;
    private final String suffix;
    private final Settings settings;
    private final String name;

    public Configuration(String mark, String color, String name, Settings settings) {
        this.prefix = "\\addplot[\n" +
                "        smooth,\n" +
                "        mark=" + mark + ",\n" +
                "        " + color + ",\n" +
                "        error bars/.cd, y dir=both, y explicit,\n" +
                "    ] plot coordinates {\n";
        this.settings = settings;
        this.suffix = "};\n    \\addlegendentry{" + name + "}\n\n";
        this.name = name;
    }

    public Settings getSettings() {
        return settings;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getString(List<Integer> x, List<Double> results, List<Double> deviations) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < results.size(); i++) {
            sb.append("        (").append(x.get(i)).append(",").append(results.get(i)).append(") +=(0,").append(deviations.get(i)).append(") -= (0,").append(deviations.get(i)).append(")\n");
        }
        return sb.append(suffix).toString();
    }
}



