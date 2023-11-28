package com.dohatecca.util.pdf;

import com.dohatecca.util.Config;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;

import java.util.Date;
import java.util.UUID;

public class PdfMergerUtil {
    private static final String mergedPdfFolderPath = Config.getMergedPdfFolderPath();
    public String mergePdf(String[] toBeMergedPdfPathsArray){
        try{
            String mergedPdfFilePath = mergedPdfFolderPath+"/"+getMergedPdfFileName();
            PdfDocument mergedPdf = new PdfDocument(new PdfWriter(mergedPdfFilePath));
            PdfMerger pdfMerger = new PdfMerger(mergedPdf);

            for(int i=0;i<toBeMergedPdfPathsArray.length;i++){
                PdfDocument pdf = new PdfDocument(new PdfReader(toBeMergedPdfPathsArray[i]));
                pdfMerger.merge(pdf,1,pdf.getNumberOfPages());
                pdf.close();
            }

            mergedPdf.close();
            return mergedPdfFilePath;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String getMergedPdfFileName(){
        String[] id = UUID.randomUUID().toString().split("-");
        String[] today = new Date().toString().split(" ");
        String mergedPdfFileName = String.format("merged%s%s.pdf",today[1],id[0]);
        return mergedPdfFileName;
    }
}
