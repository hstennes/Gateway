package com.logic.files;

import com.logic.components.Custom;
import com.logic.components.LComponent;

import java.util.ArrayList;

/**
 * Encapsulates the data sent to a new JSONFile
 */
public class FileData {

    /**
     * The list of LComponents from cp
     */
    private ArrayList<LComponent> lcomps;

    /**
     * The list of custom components from CustomCreator
     */
    private ArrayList<Custom> customs;

    private double[] camera;

    private int[] settings;

    /**
     * Constructs FileData object
     * @param lcomps The LComponents
     * @param customs The custom components
     * @param camera The camera info array (see JSONFile.camera)
     * @param settings The settings array (see JSONFile.settings)
     */
    public FileData(ArrayList<LComponent> lcomps, ArrayList<Custom> customs, double[] camera, int[] settings) {
        this.lcomps = lcomps;
        this.customs = customs;
        this.camera = camera;
        this.settings = settings;
    }

    public ArrayList<LComponent> getLcomps() {
        return lcomps;
    }

    public ArrayList<Custom> getCustoms() {
        return customs;
    }

    public double[] getCamera() {
        return camera;
    }

    public int[] getSettings() {
        return settings;
    }
}
