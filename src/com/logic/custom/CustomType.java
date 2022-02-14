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

    /**
     * All inner components included in the custom chips
     */
    public final ArrayList<LComponent> lcomps;

    /**
     * Specifies how lights and switches correspond to connections on the chip. Format content[side number][index on side]
     */
    public final LComponent[][] content;

    /**
     * Maps components to their index in the Node system
     */
    public final Map<LComponent, Integer> nbIndex;

    /**
     * Holds an optimized representation of the components in the chip
     */
    public NodeBox2 nodeBox;

    /**
     * A SignalProvider based on the signals of the LComponents originally used to create the chip
     */
    public SignalProvider defaultSP;

    /**
     * The custom chip label
     */
    public final String label;

    /**
     * An ID value that corresponds to the index of this CustomType in the main CustomTypes list
     */
    public final int typeID;

    /**
     * Width and height of the custom chip in the UI
     */
    public final int width, height;

    public int[] defaultSignals;

    /**
     * The CustomHelper, which provides useful UI related functionality for the chip
     */
    public final CustomHelper helper;

    public int nestedAddressStart;

    /**
     * Each int[] corresponds to an individual clock nested at any level in the custom chip. Each int[] has the the format
     * {clock delay, ID in enclosing NodeBox, innermost spIndex, ...outermost spIndex}
     */
    public final ArrayList<int[]> clocks;

    public CustomType(String label, LComponent[][] content, ArrayList<LComponent> lcomps, int typeID){
        this.label = label;
        this.content = content;
        this.lcomps = lcomps;
        this.typeID = typeID;
        helper = new CustomHelper(content);
        width = helper.chooseWidth(label, Renderer.CUSTOM_LABEL_FONT);
        height = helper.chooseHeight();
        nbIndex = new HashMap<>();
        clocks = new ArrayList<>();
        init2();
    }

    private void init(){
        /*//the list of all components that will be converted to nodes. Starts with all Switches in the order that input connections will be considered
        ArrayList<LComponent> nodeComps = new ArrayList<>();
        //The index of the output connection corresponding to each Light.
        Map<Light, Integer> lightIndex = new HashMap<>();
        //First step: consider lights and switches and add to nodeComps and lightIndex
        int[] numConnect = mapIO(content, compIndex, nodeComps, lightIndex);

        for (LComponent lcomp : lcomps) {
            //Ignore Lights because they have no nodes. Ignore Switches because they were already added.
            if(lcomp instanceof Light || lcomp instanceof Switch) continue;
            if(lcomp instanceof OpCustom2) customCount++;
            compIndex.put(lcomp, nodeComps.size());
            nodeComps.add(lcomp);
        }

        //final nodes array that goes to NodeBox
        Node[] nodes = new Node[nodeComps.size()];
        //final outNodes array that goes to NodeBox
        int[] outNodes = new int[numConnect[1] * 2];
        //Collects signal providers from nested chips, which are used to construct the signal provider for this chip
        SignalProvider[] nested = new SignalProvider[customCount];
        //The top-level signals for the chip
        int[][] signals = new int[nodes.length][];

        int spIndexCounter = 0;
        for(int i = 0; i < nodes.length; i++){
            LComponent lcomp = nodeComps.get(i);
            int[] in = getNodeIn(lcomp, compIndex);
            int[][] out = getNodeOut(lcomp, compIndex, lightIndex, outNodes);
            signals[i] = getSignals(lcomp);

            if(lcomp instanceof BasicGate) nodes[i] = new BasicGateNode(in, out, lcomp.getType());
            //else if(lcomp instanceof SingleInputGate) nodes[i] = new SingleInputGateNode(in, out, lcomp.getType());
            else if(lcomp instanceof Switch) nodes[i] = new StartNode(in, out);
            //else if(lcomp instanceof SplitIn) nodes[i] = new SplitInNode(in, out, ((SplitIn) lcomp).getSplit());
            //else if(lcomp instanceof SplitOut) nodes[i] = new SplitOutNode(in, out, ((SplitOut) lcomp).getSplit());
            else if(lcomp instanceof Clock) {
                //clocks.add(new int[] {((Clock) lcomp).getDelay(), i});
                //nodes[i] = new ClockNode(in, out);
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
            //else nodes[i] = new PlaceholderNode(lcomp.getType(), in, out);
        }

        defaultSP = new SignalProvider(signals, nested);
        nodeBox = new NodeBox2(nodes, outNodes);*/
    }

    private void init2(){
        HashMap<LComponent, Integer> nbIndex = new HashMap<>();
        HashMap<LComponent, Integer> sigIndex = new HashMap<>();
        HashMap<OpCustom2, Integer> nestedIndex = new HashMap<>();

        //the list of all components that will be converted to nodes. Starts with all Switches in the order that input connections will be considered
        ArrayList<LComponent> nodeComps = new ArrayList<>();
        //The index of the output connection corresponding to each Light.
        Map<Light, Integer> lightIndex = new HashMap<>();
        //First step: consider lights and switches and add to nodeComps and lightIndex
        int[] numConnect = mapIO(content, nbIndex, sigIndex, lightIndex, nodeComps);

        int sigLength = numConnect[0] + 1;
        ArrayList<OpCustom2> customs = new ArrayList<>();

        for (LComponent lcomp : lcomps) {
            if(lcomp instanceof Light || lcomp instanceof Switch) continue;
            if(lcomp instanceof OpCustom2) customs.add((OpCustom2) lcomp);
            nbIndex.put(lcomp, nodeComps.size());
            sigIndex.put(lcomp, sigLength);
            nodeComps.add(lcomp);
            sigLength += lcomp.getIO().getNumOutputs();
        }
        nestedAddressStart = sigLength;

        for(OpCustom2 custom : customs){
            nestedIndex.put(custom, sigLength);
            sigLength += custom.getCustomType().defaultSignals.length;
        }

        //final nodes array that goes to NodeBox
        Node[] nodes = new Node[nodeComps.size()];
        //final outNodes array that goes to NodeBox
        int[] outNodes = new int[numConnect[1]];
        //All the signals
        int[] signals = new int[sigLength];

        for(int i = 0; i < nodes.length; i++){
            LComponent lcomp = nodeComps.get(i);
            int[] in = getNodeIn2(lcomp, sigIndex);
            int[][] mark = getMarkList(lcomp, nbIndex, sigIndex, lightIndex, outNodes);
            int address = sigIndex.get(lcomp);
            int[] lcompSignals = getSignals(lcomp);
            System.arraycopy(lcompSignals, 0, signals, sigIndex.get(lcomp), lcompSignals.length);

            if(lcomp instanceof BasicGate) nodes[i] = new BasicGateNode(in, mark, address, lcomp.getType());
            else if(lcomp instanceof SingleInputGate) nodes[i] = new SingleInputGateNode(in, mark, address, lcomp.getType());
            else if(lcomp instanceof Switch) nodes[i] = new StartNode(in, mark, address);
            else if(lcomp instanceof SplitIn) nodes[i] = new SplitInNode(in, mark, address, ((SplitIn) lcomp).getSplit());
            else if(lcomp instanceof SplitOut) nodes[i] = new SplitOutNode(in, mark, address, ((SplitOut) lcomp).getSplit());
            else if(lcomp instanceof Clock) {
                //clocks.add(new int[] {((Clock) lcomp).getDelay(), i});
                //nodes[i] = new ClockNode(in, out);
            }
            else if(lcomp instanceof OpCustom2) {
                OpCustom2 custom = (OpCustom2) lcomp;
                int nestedOffset = nestedIndex.get(custom);
                int[] innerSignals = custom.getSignals();
                nodes[i] = new CustomNode(in, mark, address, custom.getCustomType(), nestedOffset);
                System.arraycopy(innerSignals, 0, signals, nestedOffset, innerSignals.length);
                /*clocks.addAll(custom.getCustomType().clocks
                        .stream()
                        .map(oldClock -> nestClock(oldClock, spc))
                        .collect(Collectors.toList()));*/
                //TODO clocks are very, very broken
            }
            //else nodes[i] = new PlaceholderNode(lcomp.getType(), in, out);
        }

        defaultSignals = signals;
        nodeBox = new NodeBox2(nodes, outNodes);
    }

    public void projectInnerState(OpCustom2 custom){
        if(custom.getCustomType() != this) throw new IllegalArgumentException("Custom component supplied to projectInnerState must be of the same CompType");
        SignalProvider sp = custom.getSignalProvider();

        for(LComponent lcomp : lcomps){
            if(lcomp instanceof Light) continue;
            int id = nbIndex.get(lcomp);
            if(lcomp instanceof Switch) ((Switch) lcomp).setState(sp.getSignal(id, 0));
            IOManager io = lcomp.getIO();
            for(int i = 0; i < io.getNumOutputs(); i++){
                OutputPin outputPin = io.outputConnection(i);
                outputPin.setSignal(sp.getSignal(id, i));
            }
        }
    }

    private int[] mapIO(LComponent[][] content, Map<LComponent, Integer> nbIndex, Map<LComponent, Integer> sigIndex,  Map<Light, Integer> lightIndex, ArrayList<LComponent> nodeComps){
        int numInputs = 0, numOutputs = 0;
        for(int s = Constants.RIGHT; s <= Constants.UP; s++) {
            LComponent[] side = content[s];
            if(side == null) continue;
            for (LComponent lComponent : side) {
                if (lComponent instanceof Switch) {
                    nbIndex.put(lComponent, numInputs);
                    //signals[0] will be left as 0. Empty input connections are directed to this address.
                    sigIndex.put(lComponent, numInputs + 1);
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

    private int[] getNodeIn2(LComponent lcomp, Map<LComponent, Integer> sigIndex){
        IOManager io = lcomp.getIO();
        int[] in = new int[io.getNumInputs()];
        for(int n = 0; n < in.length; n++){
            InputPin inputPin = io.inputConnection(n);
            if(inputPin.numWires() > 0) {
                OutputPin source = inputPin.getWire().getSourceConnection();
                in[n] = sigIndex.get(source.getLcomp()) + source.getIndex();
            }
            else in[n] = 0;
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

    private int[][] getMarkList(LComponent lcomp, Map<LComponent, Integer> nbIndex, Map<LComponent, Integer> sigIndex, Map<Light, Integer> lightIndex, int[] outNodes){
        IOManager io = lcomp.getIO();
        int[][] mark = new int[io.getNumOutputs()][];
        for(int n = 0; n < io.getNumOutputs(); n++){
            OutputPin outputPin = io.outputConnection(n);

            ArrayList<LComponent> connected = new ArrayList<>();
            for(int w = 0; w < outputPin.numWires(); w++){
                LComponent dest = outputPin.getWire(w).getDestConnection().getLcomp();
                if(nbIndex.containsKey(dest))
                    connected.add(dest);
                else if(lightIndex.containsKey(dest)){
                    int index = lightIndex.get(dest);
                    outNodes[index] = sigIndex.get(lcomp) + outputPin.getIndex();
                }
            }

            mark[n] = new int[connected.size()];
            for(int o = 0; o < mark[n].length; o++) mark[n][o] = nbIndex.get(connected.get(o));
        }
        return mark;
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

    /**
     * Follows same format as CustomType.content, but the ints represent the bit width of each connection
     * @return IO structure array
     */
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

    public Node getNode(LComponent lcomp){
        return nodeBox.getNodes()[nbIndex.get(lcomp)];
    }
}
