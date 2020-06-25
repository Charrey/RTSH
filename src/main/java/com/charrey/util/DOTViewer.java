package com.charrey.util;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Class used to view DOT strings in a web browser
 */
public class DOTViewer {


    /**
     * Opens a visualization of two dot strings in a web browser.
     *
     * @param dotPattern a DOT string of a graph (or the empty string)
     * @param dotTarget  a DOT string of another graph (or the empty string)
     * @throws IOException thrown when the file system could not be used for some reason.
     */
    public static void openInBrowser(String dotPattern, String dotTarget) throws IOException {
        System.out.println(dotPattern);
        System.out.println(dotTarget);
        String html = new String(Files.readAllBytes(Paths.get("html/template/ExampleViewer.html").toRealPath()));
        html = html.replace("<patternGraphHere/>", "//pattern\n" + dotPattern);
        html = html.replace("<targetGraphHere/>", "//target\n" + dotTarget);
        try (FileWriter writer = new FileWriter(new File("html/temp/ExampleViewer.html"))) {
            writer.write(html);
        }
        Desktop.getDesktop().browse(Paths.get("html/temp/ExampleViewer.html").toUri());
    }

}
