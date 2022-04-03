package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

import java.awt.*;
import java.util.HashSet;

public class Screen extends LComponent{

    public final static int ADDR = 16384;

    public final static int PX_WIDTH = 512;

    public final static int PX_HEIGHT = 256;

    public final static int PADDING = 50;

    private final static int WIDTH = 2 * PADDING + 2 * PX_WIDTH;

    private final static int HEIGHT = 2 * PADDING + 2 * PX_HEIGHT;

    private HashSet<Integer> ramUpdates;

    private boolean prevRamNull = true;

    private boolean fullRedraw = false;

    public Screen(int x, int y) {
        super(x, y, CompType.SCREEN);
        io.addConnection(-25, HEIGHT / 2, Connection.INPUT, Constants.LEFT);
        io.inputConnection(0).setSpecial(Connection.SCREEN);
        ramUpdates = new HashSet<>();
    }

    @Override
    public void update(LogicEngine logicEngine) {
        RAM ram = getRamIfExists();
        if(ram != null) {
            if(prevRamNull) fullRedraw = true;
            else ramUpdates.add(ram.getCurrentAddress());
            prevRamNull = false;
        }
        else {
            if(!prevRamNull) fullRedraw = true;
            ramUpdates.clear();
            prevRamNull = true;
        }
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

    public RAM getRamIfExists(){
        InputPin inputPin = io.inputConnection(0);
        LComponent connected = null;
        if(inputPin.numWires() > 0){
            OutputPin source = inputPin.getWire().getSourceConnection();
            if(source != null) connected = source.getLcomp();
        }
        if(connected instanceof RAM) return (RAM) connected;
        return null;
    }

    public boolean mustFullRedraw(){
        return fullRedraw;
    }

    public void didFullRedraw(){
        fullRedraw = false;
    }

    public HashSet<Integer> getRamUpdates(){
        return ramUpdates;
    }

    public void clearRamUpdates(){
        ramUpdates.clear();
    }
}
