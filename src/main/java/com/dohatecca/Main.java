package com.dohatecca;

import com.dohatecca.application.HomeScreen;
import com.dohatecca.util.Cleaner;

import javax.swing.*;
import java.io.IOException;

import static com.dohatecca.util.Config.getApplicationFilesPath;

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
        cleaner.cleanApplicationDirectory(getApplicationFilesPath()+"/ConvertedI2P");
        cleaner.cleanApplicationDirectory(getApplicationFilesPath()+"/MergedPdf");
        new HomeScreen();
    }
}