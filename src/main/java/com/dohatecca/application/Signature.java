package com.dohatecca.application;

import com.dohatecca.util.GeoLocation;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dohatecca.util.Config.*;
import static com.dohatecca.util.Message.showErrorMessage;
import static com.dohatecca.util.Message.showGeneralMessage;

public class Signature {
    //height and width measured in user units which is unit of 1/72 inch
    private static final int SIGNATURE_HEIGHT = 36;
    private static final int SIGNATURE_WIDTH = 72;

    public boolean sign(
            String pdfFilePath,
            String signatureImagePath,
            KeyStore keyStore,
            String keyStoreAlias,
            String reason,
            int pageNumber
    ) throws IOException {
        FileOutputStream fos = new FileOutputStream(getApplicationFilesPath()+"/temp.pdf");
        try {
            int numberOfExistingSignatures = getNumberOfExistingSignatures(pdfFilePath);

            PrivateKey privateKey = getPrivateKey(keyStore,keyStoreAlias);
            Certificate[] certificateChain = getCertificateChain(keyStore,keyStoreAlias);

            IOcspClient ocspClient = getOCSPClient();
            ITSAClient tsaClient = getTimestampAuthorityClient(certificateChain);
            List<ICrlClient> crlClientList = getOfflineCRLClients(certificateChain);


            PdfReader reader = new PdfReader(pdfFilePath);
            PdfSigner signer = new PdfSigner(
                    reader,
                    fos,
                    new StampingProperties().useAppendMode()
            );
            signer.setFieldName(String.format("Digital Signature %d",numberOfExistingSignatures+1));

            Rectangle rectangle = new Rectangle(
                    getSignaturePositionX(pdfFilePath,numberOfExistingSignatures),
                    getSignaturePositionY(pdfFilePath,numberOfExistingSignatures),
                    SIGNATURE_WIDTH,
                    SIGNATURE_HEIGHT
            );

            setSignatureAppearance(
                    signer.getSignatureAppearance(),
                    rectangle,
                    signatureImagePath,
                    reason,
                    pageNumber
            );

            PrivateKeySignature privateKeySignature = (PrivateKeySignature) getPrivateKeySignature(privateKey);
            IExternalDigest digest = getDigest();

            signer.signDetached(
                    digest,
                    privateKeySignature,
                    certificateChain,
                    crlClientList,
                    null,
                    null,
                    0,
                    PdfSigner.CryptoStandard.CMS
            );
            fos.close();
            return true;
        }
        catch (Exception ex) {
            fos.close();
            showErrorMessage(ex.getMessage(), null);
            return false;
        }
    }

    private IExternalDigest getDigest(){
        return new BouncyCastleDigest();
    }

    private IExternalSignature getPrivateKeySignature(PrivateKey privateKey){
        IExternalSignature privateKeySignature = new PrivateKeySignature(
                privateKey,
                DigestAlgorithms.SHA256,
                "SunMSCAPI"
        );
        return privateKeySignature;
    }

    private void setSignatureAppearance(
            PdfSignatureAppearance pdfSignatureAppearance,
            Rectangle rectangle,
            String signatureImagePath,
            String reason,
            int pageNumber
    ) {
        try{
            ImageData signatureImage = ImageDataFactory.create(signatureImagePath);
            pdfSignatureAppearance
                    .setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION)
                    .setReuseAppearance(false)
                    .setPageNumber(pageNumber)
                    .setPageRect(rectangle)
                    .setSignatureCreator("DDST2")
                    .setSignatureGraphic(signatureImage)
                    .setReason(reason)
                    .setLocation(GeoLocation.getLocationFromTimeZone());
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private int getSignaturePositionX(String pdfFilePath,int numberOfExistingSignatures){
        try{
            PdfReader reader = new PdfReader(pdfFilePath);
            PdfDocument document = new PdfDocument(reader);
            int pdfPageWidth = (int) document.getPage(1).getPageSize().getWidth();
            int numberOfSignaturesPerRow = pdfPageWidth/SIGNATURE_WIDTH;
            int signaturePositionX = (numberOfExistingSignatures%numberOfSignaturesPerRow)*SIGNATURE_WIDTH;
            return signaturePositionX;
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private int getSignaturePositionY(String pdfFilePath,int numberOfExistingSignatures){
        try{
            PdfReader reader = new PdfReader(pdfFilePath);
            PdfDocument document = new PdfDocument(reader);
            int pdfPageWidth = (int) document.getPage(1).getPageSize().getWidth();
            int numberOfSignaturesPerRow = pdfPageWidth/SIGNATURE_WIDTH;
            int rowCount = numberOfExistingSignatures/numberOfSignaturesPerRow;
            int signaturePositionY = rowCount*SIGNATURE_HEIGHT;
            return signaturePositionY;
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private int getNumberOfExistingSignatures(String pdfFilePath) {
        try {
            PdfReader reader = new PdfReader(pdfFilePath);
            PdfDocument document = new PdfDocument(reader);
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(document,false);

            if(acroForm == null){
                return 0;
            }
            else {
                Map<String, PdfFormField> fields = acroForm.getAllFormFields();
                int signatureCount = 0;
                for(PdfFormField field: fields.values()) {
                    if(field instanceof PdfSignatureFormField){
                        signatureCount++;
                    }
                }
                return signatureCount;
            }
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            return 0;
        }
    }

    private PrivateKey getPrivateKey(KeyStore keyStore,String keyStoreAlias){
        try {
            return (PrivateKey) keyStore.getKey(keyStoreAlias,null);
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private Certificate[] getCertificateChain(KeyStore keyStore,String keyStoreAlias) {
        try{
            return keyStore.getCertificateChain(keyStoreAlias);
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private IOcspClient getOCSPClient(){
        try{
            return new OcspClientBouncyCastle(null);
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private ITSAClient getTimestampAuthorityClient(Certificate[] certificateChain){
        try{
            ITSAClient tsaClient = null;
            for (Certificate certificate : certificateChain) {
                X509Certificate cert = (X509Certificate) certificate;
                String tsaUrl = CertificateUtil.getTSAURL(cert);
                if (tsaUrl != null) {
                    tsaClient = new TSAClientBouncyCastle(tsaUrl);
                }
            }
            return tsaClient;
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private List<ICrlClient> getOfflineCRLClients(Certificate[] certificateChain){
        try{
            List<ICrlClient> crlClientList =  new ArrayList<ICrlClient>();
            FileInputStream is = new FileInputStream(getResourcesPath()+"/crls/dohatecca_crl.crl");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024*1024];
            while (is.read(buf) != -1) {
                baos.write(buf);
            }
            crlClientList.add(new CrlClientOffline(baos.toByteArray()));
            return crlClientList;
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }

    private List<ICrlClient> getOnlineCRLClients(Certificate[] certificateChain){
        try{
            List<ICrlClient> crlClientList =  new ArrayList<ICrlClient>();
            crlClientList.add(new CrlClientOnline(certificateChain));
            return crlClientList;
        }
        catch (Exception ex) {
            showErrorMessage(ex.getMessage(), null);
            throw new RuntimeException(ex);
        }
    }
}
