package com.dohatecca.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.dohatecca.util.Message.showErrorMessage;

public class Config {
    public static String getResourcesPath(){
        return "src/main/resources";
    }

    public static String getApplicationFilesPath(){
        String systemDrive = System.getenv("SystemDrive");
        if(!Files.isDirectory(Path.of(systemDrive+"/DohatecCA/DST2"))){
            File programDir = new File(systemDrive+"/DohatecCA/DST2");
            programDir.mkdirs();
        }
        return systemDrive+"/DohatecCA/DST2";
    }

    public static String getConvertedI2PFolderPath(){
        if(!Files.exists(Path.of(getApplicationFilesPath()+"/ConvertedI2P"))){
            File convertedI2PDir = new File(getApplicationFilesPath()+"/ConvertedI2P");
            convertedI2PDir.mkdirs();
        }
        return getApplicationFilesPath()+"/ConvertedI2P";
    }

    public static String getMergedPdfFolderPath(){
        if(!Files.exists(Path.of(getApplicationFilesPath()+"/MergedPdf"))){
            File mergedPdfDir = new File(getApplicationFilesPath()+"/MergedPdf");
            mergedPdfDir.mkdirs();
        }
        return getApplicationFilesPath()+"/MergedPdf";
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
        return new Color(0xFF8000);
    }

    public static Color getDangerColor(){
        return new Color(0xD91919);
    }

    public static ImageIcon getDohatecLogo(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/Dohatec.png")
                .getImage()
                .getScaledInstance(512,512,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static ImageIcon getOpenIcon(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/Open.gif")
                .getImage()
                .getScaledInstance(64,64,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static ImageIcon getImageIcon(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/Image.gif")
                .getImage()
                .getScaledInstance(64,64,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static ImageIcon getSignIcon(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/Sign.gif")
                .getImage()
                .getScaledInstance(64,64,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static ImageIcon getSaveIcon(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/Save.gif")
                .getImage()
                .getScaledInstance(64,64,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static ImageIcon getAboutIcon(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/About.gif")
                .getImage()
                .getScaledInstance(64,64,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static ImageIcon getDefaultSignatureImage(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/DefaultSignature.png")
                .getImage()
                .getScaledInstance(200,100,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static ImageIcon getLoadingIcon(){
        Image iconImage = new ImageIcon(getResourcesPath()+"/images/Loading.gif")
                .getImage()
                .getScaledInstance(64,64,Image.SCALE_DEFAULT);
        return new ImageIcon(iconImage);
    }

    public static String getWelcomePdfPath(){
        return getResourcesPath()+"/docs/Welcome.pdf";
    }
}
