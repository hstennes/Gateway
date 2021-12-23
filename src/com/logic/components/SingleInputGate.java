package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.engine.LogicFunctions;
import com.logic.util.Constants;

/**
 * This class represents not gates and buffers
 * @author Hank Stennes
 *
 */
public class SingleInputGate extends LComponent implements BitWidthEntity{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The index of the function that either inverts its input or returns its input unchanged depending on the type of component
	 */
	private int function;
	
	/**
	 * Constructs a new SingleInputGate
	 * @param x The x position
	 * @param y The y position
	 * @param type The type (CompType.NOT, CompType.BUFFER)
	 */
	public SingleInputGate(int x, int y, CompType type) {
		super(x, y, type);
		if(type == CompType.BUFFER) function = 1;
		else if(type == CompType.NOT) function = 0;
		setImages(new int[] {0});

		io.addConnection(-25, 40, Connection.INPUT, Constants.LEFT);
		io.addConnection(105, 40, Connection.OUTPUT, Constants.RIGHT);
	}
	
	@Override
	public void update(LogicEngine engine) {
		io.setOutput(0, LogicFunctions.singleInput.get(function).apply(io.getInput(0)), engine);
	}
	
	@Override
	public LComponent makeCopy() {
		SingleInputGate result = new SingleInputGate(x, y, type);
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
		io.inputConnection(0).changeBitWidth(bitWidth);
		io.outputConnection(0).changeBitWidth(bitWidth);
	}
}
