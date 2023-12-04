package com.dohatecca.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.dohatecca.util.Message.showErrorMessage;

public class Cleaner {
    public void cleanApplicationDirectory(String applicationDirectory){
        try{
            Path applicationDirectoryPath = Path.of(applicationDirectory);
            if(Files.exists(applicationDirectoryPath)){
                Files.walk(applicationDirectoryPath)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception e) {
                                showErrorMessage(e.getMessage(),null);
                            }
                        });
            }
        }
        catch (Exception e){
            showErrorMessage(e.getMessage(),null);
        }
    }

    public void cleanTempPdfFile(){
        Path tempPdfPath = Path.of(Config.getProgramFilesPath()+"/temp.pdf");
        try {
            Files.deleteIfExists(tempPdfPath);
        } catch (Exception e) {
            showErrorMessage(e.getMessage(),null);
        }
    }
}
