package com.logic.test;

import com.logic.components.*;
import com.logic.custom.OpCustom2;
import com.logic.engine.LogicEngine;
import com.logic.engine.LogicWorker;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class ChipTester extends SwingWorker<Void, Void> {

    private static final int testLength = 100;

    private static final int maxAddress = 16384;

    private static final int maxValue = 65536;

    private LComponent chip;

    public ChipTester(LComponent chip){
        this.chip = chip;
    }

    /**
     * Tests random reads and writes from the memory chip. Run this method on a worker thread.
     * @param ram A Ram16K chip for the Hack platform
     */
    private void testRam16K(LComponent ram){
        //clock, address, load, value (not sure why its in this order)
        ArrayList<LComponent> ramList = new ArrayList<>();
        ramList.add(ram);

        IOManager io = ram.getIO();
        OutputPin wClock = io.inputConnection(0).getWire(0).getSourceConnection();
        OutputPin wAddress = io.inputConnection(1).getWire(0).getSourceConnection();
        OutputPin wLoad = io.inputConnection(2).getWire(0).getSourceConnection();
        OutputPin wValue = io.inputConnection(3).getWire(0).getSourceConnection();
        OutputPin dataOut = io.outputConnection(0);

        Random rand = new Random();
        int[] addresses = new int[testLength];
        int[] values = new int[testLength];

        wClock.setSignal(1);
        new LogicEngine(ramList).doLogic();
        long start = System.currentTimeMillis();
        for(int i = 0; i < testLength; i++){
            int address = rand.nextInt(maxAddress);
            addresses[i] = address;

            wClock.setSignal(0);
            wAddress.setSignal(address);
            new LogicEngine(ramList).doLogic();

            int value = rand.nextInt(maxValue);
            boolean load = rand.nextBoolean();
            values[i] = load ? value : dataOut.getSignal();
            wLoad.setSignal(load ? 1 : 0);
            wValue.setSignal(value);
            new LogicEngine(ramList).doLogic();
            wClock.setSignal(1);
            new LogicEngine(ramList).doLogic();
        }
        System.out.println("Finished writing. Avg cycle time " +
                (System.currentTimeMillis() - start) / testLength);

        wLoad.setSignal(0);
        for(int i = 0; i < testLength; i++){
            wAddress.setSignal(addresses[i]);
            new LogicEngine(ramList).doLogic();
            if(dataOut.getSignal() == values[i])
                System.out.println("Correct value of " + values[i] + " at " + addresses[i]);
            else
                System.out.println("MEMORY ERROR: expected " + values[i] + ", read " + dataOut.getSignal());
        }
    }

    private void testALU(LComponent alu) {
        int[][] data = null;
        try {
            data = loadCMP("/ALU.cmp");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<LComponent> aluList = new ArrayList<>();
        aluList.add(alu);

        IOManager io = alu.getIO();
        OutputPin x = io.inputConnection(0).getWire(0).getSourceConnection();
        OutputPin y = io.inputConnection(1).getWire(0).getSourceConnection();
        OutputPin nx = io.inputConnection(2).getWire(0).getSourceConnection();
        OutputPin zx = io.inputConnection(3).getWire(0).getSourceConnection();
        OutputPin ny = io.inputConnection(4).getWire(0).getSourceConnection();
        OutputPin zy = io.inputConnection(5).getWire(0).getSourceConnection();
        OutputPin f = io.inputConnection(6).getWire(0).getSourceConnection();
        OutputPin no = io.inputConnection(7).getWire(0).getSourceConnection();
        OutputPin out = io.outputConnection(0);
        OutputPin zr = io.outputConnection(1);
        OutputPin ng = io.outputConnection(2);

        for(int[] test : data){
            x.setSignal(test[0]);
            y.setSignal(test[1]);
            nx.setSignal(test[3]);
            zx.setSignal(test[2]);
            ny.setSignal(test[5]);
            zy.setSignal(test[4]);
            f.setSignal(test[6]);
            no.setSignal(test[7]);
            new LogicEngine(aluList).doLogic();

            if(out.getSignal() != test[8]) System.out.println("OUT FAILED");
            if((zr.getSignal() & 1) != (test[9] & 1)) System.out.println("ZR FAILED");
            if(ng.getSignal() != test[10]) System.out.println("NG FAILED");
        }
        System.out.println("DONE TESTING");
    }

    private void simulateMaxClockSpeed(LComponent clock){
        //Debug.start("PONG_TEST");
        boolean clockState = false;
        long iterations = 0;
        long startTime = System.currentTimeMillis();
        ArrayList<LComponent> clockList = new ArrayList<>();
        clockList.add(clock);
        IOManager io = clock.getIO();
        while(io.outputConnection(0).numWires() > 0 /*iterations < 1000000*/){
            ((Clock) clock).setOn(clockState);
            new LogicEngine(clockList).doLogic();
            clockState = !clockState;
            iterations++;
        }

        float hz = ((float) iterations / (System.currentTimeMillis() - startTime)) * 1000;
        System.out.println("Averaged " + hz + "HZ");
    }

    private int[][] loadCMP(String path) throws IOException {
        File file = new File(Objects.requireNonNull(getClass().getResource(path)).getPath());
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        StringBuilder content = new StringBuilder();
        String line;
        br.readLine();
        while ((line = br.readLine()) != null) {
            content.append(line).append("\n");
        }

        String[] lines = content.substring(0, content.length() - 1).split("\n");
        int[][] data = new int[lines.length][];
        for(int i = 0; i < data.length; i++) {
            String[] lineSplit = lines[i].substring(1).split("\\|");
            data[i] = new int[lineSplit.length];
            for(int n = 0; n < data[i].length; n++) data[i][n] = Integer.parseInt(lineSplit[n].trim(), 2);
        }
        return data;
    }

    @Override
    protected Void doInBackground() {
        if(chip instanceof OpCustom2) {
            switch (((OpCustom2) chip).getCustomType().label) {
                case "RAM16K":
                    testRam16K(chip);
                    break;
                case "ALU":
                    testALU(chip);
                    break;
            }
        }
        else if(chip instanceof Clock) simulateMaxClockSpeed(chip);
        else if(chip instanceof RAM) testRam16K(chip);
        return null;
    }
}
