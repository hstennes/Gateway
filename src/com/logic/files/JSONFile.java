package com.logic.files;

import com.logic.components.LComponent;

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
}
