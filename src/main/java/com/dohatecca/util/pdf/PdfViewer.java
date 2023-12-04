package com.dohatecca.util.pdf;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import javax.swing.*;

public class PdfViewer {
    private final SwingController controller = new SwingController();
    private final SwingViewBuilder factory = new SwingViewBuilder(controller);
    private final JPanel pdfViewerPanel = factory.buildViewerPanel();

    public JPanel getPdfViewerPanel() {
        controller.setToolBarVisible(false);
        controller.setPageViewMode(1,true);
        return pdfViewerPanel;
    }

    public void openPdf(String filePath) {
        controller.openDocument(filePath);
        controller.setToolBarVisible(false);
    }

    public int getCurrentPageNumber() {
        return controller.getCurrentPageNumber();
    }

    public void closePdf() {
        controller.closeDocument();
    }
}