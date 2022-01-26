package com.logic.files;

import com.logic.components.Custom;
import com.logic.components.LComponent;
import com.logic.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one distinct type of custom component, the template can then be duplicated for all components of the same type
 */
public class CustomBlueprintCompat {

    /**
     * The label of the Custom component
     */
    public String label;

    /**
     * The list of inner components
     */
    public FileComponent[] components;

    /**
     * Holds the indexes of the lights and switches that correspond to the connections. Format io[side][index on side]
     */
    public int[][] io;

    /**
     * Creates a new custom blueprint
     * @param custom The custom component
     */
    public CustomBlueprintCompat(Custom custom){
        label = custom.getLabel();

        ArrayList<LComponent> innerComps = custom.getInnerComps();
        Map<LComponent, Integer> compIndex = new HashMap<>();
        for(int i = 0; i < innerComps.size(); i++) compIndex.put(innerComps.get(i), i);

        io = new int[4][];
        LComponent[][] content = custom.getContent();
        fillIOArray(compIndex, content, Constants.RIGHT);
        fillIOArray(compIndex, content, Constants.UP);
        fillIOArray(compIndex, content, Constants.LEFT);
        fillIOArray(compIndex, content, Constants.DOWN);

        components = new FileComponent[innerComps.size()];
        for(int i = 0; i < innerComps.size(); i++) components[i] = new FileComponent(innerComps.get(i), compIndex, null, false);
    }

    /**
     * Needed for deserialization to work
     */
    public CustomBlueprintCompat(){}

    /**
     * Completes one side of the io array based on the content from the custom component
     * @param compIndex The compIndex map
     * @param content The content from the custom component
     * @param direction The direction to consider
     */
    private void fillIOArray(Map<LComponent, Integer> compIndex, LComponent[][] content, int direction){
        io[direction] = new int[content[direction].length];
        for(int i = 0; i < content[direction].length; i++) io[direction][i] = compIndex.get(content[direction][i]);
    }
}
