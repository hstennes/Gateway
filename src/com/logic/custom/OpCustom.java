package com.logic.custom;

import com.logic.components.*;
import com.logic.engine.LogicEngine;
import com.logic.files.FileComponent;
import com.logic.ui.Renderer;
import com.logic.util.Constants;
import com.logic.util.CustomHelper;
import com.logic.util.CustomInput;
import com.logic.util.CustomOutput;

import javax.crypto.spec.PSource;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OpCustom extends LComponent {

    private NodeBox nodeBox;

    private String label;

    private int typeID;

    private final int width, height;

    public OpCustom(int x, int y, String label, LComponent[][] content, ArrayList<LComponent> lcomps, int typeID) {
        super(x, y, CompType.CUSTOM);
        this.label = label;
        this.typeID=  typeID;

        CustomHelper helper = new CustomHelper(content);
        width = helper.chooseWidth(label, Renderer.CUSTOM_LABEL_FONT);
        height = helper.chooseHeight();

        //maps components to ID values, ignoring lights and other components that will not be included in the NodeBox. This should be the same
        //as the indexes of the components in nodeComps, and the same components are included in both
        Map<LComponent, Integer> compIndex = new HashMap<>();

        //the list of all components that will be converted to nodes. Starts with all Switches in the order that input connections will be considered
        ArrayList<LComponent> nodeComps = new ArrayList<>();

        //A list of lights. Currently serves no purpose besides tracking the number of lights that have been added. Will optimize later.
        ArrayList<Light> lights = new ArrayList<>();

        //The index of the output connection corresponding to each Light. Used for creating outNodes array, which tells NodeBox how to set the output connections
        //based on Node states at the end of the update method.
        Map<Light, Integer> lightIndex = new HashMap<>();

        //Loops through sides of content array, uses same format as old Custom
        for(int s = Constants.RIGHT; s <= Constants.UP; s++) {
            LComponent[] side = content[s];
            if(side == null) continue;
            Point[] connectionPoints = helper.getConnectionPoints(s, width, height);
            for(int i = 0; i < side.length; i++) {
                if(side[i] instanceof Switch) {
                    //Add connection
                    int connectionIndex = io.addConnection(connectionPoints[i].x, connectionPoints[i].y, Connection.INPUT, s);
                    io.inputConnection(connectionIndex).changeBitWidth(((Switch) side[i]).getBitWidth());

                    //Add lcomp to list and keep track of index with map
                    compIndex.put(side[i], nodeComps.size());
                    nodeComps.add(side[i]);
                }
                else if(side[i] instanceof Light) {
                    //Add connection
                    int connectionIndex = io.addConnection(connectionPoints[i].x, connectionPoints[i].y, Connection.OUTPUT, s);
                    io.outputConnection(connectionIndex).changeBitWidth(((Light) side[i]).getBitWidth());

                    //Add to Lights array and keep track of index, which should also correspond to the index of the output connection
                    lightIndex.put((Light) side[i], lights.size());
                    lights.add((Light) side[i]);
                }
            }
        }

        //loop through all lcomps to fill out nodeComps
        for (LComponent lcomp : lcomps) {
            //Ignore Lights because they have no nodes. Ignore Switches because they were already added. Will also ignore other components in the future.
            if (lcomp instanceof Light || lcomp instanceof Switch) continue;

            //Add lcomp to list and keep track of index with map
            compIndex.put(lcomp, nodeComps.size());
            nodeComps.add(lcomp);
        }

        //final nodes array that goes to NodeBox
        Node[] nodes = new Node[nodeComps.size()];

        //final outNodes array. Goes in order of connections. Alternates component id, output connection number on that component
        int[] outNodes = new int[lights.size() * 2];

        for(int i = 0; i < nodes.length; i++){
            LComponent lcomp = nodeComps.get(i);

            if(lcomp instanceof BasicGate){
                IOManager io = lcomp.getIO();
                int[] in = new int[io.getNumInputs() * 2];
                for(int n = 0; n < io.getNumInputs(); n++){
                    InputPin inputPin = io.inputConnection(n);
                    if(inputPin.numWires() > 0) {
                        OutputPin source = inputPin.getWire().getSourceConnection();
                        in[2 * n] = compIndex.get(source.getLcomp());
                        in[2 * n + 1] = source.getIndex();
                    }
                    else{
                        in[2 * n] = -1;
                        in[2 * n + 1] = -1;
                    }
                }

                OutputPin outputPin = io.outputConnection(0);
                int[] out = checkAndSetOutputs(lcomp, outputPin, compIndex, lightIndex, outNodes);
                nodes[i] = new BasicGateNode(in, out, outputPin.getSignal(), lcomp.getType());
            }
            else if(lcomp instanceof SingleInputGate){
                IOManager io = lcomp.getIO();
                InputPin inputPin = io.inputConnection(0);
                int in = -1, inOut = -1;
                if(inputPin.numWires() > 0) {
                    OutputPin source = inputPin.getWire().getSourceConnection();
                    in = compIndex.get(source.getLcomp());
                    inOut = source.getIndex();
                }

                OutputPin outputPin = io.outputConnection(0);
                int[] out = checkAndSetOutputs(lcomp, outputPin, compIndex, lightIndex, outNodes);
                nodes[i] = new SingleInputGateNode(in, inOut, out, outputPin.getSignal(), lcomp.getType());
            }
            else if(lcomp instanceof OpCustom){
                //die
                NodeBox box = ((OpCustom) lcomp).getNodeBox().duplicate();

                IOManager io = lcomp.getIO();
                int[] in = new int[io.getNumInputs() * 2];
                for(int n = 0; n < io.getNumInputs(); n++){
                    InputPin inputPin = io.inputConnection(n);
                    if(inputPin.numWires() > 0) {
                        OutputPin source = inputPin.getWire().getSourceConnection();
                        in[2 * n] = compIndex.get(source.getLcomp());
                        in[2 * n + 1] = source.getIndex();
                    }
                    else{
                        in[2 * n] = -1;
                        in[2 * n + 1] = -1;
                    }
                }

                int[][] out = new int[io.getNumOutputs()][];
                int[] signal = new int[io.getNumOutputs()];
                for(int n = 0; n < io.getNumOutputs(); n++) {
                    OutputPin outputPin = io.outputConnection(n);
                    out[n] = checkAndSetOutputs(lcomp, outputPin, compIndex, lightIndex, outNodes);
                    signal[n] = outputPin.getSignal();
                }
                box.connect(in, out, signal);
                nodes[i] = box;
            }
            else if(lcomp instanceof SplitIn){
                IOManager io = lcomp.getIO();
                int[] in = new int[io.getNumInputs() * 2];
                for(int n = 0; n < io.getNumInputs(); n++){
                    InputPin inputPin = io.inputConnection(n);
                    if(inputPin.numWires() > 0) {
                        OutputPin source = inputPin.getWire().getSourceConnection();
                        in[2 * n] = compIndex.get(source.getLcomp());
                        in[2 * n + 1] = source.getIndex();
                    }
                    else{
                        in[2 * n] = -1;
                        in[2 * n + 1] = -1;
                    }
                }

                OutputPin outputPin = io.outputConnection(0);
                int[] out = checkAndSetOutputs(lcomp, outputPin, compIndex, lightIndex, outNodes);
                nodes[i] = new SplitInNode(((SplitIn) lcomp).getSplit(), in, out, outputPin.getSignal());
            }
            else if(lcomp instanceof SplitOut){
                IOManager io = lcomp.getIO();
                InputPin inputPin = io.inputConnection(0);
                int in = -1, inOut = -1;
                if(inputPin.numWires() > 0) {
                    OutputPin source = inputPin.getWire().getSourceConnection();
                    in = compIndex.get(source.getLcomp());
                    inOut = source.getIndex();
                }

                int[][] out = new int[io.getNumOutputs()][];
                int[] signal = new int[io.getNumOutputs()];
                for(int n = 0; n < io.getNumOutputs(); n++) {
                    OutputPin outputPin = io.outputConnection(n);
                    out[n] = checkAndSetOutputs(lcomp, outputPin, compIndex, lightIndex, outNodes);
                    signal[n] = outputPin.getSignal();
                }

                nodes[i] = new SplitOutNode(((SplitOut) lcomp).getSplit(), in, inOut, out, signal);
            }
            else if(lcomp instanceof Switch){
                OutputPin outputPin = lcomp.getIO().outputConnection(0);
                int[] out = checkAndSetOutputs(lcomp, outputPin, compIndex, lightIndex, outNodes);
                nodes[i] = new StartNode(out);
            }
        }

        int[] signal = new int[io.getNumOutputs()];
        for(int i  = 0; i < signal.length; i++){
            signal[i] = io.outputConnection(i).getSignal();
        }

        nodeBox = new NodeBox(nodes, outNodes, signal);
    }

    private OpCustom(int x, int y, NodeBox nodeBox, String label, int typeID, int width, int height){
        super(x, y, CompType.CUSTOM);
        this.nodeBox = nodeBox;
        this.label = label;
        this.typeID = typeID;
        this.width = width;
        this.height = height;
    }

    /**
     * Gets the output array (list of connected component IDs) for the given OutputPin. If some connected components are Lights, which correspond to
     * the outputs of this Custom component, then the lightIndex list is used to get the index of that output connection, which is used to place the correct data
     * in the outNodes array.
     * @param lcomp The LComponent, needed if this component is going to be a part of outIndex
     * @param outputPin The pin on the component being considered
     * @param compIndex The compIndex map
     * @param lightIndex The lightIndex map
     * @param outNodes The outNodes array, which will be modified if necessary
     * @return The out array for this connection, which holds connected node IDs
     */
    private int[] checkAndSetOutputs(LComponent lcomp, OutputPin outputPin, Map<LComponent, Integer> compIndex, Map<Light, Integer> lightIndex, int[] outNodes){
        ArrayList<LComponent> connected = new ArrayList<>();
        for(int n = 0; n < outputPin.numWires(); n++){
            LComponent dest = outputPin.getWire(n).getDestConnection().getLcomp();
            if(compIndex.containsKey(dest))
                connected.add(dest);
            else if(lightIndex.containsKey(dest)){
                int index = lightIndex.get(dest);
                outNodes[2 * index] = compIndex.get(lcomp);
                outNodes[2 * index + 1] = outputPin.getIndex();
            }
        }

        int[] out = new int[connected.size()];
        for(int n = 0; n < out.length; n++){
            LComponent dest = outputPin.getWire(n).getDestConnection().getLcomp();
            out[n] = compIndex.get(dest);
        }
        return out;
    }

    @Override
    public void update(LogicEngine engine) {
        int[] inputs = new int[io.getNumInputs()];
        for(int i = 0; i < inputs.length; i++){
            inputs[i] = io.getInput(i);
        }
        nodeBox.update(inputs);

        for(int i = 0; i < io.getNumOutputs(); i++){
            io.setOutput(i, nodeBox.getSignal(i), engine);
        }
    }

    /**
     * Returns a bounding box for this component based on its connections, which determine the shape of a custom component
     * @return A bounding box for the component
     */
    @Override
    public Rectangle getBounds() {
        if(rotation == Constants.UP || rotation == Constants.DOWN) return new Rectangle(x, y, height, width);
        else return new Rectangle(x, y, width, height);
    }

    /**
     * Returns a bounding box for this component when it is facing in a rightward direction
     * @return A bounding box for the component
     */
    @Override
    public Rectangle getBoundsRight() {
        return new Rectangle(0, 0, width, height);
    }

    @Override
    public LComponent makeCopy() {
        OpCustom result = new OpCustom(x, y, nodeBox.duplicate(), label, typeID, width, height);
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

    public String getLabel(){
        return label;
    }

    public NodeBox getNodeBox(){
        return nodeBox;
    }
}
