package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

import java.awt.*;

public class ROM extends LComponent{

    public static int PROGRAM_SIZE = 32768;

    public static int WIDTH = 100, HEIGHT = 100;

    private int[] program;

    public ROM(int x, int y) {
        super(x, y, CompType.ROM);
        io.addConnection(-25, HEIGHT / 2, Connection.INPUT, Constants.LEFT);
        io.addConnection(WIDTH + 25, HEIGHT / 2, Connection.OUTPUT, Constants.RIGHT);
    }

    @Override
    public void update(LogicEngine engine) {
        int address = io.getInput(0);
        int output = 0;
        if(address < program.length) output = program[address];
        io.setOutput(0, output, engine);
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
        ROM newRom = new ROM(x, y);
        newRom.setProgram(program);
        return newRom;
    }

    public void setProgram(int[] program){
        this.program = program;
    }

    public int[] getProgram(){
        return program;
    }
}
