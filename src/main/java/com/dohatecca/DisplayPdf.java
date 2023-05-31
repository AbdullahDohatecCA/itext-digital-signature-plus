package com.dohatecca;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import javax.swing.*;

public class DisplayPdf {
    private final SwingController controller = new SwingController();
    private final SwingViewBuilder factory = new SwingViewBuilder(controller);
    private final JPanel viewerPanel = factory.buildViewerPanel();

    public JPanel getPdfViewerPanel() {
        controller.setToolBarVisible(false);
        return viewerPanel;
    }

    public void showPdf(String filePath) {
        controller.openDocument(filePath);
        controller.setToolBarVisible(false);
    }
}
