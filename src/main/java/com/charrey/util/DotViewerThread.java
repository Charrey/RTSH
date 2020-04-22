//package com.charrey.util;
//
//import com.charrey.matching.EdgeMatching;
//import com.charrey.matching.VertexMatching;
//import org.apache.batik.swing.svg.JSVGComponent;
//import org.w3c.dom.Document;
//
//import javax.swing.*;
//import java.awt.*;
//
//import static com.charrey.util.DOTViewer.getState;
//import static org.apache.batik.swing.svg.JSVGComponent.ALWAYS_STATIC;
//
//public enum  DotViewerThread implements Runnable {
//    instance;
//
//    public static volatile VertexMatching vertexMatching;
//    public static volatile EdgeMatching edgeMatching;
//    public static volatile String targetSVG;
//    public static volatile String sourceSVG;
//
//    @Override
//    public void run() {
//        synchronized (this) {
//            try {
//                while (true) {
//                    this.wait();
//                    synchronized (this) {
//                        Document doc = getState(vertexMatching, edgeMatching, sourceSVG, targetSVG);
//                        JSVGComponent canvas = getCanvas();
//                        canvas.setDocument(doc);
//                    }
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private static JFrame frame;
//    private static JSVGComponent canvas;
//    private static JSVGComponent getCanvas() {
//        if (frame == null) {
//            frame = new JFrame();
//            canvas = new JSVGComponent();
//            frame.getContentPane().add(canvas);
//            frame.setVisible(true);
//            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//            frame.setSize(new Dimension(1920, 1080));
//        }
//        canvas.setDocumentState(ALWAYS_STATIC);
//        return canvas;
//    }
//}