package com.dohatecca;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class BaseFrame extends JFrame implements ActionListener, MouseListener {
    JPanel menuContainer;
    JPanel menubarPanel;
    JPanel contentPanel;
    JPanel imagePreviewPanel;
    JLabel previewImageLabel;
    JButton open;
    JButton sign;
    JButton save;
    JButton selectImage;
    ImageIcon dohatecLogo;
    ImageIcon openIcon;
    ImageIcon signIcon;
    ImageIcon saveIcon;
    ImageIcon imageIcon;
    ImageIcon previewImage;
    ImageIcon dummySignatureImage;
    JFormattedTextField previewImageText;

    String selectedPdfFilePath;
    String signatureImageFilePath;

    public BaseFrame() {
        dohatecLogo = new ImageIcon("src/main/resources/Dohatec.png");
        dummySignatureImage = new ImageIcon("src/main/resources/DummySignature.png");
        openIcon = new ImageIcon("src/main/resources/Open.gif");
        signIcon = new ImageIcon("src/main/resources/Sign.gif");
        saveIcon = new ImageIcon("src/main/resources/Save.gif");
        imageIcon = new ImageIcon("src/main/resources/Image.gif");

        setTitle("Dohatec Digital Signature Tool 2 Demo");
        setIconImage(dohatecLogo.getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,450);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xB3B3B3));

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
            JFileChooser fileChooser = new JFileChooser();
            int isSelected = fileChooser.showOpenDialog(null);
            if(isSelected == JFileChooser.APPROVE_OPTION) {
                selectedPdfFilePath = fileChooser.getSelectedFile().getAbsolutePath();
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
                    JFileChooser fileChooser = new JFileChooser();
                    int isSelected = fileChooser.showOpenDialog(null);
                    if(isSelected == JFileChooser.APPROVE_OPTION) {
                        signatureImageFilePath = fileChooser.getSelectedFile().getAbsolutePath();
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
                        previewImageText.repaint();
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
                    BaseFrame.this,
                    "Please open a PDF document first.",
                    "Document File Warning",
                    JOptionPane.WARNING_MESSAGE
                );
            }
            else if(signatureImageFilePath == null || signatureImageFilePath.equals("")) {
                JOptionPane.showMessageDialog(
                        BaseFrame.this,
                        "Please selected a signature image.",
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
            JFileChooser saveLocationSelector = new JFileChooser();
            int isSelected = saveLocationSelector.showSaveDialog(null);
            if(isSelected == JFileChooser.APPROVE_OPTION){
                String saveLocation = saveLocationSelector.getSelectedFile().getAbsolutePath();
                System.out.println(saveLocation);
                File saveFile = new File(saveLocation);
                try {
                    FileOutputStream fos = new FileOutputStream(saveFile);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
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
        add(menuContainer,BorderLayout.NORTH);
        add(contentPanel,BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
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
