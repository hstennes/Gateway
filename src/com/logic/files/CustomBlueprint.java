package com.logic.files;

import com.logic.components.Custom;
import com.logic.components.LComponent;
import com.logic.components.Light;
import com.logic.components.Switch;
import com.logic.ui.CompRotator;
import com.logic.util.CompUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomBlueprint {

    public String label;

    public FileComponent[] components;

    public int[][] io;

    public CustomBlueprint(Custom custom){
        label = custom.getLabel();

        ArrayList<LComponent> innerComps = custom.getInnerComps();
        Map<LComponent, Integer> compIndex = new HashMap<>();
        for(int i = 0; i < innerComps.size(); i++) compIndex.put(innerComps.get(i), i);

        io = new int[4][];
        LComponent[][] content = custom.getContent();
        fillIOArray(compIndex, content, CompRotator.RIGHT);
        fillIOArray(compIndex, content, CompRotator.UP);
        fillIOArray(compIndex, content, CompRotator.LEFT);
        fillIOArray(compIndex, content, CompRotator.DOWN);

        components = new FileComponent[innerComps.size()];
        for(int i = 0; i < innerComps.size(); i++) components[i] = new FileComponent(innerComps.get(i), compIndex, null, false);
    }

    public CustomBlueprint(){}

    private void fillIOArray(Map<LComponent, Integer> compIndex, LComponent[][] content, int direction){
        io[direction] = new int[content[direction].length];
        for(int i = 0; i < content[direction].length; i++) io[direction][i] = compIndex.get(content[direction][i]);
    }
}
