package com.logic.files;

import com.logic.components.Constant;
import com.logic.components.LComponent;
import com.logic.custom.CustomType;
import com.logic.custom.Node;
import com.logic.custom.NodeBox;
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
     * Current idea is to have array with number of connections on each side. From RIGHT to UP.
     */
    public int[] sides;

    public CustomBlueprint(CustomType type){
        label = type.getLabel();

        ArrayList<LComponent> innerComps = type.getInnerComps();
        Map<LComponent, Integer> compIndex = new HashMap<>();
        for(int i = 0; i < innerComps.size(); i++) compIndex.put(innerComps.get(i), i);

        source = new FileComponent[innerComps.size()];
        for(int i = 0; i < innerComps.size(); i++) source[i] = new FileComponent(innerComps.get(i), compIndex, null, true);

        NodeBox nb = type.getNodeBox();
        outNodes = nb.getOutNodes();

        sides = new int[4];
        LComponent[][] content = type.getContent();
        //TODO have to rework because inputs are different from outputs
        for(int s = Constants.RIGHT; s <= Constants.UP; s++) sides[s] = content[s].length;

        Node[] innerNodes = nb.getInnerNodes();
        nodes = new FileNode[innerNodes.length];
        for(int i = 0; i < nodes.length; i++) nodes[i] = innerNodes[i].serialize();
    }
}
