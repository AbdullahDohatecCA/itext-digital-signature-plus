package com.dohatecca.application;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.awt.*;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import static com.dohatecca.util.Config.*;
import static com.dohatecca.util.Message.*;

public class SignScreen extends SwingWorker<Void,Void> {
    private JFrame keySelectionWindow;
    private JPanel headerPanel;
    private JLabel headerLabel;
    private JPanel keyListPanel;
    private JPanel footerPanel;
    private JDialog signProgressDialog;
    private JPanel signProgressPanel;
    private JLabel signProgressLabel;
    private JTable keyListTable;
    private JScrollPane keyListScrollPane;
    private JButton cancelButton;
    private JButton okButton;
    private ImageIcon signProgressIcon;
    private String pdfFilePath;
    private String signatureImagePath;
    private static PdfReader reader;
    private static PdfSigner signer;
    private static KeyStore keyStore;
    private static Enumeration<String> aliases;
    private static String[][] certificateInfoTable;
    private static String alias;
    private static String reason;
    private static int pageNumber;

    public void sign() {
        try {
            initProvider();
            initKeyStore();

            createKeySelectionWindowHeader();
            createCertificateInfoTableFromAliases();
            createKeyListTable();
            createKeyListPanel();
            createCancelButton();
            createOkButton();
            createKeySelectionWindowFooter();
            createKeySelectionWindow();
        }
        catch (Exception e){
            showErrorMessage(e.getMessage(), keySelectionWindow);
            throw new RuntimeException(e);
        }
    }

    private void initProvider() {
        BouncyCastleProvider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);
    }
    
    private void initKeyStore() {
        try {
            keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null,null);
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage(), keySelectionWindow);
            throw new RuntimeException(e);
        }
    }

    private void createKeySelectionWindow(){
        keySelectionWindow = new JFrame();
        keySelectionWindow.setTitle("Keys");
        keySelectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        keySelectionWindow.setLayout(new BorderLayout());
        keySelectionWindow.setSize(1000,337);
        keySelectionWindow.add(headerPanel,BorderLayout.NORTH);
        keySelectionWindow.add(keyListPanel,BorderLayout.CENTER);
        keySelectionWindow.add(footerPanel,BorderLayout.SOUTH);
        keySelectionWindow.setLocationRelativeTo(null);
        keySelectionWindow.setVisible(true);
    }

    private void createKeySelectionWindowHeader(){
        headerPanel = new JPanel();
        headerLabel = new JLabel();
        headerPanel.setSize(new Dimension(1000,100));
        headerPanel.setBackground(getSecondaryColor());
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerLabel.setText("Select Key");
        headerLabel.setFont(getBoldFont());
        headerLabel.setBackground(null);
        headerLabel.setForeground(getBackgroundColor());
        headerLabel.setBorder(null);
        headerPanel.add(headerLabel);
    }

    private void createCertificateInfoTableFromAliases(){
        try{
            aliases = keyStore.aliases();
            certificateInfoTable = new String[50][4];
            int i = 0;
            while(aliases.hasMoreElements()){
                String currentAlias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(currentAlias);
                String issuer = certificate.getIssuerX500Principal().getName();
                String issuerCN = issuer.substring(issuer.indexOf("CN=")+3);
                issuerCN = issuerCN.substring(0,issuerCN.indexOf(","));
                if(!issuerCN.contains("Dohatec")) continue;
                certificateInfoTable[i][0] = currentAlias;
                certificateInfoTable[i][1] = certificate.getNotBefore().toString();
                certificateInfoTable[i][2] = certificate.getNotAfter().toString();
                certificateInfoTable[i][3] = issuerCN;
                i++;
            }
        }
        catch (Exception e){
            showErrorMessage(e.getMessage(), keySelectionWindow);
            throw new RuntimeException(e);
        }
    }

    private void createKeyListTable(){
        keyListTable = new JTable(certificateInfoTable, new String[]{"Common Name","Issue Date","Expiration Date","Issuer"});
        keyListTable.setFont(getRegularFont());
        keyListTable.setSelectionBackground(getSecondaryColor());
        keyListTable.setSelectionForeground(getBackgroundColor());
        keyListTable.setRowHeight(50);
        keyListScrollPane = new JScrollPane(keyListTable);
    }

    private void createKeyListPanel(){
        keyListPanel = new JPanel();
        keyListPanel.setBackground(new Color(0xB3B3B3));
        keyListPanel.setLayout(new BorderLayout());
        keyListPanel.add(keyListScrollPane,BorderLayout.CENTER);
    }

    private void createKeySelectionWindowFooter(){
        footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(getSecondaryColor());
        footerPanel.add(cancelButton);
        footerPanel.add(okButton);
    }

    private void createCancelButton(){
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setForeground(getDangerColor());
        cancelButton.setFocusable(false);
        cancelButton.setFont(getRegularFont());
        cancelButton.addActionListener(
                e -> {
                    keySelectionWindow.dispose();
                }
        );
    }

    private void createOkButton(){
        okButton = new JButton();
        okButton.setText("OK");
        okButton.setForeground(getSuccessColor());
        okButton.setFocusable(false);
        okButton.setFont(getRegularFont());
        okButton.addActionListener(
                e -> {
                    int selectedRow = keyListTable.getSelectedRow();
                    if (selectedRow == -1) {
                        showWarningMessage("Please select a keystore first.", keySelectionWindow);
                    }
                    else {
                        setAlias((String) keyListTable.getValueAt(selectedRow, 0));
                        setReason(
                                showQuestionMessage("What is the reason for this digital signature?",keySelectionWindow)
                        );
                        createSignProgressDialog();
                        //performs signature with the sign() method inside doInBackground() method.
                        this.execute();
                        keySelectionWindow.dispose();
                    }
                }
        );
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

    public void setPdfFilePath(String pdfFilePath) {
        this.pdfFilePath = pdfFilePath;
    }

    public void setSignatureImagePath(String signatureImagePath) {
        this.signatureImagePath = signatureImagePath;
    }

    private void createSignProgressDialog(){
        signProgressDialog = new JDialog();
        signProgressDialog.setTitle("Signing");
        signProgressDialog.setIconImage(new ImageIcon(getResourcesPath()+"/images/Dohatec.png").getImage());
        signProgressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        signProgressDialog.setSize(300,125);
        signProgressDialog.getContentPane().setBackground(getBackgroundColor());
        signProgressDialog.getContentPane().setForeground(getPrimaryColor());

        signProgressPanel = new JPanel();
        signProgressPanel.setBackground(getBackgroundColor());

        signProgressLabel = new JLabel();
        signProgressIcon = new ImageIcon(getResourcesPath()+"/images/Loader.gif");
        signProgressLabel.setIcon(
                new ImageIcon(
                        signProgressIcon
                                .getImage()
                                .getScaledInstance(64,64,Image.SCALE_DEFAULT)
                )
        );
        signProgressLabel.setText("Signing document. Please wait...");
        signProgressLabel.setIconTextGap(5);

        signProgressPanel.add(signProgressLabel);
        signProgressDialog.getContentPane().add(signProgressPanel);
        signProgressDialog.setLocationRelativeTo(null);
        signProgressDialog.setVisible(true);
    }

    @Override
    protected Void doInBackground() {
        Signature signature = new Signature();
        signature.sign(
                pdfFilePath,
                signatureImagePath,
                keyStore,
                alias,
                reason,
                pageNumber
        );
        return null;
    }

    @Override
    protected void done() {
        signProgressDialog.dispose();
        showGeneralMessage("Signature applied.",null);
    }
}
