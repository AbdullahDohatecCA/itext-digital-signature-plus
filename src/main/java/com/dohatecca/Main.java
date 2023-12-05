package com.dohatecca;

import com.dohatecca.application.HomeScreen;
import com.dohatecca.util.Cleaner;

import javax.swing.*;
import java.io.IOException;

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
        Cleaner cleaner = new Cleaner();
        cleaner.cleanTempPdfFile();
        cleaner.cleanApplicationDirectory(getProgramFilesPath()+"/ConvertedI2P");
        cleaner.cleanApplicationDirectory(getProgramFilesPath()+"/MergedPdf");
        new HomeScreen();
    }
}