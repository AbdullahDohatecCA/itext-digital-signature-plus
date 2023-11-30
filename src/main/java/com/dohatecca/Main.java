package com.dohatecca;

import com.dohatecca.application.HomeScreen;
import com.dohatecca.application.PdfSelectionScreen;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.dohatecca.util.Config.getProgramFilesPath;

public class Main {
    public static void main(String[] args) throws
            UnsupportedLookAndFeelException,
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException, IOException {
        UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
        );
        Files.deleteIfExists(Path.of(getProgramFilesPath() + "/temp.pdf"));
//        new HomeScreen();
        new PdfSelectionScreen();
    }
}