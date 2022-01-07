package com.logic.custom;

import com.logic.components.LComponent;

import java.util.ArrayList;

public class CustomType {

    private final ArrayList<LComponent> innerComps;

    private final LComponent[][] content;

    private final String label;

    private final int typeID;

    private final NodeBox nodeBox;

    public CustomType(String label, LComponent[][] content, ArrayList<LComponent> innerComps, int typeID, NodeBox nodeBox){
        this.label = label;
        this.content = content;
        this.innerComps = innerComps;
        this.typeID = typeID;
        this.nodeBox = nodeBox;
    }

    public ArrayList<LComponent> getInnerComps() {
        return innerComps;
    }

    public LComponent[][] getContent() {
        return content;
    }

    public String getLabel() {
        return label;
    }

    public int getTypeID() {
        return typeID;
    }

    public NodeBox getNodeBox(){
        return nodeBox;
    }
}
