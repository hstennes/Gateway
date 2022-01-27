package com.logic.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logic.files.FileData;
import com.logic.files.FileManager;
import com.logic.files.JSONFile;
import com.logic.input.Camera;
import com.logic.ui.CircuitPanel;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

public class JacksonTest {

    public static void main(String[] args){
        JacksonTestObject file = new JacksonTestObject(null);
        try {
            new ObjectMapper().writeValue(Paths.get("C:/Users/HPSte/myfile.json").toFile(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    FILE VERSION 4:

    components: [
        {
            type from CompType
            x, y, rotation
            name, comments (if present?)
            input: [
                [id, output_number, *bit width, signal] (connected)
                [-1, -1, bit width] (nothing connected)
            ]
            *output: [output 1 bit width, output 2 bit width...]

            state (just for switch)
            delay (just for clock)
            cTypeId, cDataId (just for custom)
        }
    ]
    cTypes: [
        {
            label
            components: [
                {
                    type from CompType
                    ...
                    input: [
                        [id, output_number, *bit width] (connected)
                        [-1, -1, bit width] (nothing connected)
                    ]
                    output: [output 1 bit width, output 2 bit width...]
                }
            ]
        }
    ]
    eExamples: [
        {
            A custom component, identical to a custom component that could appear in "components", with its own cData index.
        }
    ]
    cData: [
        [[comp1 wires], [comp2 wires], [custom wires, cDataId]],
        [[comp1 wires], [comp2 wires], [custom wires, cDataId]]
    ]
     */

    /*
    input and output array format across file versions:

    version 3 and below:
    input array, inner array for each connection with following format:
        "top level" (present in the circuit, not inside of a custom): [id, output number, signal]
        "inner level" (inside of at least one custom component): [id, output number]
        nothing connected: []

    version 4:
    input array, inner array for each connection with following format:
        "top level": [id, output number, bit width, signal]
        "inner level": [id, output number, bit width]
        nothing connected: [-1, -1, bit width]
    new 1D output array, just lists bit width for each output connection

    version 5:
    input array, inner array for each connection with following format:
    note: no more "top level" and "inner level". FileComponents are either present directly in the circuit, or present in the CustomSource
    section of a CustomBlueprint. In either case, signal data needs to be stored.
        wire connected: [id, output number, bit width, signal]
        nothing connected: [bit width]
     */

    /*
    Major changes to support OpCustom (file format version 5)

    cTypes will hold both custom source and compiled representation for each type
    new "CustomBlueprint", notably different from current CustomBlueprint. Contains two parts:
        custom source LComponents:
        The exact same data as current CustomBlueprint. However, these components will now hold their own signal values
        Saving - easy because this is the same as current CustomBlueprint code
        Loading - easy because this is the same as FileComponent.makeCustom, just returning Custom constructor params rather than the actual component

        compiled structure:
        essentially a copy of how the structure is stored in memory. However, signals are omitted because they are held in cData.
        Saving - Need standardized data format for Nodes. Also need to put signals in cData.
        Loading - File format will be similar to the Node data. Load back cData into signals. Need to recursively load in inner NodeBoxes

    cExamples:
    No longer needed, because the program will now store CustomSources rather than actual custom components for the internal list of types.

    Backwards compatibility is easy. Use the current CustomBlueprint to create OpCustom since it shares the same constructor as Custom
     */
}
