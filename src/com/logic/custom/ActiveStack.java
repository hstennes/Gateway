package com.logic.custom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class ActiveStack implements Iterator<Integer> {

    private final Stack<Integer> activeA;

    private final Stack<Integer> activeB;

    private final Stack<LevelData> levelStack;

    private Stack<Integer> markStackPtr;

    private Stack<Integer> remainStackPtr;

    private boolean flip;

    private int markCount;

    private int remainCount;

    public ActiveStack(){
        activeA = new Stack<>();
        activeB = new Stack<>();
        levelStack = new Stack<>();

        markStackPtr = activeA;
        remainStackPtr = activeB;
    }

    public void startInner(){
        levelStack.push(new LevelData(markCount, remainCount, flip));
        markCount = 0;
        remainCount = 0;
        flip = false;
        markStackPtr = activeA;
        remainStackPtr = activeB;
    }

    public void finishInner(){
        LevelData ld = levelStack.pop();
        markCount = ld.markCount;
        remainCount = ld.remainCount;
        flip = ld.flip;
        if(flip){
            remainStackPtr = activeA;
            markStackPtr = activeB;
        }
        else{
            remainStackPtr = activeB;
            markStackPtr = activeA;
        }
    }

    public void mark(Integer[] n){
        markCount += n.length;
        for(Integer i : n) markStackPtr.push(i);
    }

    public boolean nextIteration(){
        if(markCount == 0) return false;
        remainCount = markCount;
        markCount = 0;

        //flip is true -> A is remain stack, B is mark stack
        flip = !flip;
        if(flip){
            remainStackPtr = activeA;
            markStackPtr = activeB;
        }
        else{
            remainStackPtr = activeB;
            markStackPtr = activeA;
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        return remainCount > 0;
    }

    @Override
    public Integer next() {
        remainCount--;
        return remainStackPtr.pop();
    }

    private static class LevelData{
        public int markCount;
        public int remainCount;
        public boolean flip;

        public LevelData(int markCount, int remainCount, boolean flip) {
            this.remainCount = remainCount;
            this.markCount = markCount;
            this.flip = flip;
        }
    }
}
