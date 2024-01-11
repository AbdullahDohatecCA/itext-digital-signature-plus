package com.dohatecca.application;

import com.dohatecca.util.image.ImageScaler;
import com.dohatecca.util.pdf.PdfViewer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.dohatecca.util.Config.*;
import static com.dohatecca.util.Message.*;

public class HomeScreen implements ActionListener, MouseListener {
    private OpenScreen openScreen;
    private JFrame homeScreenFrame;
    private JFrame savingFrame;
    private JPanel menuContainer;
    private JPanel menubarPanel;
    private JPanel pdfContentPanel;
    private JPanel imagePreviewPanel;
    private JPanel savingPanel;
    private JLabel previewImageLabel;
    private JLabel savingLabel;
    private JButton open;
    private JButton sign;
    private JButton save;
    private JButton about;
    private JButton selectImage;
    private ImageIcon dohatecLogo;
    private ImageIcon openIcon;
    private ImageIcon imageIcon;
    private ImageIcon signIcon;
    private ImageIcon saveIcon;
    private ImageIcon aboutIcon;
    private ImageIcon signaturePreviewImage;
    private ImageIcon loadingIcon;
    private JFormattedTextField previewImageText;
    private String selectedDocumentFilePath;
    private String signatureImageFilePath;
    private String signedFileSaveLocationPath;
    private File lastSignatureImageLocationFile;
    private static PdfViewer pdfViewer;
    private static ImageScaler imageScaler;
    public HomeScreen() {
        initPdfViewer();
        initIcons();
        initImageScaler();

        createHomeScreenFrame();
        createMenuContainer();
        createMenubarPanel();
        createImagePreviewPanel();
        createSignatureImagePreview();
        createOpenButton();
        createSelectImageButton();
        createSignButton();
        createSaveButton();
        createAboutButton();

        menubarPanel.add(open);
        menubarPanel.add(selectImage);
        menubarPanel.add(sign);
        menubarPanel.add(save);
        menubarPanel.add(about);
        imagePreviewPanel.add(previewImageLabel);
        imagePreviewPanel.add(previewImageText);
        menuContainer.add(menubarPanel,BorderLayout.NORTH);
        menuContainer.add(imagePreviewPanel,BorderLayout.SOUTH);
        homeScreenFrame.add(menuContainer,BorderLayout.NORTH);
        homeScreenFrame.add(pdfContentPanel,BorderLayout.CENTER);
        homeScreenFrame.setLocationRelativeTo(null);
        homeScreenFrame.setVisible(true);
    }

    private void initPdfViewer(){
        pdfViewer = new PdfViewer();
        pdfContentPanel = pdfViewer.getPdfViewerPanel();
        pdfViewer.openPdf(getWelcomePdfPath());
    }

    public PdfViewer getPdfViewer(){
        return pdfViewer;
    }

    private void initIcons(){
        dohatecLogo = getDohatecLogo();
        openIcon = getOpenIcon();
        imageIcon = getImageIcon();
        signIcon = getSignIcon();
        saveIcon = getSaveIcon();
        aboutIcon = getAboutIcon();
        loadingIcon = getLoadingIcon();
    }

    private void initImageScaler(){
        imageScaler = new ImageScaler();
    }

    private void createHomeScreenFrame(){
        homeScreenFrame = new JFrame();
        homeScreenFrame.setTitle("DohatecCA Digital Signature Tool 2");
        homeScreenFrame.setIconImage(dohatecLogo.getImage());
        homeScreenFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        homeScreenFrame.setSize(1200,900);
        homeScreenFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        homeScreenFrame.setLayout(new BorderLayout());
        homeScreenFrame.getContentPane().setBackground(getBackgroundColor());
    }

    private void createMenuContainer(){
        menuContainer = new JPanel();
        menuContainer.setLayout(new BorderLayout());
    }

    private void createMenubarPanel() {
        menubarPanel = new JPanel();
        menubarPanel.setBackground(getPrimaryColor());
        menubarPanel.setLayout(new FlowLayout(FlowLayout.LEADING,25,10));
    }

    private void createImagePreviewPanel() {
        imagePreviewPanel = new JPanel();
        imagePreviewPanel.setBackground(getBackgroundColor());
        imagePreviewPanel.setLayout(new FlowLayout(FlowLayout.LEADING,25,10));
    }

    private void createSignatureImagePreview(){
        try{
            previewImageLabel = new JLabel();
            previewImageLabel.setPreferredSize(new Dimension(200,100));
            previewImageText = new JFormattedTextField();
            previewImageText.setFont(getRegularFont());
            previewImageText.setBackground(null);
            previewImageText.setBorder(null);
            previewImageText.setEditable(false);
            previewImageText.setFocusable(false);

            String lastSignatureImageLocationPath = getApplicationFilesPath()+"/lastSignatureImageLocationPath.txt";
            if(Files.exists(Path.of(lastSignatureImageLocationPath))){
                lastSignatureImageLocationFile = new File(lastSignatureImageLocationPath);
                BufferedReader fileReader = new BufferedReader(new FileReader(lastSignatureImageLocationFile));
                signatureImageFilePath = fileReader.readLine();
                fileReader.close();
                setSignaturePreviewImage(signatureImageFilePath);
                previewImageLabel.setIcon(getSignaturePreviewImage());
                previewImageText.setText("Signature image obtained from last used signature image.");
                previewImageText.setForeground(getPrimaryColor());
            }
            else{
                signatureImageFilePath = getResourcesPath()+"/images/DefaultSignature.jpeg";
                setSignaturePreviewImage(signatureImageFilePath);
                previewImageLabel.setIcon(getSignaturePreviewImage());
                previewImageText.setText("Select your signature image to change default image.");
                previewImageText.setForeground(getWarningColor());
            }
        }
        catch (Exception e){
            showErrorMessage(e.getMessage(),homeScreenFrame);
        }
    }

    private void createOpenButton(){
        open = new JButton();
        open.setBackground(null);
        open.setForeground(getBackgroundColor());
        open.setBorder(null);
        open.setFocusable(false);
        open.setText("Open");
        open.setIcon(openIcon);
        open.setFont(getRegularFont());
        open.addActionListener(event -> {
            if(openScreen != null) openScreen.closeOpenScreen();
            openScreen = new OpenScreen(this);
        });
        open.addMouseListener(this);
    }

    private void createSelectImageButton(){
        selectImage = new JButton();
        selectImage.setBackground(null);
        selectImage.setForeground(getBackgroundColor());
        selectImage.setBorder(null);
        selectImage.setFocusable(false);
        selectImage.setText("Select Image");
        selectImage.setIcon(imageIcon);
        selectImage.setFont(getRegularFont());
        selectImage.addActionListener(event -> {
            JFileChooser imageFileChooser = new JFileChooser();
            FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                    "Image files",
                    "jpg","png","gif","bmp","jfif"
            );
            imageFileChooser.setFileFilter(imageFilter);
            int isSelected = imageFileChooser.showOpenDialog(homeScreenFrame);
            if(isSelected == JFileChooser.APPROVE_OPTION) {
                signatureImageFilePath = imageFileChooser.getSelectedFile().getAbsolutePath();
                try{
                    lastSignatureImageLocationFile = new File(getApplicationFilesPath()+"/lastSignatureImageLocationPath.txt");
                    FileWriter fileWriter = new FileWriter(lastSignatureImageLocationFile);
                    fileWriter.write(signatureImageFilePath);
                    fileWriter.close();
                }
                catch(Exception e){
                    showErrorMessage(e.getMessage(),homeScreenFrame);
                }
                setSignaturePreviewImage(signatureImageFilePath);
                previewImageLabel.setIcon(getSignaturePreviewImage());
                previewImageText.setText("Signature image selected.");
                previewImageText.setForeground(getPrimaryColor());
            }
        });
        selectImage.addMouseListener(this);
    }

    private void createSignButton(){
        sign = new JButton();
        sign.setBorder(null);
        sign.setBackground(null);
        sign.setForeground(getBackgroundColor());
        sign.setFocusable(false);
        sign.setText("Sign");
        sign.setIcon(signIcon);
        sign.setFont(getRegularFont());
        sign.addActionListener(event -> {
            if(selectedDocumentFilePath == null || selectedDocumentFilePath.isEmpty()) {
                showWarningMessage("Please open a PDF document first.",homeScreenFrame);
            }
            else if(signatureImageFilePath == null || signatureImageFilePath.isEmpty()) {
                showWarningMessage("Please select a signature image.",homeScreenFrame);
            }
            else{
                SignScreen signScreen = new SignScreen();
                signScreen.setPdfFilePath(selectedDocumentFilePath);
                signScreen.setSignatureImagePath(signatureImageFilePath);
                signScreen.setPageNumber(pdfViewer.getCurrentPageNumber()+1);
                signScreen.sign(this);
            }
        });
        sign.addMouseListener(this);
    }

    private void createSaveButton(){
        save = new JButton();
        save.setBorder(null);
        save.setBackground(null);
        save.setForeground(getBackgroundColor());
        save.setFocusable(false);
        save.setText("Save");
        save.setIcon(saveIcon);
        save.setFont(getRegularFont());
        save.addActionListener(event -> {
            Path tempSignedPdfPath = Path.of(getApplicationFilesPath()+"/temp.pdf");
            if(selectedDocumentFilePath == null || selectedDocumentFilePath.isEmpty()) {
                showWarningMessage("Please open a PDF document first.",homeScreenFrame);
            }
            else if (signatureImageFilePath == null || signatureImageFilePath.isEmpty()) {
                showWarningMessage("Please select a signature image.",homeScreenFrame);
            }
            else if (!Files.exists(tempSignedPdfPath)) {
                showWarningMessage("Please sign your document before saving.",homeScreenFrame);
            }
            else {
                JFileChooser saveLocationSelector = new JFileChooser();
                FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(
                        "PDF files",
                        "pdf"
                );
                saveLocationSelector.setFileFilter(pdfFilter);
                int isSelected = saveLocationSelector.showSaveDialog(homeScreenFrame);
                if(isSelected == JFileChooser.APPROVE_OPTION){
                    signedFileSaveLocationPath = saveLocationSelector.getSelectedFile().getAbsolutePath();
                    if(!signedFileSaveLocationPath.endsWith(".pdf")){
                        signedFileSaveLocationPath += ".pdf";
                    }
                    try {
                        selectedDocumentFilePath = null;
                        pdfViewer.closePdf();
                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(this::saveSignedFile);
                        pdfViewer.openPdf(getWelcomePdfPath());
                    } catch (Exception e) {
                        showErrorMessage(e.getMessage(),homeScreenFrame);
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        save.addMouseListener(this);
    }

    private void createAboutButton(){
        about = new JButton();
        about.setBackground(null);
        about.setForeground(getBackgroundColor());
        about.setBorder(null);
        about.setFocusable(false);
        about.setText("About");
        about.setIcon(aboutIcon);
        about.setFont(getRegularFont());
        about.addActionListener(
                event -> {
                    showGeneralMessage(
                            "About\n" +
                                    "Version 2024.1.1\n" +
                                    "Developed by DohatecCA Team\n" +
                                    "for RAJUK\n" +
                                    "to be used on Electronic Construction Permitting System (ECPS)\n" +
                                    "Icons by Lordicon.com",
                            homeScreenFrame
                    );
                }
        );
        about.addMouseListener(this);
    }

    private void saveSignedFile() {
        try{
            String tempSignedPdfPath = getApplicationFilesPath()+"/temp.pdf";
            showSavingWindow();
            FileInputStream fis = new FileInputStream(tempSignedPdfPath);
            FileOutputStream fos = new FileOutputStream(signedFileSaveLocationPath);
            byte[] buffer = new byte[1024];
            Float initialSizeOfSignedFileMB = (float) fis.available() / 1000000;
            while(fis.read(buffer) != -1){
                float leftToWriteMB = (float)fis.available()/1000000;
                Float writtenSizeOfSignedFileMB = initialSizeOfSignedFileMB - leftToWriteMB;
                savingLabel.setText(String.format("Saved %.3f MB of %.3f MB", writtenSizeOfSignedFileMB, initialSizeOfSignedFileMB));
                fos.write(buffer);
            }
            closeSavingWindow();
            fis.close();
            fos.close();
            Files.deleteIfExists(Path.of(tempSignedPdfPath));
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage(),homeScreenFrame);
            throw new RuntimeException(e);
        }
    }

    private void showSavingWindow() {
        savingFrame = new JFrame();
        savingFrame.setTitle("Saving");
        savingFrame.setIconImage(dohatecLogo.getImage());
        savingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        savingFrame.setSize(300,125);
        savingFrame.setFont(getRegularFont());
        savingFrame.getContentPane().setBackground(getBackgroundColor());

        savingPanel = new JPanel();
        savingPanel.setBackground(getBackgroundColor());

        savingLabel = new JLabel();
        savingLabel.setIcon(loadingIcon);

        savingPanel.add(savingLabel);
        savingFrame.add(savingPanel,BorderLayout.CENTER);

        savingFrame.setVisible(true);
        savingFrame.setLocationRelativeTo(homeScreenFrame);
    }

    private void closeSavingWindow() {
        savingFrame.dispose();
    }

    public void setSelectedDocumentFilePath(String path){
        this.selectedDocumentFilePath = path;
    }

    public String getSelectedDocumentFilePath(){
        return this.selectedDocumentFilePath;
    }

    public void setSignaturePreviewImage(String imagePath){
        signaturePreviewImage = imageScaler.scaleImage(
                new ImageIcon(imagePath),
                200,
                100
        );
    }

    public ImageIcon getSignaturePreviewImage(){
        return signaturePreviewImage;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if(e.getSource()==open){
            open.setBackground(getBackgroundColor());
            open.setForeground(getPrimaryColor());
        }
        else if(e.getSource()==selectImage){
            selectImage.setBackground(getBackgroundColor());
            selectImage.setForeground(getPrimaryColor());
        }
        else if(e.getSource()==sign){
            sign.setBackground(getBackgroundColor());
            sign.setForeground(getPrimaryColor());
        }
        else if(e.getSource()==save){
            save.setBackground(getBackgroundColor());
            save.setForeground(getPrimaryColor());
        }
        else if(e.getSource()==about){
            about.setBackground(getBackgroundColor());
            about.setForeground(getPrimaryColor());
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(e.getSource()==open){
            open.setBackground(null);
            open.setForeground(getBackgroundColor());
        }
        else if(e.getSource()==selectImage){
            selectImage.setBackground(null);
            selectImage.setForeground(getBackgroundColor());
        }
        else if( e.getSource()==sign){
            sign.setBackground(null);
            sign.setForeground(getBackgroundColor());
        }
        else if(e.getSource()==save){
            save.setBackground(null);
            save.setForeground(getBackgroundColor());
        }
        else if(e.getSource()==about){
            about.setBackground(null);
            about.setForeground(getBackgroundColor());
        }
    }
}