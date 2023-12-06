package com.dohatecca.application;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.dohatecca.util.Config.*;
import static com.dohatecca.util.Message.*;

public class SignScreen {
    private HomeScreen homeScreen;
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
    private ImageIcon dohatecLogo;
    private ImageIcon signProgressIcon;
    private String pdfFilePath;
    private String signatureImagePath;
    private static KeyStore keyStore;
    private static Enumeration<String> aliases;
    private static String[][] certificateInfoArray;
    private static String alias;
    private static String reason;
    private static int pageNumber;

    public void sign(HomeScreen homeScreenRef) {
        try {
            homeScreen = homeScreenRef;

            initProvider();
            initKeyStore();
            initIcons();

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

    private void initIcons(){
        dohatecLogo = getDohatecLogo();
        signProgressIcon = getLoadingIcon();
    }

    private void createKeySelectionWindow(){
        keySelectionWindow = new JFrame();
        keySelectionWindow.setTitle("Sign");
        keySelectionWindow.setIconImage(dohatecLogo.getImage());
        keySelectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        keySelectionWindow.setLayout(new BorderLayout());
        keySelectionWindow.setSize(800,600);
        keySelectionWindow.add(headerPanel,BorderLayout.NORTH);
        keySelectionWindow.add(keyListPanel,BorderLayout.CENTER);
        keySelectionWindow.add(footerPanel,BorderLayout.SOUTH);
        keySelectionWindow.setLocationRelativeTo(null);
        keySelectionWindow.setVisible(true);
    }

    private void createKeySelectionWindowHeader(){
        headerPanel = new JPanel();
        headerLabel = new JLabel();
        headerPanel.setBackground(getSecondaryColor());
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT,25,25));
        headerLabel.setText("Select Digital Certificate");
        headerLabel.setFont(getBoldFont());
        headerLabel.setBackground(null);
        headerLabel.setForeground(getBackgroundColor());
        headerLabel.setBorder(null);
        headerPanel.add(headerLabel);
    }

    private void createCertificateInfoTableFromAliases(){
        try{
            aliases = keyStore.aliases();
            certificateInfoArray = new String[50][4];
            int i = 0;
            while(aliases.hasMoreElements()){
                String currentAlias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(currentAlias);
                String issuer = certificate.getIssuerX500Principal().getName();
                int issuerCNIndex = issuer.indexOf("CN=");
                String issuerCN;
                if(issuerCNIndex != -1) {
                    issuerCN = issuer.substring(issuerCNIndex+3,issuerCNIndex+18);
                }
                else {
                    issuerCN = "Unknown";
                }
                certificateInfoArray[i][0] = currentAlias;
                certificateInfoArray[i][1] = certificate.getNotBefore().toString();
                certificateInfoArray[i][2] = certificate.getNotAfter().toString();
                certificateInfoArray[i][3] = issuerCN;
                ++i;
            }
        }
        catch (Exception e){
            showErrorMessage(e.getMessage(), keySelectionWindow);
            throw new RuntimeException(e);
        }
    }

    private void createKeyListTable(){
        keyListTable = new JTable(certificateInfoArray, new String[]{"Common Name","Issue Date","Expiration Date","Issuer"});
        keyListTable.setFont(getRegularFont());
        keyListTable.setSelectionBackground(getSecondaryColor());
        keyListTable.setSelectionForeground(getBackgroundColor());
        keyListTable.setRowHeight(50);
        keyListScrollPane = new JScrollPane(keyListTable);
    }

    private void createKeyListPanel(){
        keyListPanel = new JPanel();
        keyListPanel.setBackground(getBackgroundColor());
        keyListPanel.setLayout(new BorderLayout());
        keyListPanel.add(keyListScrollPane,BorderLayout.CENTER);
    }

    private void createKeySelectionWindowFooter(){
        footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(getBackgroundColor());
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
                        try {
                            setAlias((String) keyListTable.getValueAt(selectedRow, 0));
                            setReason(
                                    showQuestionMessage("What is the reason for this digital signature?",keySelectionWindow)
                            );
                            Executor signOperationExecutor = Executors.newSingleThreadExecutor();
                            signOperationExecutor.execute(this::doSignature);
                            keySelectionWindow.dispose();
                        } catch (Exception x) {
                            showErrorMessage(x.getMessage(),null);
                        }
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
        signProgressDialog.setIconImage(dohatecLogo.getImage());
        signProgressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        signProgressDialog.setSize(300,125);
        signProgressDialog.getContentPane().setBackground(getBackgroundColor());
        signProgressDialog.getContentPane().setForeground(getPrimaryColor());

        signProgressPanel = new JPanel();
        signProgressPanel.setBackground(getBackgroundColor());

        signProgressLabel = new JLabel();
        signProgressLabel.setIcon(signProgressIcon);
        signProgressLabel.setText("Signing document. Please wait...");
        signProgressLabel.setIconTextGap(5);

        signProgressPanel.add(signProgressLabel);
        signProgressDialog.getContentPane().add(signProgressPanel);
        signProgressDialog.setLocationRelativeTo(null);
        signProgressDialog.setVisible(true);
    }

    private void closeSignProgressDialog(){
        signProgressDialog.dispose();
    }

    private void doSignature(){
        createSignProgressDialog();
        Signature signature = new Signature();
        boolean isSigned = signature.sign(
                pdfFilePath,
                signatureImagePath,
                keyStore,
                alias,
                reason,
                pageNumber
        );
        closeSignProgressDialog();
        if(isSigned) {
            homeScreen.getPdfViewer().openPdf(
                    getApplicationFilesPath()+"/temp.pdf"
            );
            showGeneralMessage("Signature applied.", null);
        }
    }
}
