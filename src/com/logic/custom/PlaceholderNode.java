package com.logic.custom;

import com.logic.components.CompType;

import java.util.ArrayList;

public class PlaceholderNode extends Node{

    private final CompType type;

    public PlaceholderNode(int[] in, int[][] mark, int address, CompType type){
        super(in, mark, address);
        this.type = type;
    }

    @Override
    public void updateEvent(int[] signals, int offset, ActiveStack active) { }

    @Override
    public void updateLCC(int[] signals, int offset, ActiveStack active) { }

    public CompType getType(){
        return type;
    }

    @Override
    public int getNumOutputs(){
        //TODO not accurate for displays and possibly other components
        return 1;
    }
}
