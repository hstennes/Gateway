package com.logic.files;

import com.logic.components.Custom;
import com.logic.components.LComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomBlueprint {

    public String label;

    public FileComponent[] components;

    public CustomBlueprint(Custom custom){
        label = custom.getLabel();

        ArrayList<LComponent> innerComps = custom.getInnerComps();
        Map<LComponent, Integer> compIndex = new HashMap<>();
        for(int i = 0; i < innerComps.size(); i++) compIndex.put(innerComps.get(i), i);

        components = new FileComponent[innerComps.size()];
        for(int i = 0; i < innerComps.size(); i++) components[i] = new FileComponent(innerComps.get(i), compIndex, null, false);
    }

    public CustomBlueprint(){}
}
