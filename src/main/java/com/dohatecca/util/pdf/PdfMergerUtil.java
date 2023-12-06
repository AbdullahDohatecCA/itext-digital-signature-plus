package com.dohatecca.util.pdf;

import com.dohatecca.util.Config;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.collection.PdfCollection;
import com.itextpdf.kernel.pdf.collection.PdfCollectionSchema;
import com.itextpdf.kernel.pdf.filespec.PdfFileSpec;
import com.itextpdf.kernel.utils.PdfMerger;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.dohatecca.util.Message.showErrorMessage;

public class PdfMergerUtil {
    private static final String mergedPdfFolderPath = Config.getMergedPdfFolderPath();
    public String mergePdf(ArrayList<String> toBeMergedPdfPathsLsit){
        try{
            String mergedPdfFilePath = mergedPdfFolderPath+"/"+getMergedPdfFileName();
            PdfDocument mergedPdf = new PdfDocument(new PdfWriter(mergedPdfFilePath));
            PdfMerger pdfMerger = new PdfMerger(mergedPdf);

            for(String path: toBeMergedPdfPathsLsit){
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
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
        }
    }

    private String getMergedPdfFileName(){
        String[] id = UUID.randomUUID().toString().split("-");
        String[] today = new Date().toString().split(" ");
        String mergedPdfFileName = String.format("merged%s%s.pdf",today[1],id[0]);
        return mergedPdfFileName;
    }
}
