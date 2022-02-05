package com.logic.files;

import com.logic.components.LComponent;
import com.logic.custom.CustomType;
import com.logic.custom.Node;
import com.logic.custom.NodeBox2;
import com.logic.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomBlueprint {

    public String label;

    /**
     * Corresponds to CustomType.innerComps
     */
    public FileComponent[] source;

    /**
     * The nodes inside the NodeBox
     */
    public FileNode[] nodes;

    /**
     * Taken directly from NodeBox
     */
    public int[] outNodes;

    /**
     * io[side][index on side] = 0 for input, 1 for output
     */
    public int[][] io;

    public CustomBlueprint(CustomType type){
        label = type.getLabel();

        ArrayList<LComponent> innerComps = type.getInnerComps();
        Map<LComponent, Integer> compIndex = new HashMap<>();
        for(int i = 0; i < innerComps.size(); i++) compIndex.put(innerComps.get(i), i);

        source = new FileComponent[innerComps.size()];
        for(int i = 0; i < innerComps.size(); i++) source[i] = new FileComponent(innerComps.get(i), compIndex, null, true);

        NodeBox2 nb = type.getNodeBox();
        outNodes = nb.getOutNodes();

        io = new int[4][];
        LComponent[][] content = type.getContent();
        fillIOArray(compIndex, content, Constants.RIGHT);
        fillIOArray(compIndex, content, Constants.UP);
        fillIOArray(compIndex, content, Constants.LEFT);
        fillIOArray(compIndex, content, Constants.DOWN);

        Node[] innerNodes = nb.getInnerNodes();
        nodes = new FileNode[innerNodes.length];
        for(int i = 0; i < nodes.length; i++) nodes[i] = innerNodes[i].serialize();
    }

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
