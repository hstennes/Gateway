package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logic.components.CompType;
import com.logic.components.Connection;
import com.logic.components.LComponent;
import com.logic.components.Wire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONFile {

    public FileComponent[] components;

    public JSONFile(List<LComponent> lcomps){
        Map<LComponent, Integer> idMap = new HashMap<>();
        for(int i = 0; i < lcomps.size(); i++) idMap.put(lcomps.get(i), i);

        components = new FileComponent[lcomps.size()];
        for(int i = 0; i < lcomps.size(); i++) components[i] = new FileComponent(lcomps.get(i), idMap);
    }

    public JSONFile(){ }

    @JsonIgnore
    public ArrayList<LComponent> getLComps(){
        ArrayList<LComponent> lcomps = new ArrayList<>();
        for(FileComponent fc : components) lcomps.add(fc.makeComponent());

        for(int i = 0; i < components.length; i++){
            FileComponent fc = components[i];
            if(fc.input == null) continue;
            for(int x = 0; x < fc.input.length; x++){
                int[] input = fc.input[x];
                if(input.length == 0) continue;
                Wire wire = new Wire();
                wire.setSignal(input[2] == 1);
                lcomps.get(input[0]).getIO().connectionAt(input[1], Connection.OUTPUT).addWire(wire);
                lcomps.get(i).getIO().connectionAt(x, Connection.INPUT).addWire(wire);
            }
        }
        return lcomps;
    }
}
