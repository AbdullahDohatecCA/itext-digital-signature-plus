package com.dohatecca.util.pdf;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.PdfMerger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class PdfMergerUtil {
    public String mergePdf(ArrayList<String> toBeMergedPdfPathsList){
        try{
            String mergedPdfFilePath = "src/main/resources/output/"+getMergedPdfFileName();
            PdfDocument mergedPdf = new PdfDocument(new PdfWriter(mergedPdfFilePath));
            PdfMerger pdfMerger = new PdfMerger(mergedPdf);

            for(String path: toBeMergedPdfPathsList){
                PdfDocument pdf = new PdfDocument(new PdfReader(path));
                removeAllExistingSignatures(pdf);
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

    private void removeAllExistingSignatures(PdfDocument document) {
        try {
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(document,false);

            if(acroForm == null){
                return;
            }
            Map<String, PdfFormField> fields = acroForm.getAllFormFields();
            for(PdfFormField field: fields.values()) {
                if(field instanceof PdfSignatureFormField){
                    acroForm.removeField(String.valueOf(field.getFieldName()));
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getMergedPdfFileName(){
        String[] id = UUID.randomUUID().toString().split("-");
        String[] today = new Date().toString().split(" ");
        String mergedPdfFileName = String.format("merged%s%s.pdf",today[1],id[0]);
        return mergedPdfFileName;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<String> toBeMerged = new ArrayList<>();
        toBeMerged.add("src/main/resources/input/pdf_sample.pdf");
        toBeMerged.add("src/main/resources/input/sample_pdf_2.pdf");
        PdfMergerUtil merger = new PdfMergerUtil();
        FileOutputStream output = new FileOutputStream(merger.mergePdf(toBeMerged));
        output.close();
    }
}
