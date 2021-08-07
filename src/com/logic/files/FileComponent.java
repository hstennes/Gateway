package com.logic.files;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logic.components.*;

import java.util.Map;

public class FileComponent {

    public CompType type;

    public int x, y;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int rot;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String name;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String com;

    public int[][] input;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean state;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int delay;

    public FileComponent(LComponent lcomp, Map<LComponent, Integer> idMap){
        type = lcomp.getType();
        x = lcomp.getX();
        y = lcomp.getY();
        rot = lcomp.getRotator().getRotation();
        name = lcomp.getName();
        com = lcomp.getComments();
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
}
