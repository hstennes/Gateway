package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logic.components.*;

import java.util.*;

public class JSONFile {

    public int version = 1;

    public FileComponent[] components;

    public CustomBlueprint[] cTypes;

    public ArrayList<Integer[][]> cData;

    public JSONFile(List<LComponent> lcomps, List<Custom> customs){
        Map<LComponent, Integer> compIndex = new HashMap<>();
        Map<Custom, Integer> cDataIndex = new HashMap<>();
        cData = new ArrayList<>();

        for(int i = 0; i < lcomps.size(); i++) {
            LComponent lcomp = lcomps.get(i);
            compIndex.put(lcomp, i);
            if(lcomp.getType() == CompType.CUSTOM){
                populateCustomData((Custom) lcomp);
                cDataIndex.put((Custom) lcomp, cData.size() - 1);
            }
        }

        components = new FileComponent[lcomps.size()];
        for(int i = 0; i < lcomps.size(); i++) components[i] = new FileComponent(lcomps.get(i), compIndex, cDataIndex, true);

        cTypes = new CustomBlueprint[customs.size()];
        for(int i = 0; i < customs.size(); i++) cTypes[i] = new CustomBlueprint(customs.get(i));
    }

    /**
     * Recursively adds the wire state data for this custom component and each custom it contains to the cData list.  The state for
     * this component is added last after any dependencies, so its index will be cData.size() - 1 immediately after this call.
     * @param custom The custom component
     */
    private void populateCustomData(Custom custom){
        ArrayList<LComponent> innerComps = custom.getInnerComps();
        Integer[][] data = new Integer[innerComps.size()][];
        for(int i = 0; i < innerComps.size(); i++){
            LComponent lcomp = innerComps.get(i);
            IOManager io = lcomp.getIO();
            data[i] = new Integer[lcomp.getType() == CompType.CUSTOM ? io.getNumInputs() + 1 : io.getNumInputs()];
            for(int x = 0; x < io.getNumInputs(); x++){
                Connection conn = io.connectionAt(x, Connection.INPUT);
                if(conn.numWires() > 0) data[i][x] = conn.getWire().getSignal() ? 1 : 0;
            }
            if(lcomp.getType() == CompType.CUSTOM) {
                populateCustomData((Custom) lcomp);
                data[i][data[i].length - 1] = cData.size() - 1;
            }
        }
        cData.add(data);
    }

    public JSONFile(){ }

    @JsonIgnore
    public ArrayList<LComponent> getLComps(){
        ArrayList<LComponent> lcomps = new ArrayList<>();
        for(FileComponent fc : components) lcomps.add(fc.makeComponent(cTypes, cData, true, -1));

        for(int i = 0; i < components.length; i++){
            FileComponent fc = components[i];
            if(fc.input == null) continue;
            for(int x = 0; x < fc.input.length; x++){
                int[] input = fc.input[x];
                if(input.length == 0) continue;
                Wire wire = new Wire();
                wire.setSignal(input[2] == 1);
                lcomps.get(input[0]).getIO().connectionAt(input[1], Connection.OUTPUT).addWire(wire);
                lcomps.get(i).getIO().connectionAt(x, Connection.INPUT).addWire(wire);
            }
        }
        return lcomps;
    }
}
