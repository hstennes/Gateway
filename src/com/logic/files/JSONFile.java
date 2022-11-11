package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.logic.components.CompType;
import com.logic.components.LComponent;
import com.logic.components.OutputPin;
import com.logic.components.Wire;
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

    public FileWires[] wires;

    /**
     * Holds all wire data associated with custom components, including nested customs
     */
    public ArrayList<int[][]> cData;

    public FileSignalProvider cSignals;

    public ArrayList<int[]> cSigs;

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
        cSigs = new ArrayList<>();

        for(int i = 0; i < lcomps.size(); i++) {
            LComponent lcomp = lcomps.get(i);
            compIndex.put(lcomp, i);
        }

        cTypes = new CustomBlueprint[customTypes.size()];
        for(int i = 0; i < customTypes.size(); i++){
            cTypes[i] = new CustomBlueprint();
            cTypes[i].init(customTypes.get(i), cSigs);
        }

        components = new FileComponent[lcomps.size()];
        wires = new FileWires[lcomps.size()];
        for(int i = 0; i < lcomps.size(); i++) {
            LComponent lcomp = lcomps.get(i);
            if(lcomp.getType() == CompType.CUSTOM) {
                cSigs.add(((OpCustom2) lcomp).getSignals());
                components[i] = new FileComponent(lcomps.get(i), compIndex, cSigs.size() - 1);
            }
            else components[i] = new FileComponent(lcomps.get(i), compIndex, 0);
            wires[i] = new FileWires(lcomp);
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
            int exampleCDataID = version < 5 ? cExamples[i].cDataId : -1;
            if(version >= 6) customs.add(cType.makeCustomTypeV6(version, i, cSigs, customs, cData, exampleCDataID));
            else customs.add(cType.makeCustomTypeV5(version, i, cSignals, customs, cData, exampleCDataID));
        }

        for(FileComponent fc : components) {
            LComponent lcomp;
            if(version >= 6) lcomp = fc.makeComponent(version, cSigs, customs);
            else lcomp = fc.makeComponent(version, cSignals, customs);

            if(version < 5 && lcomp instanceof OpCustom2){
                //TODO file loading before version 5 not supported yet
                OpCustom2 custom = (OpCustom2) lcomp;
                custom.setSignalProvider(FileSignalProvider.buildSPFromOldCData(custom.getCustomType(), cData, fc.cDataId));
            }
            lcomps.add(lcomp);
        }

        for(int i = 0; i < components.length; i++){
            FileComponent fc = components[i];
            if(fc.input == null) continue;
            for(int x = 0; x < fc.input.length; x++){
                int[] input = fc.input[x];
                if(isEmptyConnection(input, version)) continue;
                Wire wire = new Wire();
                if(wires != null) wires[i].populateShapePoints(wire, x);
                OutputPin source = lcomps.get(input[0]).getIO().outputConnection(input[1]);
                applySignal(version, source, input);
                source.addWire(wire);
                lcomps.get(i).getIO().inputConnection(x).addWire(wire);
            }
        }
        return new FileData(version, lcomps, customs, camera, settings);
    }

    public static boolean isEmptyConnection(int[] input, int version){
        return version <= 3 && input.length == 0 || version > 3 && input[0] == -1;
    }

    public static void applySignal(int version, OutputPin source, int[] input){
        source.setSignal(input[version > 3 ? 3 : 2]);
    }
}
