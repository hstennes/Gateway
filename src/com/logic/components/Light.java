package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

/**
 * A simple output component that appears lit up when it receives a high signal and off when it receives a low signal
 * @author Hank Stennes
 *
 */
public class Light extends LabeledComponent {
	
	private static final long serialVersionUID = 1L;
		
	/**
	 * Constructs a new Light
	 * @param x The x position
	 * @param y The y position
	 */
	public Light(int x, int y) {
		super(x, y, CompType.LIGHT);
		drawer.setImages(new int[] {7, 8});
		io.addConnection(30, 100, Connection.INPUT, Constants.DOWN);
	}
	
	@Override
	public void update(LogicEngine engine) {}

	@Override
	public int getActiveImageIndex(){
		if(io.getInput(0)) return 1;
		return 0;
	}
	
	@Override
	public LComponent makeCopy() {
		Light result = new Light(x, y);
		result.setRotation(rotation);
		result.setName(getName());
		result.setShowLabel(isShowLabel());
		return result;
	}
}
