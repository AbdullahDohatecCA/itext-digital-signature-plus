package com.dohatecca.util.pdf;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static com.dohatecca.util.Config.*;

public class PdfConverter {
    public String convertImageToPdf(String imageFilePath){
        try {
            String[] splitPath = imageFilePath.split("\\\\");
            System.out.println(Arrays.toString(splitPath));
            String i2pPath = getConvertedI2PFolderPath()+"/"+ getImage2PdfFileName(splitPath[splitPath.length-1]);
            PdfWriter pdfWriter = new PdfWriter(i2pPath);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            ImageData imgData = ImageDataFactory.create(imageFilePath);
            Image image = new Image(imgData);

            document.add(image);
            document.close();

            return i2pPath;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String getImage2PdfFileName(String originalFileName){
        String[] id = UUID.randomUUID().toString().split("-");
        String[] today = new Date().toString().split(" ");
        String image2PdfFileName = String.format("i2p%s%s_%s.pdf",today[1],id[0],originalFileName);
        return image2PdfFileName;
    }
}