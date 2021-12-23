package com.logic.util;

import com.logic.components.BasicGate;
import com.logic.components.CompType;
import com.logic.components.Splitter;
import com.logic.ui.CircuitPanel;

import java.util.HashMap;

/**
 * Used for performance testing
 * @author Hank Stennes
 *
 */
public class Debug {

	/**
	 * A list of times that each debug entry was started and a name for each entry
	 */
	private static HashMap<String, Long> times = new HashMap<String, Long>();
	
	/**
	 * Starts a new debug entry
	 * @param name The name of the entry
	 */
	public static void start(String name) {
		times.put(name, System.currentTimeMillis());
	}
	
	/**
	 * Ends the debug entry with the given name and prints the time between the start and end
	 * @param name The name of the entry to end
	 */
	public static void end(String name) {
		System.out.println(name + ": " + (System.currentTimeMillis() - times.get(name)) + "ms");
		times.remove(name);	
	}

	/**
	 * Fills the circuit with connected XNOR gates to test rendering performance
	 * @param cp The CircuitPanel
	 * @param includeWires Add a wire connecting each gate to the next
	 */
	public static void testCircuit1(CircuitPanel cp, boolean includeWires){

		BasicGate lastGate = null;

		/*for(int x = -CircuitPanel.GRID_RENDER_X; x < CircuitPanel.GRID_RENDER_X; x += 120){
			for(int y = -CircuitPanel.GRID_RENDER_Y; y < CircuitPanel.GRID_RENDER_Y; y += 120){
				BasicGate newGate = new BasicGate(x, y, CompType.XNOR);
				if(includeWires && lastGate != null){
					Wire w = new Wire();
					lastGate.getIO().connectionAt(0, Connection.OUTPUT).addWire(w);
					newGate.getIO().connectionAt(0).addWire(w);
					cp.addWire(w);
				}
				cp.addLComp(newGate);
				lastGate = newGate;
			}
		}*/
	}

	public static void testCircuit2(CircuitPanel cp){
		cp.addLComp(new Splitter(0, 0, CompType.SPLIT_OUT, new int[] {1, 2, 3, 1}));
	}
}
