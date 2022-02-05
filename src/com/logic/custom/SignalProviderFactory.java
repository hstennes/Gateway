package com.logic.custom;

import com.logic.components.LComponent;

import java.util.ArrayList;

public class SignalProviderFactory {

    private ArrayList<LComponent> lcomps;

    private LComponent[][] content;

    public SignalProviderFactory(ArrayList<LComponent> lcomps, LComponent[][] content){
        this.lcomps = lcomps;
        this.content = content;
    }

    public ArraySignalProvider makeSignalProvider(){

        /*int nestedCounter = 0;
        int[][] signals = new int[lcomps.size()][];

        for(int i = 0; i < lcomps.size(); i++){

        }

        Map<LComponent, Integer> compIndex = new HashMap<>();
        int compID = 0;

        for(int s = Constants.RIGHT; s <= Constants.UP; s++) {
            LComponent[] side = content[s];
            if (side == null) continue;
            for (LComponent lComponent : side) {
                if (lComponent instanceof Switch) {
                    compIndex.put(lComponent, compID);
                    compID++;
                }
            }
        }
        for (LComponent lcomp : lcomps) {
            if(lcomp instanceof Light || lcomp instanceof Switch) continue;
            compIndex.put(lcomp, compID);
            compID++;
        }*/

        return null;
    }
}
