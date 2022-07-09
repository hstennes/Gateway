package com.logic.custom;

public class LCCNodeBox extends NodeBox2{

    private final int[] levels;

    public LCCNodeBox(Node[] nodes, int[] outNodes, int[] levels) {
        super(nodes, outNodes);
        this.levels = levels;
    }

    @Override
    public int[] update(int[] signals, int[] in, int offset, ActiveStack active) {
        boolean change = false;
        for(int i = 0; i < in.length; i++){
            int address = nodes[i].address + offset;
            if(signals[address] != in[i]) {
                change = true;
                signals[address] = in[i];
            }
        }
        if(!change) return getOutputs(signals, offset);

        for(int i : levels){
            nodes[i].updateLCC(signals, offset, active);
        }
        return getOutputs(signals, offset);
    }
}
