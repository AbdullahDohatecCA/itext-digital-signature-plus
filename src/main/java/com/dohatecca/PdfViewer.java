package com.dohatecca;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import javax.swing.*;

public class PdfViewer {
    private final SwingController controller = new SwingController();
    private final SwingViewBuilder factory = new SwingViewBuilder(controller);
    private final JPanel viewerPanel = factory.buildViewerPanel();

    public JPanel getPdfViewerPanel() {
        controller.setToolBarVisible(false);
        return viewerPanel;
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
