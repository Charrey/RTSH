package com.charrey.util;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

}
