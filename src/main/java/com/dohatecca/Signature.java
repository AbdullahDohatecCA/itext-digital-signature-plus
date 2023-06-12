package com.dohatecca;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class Signature {
    protected String pdfFilePath;
    protected String signatureImagePath;
    protected static PdfReader reader;
    protected static PdfSigner signer;
    protected static KeyStore ks;
    protected static String alias;
    protected static String reason;

    protected static int pageNumber;

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

                            setReason(
                                    JOptionPane.showInputDialog(
                                            null,
                                            "What is the reason for this digital signature?",
                                            "Give reason here.",
                                            JOptionPane.QUESTION_MESSAGE
                                    )
                            );

                            sign(
                                    pdfFilePath,
                                    signatureImagePath,
                                    alias,
                                    reason,
                                    pageNumber
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

    private void setReason(String givenReason) {
        reason = givenReason;
    }

    public void setPageNumber(int pageNumber) {
        Signature.pageNumber = pageNumber;
    }
    
    private void sign(
            String pdfFilePath,
            String signatureImagePath,
            String keyStoreAlias,
            String reason,
            int pageNumber
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
            int numberOfExistingSignatures = getNumberOfExistingSignatures();
            PdfDocument document = new PdfDocument(new PdfReader(pdfFilePath));
            int pdfPageWidth = (int) document.getPage(1).getPageSize().getWidth();
            int numberOfSignaturesPerRow = pdfPageWidth/200;
            int rowCount = numberOfExistingSignatures/numberOfSignaturesPerRow;
            int signaturePositionX = (numberOfExistingSignatures%numberOfSignaturesPerRow)*200;
            int signaturePositionY = rowCount*100;
            Rectangle rectangle = new Rectangle(signaturePositionX,signaturePositionY,200,100);
            ImageData signatureImage = ImageDataFactory.create(signatureImagePath);
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason(reason)
                    .setLocation(getLocationData())
                    .setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION)
                    .setSignatureGraphic(signatureImage)
                    .setReuseAppearance(false)
                    .setPageRect(rectangle)
                    .setPageNumber(pageNumber);
            signer.setFieldName(String.format("Digital Signature %d",numberOfExistingSignatures+1));

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

    private int getNumberOfExistingSignatures() {
        try {
            PdfDocument document = new PdfDocument(new PdfReader(pdfFilePath));
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
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(ex);
        }
    }

    private String getLocationData() {
        try {
            URL infoUrl = new URL("http://ip-api.com/json/");
            HttpURLConnection infoConn = (HttpURLConnection) infoUrl.openConnection();
            infoConn.setRequestMethod("GET");
            infoConn.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader infoReader = new BufferedReader(new InputStreamReader(infoConn.getInputStream()));
            String infoLine;
            StringBuffer infoResponse = new StringBuffer();
            while ((infoLine = infoReader.readLine()) != null) {
                infoResponse.append(infoLine);
            }
            infoReader.close();

            JSONObject infoObject = new JSONObject(infoResponse.toString());
            String city = infoObject.getString("city");
            String country = infoObject.getString("country");
            return String.format("%s,%s",city,country);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            throw new RuntimeException(e);
        }
    }
}
