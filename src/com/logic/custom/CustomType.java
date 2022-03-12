package com.logic.custom;

import com.logic.components.*;
import com.logic.ui.Renderer;
import com.logic.util.Constants;
import com.logic.util.CustomHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomType {

    /**
     * All inner components included in the custom chips
     */
    public ArrayList<LComponent> lcomps;

    private ArrayList<OpCustom2> customs;

    /**
     * Specifies how lights and switches correspond to connections on the chip. Format content[side number][index on side]
     */
    public final LComponent[][] content;

    /**
     * Maps components to their index in the Node system
     */
    private final Map<LComponent, Integer> nbIndex;

    /**
     * Holds an optimized representation of the components in the chip
     */
    public NodeBox2 nodeBox;

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

    /**
     * Each int[] corresponds to an individual clock nested at any level in the custom chip. Each int[] has the the format
     * {clock delay, ID in enclosing NodeBox, innermost spIndex, ...outermost spIndex}
     */
    public final ArrayList<int[]> clocks;

    private int nestedAddressStart;

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
        init();
    }

    private void init(){
        HashMap<LComponent, Integer> sigIndex = new HashMap<>();
        HashMap<OpCustom2, Integer> nestedIndex = new HashMap<>();

        //the list of all components that will be converted to nodes. Starts with all Switches in the order that input connections will be considered
        ArrayList<LComponent> nodeComps = new ArrayList<>();
        //The index of the output connection corresponding to each Light.
        Map<Light, Integer> lightIndex = new HashMap<>();
        //First step: consider lights and switches and add to nodeComps and lightIndex
        int[] numConnect = mapIO(content, nbIndex, sigIndex, lightIndex, nodeComps);

        int sigLength = numConnect[0] + 1;
        customs = new ArrayList<>();

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
            int[] in = getNodeIn(lcomp, sigIndex);
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
            else nodes[i] = new PlaceholderNode(in, mark, address, lcomp.getType());
        }

        defaultSignals = signals;
        nodeBox = new NodeBox2(nodes, outNodes);
    }

    public void modify(HashMap<LComponent, LComponent> oldToNew){
        this.lcomps = new ArrayList<>(oldToNew.values());
        for(int s = Constants.RIGHT; s <= Constants.UP; s++) {
            LComponent[] side = content[s];
            for(int i = 0; i < side.length; i++) side[i] = oldToNew.get(side[i]);
        }
        nbIndex.clear();
        init();
    }

    public void rebuildDefaultSignals(){
        defaultSignals = rebuildSignals(defaultSignals);
    }

    public int[] rebuildSignals(int[] oldSignals) {
        int nestedSignalsSize = 0;
        for(OpCustom2 custom : customs){
            nestedSignalsSize += custom.getCustomType().defaultSignals.length;
            //TODO The custom components in lcomps / customs are not updated for modifications, but in practice it doesn't really matter
        }
        int[] newSignals = new int[nestedAddressStart + nestedSignalsSize];
        System.arraycopy(oldSignals, 0, newSignals, 0, nestedAddressStart);

        int address = nestedAddressStart;
        for(OpCustom2 custom : customs){
            int[] innerSignals = custom.getCustomType().defaultSignals;
            System.arraycopy(innerSignals, 0, newSignals, address, innerSignals.length);
        }
        return newSignals;
    }

    public void projectInnerState(OpCustom2 custom){
        if(custom.getCustomType() != this) throw new IllegalArgumentException("Custom component supplied to projectInnerState must be of the same CompType");
        int[] signals = custom.getSignals();
        Node[] nodes = nodeBox.getNodes();

        for(LComponent lcomp : lcomps){
            if(lcomp instanceof Light) continue;
            int id = nbIndex.get(lcomp);
            if(lcomp instanceof Switch) ((Switch) lcomp).setState(signals[nodes[id].address]);
            IOManager io = lcomp.getIO();
            for(int i = 0; i < io.getNumOutputs(); i++){
                OutputPin outputPin = io.outputConnection(i);
                outputPin.setSignal(signals[nodes[id].address + i]);
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

    private int[] getNodeIn(LComponent lcomp, Map<LComponent, Integer> sigIndex){
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

    public boolean dependsOn(CustomType type) {
        for(OpCustom2 custom : customs){
            if(custom.getCustomType() == type) return true;
        }
        return false;
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
