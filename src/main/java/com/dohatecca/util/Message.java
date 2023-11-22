package com.dohatecca.util;

import javax.swing.*;
import java.awt.*;

public class Message {
    public static void showWarningMessage(String message, Component parentComponent){
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }
    public static void showErrorMessage(String message, Component parentComponent){
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void showGeneralMessage(String message, Component parentComponent){
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Message",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static String showQuestionMessage(String message, Component parentComponent){
        return JOptionPane.showInputDialog(
                parentComponent,
                message,
                "Question",
                JOptionPane.QUESTION_MESSAGE
        );
    }
}
