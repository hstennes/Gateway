package com.logic.files;

import com.logic.components.Custom;
import com.logic.components.LComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomBlueprint {

    public int type;

    public String label;

    public FileComponent[] components;

    public CustomBlueprint(Custom custom){
        type = custom.getTypeID();
        label = custom.getLabel();

        ArrayList<LComponent> innerComps = custom.getInnerComps();
        Map<LComponent, Integer> idMap = new HashMap<>();
        for(int i = 0; i < innerComps.size(); i++) idMap.put(innerComps.get(i), i);

        components = new FileComponent[innerComps.size()];
        for(int i = 0; i < innerComps.size(); i++) components[i] = new FileComponent(innerComps.get(i), idMap);
    }

    public CustomBlueprint(){}
}
