package com.logic.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.logic.components.*;
import com.logic.custom.CustomType;
import com.logic.custom.OpCustom;
import com.logic.ui.CompProperties;
import com.logic.util.CompUtils;

import java.util.ArrayList;
import java.util.Map;

/**
 * A FileComponent is serialized to create a representation of an LComponent
 */
public class FileComponent {

    public CompType type;

    public int[] pos;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int rot;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String name;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public String com;

    /**
     * Encodes the input connections on this component.  Each inner array represents one input connection and has the following structure:
     * {connected component index, connected component output number, wire state (only included for top level components)}
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int[][] input;

    /**
     * Holds the bit width of each output of the component
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int[] output;

    /**
     * State for switches used prior to file version 4. Included to properly load old files. Will always be false for new saves, and
     * therefore not included in the file.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean state;

    /**
     * State for switches used for file version 4 and later (includes multi bit support)
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int mState;

    /**
     * Show label option for lights, switches, buttons
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean showLabel;

    /**
     * Delay value for clocks
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int delay;

    /**
     * If this component is a custom, stores the index in the cTypes array for its blueprint
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int cTypeId;

    /**
     * If this component is a custom, stores the index in the cData array for the associated wire state data
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public int cDataId;

    /**
     * Creates a representation of the given component that can be serialized to a json file.
     * @param lcomp The component
     * @param compIndex The mapping of components in the same list as this component to their indexes in the list
     * @param cDataIndex The mapping of custom components to the index in the cData list where their data is stored
     * @param topLevel true if this component is in the top level components list as opposed to being inside a custom component.
     */
    public FileComponent(LComponent lcomp, Map<LComponent, Integer> compIndex, Map<Custom, Integer> cDataIndex, boolean topLevel){
        type = lcomp.getType();
        pos = new int[] {lcomp.getX(), lcomp.getY()};
        rot = lcomp.getRotation();
        name = lcomp.getName().equals(CompProperties.defaultName) ? "" : lcomp.getName();
        com = lcomp.getComments().equals(CompProperties.defaultComments) ? "" : lcomp.getComments();
        if(lcomp instanceof LabeledComponent) showLabel = ((LabeledComponent) lcomp).isShowLabel();
        if(type == CompType.SWITCH) mState = ((Switch) lcomp).getState();
        else if(type == CompType.CLOCK) delay = ((Clock) lcomp).getDelay();
        else if(type == CompType.CUSTOM) {
            cTypeId = ((OpCustom) lcomp).getTypeID();
            //if(topLevel) cDataId = cDataIndex.get((OpCustom) lcomp);
        }

        IOManager io = lcomp.getIO();
        input = new int[io.getNumInputs()][topLevel ? 4 : 3];
        for(int i = 0; i < io.getNumInputs(); i++){
            Connection conn = io.inputConnection(i);
            if(conn.numWires() > 0) {
                Wire w = conn.getWire();
                Connection source = w.getSourceConnection();
                input[i][0] = compIndex.get(source.getLcomp());
                input[i][1] = source.getIndex();
                input[i][2] = conn.getBitWidth();
                if(topLevel) input[i][3] = w.getSignal();
            }
            else input[i] = new int[] {-1, -1, conn.getBitWidth()};
        }

        output = new int[io.getNumOutputs()];
        for(int i = 0; i < output.length; i++) output[i] = io.outputConnection(i).getBitWidth();
    }

    /**
     * Needed for deserialization to work
     */
    public FileComponent(){ }

    /**
     * Converts this FileComponent back to an LComponent
     * @param version The version of the file being loaded
     * @param cTypes cTypesArray to use if this component is a custom
     * @param cData cData array to use if this component is a custom
     * @param topLevel see constructor
     * @param providedCDataId gives the index for custom data; overrides internal cDataId value if this is not a top level component
     * @return The LComponent
     */
    @JsonIgnore
    public LComponent makeComponent(int version, CustomBlueprintCompat[] cTypes, ArrayList<Integer[][]> cData, boolean topLevel, int providedCDataId){
        if(type == CompType.CUSTOM) return makeCustom(version, cTypes, cData, topLevel, providedCDataId);
        if(type == CompType.SPLIT_IN || type == CompType.SPLIT_OUT) return applyProperties(makeSplitter(version));

        LComponent lcomp = applyProperties(CompUtils.makeComponent(type.toString(), pos[0], pos[1]));
        if(version > 3) {
            IOManager io = lcomp.getIO();
            for (int i = 0; i < io.getNumInputs(); i++) io.inputConnection(i).changeBitWidth(input[i][2]);
            for (int i = 0; i < io.getNumOutputs(); i++) io.outputConnection(i).changeBitWidth(output[i]);
            if(lcomp instanceof BitWidthEntity) ((BitWidthEntity) lcomp).validateBitWidth();
        }
        if(type == CompType.SWITCH) {
            if(state) ((Switch) lcomp).setState(1);
            else ((Switch) lcomp).setState(mState);
        }
        if(type == CompType.CLOCK) ((Clock) lcomp).setDelay(delay);
        if(lcomp instanceof LabeledComponent) ((LabeledComponent) lcomp).setShowLabel(showLabel);
        if(lcomp instanceof BasicGate) ((BasicGate) lcomp).setNumInputs(input.length);
        return lcomp;
    }

    /**
     * Helper method for converting to a custom component
     * @param version The version of the file being loaded
     * @param cTypes The cTypes
     * @param cData The cData
     * @param topLevel The topLevel flag
     * @param providedCDataId the providedCDataId
     * @return Custom component
     */
    private LComponent makeCustom(int version, CustomBlueprintCompat[] cTypes, ArrayList<Integer[][]> cData, boolean topLevel, int providedCDataId){
        CustomType params = makeCustomParams(version, cTypes, cData, topLevel, providedCDataId);
        return applyProperties(new OpCustom(pos[0], pos[1], params.getLabel(), params.getContent(), params.getInnerComps(), cTypeId));
    }

    public CustomType makeCustomParams(int version, CustomBlueprintCompat[] cTypes, ArrayList<Integer[][]> cData, boolean topLevel, int providedCDataId){
        int realCDataId = topLevel ? cDataId : providedCDataId;

        CustomBlueprintCompat b = cTypes[cTypeId];
        ArrayList<LComponent> lcomps = new ArrayList<>();
        for(int i = 0; i < b.components.length; i++) {
            if(b.components[i].type.toString().equals("CUSTOM")) {
                Integer[] compData = cData.get(realCDataId)[i];
                lcomps.add(b.components[i].makeComponent(version, cTypes, cData, false, compData[compData.length - 1]));
            }
            else lcomps.add(b.components[i].makeComponent(version, cTypes, cData, false, -1));
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
                if(JSONFile.isEmptyConnection(input, version)) continue;
                Wire wire = new Wire();
                OutputPin source = lcomps.get(input[0]).getIO().outputConnection(input[1]);
                source.setSignal(cData.get(realCDataId)[i][x]);
                source.addWire(wire);
                lcomps.get(i).getIO().inputConnection(x).addWire(wire);
            }
        }
        return new CustomType(b.label, content, lcomps, cTypeId, null);
    }

    private Splitter makeSplitter(int version){
        if(version <= 3) throw new IllegalArgumentException("How could the version possibly be less than 4?");
        if(type == CompType.SPLIT_IN){
            int[] split = new int[input.length];
            for(int i = 0; i < split.length; i++) split[i] = input[i][2];
            return new SplitIn(pos[0], pos[1], split);
        }
        else return new SplitOut(pos[0], pos[1], output);
    }

    /**
     * When loading a component, sets the general properties of the component that are not specified in the constructor (rot, com, name)
     * @param lcomp The component being constructed from the file
     * @return Returns the given component for convenience
     */
    private LComponent applyProperties(LComponent lcomp){
        lcomp.setRotation(rot);
        if(name != null && !name.equals("")) lcomp.setName(name);
        if(com != null && !com.equals("")) lcomp.setComments(com);
        return lcomp;
    }
}
