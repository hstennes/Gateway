package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.test.Debug;
import com.logic.util.Constants;
import org.apache.bcel.classfile.ConstantNameAndType;

import java.awt.*;

public class RAM extends LComponent{

    public static int SIZE = 32768;

    public static int WIDTH = 150, HEIGHT = 80;

    private int[] data;

    private int prevClock;

    public RAM(int x, int y) {
        super(x, y, CompType.RAM);
        io.addConnection(WIDTH / 4, HEIGHT + 25, Connection.INPUT, Constants.DOWN);
        io.addConnection(WIDTH / 2, HEIGHT + 25, Connection.INPUT, Constants.DOWN);
        io.addConnection(3 * WIDTH / 4, HEIGHT + 25, Connection.INPUT, Constants.DOWN);
        io.addConnection(-25, HEIGHT / 2, Connection.INPUT, Constants.LEFT);
        io.addConnection(WIDTH + 25, HEIGHT / 2, Connection.OUTPUT, Constants.RIGHT);
        io.inputConnection(1).changeBitWidth(15);
        io.inputConnection(3).changeBitWidth(16);
        data = new int[SIZE];
    }

    @Override
    public void update(LogicEngine engine) {
        int clock = io.getInput(0);
        int address = io.getInput(1);
        int load = io.getInput(2);
        int value = io.getInput(3);

        if(address == 24010 && value == 65535) Debug.end("PONG_TEST");

        if(clock == 1 && prevClock == 0 && load == 1) data[address] = value;
        prevClock = clock;

        io.setOutput(0, data[address], engine);
    }

    public int getCurrentAddress(){
        return io.getInput(1);
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

    @Override
    public LComponent makeCopy() {
        return new RAM(x, y);
    }

    public void clear(){
        data = new int[SIZE];
    }

    public void setData(int[] data){
        this.data = data;
    }

    public int[] getData(){
        return data;
    }
}
