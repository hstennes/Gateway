package com.logic.ui;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class FontLoader {

    public Font sevenSegFont;

    public FontLoader(){
        sevenSegFont = getFont("/DSEG7Classic-Bold.ttf");
    }

    private Font getFont(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        try {
            return Font.createFont(Font.TRUETYPE_FONT, stream);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
