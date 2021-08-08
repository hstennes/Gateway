package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.logic.components.*;
import com.logic.ui.CompProperties;
import com.logic.util.CompUtils;

import java.util.Map;

public class FileComponent {

    public CompType type;

    public int[] pos;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int rot;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String name;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String com;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int[][] input;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean state;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int delay;

    public FileComponent(LComponent lcomp, Map<LComponent, Integer> idMap){
        type = lcomp.getType();
        pos = new int[] {lcomp.getX(), lcomp.getY()};
        rot = lcomp.getRotator().getRotation();
        name = lcomp.getName().equals(CompProperties.defaultName) ? "" : lcomp.getName();
        com = lcomp.getComments().equals(CompProperties.defaultComments) ? "" : lcomp.getComments();
        if(type == CompType.SWITCH) state = ((Switch) lcomp).getState();
        else if(type == CompType.CLOCK) delay = ((Clock) lcomp).getDelay();

        IOManager io = lcomp.getIO();
        input = new int[io.getNumInputs()][3];
        for(int i = 0; i < io.getNumInputs(); i++){
            Connection conn = io.connectionAt(i, Connection.INPUT);
            if(conn.numWires() > 0) {
                Wire w = conn.getWire();
                Connection source = w.getSourceConnection();
                input[i] = new int[] {idMap.get(source.getLcomp()), source.getIndex(), w.getSignal() ? 1 : 0};
            }
            else input[i] = new int[] {};
        }
    }

    public FileComponent(){ }

    @JsonIgnore
    public LComponent makeComponent(){
        LComponent lcomp = CompUtils.makeComponent(type.toString(), pos[0], pos[1]);
        lcomp.getRotator().setRotation(rot);
        if(name != null && !name.equals("")) lcomp.setName(name);
        if(com != null && !com.equals("")) lcomp.setComments(com);
        if(type == CompType.SWITCH) ((Switch) lcomp).setState(state);
        if(type == CompType.CLOCK) ((Clock) lcomp).setDelay(delay);
        if(lcomp instanceof BasicGate) ((BasicGate) lcomp).setNumInputs(input.length);
        return lcomp;
    }
}
