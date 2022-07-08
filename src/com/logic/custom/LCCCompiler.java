package com.logic.custom;

import com.logic.components.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LCCCompiler {

    public static int[][] compile(ArrayList<LComponent> lcomps, Map<LComponent, Integer> nbIndex){
        HashMap<LComponent, Integer> compToLevel = new HashMap<>();
        HashMap<Integer, ArrayList<LComponent>> levelToComp = new HashMap<>();
        HashSet<LComponent> active = new HashSet<>();
        int expectedLeveledCount = initialize(lcomps, compToLevel, levelToComp, active);
        if(expectedLeveledCount == -1) return null;
        int globalMaxLevel = -1;
        int leveledCount = 0;
        HashSet<LComponent> nextActive = new HashSet<>();

        while(active.size() > 0) {
            HashMap<LComponent, Integer> tempCompToLevel = new HashMap<>();
            for (LComponent lcomp : active) {
                IOManager io = lcomp.getIO();
                int maxLevel = -1;
                boolean successful = true;
                for (int i = 0; i < io.getNumInputs(); i++) {
                    InputPin input = io.inputConnection(i);
                    if (input.numWires() > 0) {
                        LComponent source = input.getWire().getSourceConnection().getLcomp();
                        if (compToLevel.containsKey(source)) {
                            int sourceLevel = compToLevel.get(source);
                            if (sourceLevel > maxLevel) maxLevel = sourceLevel;
                        } else {
                            successful = false;
                            break;
                        }
                    }
                }
                if (successful) {
                    //if the component has already been assigned a level, the circuit has back propagation and can't use LCC
                    if(compToLevel.containsKey(lcomp))
                        return null;
                    leveledCount++;
                    int newLevel = maxLevel + 1;
                    if(newLevel > globalMaxLevel) globalMaxLevel = newLevel;
                    tempCompToLevel.put(lcomp, newLevel);
                    updateMapList(levelToComp, newLevel, lcomp);
                    markNext(lcomp, nextActive);
                }
            }
            compToLevel.putAll(tempCompToLevel);
            tempCompToLevel.clear();
            active = nextActive;
            nextActive = new HashSet<>();
        }
        if(leveledCount != expectedLeveledCount) return null;
        return convertToIntArray(levelToComp, nbIndex, globalMaxLevel);
    }

    private static void markNext(LComponent lcomp, HashSet<LComponent> nextActive){
        IOManager io = lcomp.getIO();
        for (int i = 0; i < io.getNumOutputs(); i++) {
            OutputPin output = io.outputConnection(i);
            for (int w = 0; w < output.numWires(); w++) {
                LComponent markComp = output.getWire(w).getDestConnection().getLcomp();
                if(markComp.getType() == CompType.LIGHT) continue;
                nextActive.add(markComp);
            }
        }
    }

    private static int initialize(ArrayList<LComponent> lcomps,
                                                    HashMap<LComponent, Integer> compToLevel,
                                                    HashMap<Integer, ArrayList<LComponent>> levelToComp,
                                                    HashSet<LComponent> active){
        int leveledCount = 0;
        for(LComponent lcomp : lcomps){
            if(lcomp.getType() == CompType.CUSTOM) return -1;
            else if(lcomp.getType() == CompType.SWITCH || lcomp.getType() == CompType.BUTTON){
                markNext(lcomp, active);
                compToLevel.put(lcomp, -1);
            }
            else {
                IOManager io = lcomp.getIO();
                boolean connected = false;
                for(int i = 0; i < io.getNumInputs(); i++){
                    InputPin input = io.inputConnection(i);
                    if(input.numWires() > 0) {
                        connected = true;
                        break;
                    }
                }
                if(!connected){
                    markNext(lcomp, active);
                    compToLevel.put(lcomp, -1);
                    updateMapList(levelToComp, 0, lcomp);
                }
                else if(lcomp.getType() != CompType.LIGHT) leveledCount++;
            }
        }
        return leveledCount;
    }

    private static int[][] convertToIntArray(HashMap<Integer, ArrayList<LComponent>> levelToComp,
                                             Map<LComponent, Integer> nbIndex,
                                             int globalMaxLevel){
        int[][] levels = new int[globalMaxLevel + 1][];
        for(int i = 0; i < levels.length; i++){
            ArrayList<LComponent> compList = levelToComp.get(i);
            levels[i] = new int[compList.size()];
            int j = 0;
            for(LComponent lcomp : compList) {
                levels[i][j] = nbIndex.get(lcomp);
                j++;
            }
        }
        return levels;
    }

    private static void updateMapList(HashMap<Integer, ArrayList<LComponent>> map, int index, LComponent lcomp){
        if(map.containsKey(index)) map.get(index).add(lcomp);
        else {
            ArrayList<LComponent> compList = new ArrayList<>();
            compList.add(lcomp);
            map.put(index, compList);
        }
    }
}
