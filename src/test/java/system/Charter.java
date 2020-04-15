package system;

import com.charrey.util.Util;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.jfree.data.statistics.HistogramType.RELATIVE_FREQUENCY;

public class Charter extends JFrame implements Runnable {

    private final File file;
    private ChartPanel chartPanel;

    public Charter(String file) throws IOException {
        this.file = Paths.get(file).toRealPath().toFile();
    }

    @Override
    public void run() {
        setupGui();
        while (true) {
            try {
                Thread.sleep(1000);
                System.out.println("foo");
                reloadData();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupGui() {
        this.setVisible(true);
        final XYSeries series = new XYSeries("Random Data");
        final JFreeChart chart = ChartFactory.createHistogram("Foo", "X", "Y", new HistogramDataset());
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
        this.pack();
    }

    private void reloadData() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("benchmark.txt").toRealPath());
        final XYSeries series = new XYSeries("series");
        final XYDataset dataset = new XYSeriesCollection(series);
        double[] values = lines.stream().mapToDouble(Double::parseDouble).sorted().toArray();
        int x = 0;
        for (int i = values.length - 1; i >=0; i--) {
            series.add(++x, values[i]);
        }


        JFreeChart chart = ChartFactory.createXYLineChart("Histogram", "X", "Y", dataset);
        ((XYPlot)chart.getPlot()).setDomainAxis(new LogarithmicAxis("foo"));
        ((XYPlot)chart.getPlot()).setRangeAxis(new LogarithmicAxis("bar"));
        chartPanel.setChart(chart);

    }
}
