package com.dohatecca;

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

import static com.dohatecca.MessageUtil.showErrorMessage;
import static com.dohatecca.MessageUtil.showWarningMessage;

public class HomeScreen implements ActionListener, MouseListener {
    private JFrame homeScreenFrame;
    private JFrame savingLoaderFrame;
    private JPanel menuContainer;
    private JPanel menubarPanel;
    private JPanel pdfContentPanel;
    private JPanel imagePreviewPanel;
    private JPanel loaderPanel;
    private JLabel previewImageLabel;
    private JLabel loaderLabel;
    private JButton open;
    private JButton sign;
    private JButton save;
    private JButton selectImage;
    private ImageIcon dohatecLogo;
    private ImageIcon openIcon;
    private ImageIcon signIcon;
    private ImageIcon saveIcon;
    private ImageIcon imageIcon;
    private ImageIcon previewImage;
    private ImageIcon defaultSignatureImage;
    private ImageIcon loaderIcon;
    private JFormattedTextField previewImageText;
    private String selectedDocumentFilePath;
    private String signatureImageFilePath;
    private String signedFileSaveLocationPath;
    private Float initialSizeOfSignedFileMB;
    private Float writtenSizeOfSignedFileMB;
    private File lastSignatureImageLocationFile;
    public HomeScreen() {
        DisplayPdf displayPdf = new DisplayPdf();
        PdfConverter pdfConverter = new PdfConverter();


        initIcons();
        createHomeScreenFrame();
        createMenuContainer();
        createMenubarPanel();
        createImagePreviewPanel();
        setSignatureImagePreview();

        pdfContentPanel = displayPdf.getPdfViewerPanel();
        displayPdf.openPdf("docs/Welcome.pdf");

        open = new JButton();
        open.setBackground(null);
        open.setBorder(null);
        open.setFocusable(false);
        open.setText("Open");
        open.setIcon(openIcon);
        open.setFont(new Font("Nunito",Font.PLAIN,18));
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
                    displayPdf.openPdf(selectedDocumentFilePath);
                }
                else if(imageFilter.accept(selectedDocumentFile)){
                    pdfConverter.convertImageToPdf(selectedDocumentFilePath);
                    selectedDocumentFilePath = "C:/DohatecCA_DST2/tempI2PFile.pdf";
                    if(Files.exists(Path.of(selectedDocumentFilePath))){
                        displayPdf.openPdf("C:/DohatecCA_DST2/tempI2PFile.pdf");
                    }
                }
                else{
                    showErrorMessage("Invalid file type.",homeScreenFrame);
                }

            }
        });
        open.addMouseListener(this);

        selectImage = new JButton();
        selectImage.setBackground(null);
        selectImage.setBorder(null);
        selectImage.setFocusable(false);
        selectImage.setText("Select Image");
        selectImage.setIcon(imageIcon);
        selectImage.setFont(new Font("Nunito",Font.PLAIN,18));
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
                            if(!Files.isDirectory(Path.of("C:/DohatecCA_DST2/"))){
                                File dohatecCADST2Dir = new File("C:/DohatecCA_DST2/");
                                dohatecCADST2Dir.mkdirs();
                            }
                            lastSignatureImageLocationFile = new File("C:/DohatecCA_DST2/lastSignatureImageLocationPath.txt");
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
                        previewImageText.setForeground(new Color(0x51F851));
                    }
                });
        selectImage.addMouseListener(this);

        sign = new JButton();
        sign.setBackground(null);
        sign.setBorder(null);
        sign.setFocusable(false);
        sign.setText("Sign");
        sign.setIcon(signIcon);
        sign.setFont(new Font("Nunito",Font.PLAIN,18));
        sign.addActionListener(event -> {
            if(selectedDocumentFilePath == null || selectedDocumentFilePath.equals("")) {
                showWarningMessage("Please open a PDF document first.",homeScreenFrame);
            }
            else if(signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                showWarningMessage("Please select a signature image.",homeScreenFrame);
            }
            else{
                Signature signature = new Signature();
                signature.initProvider();
                signature.initKeyStore();
                signature.setPdfFilePath(selectedDocumentFilePath);
                signature.setSignatureImagePath(signatureImageFilePath);
                signature.setPageNumber(displayPdf.getCurrentPageNumber()+1);
                signature.selectKeystoreAndSign();
            }
        });
        sign.addMouseListener(this);

        save = new JButton();
        save.setBackground(null);
        save.setBorder(null);
        save.setFocusable(false);
        save.setText("Save");
        save.setIcon(saveIcon);
        save.setFont(new Font("Nunito",Font.PLAIN,18));
        save.addActionListener(event -> {
            if(selectedDocumentFilePath == null || selectedDocumentFilePath.equals("")) {
                showWarningMessage("Please open a PDF document first.",homeScreenFrame);
            }
            else if (signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                showWarningMessage("Please select a signature image.",homeScreenFrame);
            }
            else if (!Files.exists(Path.of("C://DohatecCA_DST2/temp.pdf"))) {
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
                        displayPdf.closePdf();
                        displayPdf.openPdf("docs/Welcome.pdf");
                    } catch (Exception e) {
                        showErrorMessage(e.getMessage(),homeScreenFrame);
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        save.addMouseListener(this);

        menubarPanel.add(open);
        menubarPanel.add(selectImage);
        menubarPanel.add(sign);
        menubarPanel.add(save);
        menuContainer.add(menubarPanel,BorderLayout.NORTH);
        imagePreviewPanel.add(previewImageLabel);
        imagePreviewPanel.add(previewImageText);
        menuContainer.add(imagePreviewPanel,BorderLayout.SOUTH);
        homeScreenFrame.add(menuContainer,BorderLayout.NORTH);
        homeScreenFrame.add(pdfContentPanel,BorderLayout.CENTER);
        homeScreenFrame.setLocationRelativeTo(null);
        homeScreenFrame.setVisible(true);
    }

    private void initIcons(){
        dohatecLogo = new ImageIcon("src/main/resources/images/Dohatec.png");
        defaultSignatureImage = new ImageIcon("src/main/resources/images/DefaultSignature.png");
        openIcon = new ImageIcon("src/main/resources/images/Open.gif");
        signIcon = new ImageIcon("src/main/resources/images/Sign.gif");
        saveIcon = new ImageIcon("src/main/resources/images/Save.gif");
        imageIcon = new ImageIcon("src/main/resources/images/Image.gif");
        loaderIcon = new ImageIcon("src/main/resources/images/Loader.gif");
    }

    private void createHomeScreenFrame(){
        homeScreenFrame = new JFrame();
        homeScreenFrame.setTitle("Dohatec Digital Signature Tool 2 Beta");
        homeScreenFrame.setIconImage(dohatecLogo.getImage());
        homeScreenFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        homeScreenFrame.setSize(1200,675);
        homeScreenFrame.setLayout(new BorderLayout());
        homeScreenFrame.getContentPane().setBackground(new Color(0xB3B3B3));
    }

    private void createMenuContainer(){
        menuContainer = new JPanel();
        menuContainer.setLayout(new BorderLayout());
    }

    private void createMenubarPanel() {
        menubarPanel = new JPanel();
        menubarPanel.setSize(1200,250);
        menubarPanel.setBackground(new Color(0xAACCFF));
        menubarPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
    }

    private void createImagePreviewPanel() {
        imagePreviewPanel = new JPanel();
        imagePreviewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        imagePreviewPanel.setSize(1200,250);
        imagePreviewPanel.setBackground(new Color(0x76A9F8));
    }

    private void setSignatureImagePreview(){
        try{
            previewImageLabel = new JLabel();
            previewImageLabel.setPreferredSize(new Dimension(250,100));

            previewImageText = new JFormattedTextField();
            previewImageText.setFont(new Font("Nunito",Font.PLAIN,18));
            previewImageText.setMargin(new Insets(15,15,15,15));
            previewImageText.setBackground(null);
            previewImageText.setEditable(false);
            previewImageText.setBorder(null);

            if(Files.exists(Path.of("C:/DohatecCA_DST2/lastSignatureImageLocationPath.txt"))){
                lastSignatureImageLocationFile = new File("C:/DohatecCA_DST2/lastSignatureImageLocationPath.txt");
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
                previewImageText.setForeground(new Color(0x51F851));
            }
            else{
                previewImageLabel.setIcon(defaultSignatureImage);
                previewImageText.setText("Select your signature image.");
                previewImageText.setForeground(new Color(0xF50606));
            }
        }
        catch (Exception e){
            showErrorMessage(e.getMessage(),homeScreenFrame);
        }
    }

    private void saveSignedFile() {
        try{
            showSavingWindow();

            FileInputStream fis = new FileInputStream("C://DohatecCA_DST2/temp.pdf");
            FileOutputStream fos = new FileOutputStream(signedFileSaveLocationPath);

            int readBytes;
            initialSizeOfSignedFileMB = (float)fis.available()/1000000;
            while((readBytes=fis.read()) != -1){
                float leftToWriteMB = (float)fis.available()/1000000;
                writtenSizeOfSignedFileMB = initialSizeOfSignedFileMB -leftToWriteMB;
                loaderLabel.setText(String.format("Saved %.3f MB of %.3f MB", writtenSizeOfSignedFileMB, initialSizeOfSignedFileMB));
                fos.write(readBytes);
            }

            closeSavingWindow();
            new File("C://DohatecCA_DST2/temp.pdf").delete();
            fis.close();
            fos.close();
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
        savingLoaderFrame.getContentPane().setBackground(new Color(0xB3B3B3));

        loaderPanel = new JPanel();
        loaderPanel.setBackground(new Color(0xB3B3B3));

        loaderLabel = new JLabel();
        loaderLabel.setIcon(new ImageIcon(
                loaderIcon.getImage().getScaledInstance(64,64,Image.SCALE_DEFAULT)
        ));

        loaderPanel.add(loaderLabel);
        savingLoaderFrame.add(loaderPanel,BorderLayout.CENTER);

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
            open.setBackground(new Color(0xB393BFF3));
        }
        else if(e.getSource()==selectImage){
            selectImage.setBackground(new Color(0xB393BFF3));
        }
        else if( e.getSource()==sign){
            sign.setBackground(new Color(0xB393BFF3));
        }
        else if(e.getSource()==save){
            save.setBackground(new Color(0xB393BFF3));
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if(e.getSource()==open){
            open.setBackground(null);
        }
        else if(e.getSource()==selectImage){
            selectImage.setBackground(null);
        }
        else if( e.getSource()==sign){
            sign.setBackground(null);
        }
        else if(e.getSource()==save){
            save.setBackground(null);
        }
    }
}