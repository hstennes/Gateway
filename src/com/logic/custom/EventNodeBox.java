package com.logic.custom;

public class EventNodeBox extends NodeBox2{

    public EventNodeBox(Node[] nodes, int[] outNodes) {
        super(nodes, outNodes);
    }

    @Override
    public int[] update(int[] signals, int[] in, int offset, ActiveStack active){
        boolean change = false;
        for(int i = 0; i < in.length; i++){
            int address = nodes[i].address + offset;
            int currentSignal = signals[address];
            int newSignal = in[i];

            if(currentSignal != newSignal) {
                change = true;
                signals[address] = newSignal;
                nodes[i].updateEvent(signals, offset, active);
            }
        }
        if(!change) return getOutputs(signals, offset);

        while(active.nextIteration()) {
            while(active.hasNext()) {
                nodes[active.next()].updateEvent(signals, offset, active);
            }
        }

        return getOutputs(signals, offset);
    }
}
