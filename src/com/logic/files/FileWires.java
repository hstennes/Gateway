package com.logic.files;

import com.logic.components.IOManager;
import com.logic.components.InputPin;
import com.logic.components.LComponent;
import com.logic.components.Wire;

import java.awt.*;
import java.util.ArrayList;

/**
 * Holds shape data for a collection of wires. An instance of this class holds the wires for one FileComponent.
 */
public class FileWires extends ArrayList<int[]> {

    public FileWires(LComponent lcomp) {
        populateWires(lcomp);
    }

    public FileWires() {

    }

    private void populateWires(LComponent lcomp) {
        IOManager io = lcomp.getIO();
        for(int i = 0; i < io.getNumInputs(); i++) {
            InputPin inputPin = io.inputConnection(i);
            Wire wire = inputPin.numWires() > 0 ? inputPin.getWire() : null;
            if(wire != null) add(getWireArray(wire));
            else add(new int[0]);
        }
    }

    private int[] getWireArray(Wire wire) {
        ArrayList<Point> points = wire.getShapePoints();
        int[] wireShape = new int[points.size() * 2];
        for(int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            wireShape[2 * i] = point.x;
            wireShape[2 * i + 1] = point.y;
        }
        return wireShape;
    }

    public void populateShapePoints(Wire wire, int index) {
        int[] wireShape = get(index);
        for(int i = 0; i < wireShape.length; i += 2) {
            wire.addShapePoint(new Point(wireShape[i], wireShape[i + 1]));
        }
    }
}
