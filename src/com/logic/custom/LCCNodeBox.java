package com.logic.custom;

public class LCCNodeBox extends NodeBox2{

    private final int[][] levels;

    public LCCNodeBox(Node[] nodes, int[] outNodes, int[][] levels) {
        super(nodes, outNodes);
        this.levels = levels;
    }

    @Override
    public int[] update(int[] signals, int[] in, int offset, ActiveStack active) {
        for(int i = 0; i < in.length; i++){
            signals[nodes[i].address + offset] = in[i];
        }
        for(int[] level : levels){
            for(int i : level){
                nodes[i].updateLCC(signals, offset, active);
            }
        }
        return getOutputs(signals, offset);
    }
}
