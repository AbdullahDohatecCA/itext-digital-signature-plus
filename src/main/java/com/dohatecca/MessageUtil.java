package com.dohatecca;

import javax.swing.*;
import java.awt.*;

public class MessageUtil {
    static void showWarningMessage(String message, Component parentComponent){
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }
    static void showErrorMessage(String message, Component parentComponent){
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
