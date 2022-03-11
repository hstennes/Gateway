package com.logic.custom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class ActiveStack implements Iterator<Integer> {

    private Stack<Integer> activeA;

    private Stack<Integer> activeB;

    private Stack<Boolean> flipStack;

    private Stack<Integer> markCountStack;

    private Stack<Integer> remainCountStack;

    private Stack<Integer> markStackPtr;

    private Stack<Integer> remainStackPtr;

    private boolean flip;

    private int markCount;

    private int remainCount;

    public ActiveStack(){
        activeA = new Stack<>();
        activeB = new Stack<>();
        flipStack = new Stack<>();
        markCountStack = new Stack<>();
        remainCountStack = new Stack<>();

        markStackPtr = activeA;
        remainStackPtr = activeB;
    }

    public void startInner(){
        markCountStack.push(markCount);
        remainCountStack.push(remainCount);
        flipStack.push(flip);
        markCount = 0;
        remainCount = 0;
        flip = false;
        markStackPtr = activeA;
        remainStackPtr = activeB;
    }

    public void finishInner(){
        markCount = markCountStack.pop();
        remainCount = remainCountStack.pop();
        flip = flipStack.pop();
        if(flip){
            remainStackPtr = activeA;
            markStackPtr = activeB;
        }
        else{
            remainStackPtr = activeB;
            markStackPtr = activeA;
        }
    }

    public void mark(int[] n){
        markCount += n.length;
        for(int i : n) markStackPtr.push(i);
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
}
