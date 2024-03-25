package com.dohatecca.util.pdf;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class PdfConverter {
    public String convertImageToPdf(String imageFilePath){
        try {
            String[] splitPath = imageFilePath.split("/");
            System.out.println(Arrays.toString(splitPath));
            String i2pPath = "src/main/resources/output/"+ getImage2PdfFileName(splitPath[splitPath.length-1]);
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

    public static void main(String[] args) throws IOException {
        String imagePath = "src/main/resources/input/drawing_sample.jpg";
        PdfConverter converter = new PdfConverter();
        FileOutputStream output = new FileOutputStream(converter.convertImageToPdf(imagePath));
        output.close();
    }
}
