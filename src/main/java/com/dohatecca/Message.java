package com.dohatecca;

import javax.swing.*;
import java.awt.*;

public class Message {
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

    static void showGeneralMessage(String message, Component parentComponent){
        JOptionPane.showMessageDialog(
                parentComponent,
                message,
                "Message",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    static String showQuestionMessage(String message, Component parentComponent){
        return JOptionPane.showInputDialog(
                parentComponent,
                message,
                "Question",
                JOptionPane.QUESTION_MESSAGE
        );
    }
}
