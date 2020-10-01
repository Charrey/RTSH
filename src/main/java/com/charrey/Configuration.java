package com.charrey;

import com.charrey.settings.Settings;

import java.util.List;



public class Configuration {
    private final String prefix;
    private final String suffix;
    private final Settings settingsWithContraction;
    private final String name;
    private final Settings settingsWitoutContraction;


    public Configuration(String mark, String color, String name, Settings settingsWithContraction) {
        this(mark, color, name, settingsWithContraction, null);
    }


    public Configuration(String mark, String color, String name, Settings settingsWithContraction, Settings settingsWithoutContraction) {
        this.prefix = "\\addplot[\n" +
                "        mark=" + mark + ",\n" +
                "        " + color + ",\n" +
                "    ] plot coordinates {\n";
        this.settingsWithContraction = settingsWithContraction;
        this.settingsWitoutContraction = settingsWithoutContraction;
        this.suffix = "};\n    \\addlegendentry{" + name + "}\n\n";
        this.name = name;
    }

    public Settings getSettingsWithContraction() {
        return settingsWithContraction;
    }

    public Settings getSettingsWithoutContraction() {
        return settingsWitoutContraction;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getString(List<Integer> x, List<Double> results) {
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < results.size(); i++) {
            sb.append("        (").append(x.get(i)).append(",").append(results.get(i)).append(")\n");
        }
        return sb.append(suffix).toString();
    }
}



