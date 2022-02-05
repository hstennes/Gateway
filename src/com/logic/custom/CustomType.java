package com.logic.custom;

import com.logic.components.*;
import com.logic.ui.Renderer;
import com.logic.util.Constants;
import com.logic.util.CustomHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomType {

    public final ArrayList<LComponent> lcomps;

    public final LComponent[][] content;

    public final String label;

    public final int typeID;

    public final NodeBox2 nodeBox;

    public final int width, height;

    public final CustomHelper helper;

    public final ArraySignalProvider defaultSP;

    public CustomType(String label, LComponent[][] content, ArrayList<LComponent> lcomps, int typeID){
        this.label = label;
        this.content = content;
        this.lcomps = lcomps;
        this.typeID = typeID;
        helper = new CustomHelper(content);
        width = helper.chooseWidth(label, Renderer.CUSTOM_LABEL_FONT);
        height = helper.chooseHeight();

        //maps components to ID values. This should be the same as the indexes of the components in nodeComps.
        Map<LComponent, Integer> compIndex = new HashMap<>();
        //the list of all components that will be converted to nodes. Starts with all Switches in the order that input connections will be considered
        ArrayList<LComponent> nodeComps = new ArrayList<>();
        //The index of the output connection corresponding to each Light. Used for creating outNodes array, which tells NodeBox how to set the output connections
        //based on Node states at the end of the update method.
        Map<Light, Integer> lightIndex = new HashMap<>();
        //Initialize connections, modifying the above 3 objects in the process
        int[] numConnect = initConnections(content, compIndex, nodeComps, lightIndex);
        int customCount = 0;

        for (LComponent lcomp : lcomps) {
            //Ignore Lights because they have no nodes. Ignore Switches because they were already added.
            if(lcomp instanceof Light || lcomp instanceof Switch) continue;
            if(lcomp instanceof OpCustom2) customCount++;
            compIndex.put(lcomp, nodeComps.size());
            nodeComps.add(lcomp);
        }

        //final nodes array that goes to NodeBox
        Node[] nodes = new Node[nodeComps.size()];
        //final outNodes array. Goes in order of connections. Alternates component id, output connection number on that component
        int[] outNodes = new int[numConnect[1] * 2];
        ArraySignalProvider[] nested = new ArraySignalProvider[customCount];
        int[][] signals = new int[nodes.length][];

        int spIndexCounter = 0;

        for(int i = 0; i < nodes.length; i++){
            LComponent lcomp = nodeComps.get(i);
            int[] in = getNodeIn(lcomp, compIndex);
            int[][] out = getNodeOut(lcomp, compIndex, lightIndex, outNodes);
            signals[i] = getSignals(lcomp);

            if(lcomp instanceof BasicGate) nodes[i] = new BasicGateNode(in, out, lcomp.getType());
            else if(lcomp instanceof SingleInputGate) nodes[i] = new SingleInputGateNode(in, out, lcomp.getType());
            else if(lcomp instanceof Switch) nodes[i] = new StartNode(in, out);
            else if(lcomp instanceof SplitIn) nodes[i] = new SplitInNode(in, out, ((SplitIn) lcomp).getSplit());
            else if(lcomp instanceof SplitOut) nodes[i] = new SplitOutNode(in, out, ((SplitOut) lcomp).getSplit());
            else if(lcomp instanceof OpCustom2) {
                OpCustom2 custom = (OpCustom2) lcomp;
                nodes[i] = custom.getCustomType().nodeBox.duplicate(in, out, spIndexCounter);
                nested[spIndexCounter] = custom.getNestedSP(0).duplicate();
                spIndexCounter++;
            }
            else nodes[i] = new PlaceholderNode(lcomp.getType(), in, out);
        }

        int[] in = new int[numConnect[0] * 2];
        for(int i = 0; i < numConnect[0]; i++){
            in[2 * i] = i;
            in[2 * i + 1] = 0;
        }

        defaultSP = new ArraySignalProvider(signals, nested);
        nodeBox = new NodeBox2(in, nodes, outNodes);
    }

    /**
     * Initializes connections and adds data to compIndex, nodeComps, and lightIndex
     * @param content The content array
     * @param compIndex The compIndex map
     * @param nodeComps The nodeComps list
     * @param lightIndex The lightIndex map
     */
    private int[] initConnections(LComponent[][] content, Map<LComponent, Integer> compIndex, ArrayList<LComponent> nodeComps, Map<Light, Integer> lightIndex){
        int numInputs = 0, numOutputs = 0;
        for(int s = Constants.RIGHT; s <= Constants.UP; s++) {
            LComponent[] side = content[s];
            if(side == null) continue;

            for (LComponent lComponent : side) {
                if (lComponent instanceof Switch) {
                    compIndex.put(lComponent, nodeComps.size());
                    nodeComps.add(lComponent);
                    numInputs++;
                }
                else if (lComponent instanceof Light) {
                    lightIndex.put((Light) lComponent, numOutputs);
                    numOutputs++;
                }
            }
        }
        return new int[] {numInputs, numOutputs};
    }

    public int[] getSignals(LComponent lcomp){
        IOManager io = lcomp.getIO();
        int[] signals = new int[io.getNumOutputs()];
        for(int i = 0; i < signals.length; i++){
            signals[i] = io.outputConnection(i).getSignal();
        }
        return signals;
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

    public int[][] getIOStructure(){
        int[][] io = new int[4][];
        for(int i = Constants.RIGHT; i <= Constants.UP; i++){
            io[i] = new int[content[i].length];
            for(int j = 0; j < content[i].length; j++) {
                if(content[i][j] instanceof Light)
                    io[i][j] = content[i][j].getIO().inputConnection(0).getBitWidth();
                else
                    io[i][j] = -content[i][j].getIO().outputConnection(0).getBitWidth();
            }
        }
        return io;
    }

    public ArrayList<LComponent> getInnerComps() {
        return lcomps;
    }

    public LComponent[][] getContent() {
        return content;
    }

    public String getLabel() {
        return label;
    }

    public int getTypeID() {
        return typeID;
    }

    public NodeBox2 getNodeBox(){
        return nodeBox;
    }

    public CustomHelper getHelper() {
        return helper;
    }
}
