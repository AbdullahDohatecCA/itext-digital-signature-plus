package com.dohatecca.application;

import com.dohatecca.util.pdf.PdfConverter;
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
    private JFrame homeScreenFrame;
    private JFrame savingLoaderFrame;
    private JPanel menuContainer;
    private JPanel menubarPanel;
    private JPanel pdfContentPanel;
    private JPanel imagePreviewPanel;
    private JPanel savingProgressPanel;
    private JLabel previewImageLabel;
    private JLabel savingProgressLabel;
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
    private ImageIcon previewImage;
    private ImageIcon defaultSignatureImage;
    private ImageIcon savingProgressIcon;
    private JFormattedTextField previewImageText;
    private String selectedDocumentFilePath;
    private String signatureImageFilePath;
    private String signedFileSaveLocationPath;
    private Float initialSizeOfSignedFileMB;
    private Float writtenSizeOfSignedFileMB;
    private File lastSignatureImageLocationFile;
    private static PdfViewer pdfViewer;
    private static PdfConverter pdfConverter;
    public HomeScreen() {
        initPdfViewer();
        initPdfConverter();


        initIcons();
        createHomeScreenFrame();
        createMenuContainer();
        createMenubarPanel();
        createImagePreviewPanel();
        setSignatureImagePreview();
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
        menuContainer.add(menubarPanel,BorderLayout.NORTH);
        imagePreviewPanel.add(previewImageLabel);
        imagePreviewPanel.add(previewImageText);
        menuContainer.add(imagePreviewPanel,BorderLayout.SOUTH);
        homeScreenFrame.add(menuContainer,BorderLayout.NORTH);
        homeScreenFrame.add(pdfContentPanel,BorderLayout.CENTER);
        homeScreenFrame.setLocationRelativeTo(null);
        homeScreenFrame.setVisible(true);
    }

    private void initPdfViewer(){
        pdfViewer = new PdfViewer();
        pdfContentPanel = pdfViewer.getPdfViewerPanel();
        pdfViewer.openPdf(getResourcesPath()+"/docs/Welcome.pdf");
    }

    private void initPdfConverter(){
        pdfConverter = new PdfConverter();
    }

    private void initIcons(){
        dohatecLogo = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/Dohatec.png")
                        .getImage()
                        .getScaledInstance(512,512,Image.SCALE_DEFAULT)
        );
        defaultSignatureImage = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/DefaultSignature.png")
                        .getImage()
                        .getScaledInstance(256,128,Image.SCALE_DEFAULT)
        );
        openIcon = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/Open.gif")
                        .getImage()
                        .getScaledInstance(64,64,Image.SCALE_DEFAULT)
        );
        imageIcon = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/Image.gif")
                        .getImage()
                        .getScaledInstance(64,64,Image.SCALE_DEFAULT)
        );
        signIcon = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/Sign.gif")
                        .getImage()
                        .getScaledInstance(64,64,Image.SCALE_DEFAULT)
        );
        saveIcon = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/Save.gif")
                        .getImage()
                        .getScaledInstance(64,64,Image.SCALE_DEFAULT)
        );
        aboutIcon = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/About.gif")
                        .getImage()
                        .getScaledInstance(64,64,Image.SCALE_DEFAULT)
        );
        savingProgressIcon = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/Loader.gif")
                        .getImage()
                        .getScaledInstance(64,64,Image.SCALE_DEFAULT)
        );
    }

    private void createHomeScreenFrame(){
        homeScreenFrame = new JFrame();
        homeScreenFrame.setTitle("Dohatec Digital Signature Tool 2 Beta");
        homeScreenFrame.setIconImage(dohatecLogo.getImage());
        homeScreenFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        homeScreenFrame.setSize(1200,900);
        homeScreenFrame.setLayout(new BorderLayout());
        homeScreenFrame.getContentPane().setBackground(getBackgroundColor());
    }

    private void createMenuContainer(){
        menuContainer = new JPanel();
        menuContainer.setLayout(new BorderLayout());
    }

    private void createMenubarPanel() {
        menubarPanel = new JPanel();
        menubarPanel.setSize(1200,250);
        menubarPanel.setBackground(getPrimaryColor());
        menubarPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
    }

    private void createImagePreviewPanel() {
        imagePreviewPanel = new JPanel();
        imagePreviewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        imagePreviewPanel.setSize(1200,250);
        imagePreviewPanel.setBackground(getBackgroundColor());
    }

    private void setSignatureImagePreview(){
        try{
            previewImageLabel = new JLabel();
            previewImageLabel.setPreferredSize(new Dimension(250,100));

            previewImageText = new JFormattedTextField();
                previewImageText.setFont(getRegularFont());
            previewImageText.setMargin(new Insets(15,15,15,15));
            previewImageText.setBackground(null);
            previewImageText.setEditable(false);
            previewImageText.setBorder(null);

            if(Files.exists(Path.of(getProgramFilesPath()+"/lastSignatureImageLocationPath.txt"))){
                lastSignatureImageLocationFile = new File(getProgramFilesPath()+"/lastSignatureImageLocationPath.txt");
                BufferedReader fileReader = new BufferedReader(new FileReader(lastSignatureImageLocationFile));
                signatureImageFilePath = fileReader.readLine();
                fileReader.close();

                previewImage = new ImageIcon(
                        new ImageIcon(signatureImageFilePath)
                                .getImage()
                                .getScaledInstance(250,100,Image.SCALE_DEFAULT)
                );
                previewImageLabel.setIcon(previewImage);
                previewImageText.setText("Image obtained from last selection.");
                previewImageText.setForeground(getPrimaryColor());
            }
            else{
                previewImageLabel.setIcon(defaultSignatureImage);
                previewImageText.setText("Select your signature image.");
                previewImageText.setForeground(getPrimaryColor());
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
            JFileChooser documentFileChooser = new JFileChooser();
            FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(
                    "PDF files",
                    "pdf"
            );
            FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                    "Image files",
                    "jpg","png","gif","bmp"
            );
            documentFileChooser.addChoosableFileFilter(pdfFilter);
            documentFileChooser.addChoosableFileFilter(imageFilter);
            documentFileChooser.setFileFilter(pdfFilter);
            int isSelected = documentFileChooser.showOpenDialog(homeScreenFrame);
            if(isSelected == JFileChooser.APPROVE_OPTION) {
                File selectedDocumentFile = documentFileChooser.getSelectedFile();
                selectedDocumentFilePath = selectedDocumentFile.getAbsolutePath();
                if(pdfFilter.accept(selectedDocumentFile)){
                    pdfViewer.openPdf(selectedDocumentFilePath);
                }
                else if(imageFilter.accept(selectedDocumentFile)){
                    pdfConverter.convertImageToPdf(selectedDocumentFilePath);
                    System.out.println("Converted");
                    selectedDocumentFilePath = getProgramFilesPath()+"/tempI2PFile.pdf";
                    if(Files.exists(Path.of(selectedDocumentFilePath))){
                        pdfViewer.openPdf(getProgramFilesPath()+"/tempI2PFile.pdf");
                    }
                }
                else{
                    showErrorMessage("Invalid file type.",homeScreenFrame);
                }

            }
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
                    "jpg","png","gif","bmp"
            );
            imageFileChooser.setFileFilter(imageFilter);

            int isSelected = imageFileChooser.showOpenDialog(homeScreenFrame);
            if(isSelected == JFileChooser.APPROVE_OPTION) {
                signatureImageFilePath = imageFileChooser.getSelectedFile().getAbsolutePath();
                try{
                    if(!Files.isDirectory(Path.of(getProgramFilesPath()))){
                        File dohatecCADST2Dir = new File(getProgramFilesPath());
                        dohatecCADST2Dir.mkdirs();
                    }
                    lastSignatureImageLocationFile = new File(getProgramFilesPath()+"/lastSignatureImageLocationPath.txt");
                    FileWriter fileWriter = new FileWriter(lastSignatureImageLocationFile);
                    fileWriter.write(signatureImageFilePath);
                    fileWriter.close();
                }
                catch(Exception e){
                    showErrorMessage(e.getMessage(),homeScreenFrame);
                }
                previewImage = new ImageIcon(
                        new ImageIcon(signatureImageFilePath)
                                .getImage()
                                .getScaledInstance(250,100,Image.SCALE_DEFAULT)
                );
                previewImageLabel.setIcon(previewImage);
                previewImageText.setText("You have selected this image.");
                previewImageText.setForeground(getPrimaryColor());
            }
        });
        selectImage.addMouseListener(this);
    }

    private void createSignButton(){
        sign = new JButton();
        sign.setBackground(null);
        sign.setForeground(getBackgroundColor());
        sign.setBorder(null);
        sign.setFocusable(false);
        sign.setText("Sign");
        sign.setIcon(signIcon);
        sign.setFont(getRegularFont());
        sign.addActionListener(event -> {
            if(selectedDocumentFilePath == null || selectedDocumentFilePath.equals("")) {
                showWarningMessage("Please open a PDF document first.",homeScreenFrame);
            }
            else if(signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                showWarningMessage("Please select a signature image.",homeScreenFrame);
            }
            else{
                SignScreen signScreen = new SignScreen();
                signScreen.setPdfFilePath(selectedDocumentFilePath);
                signScreen.setSignatureImagePath(signatureImageFilePath);
                signScreen.setPageNumber(pdfViewer.getCurrentPageNumber()+1);
                signScreen.sign();
            }
        });
        sign.addMouseListener(this);
    }

    private void createSaveButton(){
        save = new JButton();
        save.setBackground(null);
        save.setForeground(getBackgroundColor());
        save.setBorder(null);
        save.setFocusable(false);
        save.setText("Save");
        save.setIcon(saveIcon);
        save.setFont(getRegularFont());
        save.addActionListener(event -> {
            if(selectedDocumentFilePath == null || selectedDocumentFilePath.equals("")) {
                showWarningMessage("Please open a PDF document first.",homeScreenFrame);
            }
            else if (signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                showWarningMessage("Please select a signature image.",homeScreenFrame);
            }
            else if (!Files.exists(Path.of(getProgramFilesPath()+"/temp.pdf"))) {
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
                        Executor executor = Executors.newSingleThreadExecutor();
                        executor.execute(this::saveSignedFile);
                        selectedDocumentFilePath = null;
                        pdfViewer.closePdf();
                        pdfViewer.openPdf(getResourcesPath()+"/docs/Welcome.pdf");
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
                            String.format("Developed by DohatecCA Team\nIcons by Lordicon.com"),
                            null
                    );
                }
        );
        about.addMouseListener(this);
    }

    private void saveSignedFile() {
        try{
            showSavingWindow();

            FileInputStream fis = new FileInputStream(getProgramFilesPath()+"/temp.pdf");
            FileOutputStream fos = new FileOutputStream(signedFileSaveLocationPath);

            int readBytes;
            byte[] buffer = new byte[1024];
            initialSizeOfSignedFileMB = (float)fis.available()/1000000;
            while((readBytes=fis.read(buffer)) != -1){
                float leftToWriteMB = (float)fis.available()/1000000;
                writtenSizeOfSignedFileMB = initialSizeOfSignedFileMB -leftToWriteMB;
                savingProgressLabel.setText(String.format("Saved %.3f MB of %.3f MB", writtenSizeOfSignedFileMB, initialSizeOfSignedFileMB));
                fos.write(buffer);
            }

            closeSavingWindow();
            fis.close();
            fos.close();
            Files.deleteIfExists(Path.of(getProgramFilesPath() + "/temp.pdf"));
            Files.deleteIfExists(Path.of(getProgramFilesPath() + "/tempI2PFile.pdf"));
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage(),homeScreenFrame);
            throw new RuntimeException(e);
        }
    }

    private void showSavingWindow() {
        savingLoaderFrame = new JFrame();
        savingLoaderFrame.setTitle("Saving");
        savingLoaderFrame.setIconImage(dohatecLogo.getImage());
        savingLoaderFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        savingLoaderFrame.setSize(300,125);
        savingLoaderFrame.setFont(getRegularFont());
        savingLoaderFrame.getContentPane().setBackground(getBackgroundColor());

        savingProgressPanel = new JPanel();
        savingProgressPanel.setBackground(getBackgroundColor());

        savingProgressLabel = new JLabel();
        savingProgressLabel.setIcon(new ImageIcon(
                savingProgressIcon.getImage().getScaledInstance(64,64,Image.SCALE_DEFAULT)
        ));

        savingProgressPanel.add(savingProgressLabel);
        savingLoaderFrame.add(savingProgressPanel,BorderLayout.CENTER);

        savingLoaderFrame.setVisible(true);
        savingLoaderFrame.setLocationRelativeTo(homeScreenFrame);
    }

    private void closeSavingWindow() {
        savingLoaderFrame.dispose();
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