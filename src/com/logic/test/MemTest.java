package com.logic.test;

import com.logic.components.IOManager;
import com.logic.components.LComponent;
import com.logic.components.OutputPin;
import com.logic.components.Wire;
import com.logic.custom.OpCustom2;
import com.logic.engine.LogicEngine;
import com.logic.engine.LogicWorker;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class MemTest extends SwingWorker<Void, Void> {

    private static final int testLength = 100;

    private static final int maxAddress = 16384;

    private static final int maxValue = 65536;

    private OpCustom2 ram;

    public MemTest(OpCustom2 ram){
        this.ram = ram;
    }

    /**
     * Tests random reads and writes from the memory chip. Run this method on a worker thread.
     * @param ram A Ram16K chip for the Hack platform
     */
    public static void testRam16K(OpCustom2 ram){
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

    @Override
    protected Void doInBackground() throws Exception {
        testRam16K(ram);
        return null;
    }
}
