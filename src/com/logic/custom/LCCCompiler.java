package com.logic.custom;

import com.logic.components.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LCCCompiler {

    public static int[] nodeCompile(Node[] nodes){
        HashMap<Node, Integer> nodeIndex = new HashMap<>();
        for(int i = 0; i < nodes.length; i++) nodeIndex.put(nodes[i], i);

        HashMap<Integer, Integer> sigToLevel = new HashMap<>();
        HashMap<Integer, ArrayList<Node>> levelToNode = new HashMap<>();
        HashSet<Node> active = new HashSet<>();

        int expectedLeveledCount = nodeInitialize(nodes, sigToLevel, levelToNode, active);
        if(expectedLeveledCount == -1) return null;
        int globalMaxLevel = -1;
        int leveledCount = 0;
        HashSet<Node> nextActive = new HashSet<>();

        while(active.size() > 0) {
            HashMap<Integer, Integer> tempSigToLevel = new HashMap<>();
            for (Node node : active) {
                int maxLevel = -1;
                boolean successful = true;
                for (int i = 0; i < node.in.length; i++) {
                    if (node.in[i] != 0) {
                        int source = node.in[i];
                        if (sigToLevel.containsKey(source)) {
                            int sourceLevel = sigToLevel.get(source);
                            if (sourceLevel > maxLevel) maxLevel = sourceLevel;
                        } else {
                            successful = false;
                            break;
                        }
                    }
                }
                if (successful) {
                    //if the component has already been assigned a level, the circuit has back propagation and can't use LCC
                    if(sigToLevel.containsKey(node.address))
                        return null;
                    leveledCount++;
                    int newLevel = maxLevel + 1;
                    if(newLevel > globalMaxLevel) globalMaxLevel = newLevel;
                    for(int i = 0; i < node.getNumOutputs(); i++) tempSigToLevel.put(node.address + i, newLevel);
                    nodeUpdateListMap(levelToNode, newLevel, node);
                    nodeMarkNext(nodes, node, nextActive);
                }
            }
            sigToLevel.putAll(tempSigToLevel);
            tempSigToLevel.clear();
            active = nextActive;
            nextActive = new HashSet<>();
        }
        if(leveledCount != expectedLeveledCount) return null;
        return nodeConvertToIntArray(levelToNode, nodeIndex, globalMaxLevel, leveledCount);
    }

    public static int[] compile(ArrayList<LComponent> lcomps, Map<LComponent, Integer> nbIndex){
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
        return convertToIntArray(levelToComp, nbIndex, globalMaxLevel, leveledCount);
    }

    private static void nodeMarkNext(Node[] nodes, Node node, HashSet<Node> nextActive){
        for (int i = 0; i < node.mark.length; i++) {
            for (int w = 0; w < node.mark[i].length; w++) {
                Node markComp = nodes[node.mark[i][w]];
                nextActive.add(markComp);
            }
        }
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

    private static int nodeInitialize(Node[] nodes,
                                  HashMap<Integer, Integer> sigToLevel,
                                  HashMap<Integer, ArrayList<Node>> levelToNode,
                                  HashSet<Node> active){
        int leveledCount = 0;
        for(Node node : nodes){
            if(node instanceof StartNode || node instanceof PlaceholderNode) {
                nodeMarkNext(nodes, node, active);
                for(int i = 0; i < node.getNumOutputs(); i++) sigToLevel.put(node.address + i, -1);
            }
            else {
                boolean connected = false;
                for(int i = 0; i < node.in.length; i++){
                    if(node.in[i] != 0){
                        connected = true;
                        break;
                    }
                }
                if(!connected){
                    nodeMarkNext(nodes, node, active);
                    for(int i = 0; i < node.getNumOutputs(); i++) sigToLevel.put(node.address + i, -1);
                    nodeUpdateListMap(levelToNode, 0, node);
                }
                else leveledCount++;
            }
        }
        return leveledCount;
    }

    private static int initialize(ArrayList<LComponent> lcomps,
                                                    HashMap<LComponent, Integer> compToLevel,
                                                    HashMap<Integer, ArrayList<LComponent>> levelToComp,
                                                    HashSet<LComponent> active){
        int leveledCount = 0;
        for(LComponent lcomp : lcomps){
            if(lcomp.getType() == CompType.SWITCH ||
                    lcomp.getType() == CompType.BUTTON ||
                    lcomp.getType() == CompType.ZERO ||
                    lcomp.getType() == CompType.ONE){
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

    private static int[] nodeConvertToIntArray(HashMap<Integer, ArrayList<Node>> levelToNode,
                                               Map<Node, Integer> nodeIndex,
                                               int globalMaxLevel, int leveledCount){
        int[] levels = new int[leveledCount];
        int index = 0;
        for(int i = 0; i < globalMaxLevel + 1; i++){
            ArrayList<Node> nodeList = levelToNode.get(i);
            for(Node node : nodeList) {
                levels[index] = nodeIndex.get(node);
                index++;
            }
        }
        return levels;
    }

    private static int[] convertToIntArray(HashMap<Integer, ArrayList<LComponent>> levelToComp,
                                             Map<LComponent, Integer> nbIndex,
                                             int globalMaxLevel, int leveledCount){
        int[] levels = new int[leveledCount];
        int index = 0;
        for(int i = 0; i < globalMaxLevel + 1; i++){
            ArrayList<LComponent> compList = levelToComp.get(i);
            for(LComponent lcomp : compList) {
                levels[index] = nbIndex.get(lcomp);
                index++;
            }
        }
        return levels;
    }

    private static void nodeUpdateListMap(HashMap<Integer, ArrayList<Node>> map, int index, Node node){
        if(map.containsKey(index)) map.get(index).add(node);
        else {
            ArrayList<Node> nodeList = new ArrayList<>();
            nodeList.add(node);
            map.put(index, nodeList);
        }
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
