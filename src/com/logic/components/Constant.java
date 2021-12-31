package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.util.Constants;

/**
 * A component that constantly outputs either a high or low signal, depending on its type
 * @author Hank Stennes
 *
 */
public class Constant extends LComponent implements BitWidthEntity{
	
	private static final long serialVersionUID = 1L;
		
	/**
	 * Constructs a new Constant 
	 * @param x The x position of the constant
	 * @param y The y position of the constant
	 * @param type The type of constant (CompType.ZERO for low signal, CompType.ONE for high signal)
	 */
	public Constant(int x, int y, CompType type) {
		super(x, y, type);
		if(type == CompType.ONE) setImages(new int[] {10});
		else if(type == CompType.ZERO) setImages(new int[] {9});
		io.addConnection(80, 40, Connection.OUTPUT, Constants.RIGHT);
	}
	
	@Override
	public void update(LogicEngine engine) {
		if(type == CompType.ONE) io.setOutputOld(0, true, engine);
		else io.setOutputOld(0, false, engine);
	}
	
	@Override
	public LComponent makeCopy() {
		Constant result = new Constant(x, y, type);
		result.setRotation(rotation);
		result.setName(getName());
		return result;
	}

	@Override
	public int getBitWidth() {
		return io.outputConnection(0).getBitWidth();
	}

	@Override
	public void changeBitWidth(int bitWidth) {
		io.outputConnection(0).changeBitWidth(bitWidth);
	}

	@Override
	public void validateBitWidth() { }
}
