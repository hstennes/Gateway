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

    /**
     * Constructs FileData object
     * @param lcomps The LComponents
     * @param customs The custom components
     */
    public FileData(ArrayList<LComponent> lcomps, ArrayList<Custom> customs) {
        this.lcomps = lcomps;
        this.customs = customs;
    }

    /**
     * Gets the LComponents
     * @return the LComponent list
     */
    public ArrayList<LComponent> getLcomps() {
        return lcomps;
    }

    /**
     * Gets the custom components
     * @return The custom list
     */
    public ArrayList<Custom> getCustoms() {
        return customs;
    }
}
