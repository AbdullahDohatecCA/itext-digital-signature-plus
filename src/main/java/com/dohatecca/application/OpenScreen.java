package com.dohatecca.application;

import com.dohatecca.util.pdf.PdfConverter;
import com.dohatecca.util.pdf.PdfMergerUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.dohatecca.util.Config.*;
import static com.dohatecca.util.Message.showErrorMessage;

public class OpenScreen {
    private final HomeScreen homeScreen;
    private ImageIcon dohatecLogo;
    private ImageIcon loadingIcon;
    private JFrame pdfSelectionWindow;
    private JPanel pdfSelectionHeaderPanel;
    private JPanel pdfSelectionContentPanel;
    private JPanel pdfSelectionTablePanel;
    private JPanel pdfSelectionButtonsPanel;
    private JPanel pdfSelectionFooterPanel;
    private JLabel pdfSelectionHeaderText;
    private JScrollPane pdfSelectionTableScrollPane;
    private JTable pdfSelectionTable;
    private JButton addButton;
    private JButton removeButton;
    private JButton mergeButton;
    private JPanel mergeProgressPanel;
    private JDialog mergeProgressDialog;
    private JLabel mergeProgressLabel;
    private static final ArrayList<String> selectedDocumentsFilePathsList = new ArrayList<>();
    private static final String[][] selectedDocumentsFilePathsArray = new String[100][1];

    public OpenScreen(HomeScreen homeScreenRef){
        this.homeScreen = homeScreenRef;

        initIcons();

        createAddButton();
        createRemoveButton();
        createMergeButton();

        createPdfSelectionHeader();
        createPdfSelectionTable();
        createPdfSelectionContentPanel();
        createPdfSelectionFooter();
        createPdfSelectionWindow();
    }

    private void initIcons(){
        dohatecLogo = getDohatecLogo();

        loadingIcon = getLoadingIcon();
    }

    private void createPdfSelectionWindow(){
        pdfSelectionWindow = new JFrame();
        pdfSelectionWindow.setTitle("Open");
        pdfSelectionWindow.setIconImage(dohatecLogo.getImage());
        pdfSelectionWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pdfSelectionWindow.setLayout(new BorderLayout());
        pdfSelectionWindow.setSize(800,600);
        pdfSelectionWindow.add(pdfSelectionHeaderPanel, BorderLayout.NORTH);
        pdfSelectionWindow.add(pdfSelectionContentPanel,BorderLayout.CENTER);
        pdfSelectionWindow.add(pdfSelectionFooterPanel,BorderLayout.SOUTH);
        pdfSelectionWindow.setLocationRelativeTo(null);
        pdfSelectionWindow.setVisible(true);
    }

    private void createPdfSelectionHeader(){
        pdfSelectionHeaderPanel = new JPanel();
        pdfSelectionHeaderPanel.setBackground(getSecondaryColor());
        pdfSelectionHeaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        pdfSelectionHeaderText = new JLabel("Select PDF/Image File(s)");
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
        pdfSelectionTablePanel.setLayout(new BorderLayout());
        pdfSelectionTablePanel.setBackground(getBackgroundColor());
        pdfSelectionButtonsPanel = new JPanel();
        pdfSelectionButtonsPanel.setLayout(new BoxLayout(pdfSelectionButtonsPanel, BoxLayout.PAGE_AXIS));
        pdfSelectionButtonsPanel.setBackground(getBackgroundColor());
        pdfSelectionTablePanel.add(pdfSelectionTableScrollPane, BorderLayout.CENTER);
        pdfSelectionButtonsPanel.add(addButton);
        pdfSelectionButtonsPanel.add(removeButton);
        pdfSelectionButtonsPanel.add(mergeButton);
        pdfSelectionContentPanel.add(pdfSelectionTablePanel,BorderLayout.CENTER);
        pdfSelectionContentPanel.add(pdfSelectionButtonsPanel,BorderLayout.EAST);
    }

    private void createPdfSelectionTable(){
        String[] pdfSelectionTableHeadersArray = new String[]{"Location"};
        selectedDocumentsFilePathsList.clear();
        convertListToArray(selectedDocumentsFilePathsList);
        pdfSelectionTable = new JTable(selectedDocumentsFilePathsArray,pdfSelectionTableHeadersArray);
        pdfSelectionTable.setSelectionBackground(getSecondaryColor());
        pdfSelectionTable.setSelectionForeground(getBackgroundColor());
        pdfSelectionTable.setRowHeight(50);
        pdfSelectionTableScrollPane = new JScrollPane(pdfSelectionTable);
    }

    private void convertListToArray(ArrayList<String> locationList){
        for(int i=0;i<100;i++){
            selectedDocumentsFilePathsArray[i][0] = null;
        }
        int i = 0;
        for(String location: locationList){
            if(i >= 100) break;
            selectedDocumentsFilePathsArray[i][0] = location;
            ++i;
        }
    }

    private void createPdfSelectionFooter(){
        pdfSelectionFooterPanel = new JPanel();
        pdfSelectionFooterPanel.setBackground(getSecondaryColor());
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
                            "jpg","png","gif","bmp"
                    );
                    documentFileChooser.addChoosableFileFilter(pdfFilter);
                    documentFileChooser.addChoosableFileFilter(imageFilter);
                    documentFileChooser.setFileFilter(pdfFilter);
                    documentFileChooser.setMultiSelectionEnabled(true);
                    int isSelected = documentFileChooser.showOpenDialog(pdfSelectionWindow);
                    if(isSelected == JFileChooser.APPROVE_OPTION) {
                        File[] selectedDocumentFiles = documentFileChooser.getSelectedFiles();
                        for (File selectedDocumentFile : selectedDocumentFiles) {
                            String selectedDocumentFilePath = selectedDocumentFile.getAbsolutePath();
                            if (pdfFilter.accept(selectedDocumentFile)) {
                                selectedDocumentsFilePathsList.add(selectedDocumentFilePath);
                            } else if (imageFilter.accept(selectedDocumentFile)) {
                                String convertedI2pPath = pdfConverter.convertImageToPdf(selectedDocumentFilePath);
                                if (convertedI2pPath != null) {
                                    selectedDocumentsFilePathsList.add(convertedI2pPath);
                                }
                            } else {
                                showErrorMessage("Invalid file type.", pdfSelectionWindow);
                            }
                        }
                        convertListToArray(selectedDocumentsFilePathsList);
                        pdfSelectionWindow.repaint();
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
                    int selectedFilePathIndex = pdfSelectionTable.getSelectedRow();
                    if(selectedFilePathIndex != -1){
                        selectedDocumentsFilePathsList.remove(selectedFilePathIndex);
                        convertListToArray(selectedDocumentsFilePathsList);
                        pdfSelectionTable.repaint();
                    }
                }
        );
    }

    private void createMergeButton(){
        mergeButton = new JButton();
        mergeButton.setMaximumSize(new Dimension(100,35));
        mergeButton.setText("Merge");
        mergeButton.setForeground(getPrimaryColor());
        mergeButton.setFocusable(false);
        mergeButton.setFont(getRegularFont());
        mergeButton.addActionListener(
                e -> {
                    try{
                        if(selectedDocumentsFilePathsList.isEmpty()) return;
                        Executor mergeOperationExecutor = Executors.newSingleThreadExecutor();
                        mergeOperationExecutor.execute(this::mergeAllSelectedDocuments);
                        pdfSelectionWindow.dispose();
                    }
                    catch (Exception ex){
                        showErrorMessage(ex.getMessage(),pdfSelectionWindow);
                    }
                }
        );
    }

    private void mergeAllSelectedDocuments() {
        createMergeProgressDialog();
        PdfMergerUtil pdfMergerUtil = new PdfMergerUtil();
        String mergedPdfFilePath = pdfMergerUtil.mergePdf(selectedDocumentsFilePathsList);
        homeScreen.setSelectedDocumentFilePath(mergedPdfFilePath);
        homeScreen.getPdfViewer().openPdf(mergedPdfFilePath);
        closeMergeProgressDialog();
    }

    private void createMergeProgressDialog(){
        mergeProgressDialog = new JDialog();
        mergeProgressDialog.setTitle("Signing");
        mergeProgressDialog.setIconImage(dohatecLogo.getImage());
        mergeProgressDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mergeProgressDialog.setSize(300,125);
        mergeProgressDialog.getContentPane().setBackground(getBackgroundColor());
        mergeProgressDialog.getContentPane().setForeground(getPrimaryColor());

        mergeProgressPanel = new JPanel();
        mergeProgressPanel.setBackground(getBackgroundColor());

        mergeProgressLabel = new JLabel();
        mergeProgressLabel.setIcon(loadingIcon);
        mergeProgressLabel.setText("Merging documents. Please wait...");
        mergeProgressLabel.setIconTextGap(5);

        mergeProgressPanel.add(mergeProgressLabel);
        mergeProgressDialog.getContentPane().add(mergeProgressPanel);
        mergeProgressDialog.setLocationRelativeTo(null);
        mergeProgressDialog.setVisible(true);
    }

    private void closeMergeProgressDialog(){
        mergeProgressDialog.dispose();
    }
}
