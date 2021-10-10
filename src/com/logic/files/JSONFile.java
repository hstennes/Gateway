package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logic.components.*;

import java.io.File;
import java.util.*;

/**
 * A JSONFile object is serialized to create the save file
 */
public class JSONFile {

    /**
     * File format version, incremented whenever a change is made
     */
    public final int version = FileManager.FILE_FORMAT_VERSION;

    /**
     * Holds camera position data [x, y, zoom]
     */
    public double[] camera;

    /**
     * Holds editor preferences. Format [snap to grid, show grid] with 1 for true and 0 for false
     */
    public int[] settings;

    /**
     * Represents the list of LComponents in the CircuitPanel
     */
    public FileComponent[] components;

    /**
     * The default instances of each custom type from CompCreator.customs
     */
    public FileComponent[] cExamples;

    /**
     * The list of custom types from CompCreator.customs.
     */
    public CustomBlueprint[] cTypes;

    /**
     * Holds all wire data associated with custom components, including nested customs
     */
    public ArrayList<Integer[][]> cData;

    /**
     * Creates a new JSONFile object, which is then ready to be saved
     * @param fd The FileData to use
     */
    public JSONFile(FileData fd){
        camera = fd.getCamera();
        settings = fd.getSettings();
        List<LComponent> lcomps = fd.getLcomps();
        List<Custom> customs = fd.getCustoms();

        Map<LComponent, Integer> compIndex = new HashMap<>();
        Map<Custom, Integer> cDataIndex = new HashMap<>();
        cData = new ArrayList<>();

        for(int i = 0; i < lcomps.size(); i++) {
            LComponent lcomp = lcomps.get(i);
            compIndex.put(lcomp, i);
            if(lcomp.getType() == CompType.CUSTOM) cDataIndex.put((Custom) lcomp, populateCustomData((Custom) lcomp));
        }

        for (Custom custom : customs) {
            populateCustomData(custom);
            cDataIndex.put(custom, cData.size() - 1);
        }

        components = new FileComponent[lcomps.size()];
        for(int i = 0; i < lcomps.size(); i++) components[i] = new FileComponent(lcomps.get(i), compIndex, cDataIndex, true);

        cTypes = new CustomBlueprint[customs.size()];
        for(int i = 0; i < customs.size(); i++) cTypes[i] = new CustomBlueprint(customs.get(i));

        cExamples = new FileComponent[customs.size()];
        for(int i = 0; i < customs.size(); i++) cExamples[i] = new FileComponent(customs.get(i), null, cDataIndex, true);
    }

    /**
     * Recursively adds the wire state data for this custom component and each custom it contains to the cData list and returns the index
     * in cData where the data is stored.  The data for this component holds the index of the data for each nested component so that
     * the state can be reconstructed.
     * @param custom The custom component
     * @return The index of the data
     */
    private int populateCustomData(Custom custom){
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
        return cData.size() - 1;
    }

    /**
     * Needed for deserialization to work
     */
    public JSONFile(){ }

    /**
     * Reconstructs a FileData object after the file has been deserialized
     * @return The resulting FileData object
     */
    @JsonIgnore
    public FileData getFileData(){
        ArrayList<Custom> customs = new ArrayList<>();
        ArrayList<LComponent> lcomps = new ArrayList<>();
        for(FileComponent fc : components) lcomps.add(fc.makeComponent(cTypes, cData, true, -1));
        for(FileComponent fc : cExamples) customs.add((Custom) fc.makeComponent(cTypes, cData, true, -1));

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
        return new FileData(version, lcomps, customs, camera, settings);
    }
}
