package com.dohatecca;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

public class PdfConverter {
    public void convertImageToPdf(String imageFilePath){
        try {
            PdfWriter pdfWriter = new PdfWriter("C:/DohatecCA_DST2/tempI2PFile.pdf");
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            ImageData imgData = ImageDataFactory.create(imageFilePath);
            Image image = new Image(imgData);

            document.add(image);
            document.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
