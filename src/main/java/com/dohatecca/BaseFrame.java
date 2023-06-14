package com.dohatecca;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BaseFrame extends SwingWorker<Void, Float> implements ActionListener, MouseListener {
    private final JFrame baseFrame;
    private JFrame savingLoaderFrame;
    private JPanel menuContainer;
    private JPanel menubarPanel;
    private JPanel contentPanel;
    private JPanel imagePreviewPanel;
    private JLabel previewImageLabel;
    private JPanel loaderPanel;
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
    private String signedFileSaveLocation;
    private Float initialSizeOfSignedFile;
    private Float writtenSizeOfSignedFile;
    public BaseFrame() {
        dohatecLogo = new ImageIcon("src/main/resources/Dohatec.png");
        dummySignatureImage = new ImageIcon("src/main/resources/DummySignature.png");
        openIcon = new ImageIcon("src/main/resources/Open.gif");
        signIcon = new ImageIcon("src/main/resources/Sign.gif");
        saveIcon = new ImageIcon("src/main/resources/Save.gif");
        imageIcon = new ImageIcon("src/main/resources/Image.gif");
        loaderIcon = new ImageIcon("src/main/resources/Loader.gif");

        baseFrame = new JFrame();
        baseFrame.setTitle("Dohatec Digital Signature Tool 2 Demo");
        baseFrame.setIconImage(dohatecLogo.getImage());
        baseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        baseFrame.setSize(400,450);
        baseFrame.setLayout(new BorderLayout());
        baseFrame.getContentPane().setBackground(new Color(0xB3B3B3));

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
        contentPanel = displayPdf.getPdfViewerPanel();

        open = new JButton();
        open.setBackground(null);
        open.setBorder(null);
        open.setFocusable(false);
        open.setText("Open");
        open.setIcon(openIcon);
        open.setFont(new Font("Nunito",Font.PLAIN,18));
        open.addActionListener(event -> {
            JFileChooser pdfFileChooser = new JFileChooser();
            FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("PDF files", "pdf");
            pdfFileChooser.setFileFilter(pdfFilter);
            int isSelected = pdfFileChooser.showOpenDialog(null);
            if(isSelected == JFileChooser.APPROVE_OPTION) {
                selectedPdfFilePath = pdfFileChooser.getSelectedFile().getAbsolutePath();
                displayPdf.showPdf(selectedPdfFilePath);
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
                            "jpg",
                            "png",
                            "gif",
                            "bmp"
                    );
                    imageFileChooser.setFileFilter(imageFilter);

                    int isSelected = imageFileChooser.showOpenDialog(null);
                    if(isSelected == JFileChooser.APPROVE_OPTION) {
                        signatureImageFilePath = imageFileChooser.getSelectedFile().getAbsolutePath();
                        ImageIcon tempImage = new ImageIcon(signatureImageFilePath);
                        if(tempImage.getIconWidth() > 250) {
                            previewImage = new ImageIcon(
                                    tempImage.getImage()
                                            .getScaledInstance(250,-1,Image.SCALE_DEFAULT)
                            );
                        }
                        if(tempImage.getIconHeight() > 100) {
                            previewImage = new ImageIcon(
                                    tempImage.getImage()
                                            .getScaledInstance(-1,100,Image.SCALE_DEFAULT)
                            );
                        }
                        else{
                            previewImage = tempImage;
                        }

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
                JOptionPane.showMessageDialog(
                    baseFrame,
                    "Please open a PDF document first.",
                    "Document File Warning",
                    JOptionPane.WARNING_MESSAGE
                );
            }
            else if(signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                JOptionPane.showMessageDialog(
                        baseFrame,
                        "Please select a signature image.",
                        "Signature Image Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            else{
                Signature signature = new Signature();
                signature.initProvider();
                signature.initKeyStore();
                signature.setPdfFilePath(selectedPdfFilePath);
                signature.setSignatureImagePath(signatureImageFilePath);
                signature.setPageNumber(displayPdf.getCurrentPageNumber()+1);
                signature.showKeyStoreTable();
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
                JOptionPane.showMessageDialog(
                        baseFrame,
                        "Please open a PDF document first.",
                        "Document File Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            else if (signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                JOptionPane.showMessageDialog(
                        baseFrame,
                        "Please select a signature image.",
                        "Signature Image Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            else if (!Files.exists(Path.of("C://DohatecCA_DST2/temp.pdf"))) {
                JOptionPane.showMessageDialog(
                        baseFrame,
                        "Please sign your document before saving.",
                        "Sign Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
            else {
                JFileChooser saveLocationSelector = new JFileChooser();
                FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("PDF files", "pdf");
                saveLocationSelector.setFileFilter(pdfFilter);
                int isSelected = saveLocationSelector.showSaveDialog(null);
                if(isSelected == JFileChooser.APPROVE_OPTION){
                    signedFileSaveLocation = saveLocationSelector.getSelectedFile().getAbsolutePath();
                    if(!signedFileSaveLocation.endsWith(".pdf")){
                        signedFileSaveLocation = signedFileSaveLocation+".pdf";
                    }
                    System.out.println(signedFileSaveLocation);
                    try {
                        showSavingWindow();
                        this.execute(); //saves file in the background with doInBackground() method
                        selectedPdfFilePath = null;
                        signatureImageFilePath = null;
                        displayPdf.closePdf();
                        previewImageLabel.setIcon(dummySignatureImage);
                        previewImageText.setText("Upload your signature image.");
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(
                                baseFrame,
                                e.getMessage(),
                                "File Error",
                                JOptionPane.ERROR_MESSAGE
                        );
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
        baseFrame.add(menuContainer,BorderLayout.NORTH);
        baseFrame.add(contentPanel,BorderLayout.CENTER);
        baseFrame.pack();
        baseFrame.setLocationRelativeTo(null);
        baseFrame.setVisible(true);
    }

    private void saveSignedFile() {
        try{
            FileInputStream fis = new FileInputStream("C://DohatecCA_DST2/temp.pdf");
            FileOutputStream fos = new FileOutputStream(signedFileSaveLocation);

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
            JOptionPane.showMessageDialog(
                    baseFrame,
                    e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
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
        savingLoaderFrame.setLocationRelativeTo(baseFrame);
    }

    private void closeSavingWindow() {
        savingLoaderFrame.dispose();
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
