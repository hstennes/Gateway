package com.logic.test;

import com.logic.components.CompType;
import com.logic.components.LComponent;
import com.logic.components.RAM;
import com.logic.components.ROM;
import com.logic.ui.CircuitPanel;
import com.logic.ui.UserMessage;
import com.logic.util.CompUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Locale;

public class DebugConsole {

    public static void promptCommand(CircuitPanel cp){
        String command = JOptionPane.showInputDialog(null, "Command");
        if(command == null) return;
        command = command.toLowerCase(Locale.ROOT);
        String[] split = command.split(" ");
        ArrayList<LComponent> selection = cp.getEditor().getSelection();
        switch (split[0]) {
            case "ram":
                if (split[1].equals("clear")) {
                    if (selection.size() == 1 && selection.get(0).getType() == CompType.RAM) {
                        ((RAM) selection.get(0)).clear();
                    } else badSelection(cp, split[0]);
                } else {
                    unknownCommandExt(cp, split[0]);
                }
                break;
            case "rom":
                if (split[1].equals("load")) {
                    if (selection.size() == 1 && selection.get(0).getType() == CompType.ROM) {
                        int[] program = CompUtils.promptROMProgram();
                        ((ROM) selection.get(0)).setProgram(program);
                    } else badSelection(cp, split[0]);
                } else {
                    unknownCommandExt(cp, split[0]);
                }
                break;
            case "test":
                if (selection.size() == 1) {
                    new ChipTester(cp.getEditor().getSelection().get(0)).execute();
                } else badSelection(cp, split[0]);
                break;
            default:
                unknownBaseCommand(cp, split[0]);
        }
    }

    private static void badSelection(CircuitPanel cp, String baseCommand){
        cp.dispMessage(new UserMessage(cp, baseCommand + " command not applicable to selection", 3000));
    }

    private static void unknownCommandExt(CircuitPanel cp, String baseCommand){
        cp.dispMessage(new UserMessage(cp, "Unknown chip command for " + baseCommand, 3000));
    }

    private static void unknownBaseCommand(CircuitPanel cp, String baseCommand){
        cp.dispMessage(new UserMessage(cp, "Command '" + baseCommand + "' not recognized", 3000));
    }
}
