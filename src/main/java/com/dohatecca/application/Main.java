package com.dohatecca.application;

import com.itextpdf.commons.utils.JsonUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;

public class Main {
    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        Signature signature = new Signature();
        String pdfPath = "src/main/resources/input/pdf_sample.pdf";
        String imagePath = "src/main/resources/input/signature_sample.png";
        KeyStore keyStore = KeyStore.getInstance("PKCS12","BC");
        keyStore.load(
                new FileInputStream("src/main/resources/input/test_keystore.pfx"),
                "12345678".toCharArray()
        );
        Enumeration<String> aliases = keyStore.aliases();
        String keyAlias = "";
        while(aliases.hasMoreElements()) keyAlias = aliases.nextElement();
        String reason = "Example signature";
        int page = 1;
        boolean result = signature.sign(
                pdfPath,
                imagePath,
                keyStore,
                keyAlias,
                reason,
                page
        );

        if (result) {
            System.out.println("Signed successfully.");
        } else {
            System.out.println("Sign process failed.");
        }
    }
}
