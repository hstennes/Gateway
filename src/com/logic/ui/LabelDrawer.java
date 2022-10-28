package com.logic.ui;

import java.awt.*;

public class LabelDrawer {

    public static int START = 0;

    public static int CENTER = 1;

    public static int END = 2;

    private Font font;

    private Color bgColor;

    private int xMargin, yMargin;

    public LabelDrawer(Font font, Color bgColor, int xMargin, int yMargin){
        this.font = font;
        this.bgColor = bgColor;
        this.xMargin = xMargin;
        this.yMargin = yMargin;
    }

    public void render(Graphics2D g2d, int x, int y, int alignX, int alignY, String text){
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int tWidth = metrics.stringWidth(text);
        int tHeight = metrics.getHeight();
        int bWidth = tWidth + 2 * xMargin;
        int bHeight = tHeight + 2 * yMargin;

        int boxX = alignX == START ? x : alignX == CENTER ? x - bWidth / 2 : x - bWidth;
        int boxY = alignY == START ? y : alignY == CENTER ? y - bHeight / 2 : y - bHeight;
        int textX = boxX + xMargin;
        int textY = boxY + yMargin + metrics.getAscent();

        g2d.setColor(bgColor);
        g2d.fillRect(boxX, boxY, bWidth, bHeight);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(boxX, boxY, bWidth, bHeight);
        g2d.drawString(text, textX, textY);
    }

    /**
     * Calculates the position that should be passed to g.drawString to align the text to the given coordinate
     * @param g2d The Graphics2D object
     * @param x desired x position
     * @param y desired y position
     * @param alignX x alignment
     * @param alignY y alignment
     * @param text text to consider
     * @param textFont font
     * @return Text rendering position
     */
    public static Point calcTextPositioning(Graphics2D g2d, int x, int y, int alignX, int alignY, String text, Font textFont){
        FontMetrics metrics = g2d.getFontMetrics(textFont);
        int tWidth = metrics.stringWidth(text);
        int tHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int textX = alignX == START ? x : alignX == CENTER ? x - tWidth / 2 : x - tWidth;
        int textY = alignY == START ? y + ascent : alignY == CENTER ? (y + ascent) - tHeight / 2 : y - ascent - tHeight;
        return new Point(textX, textY);
    }
}
