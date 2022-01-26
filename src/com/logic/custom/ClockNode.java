package com.logic.custom;

import com.logic.components.Clock;
import com.logic.engine.LogicWorker;
import com.logic.ui.CircuitPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClockNode implements SpontNode{

    private int delay;

    private Timer timer;

    private int[] out;

    private boolean state;

    private boolean signal;

    public ClockNode(int delay, int[] out, int signal){
        this.delay = delay;
        this.out = out;
        this.signal = signal != 0;
    }

    @Override
    public void update(NodeBox nb, List<Integer> active) {
        if(signal != state) {
            signal = state;
            active.addAll(Arrays.stream(out).boxed().collect(Collectors.toList()));
        }
    }

    @Override
    public void start(OpCustom callback) {
        if(timer != null) timer.stop();
        timer = new Timer(delay, e -> {
            state = !state;
            callback.spontaneousCallback();
        });
        timer.start();
    }

    @Override
    public void stop(){
        timer.stop();
    }

    @Override
    public int getSignal(int n) {
        return signal ? 1 : 0;
    }

    @Override
    public Node duplicate() {
        return new ClockNode(delay, out, getSignal(0));
    }
}
