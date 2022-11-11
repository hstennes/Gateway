package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

/**
 * A digit display component that shows a decimal value based on four bits of input
 * @author Hank Stennes
 *
 */
public class Display extends LComponent {

	/**
	 * Letters used to represent values greater than 9
	 */
	public static final String[] VALUE_STRS = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "b", "C", "d", "E", "F"};
	
	/**
	 * Constructs a new Display
	 * @param x The x position of the display
	 * @param y The y position of the display
	 */
	public Display(int x, int y) {
		super(x, y, CompType.DISPLAY);
		setImages(new int[] {13});
		io.addConnection(-20, 2, Connection.INPUT, Constants.LEFT);
		io.addConnection(-20, 34, Connection.INPUT, Constants.LEFT);
		io.addConnection(-20, 66, Connection.INPUT, Constants.LEFT);
		io.addConnection(-20, 98, Connection.INPUT, Constants.LEFT);
	}
	
	@Override
	public void update(LogicEngine engine) { }

	public int getValue() {
		int val = 0;
		for(int i = io.getNumInputs() - 1; i >= 0; i--){
			val = 2 * val + io.getInputStrict(i);
		}
		return val;
	}
	
	@Override
	public LComponent makeCopy() {
		Display result = new Display(x, y);
		result.setRotation(rotation);
		result.setName(getName());
		return result;
	}
}
