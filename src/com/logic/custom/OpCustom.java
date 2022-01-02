package com.logic.custom;

import com.logic.components.CompType;
import com.logic.components.LComponent;
import com.logic.engine.LogicEngine;

public class OpCustom extends LComponent {

    private NodeBox nodeBox;

    /**
     * Constructs a new LComponent
     *
     * @param x    The x position
     * @param y    The y position
     * @param type The type of component
     */
    public OpCustom(int x, int y, CompType type, Node[] nodes) {
        super(x, y, type);
    }

    @Override
    public void update(LogicEngine logicEngine) {

    }

    @Override
    public LComponent makeCopy() {
        return null;
    }
}
