package com.charrey;

import com.charrey.settings.Settings;

import java.util.List;



public class Configuration {
    private final String prefix;
    private final String suffix;
    private Settings first;
    private final String name;
    private Settings second;


    public Configuration(String mark, String color, String name, Settings first) {
        this(mark, color, name, first, null);
    }


    public Configuration(String mark, String color, String name, Settings first, Settings second) {
        this.prefix = "\\addplot[\n" +
                "        mark=" + mark + ",\n" +
                "        " + color + ",\n" +
                "    ] plot coordinates {\n";
        this.first = first;
        this.second = second;
        this.suffix = "};\n    \\addlegendentry{" + name + "}\n\n";
        this.name = name;
    }

    public Settings getFirst() {
        return first;
    }

    public Settings getSecond() {
        return second;
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

    public void setSecond(Settings second) {
        this.second = second;
    }

    public void setFirst(Settings first) {
        this.first = first;
    }
}



