package com.logic.files;

import com.logic.components.*;
import com.logic.custom.CustomType;
import com.logic.custom.OpCustom2;
import com.logic.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one distinct type of custom component, the template can then be duplicated for all components of the same type
 */
public class CustomBlueprint {

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

    public ArrayList<int[]> clocks;

    public CustomBlueprint() { }

    public void init(CustomType custom, FileSignalProvider cSignals){
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
        for(int i = 0; i < innerComps.size(); i++) {
            LComponent lcomp = innerComps.get(i);
            if(lcomp.getType() == CompType.CUSTOM) {
                int cSignalsIndex = cSignals.addSignalProvider(((OpCustom2) lcomp).getSignalProvider());
                components[i] = new FileComponent(innerComps.get(i), compIndex, cSignalsIndex, true);
            }
            else components[i] = new FileComponent(innerComps.get(i), compIndex, 0, true);
        }

        clocks = custom.clocks;
    }

    public CustomType makeCustomType(int version, int typeID, FileSignalProvider cData, ArrayList<CustomType> cTypes){
        ArrayList<LComponent> lcomps = new ArrayList<>();
        for (FileComponent component : components) lcomps.add(component.makeComponent(version, cData, cTypes));

        LComponent[][] content = new LComponent[4][];
        for(int i = 0; i < content.length; i++) {
            content[i] = new LComponent[io[i].length];
            for(int x = 0; x < io[i].length; x++) content[i][x] = lcomps.get(io[i][x]);
        }

        for(int i = 0; i < components.length; i++){
            FileComponent fc = components[i];
            if(fc.input == null) continue;
            for(int x = 0; x < fc.input.length; x++){
                int[] input = fc.input[x];
                if(JSONFile.isEmptyConnection(input, version)) continue;
                Wire wire = new Wire();
                OutputPin source = lcomps.get(input[0]).getIO().outputConnection(input[1]);
                source.addWire(wire);
                lcomps.get(i).getIO().inputConnection(x).addWire(wire);
            }
        }

        return new CustomType(label, content, lcomps, typeID);
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
