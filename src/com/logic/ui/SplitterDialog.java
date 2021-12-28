package com.logic.ui;

import com.logic.components.SplitOut;
import com.logic.components.Splitter;

import javax.swing.*;

public class SplitterDialog {

    public static Splitter getInputAndCreate(int x, int y){
        String input = JOptionPane.showInputDialog("Enter bit width split");
        String[] strSplit = input.split(",");
        int[] split = new int[strSplit.length];
        for(int i = 0; i < strSplit.length; i++) split[i] = Integer.parseInt(strSplit[i].strip());
        return new SplitOut(x, y, split);
    }

    private static JPanel getJPanel(){
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Bit width split");
        JTextField field = new JTextField();
        JComboBox<String> types = new JComboBox<>(new String[] {"Input splitter", "Output splitter"});

        panel.add(label);
        panel.add(field);
        panel.add(types);

        return panel;
    }

}
