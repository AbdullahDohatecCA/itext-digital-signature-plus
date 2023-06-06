package com.dohatecca;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Signature {
    protected String pdfFilePath;
    protected String signatureImagePath;
    protected static PdfReader reader;
    protected static PdfSigner signer;
    protected static KeyStore ks;
    protected static String alias;
    
    public void initProvider() {
        //Initiate Bouncy Castle provider
        BouncyCastleProvider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
    }
    
    public void initKeyStore() {
        try {
            //Load windows keystore
            ks = KeyStore.getInstance("Windows-MY");
            ks.load(null,null);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(e);
        }
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    public void setSignatureImagePath(String signatureImagePath) {
        this.signatureImagePath = signatureImagePath;
    }

    public void showKeyStoreTable() {
        try {
            Enumeration<String> aliases = ks.aliases();

            JFrame keystoreSelectionWindow = new JFrame();
            keystoreSelectionWindow.setTitle("Keystores");
            keystoreSelectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            keystoreSelectionWindow.setLayout(new BorderLayout());
            keystoreSelectionWindow.setSize(400,450);

            JPanel topPanel = new JPanel();
            topPanel.setSize(new Dimension(200,75));
            topPanel.setBackground(new Color(0x76A9F8));
            topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JLabel keystoreWindowHeader = new JLabel();
            keystoreWindowHeader.setText("Select Keystore");
            keystoreWindowHeader.setFont(new Font("Nunito",Font.BOLD,14));
            keystoreWindowHeader.setBackground(null);
            keystoreWindowHeader.setBorder(null);
            topPanel.add(keystoreWindowHeader);

            JPanel keystoreListPanel = new JPanel();
            keystoreListPanel.setBackground(new Color(0xB3B3B3));
            keystoreListPanel.setLayout(new BorderLayout());

            String[][] keystoreAliases = new String[100][1];
            int i = 0;
            while(aliases.hasMoreElements()){
                keystoreAliases[i] = new String[]{aliases.nextElement()};
                i++;
            }

            JTable keystoreTable = new JTable(keystoreAliases, new String[]{"AliasNames"});
            keystoreListPanel.add(keystoreTable,BorderLayout.CENTER);
            JScrollPane scrollPane = new JScrollPane(keystoreTable);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(new Color(0x76A9F8));

            JButton cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.setFocusable(false);
            cancelButton.setFont(new Font("Nunito",Font.PLAIN,18));
            cancelButton.addActionListener(
                    e -> {
                        keystoreSelectionWindow.dispose();
                    }
            );
            bottomPanel.add(cancelButton);

            JButton okButton = new JButton();
            okButton.setText("OK");
            okButton.setFocusable(false);
            okButton.setFont(new Font("Nunito",Font.PLAIN,18));
            okButton.addActionListener(
                    e -> {
                        int selectedRow = keystoreTable.getSelectedRow();
                        if (selectedRow == -1) {
                            JOptionPane.showMessageDialog(
                                    keystoreSelectionWindow,
                                    "Please select a keystore first.",
                                    "Keystore Warning",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        }
                        else {
                            String alias = (String) keystoreTable.getValueAt(selectedRow, 0);
                            System.out.println(alias);
                            setAlias(alias);

                            sign(
                                    pdfFilePath,
                                    signatureImagePath,
                                    alias
                            );

                            //show signature operation completion dialog
                            JOptionPane.showMessageDialog(
                                    null,
                                    "Signature Applied",
                                    "Digital Signature",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            keystoreSelectionWindow.dispose();
                        }
                    }
            );
            bottomPanel.add(okButton);

            keystoreSelectionWindow.add(topPanel,BorderLayout.NORTH);
            keystoreSelectionWindow.add(keystoreListPanel,BorderLayout.CENTER);
            keystoreSelectionWindow.add(bottomPanel,BorderLayout.SOUTH);
            keystoreSelectionWindow.add(scrollPane);
            keystoreSelectionWindow.setLocationRelativeTo(null);
            keystoreSelectionWindow.setVisible(true);
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(e);
        }
    }

    private void setAlias(String selectedAlias){
        alias = selectedAlias;
    }
    
    private void sign(
            String pdfFilePath,
            String signatureImagePath,
            String keyStoreAlias
    ) {
        try {
            //Load private key and certificate from keystore
            PrivateKey pk = (PrivateKey) ks.getKey(keyStoreAlias,null);
            Certificate[] chain = ks.getCertificateChain(keyStoreAlias);

            //Add OCSP client
            IOcspClient ocspClient = new OcspClientBouncyCastle(null);

            //Add time stamp Client
            ITSAClient tsaClient = null;
            for (Certificate certificate : chain) {
                X509Certificate cert = (X509Certificate) certificate;
                String tsaUrl = CertificateUtil.getTSAURL(cert);
                if (tsaUrl != null) {
                    tsaClient = new TSAClientBouncyCastle(tsaUrl);
                    break;
                }
            }

            //Add CRL
            List<ICrlClient> crlList =  new ArrayList<ICrlClient>();
            crlList.add(new CrlClientOnline(chain));

            //Load pdf and signer
            File file = new File("C:/DohatecCA_DST2/");
            boolean directoryCreationSuccess = file.mkdirs();
            if(directoryCreationSuccess){
                System.out.println("Directory created successfully on "+file.getPath());
            }
            else{
                System.out.println("Directory already exists or failed operation.");
            }
            FileOutputStream fos = new FileOutputStream("C:/DohatecCA_DST2/temp.pdf");
            reader = new PdfReader(pdfFilePath);
            signer = new PdfSigner(
                    reader,
                    new FileOutputStream("C:/DohatecCA_DST2/temp.pdf"),
                    new StampingProperties()
            );

            //Create signature appearence
            Rectangle rect = new Rectangle(200,100);
            ImageData signatureImage = ImageDataFactory.create(signatureImagePath);
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Test_Reason")
                    .setLocation("Test_Location")
                    .setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION)
                    .setSignatureGraphic(signatureImage)
                    .setReuseAppearance(false)
                    .setPageRect(rect)
                    .setPageNumber(1);
            signer.setFieldName("test_field");

            IExternalSignature pks = new PrivateKeySignature(
                    pk,
                    DigestAlgorithms.SHA256,
                    "SunMSCAPI"
            );

            IExternalDigest digest = new BouncyCastleDigest();
            //sign document using detached mode
            signer.signDetached(
                    digest,
                    pks,
                    chain,
                    crlList,
                    null,
                    tsaClient,
                    0,
                    PdfSigner.CryptoStandard.CMS
            );

            fos.close();
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(ex);
        }
    }
}
