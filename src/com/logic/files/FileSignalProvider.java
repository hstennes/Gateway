package com.logic.files;

import com.logic.components.IOManager;
import com.logic.components.InputPin;
import com.logic.components.LComponent;
import com.logic.components.OutputPin;
import com.logic.custom.CustomNode;
import com.logic.custom.CustomType;
import com.logic.custom.OpCustom2;
import com.logic.custom.SignalProvider;

import java.util.ArrayList;

public class FileSignalProvider {

    public ArrayList<int[][]> data;

    public FileSignalProvider(){ }

    /**
     * Adds a signal provider to be serialized and returns the index where it was stored
     * @param sp The SignalProvider
     * @return The location of the entry point to the signal data
     */
    public int addSignalProvider(SignalProvider sp){
        if(data == null) data = new ArrayList<>();
        populateData(sp);
        return data.size() - 1;
    }

    private void populateData(SignalProvider sp){
        int[][] signals = sp.getRawSignals();
        int[][] newData = new int[signals.length + 1][];
        System.arraycopy(signals, 0, newData, 1, signals.length);

        SignalProvider[] nested = sp.getAllNested();
        int[] refs = new int[nested.length];
        for(int i = 0; i < refs.length; i++){
            populateData(nested[i]);
            refs[i] = data.size() - 1;
        }

        newData[0] = refs;
        data.add(newData);
    }

    /**
     * De-serializes the SignalProvider saved at the given index
     * @param index The index from addSignalProvider
     * @return The SignalProvider
     */
    public SignalProvider createSignalProvider(int index){
        int[][] spData = data.get(index);
        int[] refs = spData[0];

        SignalProvider[] nested = new SignalProvider[refs.length];
        for(int i = 0; i < nested.length; i++) nested[i] = createSignalProvider(refs[i]);

        int[][] rawData = new int[spData.length - 1][];
        System.arraycopy(spData, 1, rawData, 0, rawData.length);
        return new SignalProvider(rawData, nested);
    }

    public int[] createSigs(int[] flat, int cDataID, int sigOffset){
        int[][] spData = data.get(cDataID);
        int address = sigOffset + 1;
        for(int i = 1; i < spData.length; i++){
            System.arraycopy(spData[i], 0, flat, address, spData[i].length);
            address += spData[i].length;
        }

        int[] refs = spData[0];
        for(int i = 0; i < refs.length; i++){
            createSigs(flat, refs[i], address);
        }
        return flat;
    }

    public static SignalProvider buildSPFromOldCData(CustomType type, ArrayList<int[][]> oldCData, int oldCDataID){
        /*int[][] signals = new int[type.nodeBox.getNodes().length][];
        SignalProvider[] nested = new SignalProvider[type.getCustomCount()];

        for(int i = 0; i < type.lcomps.size(); i++){
            int[] compData = oldCData.get(oldCDataID)[i];
            LComponent lcomp = type.lcomps.get(i);
            IOManager io = lcomp.getIO();
            for(int x = 0; x < io.getNumInputs(); x++){
                InputPin inputPin = io.inputConnection(x);
                if(inputPin.numWires() == 0) continue;
                OutputPin outputPin = inputPin.getWire(0).getSourceConnection();
                LComponent source = outputPin.getLcomp();
                if(type.compIndex.containsKey(source)){
                    int nodeID = type.compIndex.get(source);
                    if(signals[nodeID] == null) signals[nodeID] = new int[source.getIO().getNumOutputs()];
                    signals[nodeID][outputPin.getIndex()] = compData[x];
                }
            }
            if(lcomp instanceof OpCustom2){
                int innerCDataID = compData[compData.length - 1];
                int spIndex = ((CustomNode) type.nodeBox.getNodes()[type.compIndex.get(lcomp)]).getSpIndex();
                nested[spIndex] = buildSPFromOldCData(((OpCustom2) lcomp).getCustomType(), oldCData, innerCDataID);
            }
        }
        return new SignalProvider(signals, nested);*/
        //TODO file reading is very, very broken

        return null;
    }
}
