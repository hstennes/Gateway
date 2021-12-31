package com.logic.ui;

import com.logic.components.CompType;
import com.logic.components.SplitOut;
import com.logic.components.Splitter;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class SplitterOptionPanel extends JPanel{

    /**
     * The text field to enter the bit width split
     */
    private final JTextField bitSplitField;

    /**
     * The type box to select input or output splitter
     */
    private final JComboBox<String> typeBox;

    /**
     * Creates a new SplitterOptionPanel
     */
    public SplitterOptionPanel(){
        setLayout(new FlowLayout(FlowLayout.CENTER, 6, 5));
        bitSplitField = new JTextField(10);
        typeBox = new JComboBox<>(new String[] {"Input splitter", "Output splitter"});

        add(new JLabel("Bit width split"));
        add(bitSplitField);
        add(typeBox);
    }

    /**
     * Returns the bit width split, or null if the user entered an invalid split
     * @return The bit width split
     */
    public int[] getSplit(){
        String[] strSplit = bitSplitField.getText().split(",");
        if(strSplit.length == 1) return null;

        try {
            int[] split = new int[strSplit.length];
            for (int i = 0; i < strSplit.length; i++) split[i] = Integer.parseInt(strSplit[i].strip());
            return split;
        }
        catch (NumberFormatException e){
            return null;
        }
    }

    /**
     * Returns the selected type of splitter (SPLIT_IN or SPLIT_OUT)
     * @return The splitter type
     */
    public CompType getType(){
        return Objects.equals(typeBox.getSelectedItem(), "Input splitter") ? CompType.SPLIT_IN : CompType.SPLIT_OUT;
    }
}
