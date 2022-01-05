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
        //The index of the output connection corresponding to each Light. Used for creating outNodes array, which tells NodeBox how to set the output connections
        //based on Node states at the end of the update method.
        Map<Light, Integer> lightIndex = new HashMap<>();
        //Initialize connections, modifying the above 3 objects in the process
        initConnections(helper, content, compIndex, nodeComps, lightIndex);

        for (LComponent lcomp : lcomps) {
            //Ignore Lights because they have no nodes. Ignore Switches because they were already added. Will also ignore other components in the future.
            if (lcomp instanceof Light || lcomp instanceof Switch) continue;
            compIndex.put(lcomp, nodeComps.size());
            nodeComps.add(lcomp);
        }

        //final nodes array that goes to NodeBox
        Node[] nodes = new Node[nodeComps.size()];
        //final outNodes array. Goes in order of connections. Alternates component id, output connection number on that component
        int[] outNodes = new int[io.getNumOutputs() * 2];

        for(int i = 0; i < nodes.length; i++){
            LComponent lcomp = nodeComps.get(i);
            int[] in = getNodeIn(lcomp, compIndex);
            int[][] out = getNodeOut(lcomp, compIndex, lightIndex, outNodes);
            int[] signal = getSignal(lcomp);

            if(lcomp instanceof BasicGate) nodes[i] = new BasicGateNode(in, out[0], signal[0], lcomp.getType());
            else if(lcomp instanceof SingleInputGate) nodes[i] = new SingleInputGateNode(in[0], in[1], out[0], signal[0], lcomp.getType());
            else if(lcomp instanceof SplitIn) nodes[i] = new SplitInNode(((SplitIn) lcomp).getSplit(), in, out[0], signal[0]);
            else if(lcomp instanceof SplitOut) nodes[i] = new SplitOutNode(((SplitOut) lcomp).getSplit(), in[0], in[1], out, signal);
            else if(lcomp instanceof Switch) nodes[i] = new StartNode(out[0], signal[0]);
            else if(lcomp instanceof OpCustom){
                NodeBox box = ((OpCustom) lcomp).getNodeBox().duplicate();
                box.connect(in, out, signal);
                nodes[i] = box;
            }
        }

        int[] signal = getSignal(this);
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
     * Initializes connections and adds data to compIndex, nodeComps, and lightIndex
     * @param helper The CustomHelper
     * @param content The content array
     * @param compIndex The compIndex map
     * @param nodeComps The nodeComps list
     * @param lightIndex The lightIndex map
     */
    private void initConnections(CustomHelper helper, LComponent[][] content, Map<LComponent, Integer> compIndex, ArrayList<LComponent> nodeComps, Map<Light, Integer> lightIndex){
        int lightID = 0;
        for(int s = Constants.RIGHT; s <= Constants.UP; s++) {
            LComponent[] side = content[s];
            if(side == null) continue;
            Point[] connectionPoints = helper.getConnectionPoints(s, width, height);
            for(int i = 0; i < side.length; i++) {
                if(side[i] instanceof Switch) {
                    int connectionIndex = io.addConnection(connectionPoints[i].x, connectionPoints[i].y, Connection.INPUT, s);
                    io.inputConnection(connectionIndex).changeBitWidth(((Switch) side[i]).getBitWidth());
                    compIndex.put(side[i], nodeComps.size());
                    nodeComps.add(side[i]);
                }
                else if(side[i] instanceof Light) {
                    int connectionIndex = io.addConnection(connectionPoints[i].x, connectionPoints[i].y, Connection.OUTPUT, s);
                    io.outputConnection(connectionIndex).changeBitWidth(((Light) side[i]).getBitWidth());
                    lightIndex.put((Light) side[i], lightID);
                    lightID++;
                }
            }
        }
    }

    /**
     * Creates the node input array for the given component
     * @param lcomp The component
     * @param compIndex The compIndex map
     * @return The input array for the new Node
     */
    private int[] getNodeIn(LComponent lcomp, Map<LComponent, Integer> compIndex){
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
        return in;
    }

    /**
     * Creates the node output array for the given component. The outNodes array will be modified if the component is connected
     * to any output lights
     * @param lcomp The component
     * @param compIndex The component index map
     * @param lightIndex The light index map
     * @param outNodes The outNodes array (may be modified)
     * @return The out array for the new Node
     */
    private int[][] getNodeOut(LComponent lcomp, Map<LComponent, Integer> compIndex, Map<Light, Integer> lightIndex, int[] outNodes){
        IOManager io = lcomp.getIO();
        int[][] out = new int[io.getNumOutputs()][];
        for(int n = 0; n < io.getNumOutputs(); n++) {
            OutputPin outputPin = io.outputConnection(n);
            out[n] = checkAndSetOutputs(lcomp, outputPin, compIndex, lightIndex, outNodes);
        }
        return out;
    }

    /**
     * Gets the signal array from the outputs connections of the given LComponent
     * @param lcomp The LComponent
     * @return The signal array for the new Node
     */
    private int[] getSignal(LComponent lcomp){
        IOManager io = lcomp.getIO();
        int[] signal = new int[io.getNumOutputs()];
        for(int n = 0; n < io.getNumOutputs(); n++) signal[n] = io.outputConnection(n).getSignal();
        return signal;
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
