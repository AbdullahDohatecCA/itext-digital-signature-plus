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
import java.util.Map;

import static com.dohatecca.MessageUtil.*;

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
    private static String[][] aliasToTable;
    private static String alias;
    private static String reason;
    private static int pageNumber;

    public void sign() {
        try {
            initProvider();
            initKeyStore();

            createKeySelectionWindowHeader();
            setAliasToTable();
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
        keySelectionWindow.setSize(400,450);
        keySelectionWindow.add(headerPanel,BorderLayout.NORTH);
        keySelectionWindow.add(keyListPanel,BorderLayout.CENTER);
        keySelectionWindow.add(footerPanel,BorderLayout.SOUTH);
        keySelectionWindow.setLocationRelativeTo(null);
        keySelectionWindow.setVisible(true);
    }

    private void createKeySelectionWindowHeader(){
        headerPanel = new JPanel();
        headerLabel = new JLabel();
        headerPanel.setSize(new Dimension(200,75));
        headerPanel.setBackground(new Color(0x76A9F8));
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        headerLabel.setText("Select Key");
        headerLabel.setFont(new Font("Nunito",Font.BOLD,14));
        headerLabel.setBackground(null);
        headerLabel.setBorder(null);
        headerPanel.add(headerLabel);
    }

    private void setAliasToTable(){
        try{
            aliases = keyStore.aliases();
            aliasToTable = new String[100][1];
            int i = 0;
            while(aliases.hasMoreElements()){
                aliasToTable[i] = new String[]{aliases.nextElement()};
                i++;
            }
        }
        catch (Exception e){
            showErrorMessage(e.getMessage(), keySelectionWindow);
            throw new RuntimeException(e);
        }
    }

    private void createKeyListTable(){
        keyListTable = new JTable(aliasToTable, new String[]{"Key Names"});
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
        footerPanel.setBackground(new Color(0x76A9F8));
        footerPanel.add(cancelButton);
        footerPanel.add(okButton);
    }

    private void createCancelButton(){
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.setFont(new Font("Nunito",Font.PLAIN,18));
        cancelButton.addActionListener(
                e -> {
                    keySelectionWindow.dispose();
                }
        );
    }

    private void createOkButton(){
        okButton = new JButton();
        okButton.setText("OK");
        okButton.setFocusable(false);
        okButton.setFont(new Font("Nunito",Font.PLAIN,18));
        okButton.addActionListener(
                e -> {
                    int selectedRow = keyListTable.getSelectedRow();
                    if (selectedRow == -1) {
                        showWarningMessage("Please select a keystore first.", keySelectionWindow);
                    }
                    else {
                        setAlias((String) keyListTable.getValueAt(selectedRow, 0));
                        setReason(
                                JOptionPane.showInputDialog(
                                        null,
                                        "What is the reason for this digital signature?",
                                        "Reason",
                                        JOptionPane.QUESTION_MESSAGE
                                )
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
        signProgressDialog.setIconImage(new ImageIcon("src/main/resources/images/Dohatec.png").getImage());
        signProgressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        signProgressDialog.setSize(300,125);
        signProgressDialog.getContentPane().setBackground(new Color(0xB3B3B3));

        signProgressPanel = new JPanel();
        signProgressPanel.setBackground(new Color(0xB3B3B3));

        signProgressLabel = new JLabel();
        signProgressIcon = new ImageIcon("src/main/resources/images/Loader.gif");
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
