package com.logic.custom;

import com.logic.components.Clock;
import com.logic.components.CompType;
import com.logic.components.LComponent;
import com.logic.files.FileNode;
import com.logic.util.CompUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NodeBox implements SpontNode{

    /**
     * The Nodes contained within this NodeBox, which are a simplified reprentation of a circuit created by the user. The first
     * nodes in the array must be StartNodes, followed by all other nodes.
     */
    private final Node[] inner;

    /**
     * If this NodeBox is contained within another NodeBox, the in array holds the IDs of the nodes connected to this node.
     * All in arrays use the format {input 1 node id, input 1 node output index, input 2 node id, input 2 node output index...}
     */
    private int[] in;

    /**
     * If this NodeBox is contained within another NodeBox, the out array holds the IDs of the nodes connected to this node.
     * All out arrays use the format [output number][wire number on the output]. Out arrays do not hold the index of the
     * connection on the other node, since they are only used for marking the next node to be updated.
     */
    private int[][] out;

    /**
     * Holds the IDs of the nodes the produce the output of this NodeBox. Format {node 1 id, node 1 output index, ...}
     */
    private final int[] outNodes;

    /**
     * Holds the output signals of the NodeBox
     */
    private int[] signal;

    private int typeID;

    private final int[] spontaneous;

    /**
     * Constructs a NodeBox to be used directly inside an OpCustom component
     * @param inner The inner Nodes
     * @param outNodes The outNodes array
     * @param signal The starting signal array
     */
    public NodeBox(Node[] inner, int[] outNodes, int[] signal, int typeID){
        this.inner = inner;
        in = null;
        out = null;
        this.outNodes = outNodes;
        this.signal = signal;
        this.typeID = typeID;
        spontaneous = findSpontaneous();
    }

    /**
     * Constructs a NodeBox to be used inside another NodeBox
     * @param inner The inner Nodes
     * @param in The input array, used to connect to other Nodes
     * @param out The out array, used to connect to other Nodes
     * @param outNodes The outNodes array
     * @param signal The starting signal array
     */
    public NodeBox(Node[] inner, int[] in, int[][] out, int[] outNodes, int[] signal, int typeID) {
        this.inner = inner;
        this.in = in;
        this.out = out;
        this.outNodes = outNodes;
        this.signal = signal;
        this.typeID = typeID;
        spontaneous = findSpontaneous();
    }

    private int[] findSpontaneous(){
        ArrayList<Integer> spontList = new ArrayList<>();
        for(int i = 0; i < inner.length; i++) {
            if(inner[i] instanceof SpontNode) spontList.add(i);
        }
        return spontList.stream().mapToInt(i->i).toArray();
    }

    /**
     * Valid only for NodeBoxes included inside other NodeBoxes. Runs internal logic to update the signal array.
     * @param nb The NodeBox containing this NodeBox
     * @param active The active components list of the parent NodeBox
     */
    @Override
    public void update(NodeBox nb, List<Integer> active) {
        int[] inputs = new int[in.length / 2];
        for(int i = 0; i < inputs.length; i++) inputs[i] = nb.get(in[i * 2], in[i * 2 + 1]);
        update(inputs, active);
    }

    /**
     * Valid only for NodeBoxes that are included directly inside an OpCustom. Runs internal logic to update the signal array.
     * @param inputs The inputs to run logic on+
     */
    public void update(int[] inputs){
        update(inputs, null);
    }

    /**
     * Internal update method that performs logic after the input values are determined.
     * @param inputs The inputs
     * @param outerActive If this NodeBox is inside another NodeBox, outerNodes is the parent active array (null otherwise)
     */
    private void update(int[] inputs, List<Integer> outerActive){
        List<Integer> active = new ArrayList<>();
        for(int i = 0; i < inputs.length; i++){
            if(inputs[i] != inner[i].getSignal(0)) {
                ((StartNode) inner[i]).setSignal(inputs[i]);
                active.add(i);
            }
        }
        active.addAll(Arrays.stream(spontaneous).boxed().collect(Collectors.toList()));

        while(active.size() > 0) {
            List<Integer> oldActive = active;
            active = new ArrayList<>();
            for (int n : oldActive) {
                inner[n].update(this, active);
            }
        }

        if(outerActive != null) {
            for (int i = 0; i < signal.length; i++) {
                int newSignal = get(outNodes[2 * i], outNodes[2 * i + 1]);
                if (newSignal != signal[i]) {
                    signal[i] = newSignal;
                    outerActive.addAll(Arrays.stream(out[i]).boxed().collect(Collectors.toList()));
                }
            }
        }
        else{
            for (int i = 0; i < signal.length; i++) {
                signal[i] = get(outNodes[2 * i], outNodes[2 * i + 1]);
            }
        }
    }

    @Override
    public int getSignal(int n){
        return signal[n];
    }

    @Override
    public FileNode serialize() {
        FileNode fileNode = new FileNode(CompType.CUSTOM, in, out);
        fileNode.setOutNodes(outNodes);
        fileNode.setSpontaneous(spontaneous);
        fileNode.setnTypeId(typeID);
        return fileNode;
    }

    /**
     * Gets the specified signal being outputted by some node
     * @param node The node ID
     * @param nodeOut The output index on the node
     * @return The signal
     */
    public int get(int node, int nodeOut){
        if(node == -1) return 0;
        return inner[node].getSignal(nodeOut);
    }

    /**
     * Initializes the NodeBox to be included inside another NodeBox
     * @param in The in array
     * @param out The out array
     * @param signal The initial signals
     */
    public void connect(int[] in, int[][] out, int[] signal){
        this.in = in;
        this.out = out;
        this.signal = signal;
    }

    @Override
    public NodeBox duplicate(){
        Node[] newInner = new Node[inner.length];
        for(int i = 0; i < inner.length; i++) newInner[i] = inner[i].duplicate();
        int[] newSignal = new int[signal.length];
        System.arraycopy(signal, 0, newSignal, 0, signal.length);
        return new NodeBox(newInner, in, out, outNodes, newSignal, typeID);
    }

    @Override
    public void start(OpCustom callback){
        for(int i : spontaneous) ((SpontNode) inner[i]).start(callback);
    }

    public void stop(){
        for(int i : spontaneous) ((SpontNode) inner[i]).stop();
    }

    public Node[] getInnerNodes(){
        return inner;
    }

    public int[] getOutNodes(){
        return outNodes;
    }
}
