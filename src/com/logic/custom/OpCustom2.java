package com.logic.custom;

import com.logic.components.CompType;
import com.logic.components.Connection;
import com.logic.components.LComponent;
import com.logic.engine.LogicEngine;
import com.logic.engine.LogicWorker;
import com.logic.ui.CircuitPanel;
import com.logic.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class OpCustom2 extends LComponent {

    private CustomType type;

    private int[] signals;

    private Timer[] timers;

    public OpCustom2(int x, int y, CustomType type) {
        super(x, y, CompType.CUSTOM);
        this.type = type;
        signals = type.defaultSignals;
        initConnections();
        timers = new Timer[type.clocks.size()];
    }

    public OpCustom2(int x, int y, CustomType type, int[] signals){
        super(x, y, CompType.CUSTOM);
        this.type = type;
        this.signals = signals;
        initConnections();
        timers = new Timer[type.clocks.size()];
    }

    /**
     * Initializes connections and adds data to compIndex, nodeComps, and lightIndex
     */
    private void initConnections(){
        int[][] ioStructure = type.getIOStructure();
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
        /*for(int i = 0; i < timers.length; i++) {
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
        }*/
        //TODO clocks are very, very broken
    }

    public void stop(){
        for(Timer timer : timers) timer.stop();
    }

    @Override
    public void update(LogicEngine engine) {
        //long start = System.nanoTime();

        int[] inputs = new int[io.getNumInputs()];
        for(int i = 0; i < inputs.length; i++) inputs[i] = io.getInput(i);

        int[] outputs = type.nodeBox.update(signals, inputs, 0, new ActiveStack());
        for(int i = 0; i < io.getNumOutputs(); i++) io.setOutput(i, outputs[i], engine);
        //if(type.label.equals("CPU1")) System.out.println("CPU TIME CYCLE TIME: " + (System.nanoTime() - start));
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
        int[] newSignals = new int[signals.length];
        System.arraycopy(signals, 0, newSignals, 0, signals.length);
        OpCustom2 result = new OpCustom2(x, y, type, newSignals);
        result.setRotation(rotation);
        result.setName(getName());
        return result;
    }

    public ArrayList<LComponent> projectInnerStateToType(){
        type.projectInnerState(this);
        return type.lcomps;
    }

    public int[] getSignals(){
        return signals;
    }

    public CustomType getCustomType(){
        return type;
    }

    /**
     * Makes necessary changes to the signal array if dependent chips have changed (this method must be called after the CustomType
     * is up-to-date). If the type was modified, the signals take their new default values from the type. If the type was rebuilt,
     * this component keeps its own top-level signals, but any inner signals are copied from type defaults.
     * @return True if action was required (the type was modified or rebuilt), false otherwise
     */
    public boolean invalidate(){
        if(type.didRebuild()) {
            /*Since the type rebuilt (due to modification of a dependency), the top level signals have maintained the same
            structure, but the nested signals have changed unpredictably. We can keep this instance's top level signals,
            but the nested signals must be discarded and reset to default values from the CustomType.*/
            int nestedAddr = type.getNestedAddr();
            int[] newSignals = new int[type.defaultSignals.length];;
            System.arraycopy(signals, 0, newSignals, 0, nestedAddr);
            System.arraycopy(type.defaultSignals,
                    nestedAddr,
                    newSignals,
                    nestedAddr,
                    type.defaultSignals.length - nestedAddr);
            signals = newSignals;
            return true;
        }
        else if(type.didModify()) {
            /*The type was modified, so the entirety of the signals array may have changed. This instance's signals array is
            reset to default values from the CustomType, which are now based on the live circuit the user edited.*/
            signals = new int[type.defaultSignals.length];
            System.arraycopy(type.defaultSignals, 0, signals, 0, signals.length);
            return true;
        }
        return false;
    }

    public void setSignalProvider(SignalProvider sp){ }
}
