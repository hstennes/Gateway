package com.logic.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logic.components.LComponent;
import com.logic.files.JSONFile;
import com.logic.ui.CircuitPanel;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class JacksonTest {

    public static void testSave(List<LComponent> lcomps) {
        JSONFile file = new JSONFile(lcomps);
        try {
            new ObjectMapper().writeValue(Paths.get("testsave.json").toFile(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testLoad(CircuitPanel cp){
        try{
            long time1 = System.currentTimeMillis();
            JSONFile file = new ObjectMapper().readValue(Paths.get("testsave.json").toFile(), JSONFile.class);
            long time2=  System.currentTimeMillis();
            cp.addLComps(file.getLComps());
            long time3 = System.currentTimeMillis();

            System.out.println("JSON parsing: " + (time2 - time1));
            System.out.println("Creating components: " + (time3 - time2));

            cp.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    File format:
    components: [
        {
            id assigned when saving using list index
            type from CompType OR customID
            x, y, rotation
            name, comments (if present?)
            connect: [
                [id, output_number, signal]
            ]

            state (just for switch?)
            delay (just for clock)
        }
    ]
    customs: [
        {
            typeID
            label
            components: [
                {
                    Same as above
                }
            ]
        }
    ]
     */
}
