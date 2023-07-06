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

public class Signature extends SwingWorker<Void,Void> {
    private JFrame keystoreSelectionWindow;
    private JPanel topPanel;
    private JPanel keystoreListPanel;
    private JPanel bottomPanel;
    private JPanel loaderPanel;
    private JDialog signProgressDialog;
    private JLabel keystoreWindowHeader;
    private JLabel loaderLabel;
    private JTable keystoreTable;
    private JScrollPane scrollPane;
    private JButton cancelButton;
    private JButton okButton;
    private ImageIcon loaderIcon;
    private String pdfFilePath;
    private String signatureImagePath;
    private static PdfReader reader;
    private static PdfSigner signer;
    private static KeyStore keyStore;
    private static Enumeration<String> aliases;
    private static String[][] keystoreAliases;
    private static String alias;
    private static String reason;
    private static int pageNumber;

    public void initProvider() {
        BouncyCastleProvider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
    }
    
    public void initKeyStore() {
        try {
            keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null,null);
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    public void setSignatureImagePath(String signatureImagePath) {
        this.signatureImagePath = signatureImagePath;
    }

    public void selectKeystoreAndSign() {
        try {
            aliases = keyStore.aliases();

            keystoreSelectionWindow = new JFrame();
            keystoreSelectionWindow.setTitle("Keystores");
            keystoreSelectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            keystoreSelectionWindow.setLayout(new BorderLayout());
            keystoreSelectionWindow.setSize(400,450);

            topPanel = new JPanel();
            topPanel.setSize(new Dimension(200,75));
            topPanel.setBackground(new Color(0x76A9F8));
            topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            keystoreWindowHeader = new JLabel();
            keystoreWindowHeader.setText("Select Keystore");
            keystoreWindowHeader.setFont(new Font("Nunito",Font.BOLD,14));
            keystoreWindowHeader.setBackground(null);
            keystoreWindowHeader.setBorder(null);
            topPanel.add(keystoreWindowHeader);

            keystoreListPanel = new JPanel();
            keystoreListPanel.setBackground(new Color(0xB3B3B3));
            keystoreListPanel.setLayout(new BorderLayout());

            keystoreAliases = new String[100][1];
            int i = 0;
            while(aliases.hasMoreElements()){
                keystoreAliases[i] = new String[]{aliases.nextElement()};
                i++;
            }

            keystoreTable = new JTable(keystoreAliases, new String[]{"Keystore Aliases"});
            keystoreListPanel.add(keystoreTable,BorderLayout.CENTER);
            scrollPane = new JScrollPane(keystoreTable);

            bottomPanel = new JPanel();
            bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            bottomPanel.setBackground(new Color(0x76A9F8));

            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.setFocusable(false);
            cancelButton.setFont(new Font("Nunito",Font.PLAIN,18));
            cancelButton.addActionListener(
                    e -> {
                        keystoreSelectionWindow.dispose();
                    }
            );

            okButton = new JButton();
            okButton.setText("OK");
            okButton.setFocusable(false);
            okButton.setFont(new Font("Nunito",Font.PLAIN,18));
            okButton.addActionListener(
                    e -> {
                        int selectedRow = keystoreTable.getSelectedRow();
                        if (selectedRow == -1) {
                            showWarningMessage("Please select a keystore first.");
                        }
                        else {
                            setAlias((String) keystoreTable.getValueAt(selectedRow, 0));
                            setReason(
                                    JOptionPane.showInputDialog(
                                            null,
                                            "What is the reason for this digital signature?",
                                            "Reason",
                                            JOptionPane.QUESTION_MESSAGE
                                    )
                            );
                            showSignProgressDialog();
                            //performs signature with the sign() method inside doInBackground() method.
                            this.execute();
                            keystoreSelectionWindow.dispose();
                        }
                    }
            );

            bottomPanel.add(cancelButton);
            bottomPanel.add(okButton);
            keystoreSelectionWindow.add(topPanel,BorderLayout.NORTH);
            keystoreSelectionWindow.add(keystoreListPanel,BorderLayout.CENTER);
            keystoreSelectionWindow.add(bottomPanel,BorderLayout.SOUTH);
            keystoreSelectionWindow.add(scrollPane);
            keystoreSelectionWindow.setLocationRelativeTo(null);
            keystoreSelectionWindow.setVisible(true);
        }
        catch (Exception e){
            showErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setAlias(String selectedAlias){
        alias = selectedAlias;
    }

    private void setReason(String givenReason) {
        reason = givenReason;
    }

    public void setPageNumber(int selectedPageNumber) {
        pageNumber = selectedPageNumber;
    }

    private void showSignProgressDialog(){
        signProgressDialog = new JDialog();
        signProgressDialog.setTitle("Signing");
        signProgressDialog.setIconImage(new ImageIcon("src/main/resources/images/Dohatec.png").getImage());
        signProgressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        signProgressDialog.setSize(300,125);
        signProgressDialog.getContentPane().setBackground(new Color(0xB3B3B3));

        loaderPanel = new JPanel();
        loaderPanel.setBackground(new Color(0xB3B3B3));

        loaderLabel = new JLabel();
        loaderIcon = new ImageIcon("src/main/resources/images/Loader.gif");
        loaderLabel.setIcon(
                new ImageIcon(
                        loaderIcon
                                .getImage()
                                .getScaledInstance(64,64,Image.SCALE_DEFAULT)
                )
        );
        loaderLabel.setText("Signing Document. Please wait...");
        loaderLabel.setIconTextGap(5);

        loaderPanel.add(loaderLabel);
        signProgressDialog.getContentPane().add(loaderPanel);
        signProgressDialog.setLocationRelativeTo(null);
        signProgressDialog.setVisible(true);
    }
    
    private void sign(
            String pdfFilePath,
            String signatureImagePath,
            String keyStoreAlias,
            String reason,
            int pageNumber
    ) {
        try {
            PrivateKey pk = (PrivateKey) keyStore.getKey(keyStoreAlias,null);
            Certificate[] chain = keyStore.getCertificateChain(keyStoreAlias);

            IOcspClient ocspClient = new OcspClientBouncyCastle(null);

            ITSAClient tsaClient = null;
            for (Certificate certificate : chain) {
                X509Certificate cert = (X509Certificate) certificate;
                String tsaUrl = CertificateUtil.getTSAURL(cert);
                if (tsaUrl != null) {
                    tsaClient = new TSAClientBouncyCastle(tsaUrl);
                    break;
                }
            }

            List<ICrlClient> crlList =  new ArrayList<ICrlClient>();
            crlList.add(new CrlClientOnline(chain));

            File tempSaveDirectory = new File("C:/DohatecCA_DST2/");
            boolean directoryCreationSuccess = tempSaveDirectory.mkdirs();
            System.out.println("Directory Created: "+directoryCreationSuccess);
            FileOutputStream fos = new FileOutputStream("C:/DohatecCA_DST2/temp.pdf");
            reader = new PdfReader(pdfFilePath);
            signer = new PdfSigner(
                    reader,
                    new FileOutputStream("C:/DohatecCA_DST2/temp.pdf"),
                    new StampingProperties().useAppendMode()
            );

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
            showErrorMessage(ex.getMessage());
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
            showErrorMessage(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private String getLocationData() {
        try {
            URL ipInfoURL = new URL("http://ip-api.com/json/");
            HttpURLConnection ipInfoConnection = (HttpURLConnection) ipInfoURL.openConnection();
            ipInfoConnection.setRequestMethod("GET");
            ipInfoConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader infoReader = new BufferedReader(new InputStreamReader(ipInfoConnection.getInputStream()));
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
            showErrorMessage(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Void doInBackground() {
        sign(
                pdfFilePath,
                signatureImagePath,
                alias,
                reason,
                pageNumber
        );
        return null;
    }

    @Override
    protected void done() {
        signProgressDialog.dispose();
        //show signature operation completion dialog
        JOptionPane.showMessageDialog(
                null,
                "Signature Applied",
                "Digital Signature",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showWarningMessage(String message){
        JOptionPane.showMessageDialog(
                keystoreSelectionWindow,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }
    private void showErrorMessage(String message){
        JOptionPane.showMessageDialog(
                keystoreSelectionWindow,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
