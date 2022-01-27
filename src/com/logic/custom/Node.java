package com.logic.custom;

import com.logic.files.FileNode;

import java.util.ArrayList;
import java.util.List;

public interface Node {

    /**
     * Updates the output signal based on inputs acquired from the given NodeBox
     * @param nb The NodeBox containing the Node
     * @param active The active array for marking components that must be updated
     */
    void update(NodeBox nb, List<Integer> active);

    /**
     * Returns the output signal at the given index
     * @param n The index
     * @return The signal
     */
    int getSignal(int n);

    /**
     * Creates a FileNode of this node for saving to JSON
     * @return The FileNode object
     */
    FileNode serialize();

    /**
     * Duplicates the node. The duplicated node may reference the same data held by the original node, as long as this
     * data will never be modified.
     * @return A copy of the Node.
     */
    Node duplicate();
}
