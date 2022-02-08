package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logic.components.*;
import com.logic.custom.CustomType;
import com.logic.custom.OpCustom2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A JSONFile object is serialized to create the save file
 */
public class JSONFile {

    /**
     * File format version, incremented whenever a change is made
     */
    public int version = FileManager.FILE_FORMAT_VERSION;

    /**
     * Holds camera position data [x, y, zoom]
     */
    public float[] camera;

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

    public FileSignalProvider cSignals;

    /**
     * Creates a new JSONFile object, which is then ready to be saved
     * @param fd The FileData to use
     */
    public JSONFile(FileData fd){
        camera = fd.getCamera();
        settings = fd.getSettings();
        List<LComponent> lcomps = fd.getLcomps();
        List<CustomType> customTypes = fd.getCustomTypes();

        Map<LComponent, Integer> compIndex = new HashMap<>();

        cSignals = new FileSignalProvider();

        for(int i = 0; i < lcomps.size(); i++) {
            LComponent lcomp = lcomps.get(i);
            compIndex.put(lcomp, i);
        }

        cTypes = new CustomBlueprint[customTypes.size()];
        for(int i = 0; i < customTypes.size(); i++){
            cTypes[i] = new CustomBlueprint();
            cTypes[i].init(customTypes.get(i), cSignals);
        }

        components = new FileComponent[lcomps.size()];
        for(int i = 0; i < lcomps.size(); i++) {
            LComponent lcomp = lcomps.get(i);
            if(lcomp.getType() == CompType.CUSTOM) {
                int cSignalsIndex = cSignals.addSignalProvider(((OpCustom2) lcomp).getSignalProvider());
                components[i] = new FileComponent(lcomps.get(i), compIndex, cSignalsIndex, true);
            }
            else components[i] = new FileComponent(lcomps.get(i), compIndex, 0, true);
        }
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
        ArrayList<CustomType> customs = new ArrayList<>();
        ArrayList<LComponent> lcomps = new ArrayList<>();

        for(int i = 0; i < cTypes.length; i++){
            CustomBlueprint cType = cTypes[i];
            customs.add(cType.makeCustomType(version, i, cSignals, customs));
        }

        for(FileComponent fc : components) {
            LComponent lcomp = fc.makeComponent(version, cSignals, customs);
            lcomps.add(lcomp);
        }
        //for(FileComponent fc : cExamples) customs.add(fc.makeCustomParams(version, cTypes, cData, true, -1));

        for(int i = 0; i < components.length; i++){
            FileComponent fc = components[i];
            if(fc.input == null) continue;
            for(int x = 0; x < fc.input.length; x++){
                int[] input = fc.input[x];
                if(isEmptyConnection(input, version)) continue;
                Wire wire = new Wire();
                OutputPin source = lcomps.get(input[0]).getIO().outputConnection(input[1]);
                applySignal(source, input);
                source.addWire(wire);
                lcomps.get(i).getIO().inputConnection(x).addWire(wire);
            }
        }
        return new FileData(version, lcomps, customs, camera, settings);
    }

    public static boolean isEmptyConnection(int[] input, int version){
        return version <= 3 && input.length == 0 || version > 3 && input[0] == -1;
    }

    private void applySignal(OutputPin source, int[] input){
        source.setSignal(input[version > 3 ? 3 : 2]);
    }
}
