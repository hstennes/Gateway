package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

import java.awt.*;

public class Screen extends LComponent{

    public static int PX_WIDTH = 512;

    public static int PX_HEIGHT = 256;

    public static int PADDING = 50;

    private final static int WIDTH = 2 * PADDING + 2 * PX_WIDTH;

    private final static int HEIGHT = 2 * PADDING + 2 * PX_HEIGHT;

    public Screen(int x, int y) {
        super(x, y, CompType.SCREEN);
        io.addConnection(-25, HEIGHT / 2, Connection.INPUT, Constants.LEFT);
        io.inputConnection(0).setSpecial(Connection.SCREEN);
    }

    @Override
    public void update(LogicEngine logicEngine) {

    }

    @Override
    public LComponent makeCopy() {
        return null;
    }

    @Override
    public Rectangle getBounds() {
        if(rotation == Constants.UP || rotation == Constants.DOWN) return new Rectangle(x, y, HEIGHT, WIDTH);
        else return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    @Override
    public Rectangle getBoundsRight() {
        return new Rectangle(0, 0, WIDTH, HEIGHT);
    }
}
