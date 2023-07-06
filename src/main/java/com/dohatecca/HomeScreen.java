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
import java.util.List;

public class HomeScreen extends SwingWorker<Void, Float> implements ActionListener, MouseListener {
    private final JFrame homeScreenFrame;
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
    private ImageIcon dummySignatureImage;
    private ImageIcon loaderIcon;
    private JFormattedTextField previewImageText;
    private String selectedPdfFilePath;
    private String signatureImageFilePath;
    private String signedFileSaveLocationPath;
    private Float initialSizeOfSignedFile;
    private Float writtenSizeOfSignedFile;
    public HomeScreen() {
        dohatecLogo = new ImageIcon("src/main/resources/images/Dohatec.png");
        dummySignatureImage = new ImageIcon("src/main/resources/images/DummySignature.png");
        openIcon = new ImageIcon("src/main/resources/images/Open.gif");
        signIcon = new ImageIcon("src/main/resources/images/Sign.gif");
        saveIcon = new ImageIcon("src/main/resources/images/Save.gif");
        imageIcon = new ImageIcon("src/main/resources/images/Image.gif");
        loaderIcon = new ImageIcon("src/main/resources/images/Loader.gif");

        homeScreenFrame = new JFrame();
        homeScreenFrame.setTitle("Dohatec Digital Signature Tool 2 Demo");
        homeScreenFrame.setIconImage(dohatecLogo.getImage());
        homeScreenFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        homeScreenFrame.setSize(400,450);
        homeScreenFrame.setLayout(new BorderLayout());
        homeScreenFrame.getContentPane().setBackground(new Color(0xB3B3B3));

        menuContainer = new JPanel();
        menuContainer.setLayout(new BorderLayout());

        menubarPanel = new JPanel();
        menubarPanel.setBackground(new Color(0xAACCFF));
        menubarPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        imagePreviewPanel = new JPanel();
        imagePreviewPanel.setSize(400,250);
        imagePreviewPanel.setBackground(new Color(0x76A9F8));

        previewImageLabel = new JLabel();
        previewImageLabel.setPreferredSize(new Dimension(250,100));
        previewImageLabel.setIcon(dummySignatureImage);

        previewImageText = new JFormattedTextField();
        previewImageText.setText("Upload your signature image.");
        previewImageText.setFont(new Font("Nunito",Font.PLAIN,18));
        previewImageText.setForeground(new Color(0xF50606));
        previewImageText.setBackground(null);
        previewImageText.setEditable(false);
        previewImageText.setBorder(null);

        DisplayPdf displayPdf = new DisplayPdf();
        pdfContentPanel = displayPdf.getPdfViewerPanel();

        open = new JButton();
        open.setBackground(null);
        open.setBorder(null);
        open.setFocusable(false);
        open.setText("Open");
        open.setIcon(openIcon);
        open.setFont(new Font("Nunito",Font.PLAIN,18));
        open.addActionListener(event -> {
            JFileChooser pdfFileChooser = new JFileChooser();
            FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(
                    "PDF files",
                    "pdf"
            );
            pdfFileChooser.setFileFilter(pdfFilter);
            int isSelected = pdfFileChooser.showOpenDialog(homeScreenFrame);
            if(isSelected == JFileChooser.APPROVE_OPTION) {
                selectedPdfFilePath = pdfFileChooser.getSelectedFile().getAbsolutePath();
                displayPdf.openPdf(selectedPdfFilePath);
            }
        });
        open.addMouseListener(this);

        File lastSignatureImageLocationFile = new File("C:/DohatecCA_DST2/lastSignatureImageLocationPath.txt");
        try{
            if(Files.exists(Path.of("C:/DohatecCA_DST2/lastSignatureImageLocationPath.txt"))){
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
        }
        catch (Exception e){
            showErrorMessage(e.getMessage());
        }

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
                            FileWriter fileWriter = new FileWriter(lastSignatureImageLocationFile);
                            fileWriter.write(signatureImageFilePath);
                            fileWriter.close();
                        }
                        catch(Exception e){
                            showErrorMessage(e.getMessage());
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
            if(selectedPdfFilePath == null || selectedPdfFilePath.equals("")) {
                showWarningMessage("Please open a PDF document first.");
            }
            else if(signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                showWarningMessage("Please select a signature image.");
            }
            else{
                Signature signature = new Signature();
                signature.initProvider();
                signature.initKeyStore();
                signature.setPdfFilePath(selectedPdfFilePath);
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
            if(selectedPdfFilePath == null || selectedPdfFilePath.equals("")) {
                showWarningMessage("Please open a PDF document first.");
            }
            else if (signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                showWarningMessage("Please select a signature image.");
            }
            else if (!Files.exists(Path.of("C://DohatecCA_DST2/temp.pdf"))) {
                showWarningMessage("Please sign your document before saving.");
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
                        showSavingWindow();
                        //saves file in the background with doInBackground() method
                        this.execute();
                        selectedPdfFilePath = null;
                        displayPdf.closePdf();
                    } catch (Exception e) {
                        showErrorMessage(e.getMessage());
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
        imagePreviewPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        imagePreviewPanel.add(previewImageLabel);
        imagePreviewPanel.add(previewImageText);
        menuContainer.add(imagePreviewPanel,BorderLayout.SOUTH);
        homeScreenFrame.add(menuContainer,BorderLayout.NORTH);
        homeScreenFrame.add(pdfContentPanel,BorderLayout.CENTER);
        homeScreenFrame.pack();
        homeScreenFrame.setLocationRelativeTo(null);
        homeScreenFrame.setVisible(true);
    }

    private void saveSignedFile() {
        try{
            FileInputStream fis = new FileInputStream("C://DohatecCA_DST2/temp.pdf");
            FileOutputStream fos = new FileOutputStream(signedFileSaveLocationPath);

            int readBytes;
            initialSizeOfSignedFile = (float)fis.available()/1000000;
            while((readBytes=fis.read()) != -1){
                float leftToWriteMB = (float)fis.available()/1000000;
                publish(initialSizeOfSignedFile-leftToWriteMB);
                fos.write(readBytes);
            }

            fis.close();
            fos.close();
        }
        catch (Exception e) {
            showErrorMessage(e.getMessage());
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
    private void showWarningMessage(String message){
        JOptionPane.showMessageDialog(
                homeScreenFrame,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }
    private void showErrorMessage(String message){
        JOptionPane.showMessageDialog(
                homeScreenFrame,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
    protected Void doInBackground() {
        saveSignedFile();
        return null;
    }

    @Override
    protected void process(List<Float> chunks) {
        writtenSizeOfSignedFile = chunks.get(chunks.size()-1);
        loaderLabel.setText(String.format("Saved %.3f MB of %.3f MB",writtenSizeOfSignedFile,initialSizeOfSignedFile));
    }

    @Override
    protected void done() {
        closeSavingWindow();
        new File("C://DohatecCA_DST2/temp.pdf").delete();
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