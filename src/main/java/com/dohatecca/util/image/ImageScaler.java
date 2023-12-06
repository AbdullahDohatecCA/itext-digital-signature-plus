package com.dohatecca.util.image;

import javax.swing.*;
import java.awt.*;

public class ImageScaler {
    public ImageIcon scaleImage(ImageIcon imageIcon, int horizontalScale, int verticalScale){
        Image image = imageIcon.getImage()
                .getScaledInstance(horizontalScale,verticalScale, Image.SCALE_DEFAULT);
        return new ImageIcon(image);
    }
}
