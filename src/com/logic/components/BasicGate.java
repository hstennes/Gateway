package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.engine.LogicFunctions;
import com.logic.ui.Renderer;
import com.logic.util.CompUtils;
import com.logic.util.ConnectionLayout;
import com.logic.util.Constants;

import java.awt.*;

/**
 * The class that represents all basic logic gates, which include and, nand, or, nor, xor, and xnor
 * @author Hank Stennes
 *
 */
public class BasicGate extends LComponent implements BitWidthEntity {
		
	private static final long serialVersionUID = 1L;

	/**
	 * The maximum, minimum, and default number of inputs a BasicGate can have
	 */
	public static final int MIN_INPUTS = 2, MAX_INPUTS = 10, DEFAULT_INPUTS = 2;

	/**
	 * The index of the Function that will be used to evaluate the output of this gate
	 */
	private int function;

	/**
	 * Stores a bitmask that optionally inverts the output of the logic function depending on the type of gate
	 */
	private int inverted;

	/**
	 * Constructs a new BasicGate with the default number of inputs
	 * @param x The x position of the new gate
	 * @param y The y position of the new gate
	 * @param type The type of gate (see other constructor)
	 */
	public BasicGate(int x, int y, CompType type){
		this(x, y, type, DEFAULT_INPUTS);
	}

	/**
	 * Constructs a new BasicGate
	 * @param x The x position of the new gate
	 * @param y The y position of the new gate
	 * @param type The type of gate (valid values are CompType.AND, CompType.NAND, CompType.OR, CompType.NOR, CompType.XOR, and CompType.XNOR)
	 * @param numInputs The number of inputs, will be rounded to max / min if out of bounds
	 */
	public BasicGate(int x, int y, CompType type, int numInputs) {
		super(x, y, type);
		int normalMask = 0;
		int invertedMask = -1;
		if(type == CompType.AND) {
			setImages(new int[]{1});
			inverted = normalMask;
			function = 0;
		}
		else if(type == CompType.NAND) {
			setImages(new int[]{1});
			inverted = invertedMask;
			function = 0;
		}
		else if(type == CompType.OR) {
			setImages(new int[]{2});
			inverted = normalMask;
			function = 1;
		}
		else if(type == CompType.NOR) {
			setImages(new int[]{2});
			inverted = invertedMask;
			function = 1;
		}
		else if(type == CompType.XOR) {
			setImages(new int[]{2});
			inverted = normalMask;
			function = 2;
		}
		else if(type == CompType.XNOR) {
			setImages(new int[]{2});
			inverted = invertedMask;
			function = 2;
		}

		if(numInputs > MAX_INPUTS) numInputs = MAX_INPUTS;
		else if(numInputs < MIN_INPUTS) numInputs = MIN_INPUTS;
		Point[] connectionPositions = calcInputPositions(-25, numInputs);
		for(Point p : connectionPositions){
			io.addConnection(p.x, p.y, Connection.INPUT, Constants.LEFT);
		}
		io.addConnection(110, 40, Connection.OUTPUT, Constants.RIGHT);
	}

	@Override
	public void update(LogicEngine engine) {
		int inputs = io.getNumInputs();
		int output = io.getInput(0);
		for(int i = 1; i < inputs; i++){
			output = LogicFunctions.twoInput.get(function).apply(output, io.getInput(i));
		}
		io.setOutput(0, inverted ^ output, engine);
	}

	/**
	 * Sets the number of inputs.  Unlike the constructor, the method will do nothing and exit if the number of inputs is out of bounds
	 * @param numInputs The number of inputs
	 */
	public void setNumInputs(int numInputs){
		if(numInputs < MIN_INPUTS || numInputs > MAX_INPUTS) return;
		int currentNum = io.getNumInputs();
		if(numInputs > currentNum) {
			for(int i = 0; i < numInputs - currentNum; i++){
				io.addConnection(0, 0, Connection.INPUT, Constants.LEFT);
			}
		}
		else if(numInputs < currentNum){
			for(int i = 0; i < currentNum - numInputs; i++){
				io.removeConnection(io.getNumInputs() - 1, Connection.INPUT);
			}
		}
		Point[] positions = calcInputPositions(-25, numInputs);
		io.setConnectionLayout(new ConnectionLayout(positions, Constants.LEFT, Connection.INPUT));
	}

	@Override
	public LComponent makeCopy() {
		BasicGate result = new BasicGate(x, y, type, io.getNumInputs());
		result.setRotation(rotation);
		result.setName(getName());
		return result;
	}

	@Override
	public int getBitWidth(){
		return io.outputConnection(0).getBitWidth();
	}

	@Override
	public void changeBitWidth(int bitWidth){
		for(int i = 0; i < io.getNumInputs(); i++) io.inputConnection(i).changeBitWidth(bitWidth);
		for(int i = 0; i < io.getNumOutputs(); i++) io.outputConnection(i).changeBitWidth(bitWidth);
	}

	/**
	 * Calculates the positions of each connection so that they are centered and equally spaced in the y direction
	 * @param xPos The x position that all of the inputs will have
	 * @param numConnections The number of inputs the component will have
	 * @return An array of points showing the input positions
	 */
	private Point[] calcInputPositions(int xPos, int numConnections){
		int start = 80 / 2 - Renderer.BASIC_INPUT_SPACING / 2 * (numConnections - 1);
		Point[] positions = new Point[numConnections];
		for(int i = 0; i < numConnections; i++){
			positions[i] = new Point(xPos, start + i * Renderer.BASIC_INPUT_SPACING);
		}
		return positions;
	}
}
