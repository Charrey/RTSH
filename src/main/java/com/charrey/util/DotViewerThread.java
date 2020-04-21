package com.charrey.util;

import com.charrey.graph.Vertex;
import com.charrey.matching.EdgeMatching;
import com.charrey.matching.VertexMatching;
import org.apache.batik.swing.JSVGCanvas;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Document;

import javax.swing.*;

import static com.charrey.util.DOTViewer.getState;

public enum  DotViewerThread implements Runnable {
    instance;

    public static volatile VertexMatching vertexMatching;
    public static volatile EdgeMatching edgeMatching;
    public static volatile Graph<Vertex, DefaultEdge> patternGraph;
    public static volatile Graph<Vertex, DefaultEdge> targetGraph;

    @Override
    public void run() {
        synchronized (this) {
            try {
                while (true) {
                    this.wait();
                    Document doc = getState(patternGraph, targetGraph, vertexMatching, edgeMatching);
                    JSVGCanvas canvas = getCanvas();
                    canvas.setDocument(doc);
                    //canvas.setURI(documentUri.toString());
                    //frame.pack();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.exit(69);
    }

    private static JFrame frame;
    private static JSVGCanvas canvas;
    private static JSVGCanvas getCanvas() {
        if (frame == null) {
            frame = new JFrame();
            canvas = new JSVGCanvas();
            frame.getContentPane().add(canvas);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        }
        return canvas;
    }
}