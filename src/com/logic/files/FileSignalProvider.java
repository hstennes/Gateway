package com.logic.files;

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
}
