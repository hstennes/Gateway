package com.logic.custom;

import com.logic.components.*;
import com.logic.engine.LogicEngine;
import com.logic.engine.LogicWorker;
import com.logic.ui.CircuitPanel;
import com.logic.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class OpCustom2 extends LComponent {

    private CustomType type;

    private SignalProvider sp;

    private Timer[] timers;

    public OpCustom2(int x, int y, CustomType type) {
        super(x, y, CompType.CUSTOM);
        this.type = type;
        sp = type.defaultSP;
        initConnections(type, type.getIOStructure());
        timers = new Timer[type.clocks.size()];
    }

    public OpCustom2(int x, int y, CustomType type, SignalProvider sp){
        super(x, y, CompType.CUSTOM);
        this.type = type;
        this.sp = sp;
        initConnections(type, type.getIOStructure());
        timers = new Timer[type.clocks.size()];
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

    public void start(CircuitPanel cp){
        for(int i = 0; i < timers.length; i++) {
            if (timers[i] != null) timers[i].stop();
            int[] clockInfo = type.clocks.get(i);
            timers[i] = new Timer(clockInfo[0], e -> {
                int[] address = new int[clockInfo.length - 1];
                System.arraycopy(clockInfo, 1, address, 0, address.length);
                int[] clockSignal = sp.getNestedSignal(address);
                clockSignal[0] = ~clockSignal[0];
                LogicWorker.startLogic(this);
                cp.repaint();
            });
            timers[i].start();
        }
    }

    public void stop(){
        for(Timer timer : timers) timer.stop();
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
        return result;
    }

    public ArrayList<LComponent> projectInnerStateToType(){
        type.projectInnerState(this);
        return type.lcomps;
    }

    public CustomType getCustomType(){
        return type;
    }

    public SignalProvider getSignalProvider() {
        return sp;
    }
}
