package com.logic.custom;

import com.logic.components.*;
import com.logic.ui.Renderer;
import com.logic.util.Constants;
import com.logic.util.CustomHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomType {

    //serialized
    public final ArrayList<LComponent> lcomps;

    //serialized
    public final LComponent[][] content;

    private final Map<LComponent, Integer> compIndex;

    public NodeBox2 nodeBox;

    public SignalProvider defaultSP;

    //serialized
    public final String label;

    //serialized
    public final int typeID;

    public final int width, height;

    public final CustomHelper helper;

    //first index shows delay
    //Other indexes show where the clock is by listing spIndexes from outermost to innermost
    //serialized
    public final ArrayList<int[]> clocks;

    public CustomType(String label, LComponent[][] content, ArrayList<LComponent> lcomps, int typeID){
        this.label = label;
        this.content = content;
        this.lcomps = lcomps;
        this.typeID = typeID;
        helper = new CustomHelper(content);
        width = helper.chooseWidth(label, Renderer.CUSTOM_LABEL_FONT);
        height = helper.chooseHeight();
        compIndex = new HashMap<>();
        clocks = new ArrayList<>();
        init();
    }

    private void init(){
        //maps components to ID values. This should be the same as the indexes of the components in nodeComps.

        //the list of all components that will be converted to nodes. Starts with all Switches in the order that input connections will be considered
        ArrayList<LComponent> nodeComps = new ArrayList<>();
        //The index of the output connection corresponding to each Light. Used for creating outNodes array, which tells NodeBox how to set the output connections
        //based on Node states at the end of the update method.
        Map<Light, Integer> lightIndex = new HashMap<>();
        //Initialize connections, modifying the above 3 objects in the process
        int[] numConnect = mapIO(content, compIndex, nodeComps, lightIndex);
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
        SignalProvider[] nested = new SignalProvider[customCount];
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
            else if(lcomp instanceof Clock) {
                clocks.add(new int[] {((Clock) lcomp).getDelay(), i});
                nodes[i] = new ClockNode(in, out);
            }
            else if(lcomp instanceof OpCustom2) {
                int spc = spIndexCounter;
                OpCustom2 custom = (OpCustom2) lcomp;
                nodes[i] = new CustomNode(in, out, custom.getCustomType(), spc);
                nested[spc] = custom.getSignalProvider().duplicate();
                clocks.addAll(custom.getCustomType().clocks
                        .stream()
                        .map(oldClock -> nestClock(oldClock, spc))
                        .collect(Collectors.toList()));
                spIndexCounter++;
            }
            else nodes[i] = new PlaceholderNode(lcomp.getType(), in, out);
        }

        defaultSP = new SignalProvider(signals, nested);
        nodeBox = new NodeBox2(nodes, outNodes);
    }

    public void projectInnerState(OpCustom2 custom){
        if(custom.getCustomType() != this) throw new IllegalArgumentException("Custom component supplied to projectInnerState must be of the same CompType");
        SignalProvider sp = custom.getSignalProvider();

        for(LComponent lcomp : lcomps){
            if(lcomp instanceof Light) continue;
            int id = compIndex.get(lcomp);
            if(lcomp instanceof Switch) ((Switch) lcomp).setState(sp.getSignal(id, 0));
            IOManager io = lcomp.getIO();
            for(int i = 0; i < io.getNumOutputs(); i++){
                OutputPin outputPin = io.outputConnection(i);
                outputPin.setSignal(sp.getSignal(id, i));
            }
        }
    }

    /**
     * Initializes connections and adds data to compIndex, nodeComps, and lightIndex
     * @param content The content array
     * @param compIndex The compIndex map
     * @param nodeComps The nodeComps list
     * @param lightIndex The lightIndex map
     */
    private int[] mapIO(LComponent[][] content, Map<LComponent, Integer> compIndex, ArrayList<LComponent> nodeComps, Map<Light, Integer> lightIndex){
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

    private int[] nestClock(int[] oldClock, int spIndex){
        int[] newClock = new int[oldClock.length + 1];
        newClock[0] = oldClock[0];
        newClock[1] = spIndex;
        System.arraycopy(oldClock, 1, newClock, 2, oldClock.length - 1);
        return newClock;
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
        for(int n = 0; n < out.length; n++) out[n] = compIndex.get(connected.get(n));
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
