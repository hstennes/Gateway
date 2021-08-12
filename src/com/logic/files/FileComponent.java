package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.logic.components.*;
import com.logic.ui.CompProperties;
import com.logic.ui.CompRotator;
import com.logic.util.CompUtils;

import java.util.ArrayList;
import java.util.Map;

public class FileComponent {

    public CompType type;

    public int[] pos;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int rot;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String name;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String com;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int[][] input;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean state;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int delay;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int cTypeId;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int cDataId;

    /**
     * Creates a representation of the given component that can be serialized to a json file.
     * @param lcomp The component
     * @param compIndex The mapping of components in the same list as this component to their indexes in the list
     * @param cDataIndex The mapping of custom components to the index in the cData list where their data is stored
     * @param topLevel true if this compoent is in the top level components list as opposed to being inside a custom component.
     */
    public FileComponent(LComponent lcomp, Map<LComponent, Integer> compIndex, Map<Custom, Integer> cDataIndex, boolean topLevel){
        type = lcomp.getType();
        pos = new int[] {lcomp.getX(), lcomp.getY()};
        rot = lcomp.getRotator().getRotation();
        name = lcomp.getName().equals(CompProperties.defaultName) ? "" : lcomp.getName();
        com = lcomp.getComments().equals(CompProperties.defaultComments) ? "" : lcomp.getComments();
        if(type == CompType.SWITCH) state = ((Switch) lcomp).getState();
        else if(type == CompType.CLOCK) delay = ((Clock) lcomp).getDelay();
        else if(type == CompType.CUSTOM) {
            cTypeId = ((Custom) lcomp).getTypeID();
            if(topLevel) cDataId = cDataIndex.get((Custom) lcomp);
        }

        IOManager io = lcomp.getIO();
        input = new int[io.getNumInputs()][topLevel ? 3 : 2];
        for(int i = 0; i < io.getNumInputs(); i++){
            Connection conn = io.connectionAt(i, Connection.INPUT);
            if(conn.numWires() > 0) {
                Wire w = conn.getWire();
                Connection source = w.getSourceConnection();
                input[i][0] = compIndex.get(source.getLcomp());
                input[i][1] = source.getIndex();
                if(topLevel) input[i][2] = w.getSignal() ? 1 : 0;
            }
            else input[i] = new int[] {};
        }
    }

    public FileComponent(){ }

    @JsonIgnore
    public LComponent makeComponent(CustomBlueprint[] cTypes, ArrayList<Integer[][]> cData, boolean topLevel, int providedCDataId){
        if(type.toString().equals("CUSTOM")) return makeCustom(cTypes, cData, topLevel, providedCDataId);
        LComponent lcomp = CompUtils.makeComponent(type.toString(), pos[0], pos[1]);
        lcomp.getRotator().setRotation(rot);
        if(name != null && !name.equals("")) lcomp.setName(name);
        if(com != null && !com.equals("")) lcomp.setComments(com);
        if(type == CompType.SWITCH) ((Switch) lcomp).setState(state);
        if(type == CompType.CLOCK) ((Clock) lcomp).setDelay(delay);
        if(lcomp instanceof BasicGate) ((BasicGate) lcomp).setNumInputs(input.length);
        return lcomp;
    }

    private Custom makeCustom(CustomBlueprint[] cTypes, ArrayList<Integer[][]> cData, boolean topLevel, int providedCDataId){
        //this should all go smoothly?
        int realCDataId = topLevel ? cDataId : providedCDataId;

        CustomBlueprint b = cTypes[cTypeId];
        ArrayList<LComponent> lcomps = new ArrayList<>();
        for(int i = 0; i < b.components.length; i++) {
            if(b.components[i].type.toString().equals("CUSTOM")) {
                Integer[] compData = cData.get(realCDataId)[i];
                lcomps.add(b.components[i].makeComponent(cTypes, cData, false, compData[compData.length - 1]));
            }
            else lcomps.add(b.components[i].makeComponent(cTypes, cData, false, -1));
        }

        LComponent[][] content = new LComponent[4][];
        for(int i = 0; i < content.length; i++) {
            content[i] = new LComponent[b.io[i].length];
            for(int x = 0; x < b.io[i].length; x++) content[i][x] = lcomps.get(b.io[i][x]);
        }

        for(int i = 0; i < b.components.length; i++){
            FileComponent fc = b.components[i];
            if(fc.input == null) continue;
            for(int x = 0; x < fc.input.length; x++){
                int[] input = fc.input[x];
                if(input.length == 0) continue;
                Wire wire = new Wire();
                wire.setSignal(cData.get(realCDataId)[i][x] == 1);
                lcomps.get(input[0]).getIO().connectionAt(input[1], Connection.OUTPUT).addWire(wire);
                lcomps.get(i).getIO().connectionAt(x, Connection.INPUT).addWire(wire);
            }
        }
        return new Custom(pos[0], pos[1], b.label, content, lcomps, cTypeId);
    }
}
