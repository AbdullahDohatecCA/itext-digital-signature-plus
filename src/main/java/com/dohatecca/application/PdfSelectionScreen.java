package com.dohatecca.application;

import javax.swing.*;
import java.awt.*;

import static com.dohatecca.util.Config.*;

public class PdfSelectionScreen {
    private ImageIcon dohatecLogo;
    private JFrame pdfSelectionWindow;
    private JPanel pdfSelectionHeaderPanel;
    private JPanel pdfSelectionContentPanel;
    private JPanel pdfSelectionTablePanel;
    private JPanel pdfSelectionButtonsPanel;
    private JPanel pdfSelectionFooterPanel;
    private JLabel pdfSelectionHeaderText;
    private JTable pdfSelectionTable;
    private JButton addButton;
    private JButton removeButton;
    private JButton mergeButton;

    public PdfSelectionScreen(){
        initIcons();

        createAddButton();
        createRemoveButton();
        createMergeButton();

        createPdfSelectionHeader();
        createPdfSelectionContentPanel();
        createPdfSelectionFooter();
        createPdfSelectionWindow();
    }

    private void initIcons(){
        dohatecLogo = new ImageIcon(
                new ImageIcon(getResourcesPath()+"/images/Dohatec.png")
                        .getImage()
                        .getScaledInstance(512,512, Image.SCALE_DEFAULT)
        );
    }

    private void createPdfSelectionWindow(){
        pdfSelectionWindow = new JFrame();
        pdfSelectionWindow.setTitle("Select PDF");
        pdfSelectionWindow.setIconImage(dohatecLogo.getImage());
        pdfSelectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pdfSelectionWindow.setLayout(new BorderLayout());
        pdfSelectionWindow.setSize(1000,600);
        pdfSelectionWindow.add(pdfSelectionHeaderPanel, BorderLayout.NORTH);
        pdfSelectionWindow.add(pdfSelectionContentPanel,BorderLayout.CENTER);
        pdfSelectionWindow.add(pdfSelectionFooterPanel,BorderLayout.SOUTH);
        pdfSelectionWindow.setLocationRelativeTo(null);
        pdfSelectionWindow.setVisible(true);
    }

    private void createPdfSelectionHeader(){
        pdfSelectionHeaderPanel = new JPanel();
        pdfSelectionHeaderPanel.setSize(1000,100);
        pdfSelectionHeaderPanel.setBackground(getSecondaryColor());
        pdfSelectionHeaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        pdfSelectionHeaderText = new JLabel("Select PDF File(s)");
        pdfSelectionHeaderText.setFont(getBoldFont());
        pdfSelectionHeaderText.setForeground(getBackgroundColor());
        pdfSelectionHeaderText.setBackground(null);
        pdfSelectionHeaderText.setBorder(null);
        pdfSelectionHeaderPanel.add(pdfSelectionHeaderText);
    }

    private void createPdfSelectionContentPanel(){
        pdfSelectionContentPanel = new JPanel();
        pdfSelectionContentPanel.setLayout(new BorderLayout());
        pdfSelectionTablePanel = new JPanel();
        pdfSelectionTablePanel.setBackground(getDangerColor());
        pdfSelectionButtonsPanel = new JPanel();
        pdfSelectionButtonsPanel.setLayout(new BoxLayout(pdfSelectionButtonsPanel, BoxLayout.PAGE_AXIS));
        pdfSelectionButtonsPanel.setBackground(getBackgroundColor());
        pdfSelectionButtonsPanel.add(addButton);
        pdfSelectionButtonsPanel.add(removeButton);
        pdfSelectionButtonsPanel.add(mergeButton);
        pdfSelectionContentPanel.add(pdfSelectionTablePanel,BorderLayout.CENTER);
        pdfSelectionContentPanel.add(pdfSelectionButtonsPanel,BorderLayout.EAST);
    }

    private void createPdfSelectionFooter(){
        pdfSelectionFooterPanel = new JPanel();
        pdfSelectionFooterPanel.setBackground(getSecondaryColor());
        pdfSelectionFooterPanel.setSize(1000,100);
    }

    private void createAddButton(){
        addButton = new JButton();
        addButton.setMaximumSize(new Dimension(250,35));
        addButton.setText("Add");
        addButton.setForeground(getPrimaryColor());
        addButton.setFocusable(false);
        addButton.setFont(getRegularFont());
        addButton.addActionListener(
                e -> {

                }
        );
    }

    private void createRemoveButton(){
        removeButton = new JButton();
        removeButton.setMaximumSize(new Dimension(250,35));
        removeButton.setText("Remove");
        removeButton.setForeground(getPrimaryColor());
        removeButton.setFocusable(false);
        removeButton.setFont(getRegularFont());
        removeButton.addActionListener(
                e -> {

                }
        );
    }

    private void createMergeButton(){
        mergeButton = new JButton();
        mergeButton.setMaximumSize(new Dimension(250,35));
        mergeButton.setText("Merge");
        mergeButton.setForeground(getPrimaryColor());
        mergeButton.setFocusable(false);
        mergeButton.setFont(getRegularFont());
        mergeButton.addActionListener(
                e -> {

                }
        );
    }
}
