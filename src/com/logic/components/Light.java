package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.ui.Renderer;
import com.logic.util.Constants;

import java.awt.*;

/**
 * A simple output component that appears lit up when it receives a high signal and off when it receives a low signal
 * @author Hank Stennes
 *
 */
public class Light extends LabeledComponent implements BitWidthEntity {
	
	private static final long serialVersionUID = 1L;
		
	/**
	 * Constructs a new Light
	 * @param x The x position
	 * @param y The y position
	 */
	public Light(int x, int y) {
		super(x, y, CompType.LIGHT);
		setImages(new int[] {7, 8});
		io.addConnection(30, 100, Connection.INPUT, Constants.DOWN);
	}
	
	@Override
	public void update(LogicEngine engine) {}

	@Override
	public int getActiveImageIndex(){
		if(io.getInputOld(0)) return 1;
		return 0;
	}
	
	@Override
	public LComponent makeCopy() {
		Light result = new Light(x, y);
		result.setRotation(rotation);
		result.setName(getName());
		result.setShowLabel(isShowLabel());
		result.changeBitWidth(getBitWidth());
		return result;
	}

	@Override
	public Rectangle getBoundsRight(){
		Rectangle imageBounds = super.getBoundsRight();
		if(getBitWidth() == 1) return imageBounds;
		imageBounds.setBounds(imageBounds.x, imageBounds.y,
				Renderer.SWITCH_BIT_SPACING * getBitWidth(),
				Renderer.MULTI_BIT_SL_HEIGHT);
		return imageBounds;
	}

	@Override
	public int getBitWidth() {
		if(io.getNumInputs() == 1) return io.inputConnection(0).getBitWidth();
		return 1;
	}

	@Override
	public void changeBitWidth(int bitWidth) {
		int oldBits = getBitWidth();
		if(oldBits == bitWidth) return;

		io.inputConnection(0).changeBitWidth(bitWidth);
		bitWidthUpdate(oldBits, bitWidth, true);
	}

	@Override
	public void validateBitWidth() {
		bitWidthUpdate(-1, getBitWidth(), false);
	}

	public void bitWidthUpdate(int oldBitWidth, int newBitWidth, boolean move){
		Connection c = io.inputConnection(0);
		if(move) {
			if (newBitWidth == 1) y -= Renderer.MULTI_BIT_SL_HEIGHT;
			else if (oldBitWidth == 1) y += Renderer.MULTI_BIT_SL_HEIGHT;
		}

		if(newBitWidth == 1) {
			c.setXY(30, 100);
			c.setDirection(Constants.DOWN);
		}
		else {
			c.setXY(-20, getBoundsRight().height / 2);
			c.setDirection(Constants.LEFT);
		}
	}
}
