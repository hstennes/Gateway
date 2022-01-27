package com.logic.files;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.logic.components.CompType;

public class FileNode {

    private CompType type;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int[] in;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int[][] out;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int delay;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int[] split;

    //not sure if this will be necessary, might be better to store on blueprint
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int[] outNodes;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int[] spontaneous;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int nTypeId;

    public FileNode(CompType type, int[] in, int[][] out){
        this.type = type;
        this.in = in;
        this.out = out;
    }

    public CompType getType() {
        return type;
    }

    public int[] getIn() {
        return in;
    }

    public int[][] getOut() {
        return out;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay){
        this.delay = delay;
    }

    public int[] getSplit() {
        return split;
    }

    public void setSplit(int[] split){
        this.split = split;
    }

    public int getNTypeId() {
        return nTypeId;
    }

    public void setnTypeId(int nTypeId){
        this.nTypeId = nTypeId;
    }

    public int[] getOutNodes() {
        return outNodes;
    }

    public void setOutNodes(int[] outNodes){
        this.outNodes = outNodes;
    }

    public int[] getSpontaneous() {
        return spontaneous;
    }

    public void setSpontaneous(int[] spontaneous){
        this.spontaneous = spontaneous;
    }
}
