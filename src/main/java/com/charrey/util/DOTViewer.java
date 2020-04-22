package com.charrey.util;

import com.charrey.graph.Path;
import com.charrey.graph.Vertex;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class DOTViewer {


    public static void openInBrowser(String dotPattern, String dotTarget) throws IOException {
        String html = new String(Files.readAllBytes(Paths.get("html/template/ExampleViewer.html").toRealPath()));
        html = html.replace("<patternGraphHere/>", "//pattern\n" + dotPattern);
        html = html.replace("<targetGraphHere/>", "//target\n" + dotTarget);
        try (FileWriter writer = new FileWriter(new File("html/temp/ExampleViewer.html"))) {
            writer.write(html);
        }
        Desktop.getDesktop().browse(Paths.get("html/temp/ExampleViewer.html").toUri());
    }



    public static Document getState(VertexMatching vertexMatching, EdgeMatching edgeMatching, String patternSVG, String targetSVG) {
        try {
            Document mergedSVG = mergeSVG(patternSVG, targetSVG);
            Map<String, Pair<Element, Location>> locations = getLocations(mergedSVG);
            markVertices(mergedSVG, locations, vertexMatching.getPlacement());
            markPaths(locations, edgeMatching.allPaths());
            return mergedSVG;
            //return makePanel(mergedSVG);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }


    private static void markPaths(Map<String, Pair<Element, Location>> locations, Set<Path> paths) {
        for (Path path : paths) {
            Vertex previous = null;
            for (Vertex v : path.getPath()) {
                if (path.head() == v || path.tail() == v) {
                    Element ellipse = ((Element)locations.get("target_vertex_" + v.intData()).getFirst().getChildNodes().item(2));
                    ellipse.setAttribute("stroke", "blue");
                    ellipse.setAttribute("stroke-width", "5px");
                }
                if (previous == null) {
                    previous = v;
                    continue;
                }
                if (path.head() != v) {
                    Element ellipse = ((Element)locations.get("target_vertex_" + v.intData()).getFirst().getChildNodes().item(2));
                    ellipse.setAttribute("fill", "blue");
                }
                Element DOMedge = locations.containsKey("target_edge_" + v.intData() + "--" + previous.intData()) ?
                        locations.get("target_edge_" + v.intData() + "--" + previous.intData()).getFirst() :
                        locations.get("target_edge_" + previous.intData() + "--" + v.intData()).getFirst();
                Element DOMpath = (Element) DOMedge.getChildNodes().item(2);
                DOMpath.setAttribute("stroke", "blue");
                DOMpath.setAttribute("stroke-width", "5px");
                previous = v;
            }
        }
    }

    private static void markVertices(Document mergedSVG, Map<String, Pair<Element, Location>> locations, List<Vertex> placement) {
        for (int i = 0; i < placement.size(); i++) {
            Pair<Element, Location> patternElement = locations.get("pattern_vertex_" + i);
            ((Element)patternElement.getFirst().getChildNodes().item(2)).setAttribute("stroke", "red");
            Pair<Element, Location> targetElement = locations.get("target_vertex_" + placement.get(i).intData());
            Element ellipse = ((Element)targetElement.getFirst().getChildNodes().item(2));
            ellipse.setAttribute("fill", "red");
            addCurvedLine(mergedSVG, patternElement.getSecond(), targetElement.getSecond(), "red", true);
        }
    }

    private static void addCurvedLine(Document doc, Location first, Location second, String colour, boolean dashed) {
        second = new Location(second.x, second.y);
        Location third = Location.getThird(first, second, 10.);
        Element element = doc.createElementNS("http://www.w3.org/2000/svg", "path");
        element.setAttribute("fill", "none");
        element.setAttribute("stroke", colour);
        element.setAttribute("d", "M" + first.x + " " + first.y + " Q " + third.x + " " + third.y + " " + second.x  + " " + second.y);
        if (dashed) {
            element.setAttribute("stroke-dasharray", "5,5");
        }
        doc.getDocumentElement().appendChild(element);

    }

    private static Map<String, Pair<Element, Location>> getLocations(Document doc) {
        Map<String, Pair<Element, Location>> res = new HashMap<>();
        NodeList itemList = doc.getElementsByTagName("g");
        for (Element item : new ElementListWrapper(itemList)) {
            if (item.getAttribute("class").equals("node")) {
                Location transform = getTranslate((Element) item.getParentNode());
                double x = Double.parseDouble(((Element)item.getChildNodes().item(2)).getAttribute("cx"));
                double y = Double.parseDouble(((Element)item.getChildNodes().item(2)).getAttribute("cy"));
                res.put(item.getAttribute("id"), new Pair<>(item, new Location(x + transform.x, y + transform.y)));
            } else if (item.getAttribute("class").equals("edge")) {
                res.put(item.getAttribute("id"), new Pair<>(item, null));
            }
        }
        return res;
    }



    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static Location getTranslate(Element graph) {
        String transform = graph.getAttribute("transform");
        Pattern pattern = Pattern.compile("translate\\((-?\\d*) (-?\\d*)");
        Matcher matcher = pattern.matcher(transform);
        matcher.find();
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        return new Location(x, y);
    }

    private static Document mergeSVG(String patternSVG, String targetSVG) throws SAXException {
        Document patternTree = parseDOM(patternSVG);
        Document targetTree = parseDOM(targetSVG);
        fixIdentifiers(patternTree, "pattern");
        fixIdentifiers(targetTree, "target");
        removePolygon(patternTree);
        removePolygon(targetTree);
        int patternWidth = getWidth(patternTree);
        int targetWidth = getWidth(targetTree);
        int patternHeight = getHeight(patternTree);
        int targetHeight = getHeight(targetTree);

        Element sourceGraph = (Element) patternTree.getDocumentElement().getChildNodes().item(1);
        Element targetGraph = (Element) targetTree.getDocumentElement().getChildNodes().item(1);
        translateX(targetGraph, patternWidth);
        //translateX(sourceGraph, -targetWidth / 2);

        patternTree.getElementsByTagName("svg").item(0).appendChild(patternTree.importNode(targetGraph, true));
        setViewBox(patternTree, 0, 0., patternWidth + targetWidth, targetHeight);
        setHeight(patternTree, Math.max(patternHeight, targetHeight));
        setWidth(patternTree, patternWidth + targetWidth);
        patternTree.getDocumentElement().setAttribute("xmlns", "http://www.w3.org/2000/svg");

        return patternTree;
        }

    private static void setViewBox(Document patternTree, double startX, double startY, double width, double height) {
        patternTree.getDocumentElement().setAttribute("viewBox", startX + " " + startY + " " + width + " " + height + " ");
    }

    private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder builder;
    private static Document parseDOM(String svg) throws SAXException {
        if (builder == null) {
            try {
                factory.setNamespaceAware(true);
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return builder.parse(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void translateX(Element graph, int pixels) {
        String transform = graph.getAttribute("transform");
        Pattern pattern = Pattern.compile("(translate\\()(\\d*)");
        Matcher matcher = pattern.matcher(transform);
        matcher.find();
        int currentX = Integer.parseInt(matcher.group(2));
        int newX = currentX + pixels;
        graph.setAttribute("transform", matcher.replaceFirst("$1" + newX));
    }

    private static void setHeight(Document doc, int value) {
        ((Element)doc.getElementsByTagName("svg").item(0)).setAttribute("height", value + "px");
    }

    private static void setWidth(Document doc, int value) {
        ((Element)doc.getElementsByTagName("svg").item(0)).setAttribute("width", value + "px");
    }

    private static int getHeight(Document doc) {
        return Integer.parseInt(((Element)doc.getElementsByTagName("svg").item(0)).getAttribute("height").replace("px", ""));
    }

    private static int getWidth(Document doc) {
        return Integer.parseInt(((Element)doc.getElementsByTagName("svg").item(0)).getAttribute("width").replace("px", ""));
    }

    private static void removePolygon(Document doc) {
        Node graph = doc.getChildNodes().item(0).getChildNodes().item(1);
        graph.removeChild(doc.getElementsByTagName("polygon").item(0));
    }

    private static void fixIdentifiers(Document doc, String prefix) {
        for (Element element : new ElementListWrapper(doc.getElementsByTagName("g"))) {
            String type = element.getAttribute("class");
            switch (type) {
                case "graph":
                    element.setAttribute("id", prefix + "_graph");
                    break;
                case "edge":
                    String connection = element.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
                    element.setAttribute("id", prefix + "_edge_" + connection);
                    break;
                case "node":
                    int nodeId = Integer.parseInt(element.getChildNodes().item(4).getChildNodes().item(0).getNodeValue());
                    element.setAttribute("id", prefix + "_vertex_" + nodeId);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    public static String makeFromJGraphT(Graph<Vertex, DefaultEdge> graph) {
        MutableGraph vizGraph = mutGraph("foo").setDirected(false).setStrict(true);
        Map<Vertex, MutableNode> mapping = new IndexMap<>(graph.vertexSet().size());
        for (Vertex vertex : graph.vertexSet()) {
            MutableNode vizVertex = mutNode(String.valueOf(vertex.intData()));
            mapping.put(vertex, vizVertex);
            vizGraph.add(vizVertex);
        }
        for (DefaultEdge edge : graph.edgeSet()) {
            Vertex source = graph.getEdgeSource(edge);
            Vertex target = graph.getEdgeTarget(edge);
            mapping.get(source).addLink(mapping.get(target));
        }
        return Graphviz.fromGraph(vizGraph).render(Format.SVG).toString();
    }

//    private static final boolean shouldPrint = true;
//    private static long lastPrint = 0;
//    private static final long printInterval = 50;
//    public static synchronized void printIfNecessary(RandomTestCaseGenerator.TestCase testCase, VertexMatching vertexMatching, EdgeMatching edgeMatching) {
//        if (shouldPrint && System.currentTimeMillis() - lastPrint > printInterval) {
//            synchronized (DotViewerThread.instance) {
//                DotViewerThread.vertexMatching = vertexMatching;
//                DotViewerThread.edgeMatching = edgeMatching;
//                DotViewerThread.sourceSVG = testCase.sourceSVG;
//                DotViewerThread.targetSVG = testCase.targetSVG;
//                DotViewerThread.instance.notify();
//            }
//            lastPrint = System.currentTimeMillis();
//        }
//    }



    private static class Location {
        public final double x;
        public final double y;

        private Location(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public static Location getThird(Location first, Location second, double s) {
            s = Math.toRadians(s);
            double d = Math.sqrt(Math.pow(first.x + second.x, 2) + Math.pow(first.y + second.y, 2));
            double r0 = 0.5*d / Math.cos(s);
            double h = r0 * Math.sin(s);
            Location P2 = new Location((first.x + second.x) / 2., (first.y + second.y) /2.);
            double x3;
            double y3;
            final double offsetX = h * (second.y - first.y) / d;
            final double offsetY = h * (second.x - first.x) / d;
            if (first.y <= second.y) {
                x3 = P2.x - offsetX;
                y3 = P2.y + offsetY;
            } else {
                x3 = P2.x + offsetX;
                y3 = P2.y - offsetY;
            }
            return new Location(x3, y3);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return x == location.x &&
                    y == location.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return x + " " + y;
        }
    }

    private static class ElementListWrapper implements Iterable<Element> {
        private final NodeList list;

        public ElementListWrapper(NodeList list) {
            this.list = list;
        }

        @Override
        public Iterator<Element> iterator() {
            return new NodeListIterator(list);
        }

    }
    private static class NodeListIterator implements Iterator<Element> {
        private final NodeList list;
        int nextIndex = 0;

        public NodeListIterator(NodeList list) {
            this.list = list;
        }

        @Override
        public boolean hasNext() {
            return list.item(nextIndex) != null;
        }

        @Override
        public Element next() {
            Element toReturn = (Element) list.item(nextIndex);
            nextIndex++;
            return toReturn;
        }
    }
}
