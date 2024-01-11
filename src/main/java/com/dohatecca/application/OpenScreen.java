package com.dohatecca.application;

import com.dohatecca.util.pdf.PdfConverter;
import com.dohatecca.util.pdf.PdfMergerUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.dohatecca.util.Config.*;
import static com.dohatecca.util.Message.showErrorMessage;

public class OpenScreen {
    private final HomeScreen homeScreen;
    private ImageIcon dohatecLogo;
    private ImageIcon loadingIcon;
    private JFrame openScreenFrame;
    private JPanel openScreenHeaderPanel;
    private JPanel openScreenContentPanel;
    private JPanel selectedDocumentsTableConatinerPanel;
    private JPanel selectionButtonsContainerPanel;
    private JPanel openScreenFooterPanel;
    private JLabel openScreenHeaderText;
    private JTextArea openScreenFooterText;
    private JScrollPane selectedDocumentsTableScrollPane;
    private JTable selectedDocumentsTable;
    private JButton addButton;
    private JButton removeButton;
    private JButton openButton;
    private JDialog mergingDialog;
    private JPanel mergingPanel;
    private JLabel mergingLabel;
    private static final ArrayList<String> selectedDocumentsPathList = new ArrayList<>();
    private static final String[][] selectedDocumentsPathArray = new String[100][1];

    public OpenScreen(HomeScreen homeScreenRef){
        this.homeScreen = homeScreenRef;

        initIcons();

        createAddButton();
        createRemoveButton();
        createMergeButton();

        createHeader();
        createTable();
        createContentPanel();
        createFooter();
        createOpenScreen();
    }

    private void initIcons(){
        dohatecLogo = getDohatecLogo();
        loadingIcon = getLoadingIcon();
    }

    private void createOpenScreen(){
        openScreenFrame = new JFrame();
        openScreenFrame.setTitle("Open");
        openScreenFrame.setIconImage(dohatecLogo.getImage());
        openScreenFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        openScreenFrame.setLayout(new BorderLayout());
        openScreenFrame.setSize(800,600);
        openScreenFrame.add(openScreenHeaderPanel, BorderLayout.NORTH);
        openScreenFrame.add(openScreenContentPanel,BorderLayout.CENTER);
        openScreenFrame.add(openScreenFooterPanel,BorderLayout.SOUTH);
        openScreenFrame.setLocationRelativeTo(null);
        openScreenFrame.setVisible(true);
    }

    private void createHeader(){
        openScreenHeaderPanel = new JPanel();
        openScreenHeaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT,25,25));
        openScreenHeaderPanel.setBackground(getSecondaryColor());

        openScreenHeaderText = new JLabel("Select PDF/Image File(s)");
        openScreenHeaderText.setFont(getBoldFont());
        openScreenHeaderText.setForeground(getBackgroundColor());
        openScreenHeaderText.setBackground(null);
        openScreenHeaderText.setBorder(null);

        openScreenHeaderPanel.add(openScreenHeaderText);
    }

    private void createContentPanel(){
        openScreenContentPanel = new JPanel();
        openScreenContentPanel.setLayout(new BorderLayout());

        selectedDocumentsTableConatinerPanel = new JPanel();
        selectedDocumentsTableConatinerPanel.setLayout(new BorderLayout());
        selectedDocumentsTableConatinerPanel.setBackground(getBackgroundColor());
        selectedDocumentsTableConatinerPanel.add(selectedDocumentsTableScrollPane, BorderLayout.CENTER);

        selectionButtonsContainerPanel = new JPanel();
        selectionButtonsContainerPanel.setLayout(new BoxLayout(selectionButtonsContainerPanel, BoxLayout.PAGE_AXIS));
        selectionButtonsContainerPanel.setBackground(getBackgroundColor());

        selectionButtonsContainerPanel.add(addButton);
        selectionButtonsContainerPanel.add(removeButton);
        selectionButtonsContainerPanel.add(openButton);

        openScreenContentPanel.add(selectedDocumentsTableConatinerPanel,BorderLayout.CENTER);
        openScreenContentPanel.add(selectionButtonsContainerPanel,BorderLayout.EAST);
    }

    private void createTable(){
        String[] pdfSelectionTableHeadersArray = new String[]{"Location"};
        selectedDocumentsPathList.clear();
        copyListToArray(selectedDocumentsPathList);
        selectedDocumentsTable = new JTable(selectedDocumentsPathArray,pdfSelectionTableHeadersArray);
        selectedDocumentsTable.setFont(getRegularFont());
        selectedDocumentsTable.setSelectionBackground(getSecondaryColor());
        selectedDocumentsTable.setSelectionForeground(getBackgroundColor());
        selectedDocumentsTable.setRowHeight(50);
        selectedDocumentsTableScrollPane = new JScrollPane(selectedDocumentsTable);
    }

    private void copyListToArray(ArrayList<String> locationList){
        for(int i=0;i<100;i++){
            selectedDocumentsPathArray[i][0] = null;
        }
        int i = 0;
        for(String location: locationList){
            if(i >= 100) break;
            selectedDocumentsPathArray[i][0] = location;
            ++i;
        }
    }

    private void createFooter(){
        openScreenFooterPanel = new JPanel();
        openScreenFooterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        openScreenFooterPanel.setBackground(getBackgroundColor());

        openScreenFooterText = new JTextArea();
        openScreenFooterText.setText("Warning: When merging multiple files into one single document\n" +
                "all previous digital signatures will be removed.");
        openScreenFooterText.setFont(getBoldFont());
        openScreenFooterText.setForeground(getPrimaryColor());
        openScreenFooterText.setBackground(null);
        openScreenFooterText.setBorder(null);
        openScreenFooterText.setEditable(false);
        openScreenFooterText.setFocusable(false);

        openScreenFooterPanel.add(openScreenFooterText);
    }

    private void createAddButton(){
        addButton = new JButton();
        addButton.setMaximumSize(new Dimension(100,35));
        addButton.setText("Add");
        addButton.setForeground(getSuccessColor());
        addButton.setFocusable(false);
        addButton.setFont(getRegularFont());
        addButton.addActionListener(
                e -> {
                    PdfConverter pdfConverter = new PdfConverter();
                    JFileChooser documentFileChooser = new JFileChooser();
                    FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter(
                            "PDF files",
                            "pdf"
                    );
                    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                            "Image files",
                            "jpg","png","gif","bmp","jfif"
                    );
                    documentFileChooser.addChoosableFileFilter(pdfFilter);
                    documentFileChooser.addChoosableFileFilter(imageFilter);
                    documentFileChooser.setFileFilter(pdfFilter);
                    documentFileChooser.setMultiSelectionEnabled(true);
                    int isSelected = documentFileChooser.showOpenDialog(openScreenFrame);
                    if(isSelected == JFileChooser.APPROVE_OPTION) {
                        File[] selectedDocumentFiles = documentFileChooser.getSelectedFiles();
                        for (File selectedDocumentFile : selectedDocumentFiles) {
                            String selectedDocumentFilePath = selectedDocumentFile.getAbsolutePath();
                            if (pdfFilter.accept(selectedDocumentFile)) {
                                selectedDocumentsPathList.add(selectedDocumentFilePath);
                            } else if (imageFilter.accept(selectedDocumentFile)) {
                                String convertedI2pPath = pdfConverter.convertImageToPdf(selectedDocumentFilePath);
                                if (convertedI2pPath != null) {
                                    selectedDocumentsPathList.add(convertedI2pPath);
                                }
                            } else {
                                showErrorMessage("Invalid file type.", openScreenFrame);
                            }
                        }
                        copyListToArray(selectedDocumentsPathList);
                        openScreenFrame.repaint();
                    }
                }
        );
    }

    private void createRemoveButton(){
        removeButton = new JButton();
        removeButton.setMaximumSize(new Dimension(100,35));
        removeButton.setText("Remove");
        removeButton.setForeground(getDangerColor());
        removeButton.setFocusable(false);
        removeButton.setFont(getRegularFont());
        removeButton.addActionListener(
                e -> {
                    int selectedFilePathIndex = selectedDocumentsTable.getSelectedRow();
                    if(selectedFilePathIndex != -1){
                        if(selectedFilePathIndex >= selectedDocumentsPathList.size()
                                || selectedDocumentsPathList.get(selectedFilePathIndex).isEmpty()) return;
                        selectedDocumentsPathList.remove(selectedFilePathIndex);
                        copyListToArray(selectedDocumentsPathList);
                        selectedDocumentsTable.repaint();
                    }
                }
        );
    }

    private void createMergeButton(){
        openButton = new JButton();
        openButton.setMaximumSize(new Dimension(100,35));
        openButton.setText("Open");
        openButton.setForeground(getPrimaryColor());
        openButton.setFocusable(false);
        openButton.setFont(getRegularFont());
        openButton.addActionListener(
                e -> {
                    try{
                        if(selectedDocumentsPathList.isEmpty()) return;
                        else if (selectedDocumentsPathList.size() == 1) {
                            openSingleDocument(selectedDocumentsPathList.get(0));
                            openScreenFrame.dispose();
                            return;
                        }
                        Executor mergeOperationExecutor = Executors.newSingleThreadExecutor();
                        mergeOperationExecutor.execute(this::mergeAllSelectedDocuments);
                        openScreenFrame.dispose();
                    }
                    catch (Exception ex){
                        showErrorMessage(ex.getMessage(), openScreenFrame);
                    }
                }
        );
    }

    private void openSingleDocument(String documentPath){
        homeScreen.setSelectedDocumentFilePath(documentPath);
        homeScreen.getPdfViewer().openPdf(documentPath);
    }

    private void mergeAllSelectedDocuments() {
        createMergeProgressDialog();
        PdfMergerUtil pdfMergerUtil = new PdfMergerUtil();
        String mergedPdfFilePath = pdfMergerUtil.mergePdf(selectedDocumentsPathList);
        homeScreen.setSelectedDocumentFilePath(mergedPdfFilePath);
        homeScreen.getPdfViewer().openPdf(mergedPdfFilePath);
        closeMergeProgressDialog();
    }

    private void createMergeProgressDialog(){
        mergingDialog = new JDialog();
        mergingDialog.setTitle("Signing");
        mergingDialog.setIconImage(dohatecLogo.getImage());
        mergingDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mergingDialog.setSize(300,125);
        mergingDialog.getContentPane().setBackground(getBackgroundColor());
        mergingDialog.getContentPane().setForeground(getPrimaryColor());

        mergingPanel = new JPanel();
        mergingPanel.setBackground(getBackgroundColor());

        mergingLabel = new JLabel();
        mergingLabel.setIcon(loadingIcon);
        mergingLabel.setText("Merging documents. Please wait...");
        mergingLabel.setIconTextGap(5);

        mergingPanel.add(mergingLabel);
        mergingDialog.getContentPane().add(mergingPanel);
        mergingDialog.setLocationRelativeTo(null);
        mergingDialog.setVisible(true);
    }

    private void closeMergeProgressDialog(){
        mergingDialog.dispose();
    }

    public void closeOpenScreen(){
        openScreenFrame.dispose();
    }
}
