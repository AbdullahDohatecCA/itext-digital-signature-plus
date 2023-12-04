package com.dohatecca.util;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.dohatecca.util.Message.showErrorMessage;
public class Config {
    public static String getResourcesPath(){
        return "resources";
    }

    public static String getProgramFilesPath(){
        String systemDrive = System.getenv("SystemDrive");
        if(!Files.isDirectory(Path.of(systemDrive+"/DohatecCA/DST2"))){
            File programDir = new File(systemDrive+"/DohatecCA/DST2");
            System.out.println("Program Directory Created: "+programDir.mkdirs());
        }
        return systemDrive+"/DohatecCA/DST2";
    }

    public static String getConvertedI2PFolderPath(){
        if(!Files.exists(Path.of(getProgramFilesPath()+"/ConvertedI2P"))){
            File convertedI2PDir = new File(getProgramFilesPath()+"/ConvertedI2P");
            convertedI2PDir.mkdirs();
        }
        return getProgramFilesPath()+"/ConvertedI2P";
    }

    public static String getMergedPdfFolderPath(){
        if(!Files.exists(Path.of(getProgramFilesPath()+"/MergedPdf"))){
            File mergedPdfDir = new File(getProgramFilesPath()+"/MergedPdf");
            mergedPdfDir.mkdirs();
        }
        return getProgramFilesPath()+"/MergedPdf";
    }

    public static Font getRegularFont(){
        try{
            File regularFontFile = new File(getResourcesPath()+"/fonts/OpenSans.ttf");
            Font regularFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    regularFontFile
            );
            return regularFont.deriveFont(Font.PLAIN,16);
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage(), null);
            throw new RuntimeException(e);
        }
    }

    public static Font getBoldFont(){
        try{
            File boldFontFile = new File(getResourcesPath()+"/fonts/OpenSans.ttf");
            Font boldFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    boldFontFile
            );
            return boldFont.deriveFont(Font.BOLD,16);
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage(), null);
            throw new RuntimeException(e);
        }
    }

    public static Font getItalicFont(){
        try{
            File italicFontFile = new File(getResourcesPath()+"/fonts/OpenSans.ttf");
            Font boldFont = Font.createFont(
                    Font.TRUETYPE_FONT,
                    italicFontFile
            );
            return boldFont.deriveFont(Font.ITALIC,16);
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage(), null);
            throw new RuntimeException(e);
        }
    }

    public static Color getPrimaryColor(){
        return new Color(0x0D6BA6);
    }

    public static Color getSecondaryColor(){
        return new Color(0x5697BF);
    }

    public static Color getBackgroundColor(){
        return new Color(0xF2F2F2);
    }

    public static Color getSuccessColor(){
        return new Color(0x0F6011);
    }

    public static Color getWarningColor(){
        return new Color(0xD9BE12);
    }

    public static Color getDangerColor(){
        return new Color(0xD91919);
    }
}
