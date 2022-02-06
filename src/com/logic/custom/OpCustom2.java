package com.logic.custom;

import com.logic.components.*;
import com.logic.engine.LogicEngine;
import com.logic.ui.Renderer;
import com.logic.util.Constants;
import com.logic.util.CustomHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpCustom2 extends LComponent {

    private CustomType type;

    private ArraySignalProvider sp;

    public OpCustom2(int x, int y, CustomType type) {
        super(x, y, CompType.CUSTOM);
        this.type = type;
        this.sp = type.defaultSP.duplicate();
        initConnections(type, type.getIOStructure());
    }

    private OpCustom2(int x, int y, CustomType type, ArraySignalProvider sp){
        super(x, y, CompType.CUSTOM);
        this.type = type;
        this.sp = sp;
    }

    /**
     * Initializes connections and adds data to compIndex, nodeComps, and lightIndex
     * @param type The CustomType
     */
    private void initConnections(CustomType type, int[][] ioStructure){
        for(int s = Constants.RIGHT; s <= Constants.UP; s++) {
            int[] side = ioStructure[s];
            if(side == null) continue;
            Point[] connectionPoints = type.helper.getConnectionPoints(s, type.width, type.height);
            for(int i = 0; i < side.length; i++) {
                if(side[i] > 0) {
                    int connectionIndex = io.addConnection(connectionPoints[i].x, connectionPoints[i].y, Connection.OUTPUT, s);
                    io.outputConnection(connectionIndex).changeBitWidth(side[i]);
                }
                else{
                    int connectionIndex = io.addConnection(connectionPoints[i].x, connectionPoints[i].y, Connection.INPUT, s);
                    io.inputConnection(connectionIndex).changeBitWidth(-side[i]);
                }
            }
        }
    }

    @Override
    public void update(LogicEngine engine) {
        int[] inputs = new int[io.getNumInputs()];
        for(int i = 0; i < inputs.length; i++) inputs[i] = io.getInput(i);

        int[] outputs = type.nodeBox.update(sp, inputs);
        for(int i = 0; i < io.getNumOutputs(); i++) io.setOutput(i, outputs[i], engine);
    }

    /**
     * Returns a bounding box for this component based on its connections, which determine the shape of a custom component
     * @return A bounding box for the component
     */
    @Override
    public Rectangle getBounds() {
        if(rotation == Constants.UP || rotation == Constants.DOWN) return new Rectangle(x, y, type.height, type.width);
        else return new Rectangle(x, y, type.width, type.height);
    }

    /**
     * Returns a bounding box for this component when it is facing in a rightward direction
     * @return A bounding box for the component
     */
    @Override
    public Rectangle getBoundsRight() {
        return new Rectangle(0, 0, type.width, type.height);
    }

    @Override
    public LComponent makeCopy() {
        OpCustom2 result = new OpCustom2(x, y, type, sp.duplicate());
        result.setRotation(rotation);
        result.setName(getName());

        for(int i = 0; i < io.getNumInputs(); i++){
            Connection c = io.inputConnection(i);
            result.getIO().addConnection(c.getX(), c.getY(), Connection.INPUT, c.getDirection());
            result.getIO().inputConnection(i).changeBitWidth(c.getBitWidth());
        }

        for(int i = 0; i < io.getNumOutputs(); i++){
            Connection c = io.outputConnection(i);
            result.getIO().addConnection(c.getX(), c.getY(), Connection.OUTPUT, c.getDirection());
            result.getIO().outputConnection(i).changeBitWidth(c.getBitWidth());
        }

        return result;
    }

    public ArrayList<LComponent> projectInnerStateToType(){
        type.projectInnerState(this);
        return type.lcomps;
    }

    public CustomType getCustomType(){
        return type;
    }

    /*@Override
    public int getSignal(int node, int nodeOut) {
        if(node == io.getNumInputs()) return pendingOutput[nodeOut];
        return io.getInput(node);
    }

    @Override
    public void setSignal(int node, int nodeOut, int signal) {
        pendingOutput[nodeOut] = signal;
    }*/

    public ArraySignalProvider getSignalProvider() {
        return sp;
    }
}
