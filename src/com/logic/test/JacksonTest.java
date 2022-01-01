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

    public static void testSave(CircuitPanel cp) {
        Camera cam = cp.getCamera();
        JSONFile file = new JSONFile(new FileData(FileManager.FILE_FORMAT_VERSION, cp.lcomps,
                cp.getEditor().getCustomCreator().getCustoms(),
                new float[] {cam.getX(), cam.getY(), cam.getZoom()},
                new int[] {cp.getEditor().isSnap() ? 1 : 0, cp.isShowGrid() ? 1 : 0}));
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get("testsave2.json").toFile(), file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testJackson(){
        HashMap<String, String> stuff = new HashMap<>();
        stuff.put("Hello", "world");
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(Paths.get("testsave3.json").toFile(), stuff);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testLoad(CircuitPanel cp){
        try{
            long time1 = System.currentTimeMillis();
            JSONFile file = new ObjectMapper().readValue(Paths.get("testsave2.json").toFile(), JSONFile.class);
            long time2 = System.currentTimeMillis();
            FileData fileData = file.getFileData();
            cp.addLComps(fileData.getLcomps());
            cp.getEditor().getCustomCreator().setCustoms(fileData.getCustoms());


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
            type from CompType OR customID
            x, y, rotation
            name, comments (if present?)
            connect: [
                [id, output_number, *bit width, signal] (connected)
                [-1, -1, bit width] (nothing connected)
            ]

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
                    type from CompType of customID
                    ...
                    connect: [
                        [id, output_number, *bit width] (connected)
                        [-1, -1, bit width] (nothing connected)
                    ]
                }
            ]
        }
    ]
    cData: [
        [[comp1 wires], [comp2 wires], [custom wires, cDataId]]
    ]
     */
}
