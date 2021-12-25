package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.engine.LogicWorker;
import com.logic.ui.Renderer;
import com.logic.util.Constants;

import java.awt.*;

/**
 * An input component that toggles its state when its clickAction is fired
 * @author Hank Stennes
 *
 */
public class Switch extends IComponent implements BitWidthEntity {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new switch
	 * @param x The x position
	 * @param y The y position
	 */
	public Switch(int x, int y) {
		super(x, y, CompType.SWITCH);
		setImages(new int[] {3, 4});
		io.addConnection(80, 40, Connection.OUTPUT, Constants.RIGHT);
		setClickAction(15, 15, 30, 50);
	}
	
	@Override
	public void update(LogicEngine engine) {
		io.setOutputOld(0, getStateOld(), engine);
		//io.setOutput(0, 107, engine);
	}

	@Override
	public void clickAction(Point p) {
		setStateOld(!getStateOld());
		LogicWorker.startLogic(this);
	}

	@Override
	public void notification(int type) { }

	@Override
	public int getActiveImageIndex(){
		if(getStateOld()) return 1;
		return 0;
	}
	
	@Override
	public LComponent makeCopy() {
		Switch result = new Switch(x, y);
		result.setRotation(rotation);
		result.setName(getName());
		result.setStateOld(getStateOld());
		result.setShowLabel(isShowLabel());
		return result;
	}

	@Override
	public int getBitWidth() {
		if(io.getNumOutputs() == 1) return io.outputConnection(0).getBitWidth();
		return 1;
	}
	
	@Override
	public Rectangle getBoundsRight(){
		Rectangle imageBounds = super.getBoundsRight();
		imageBounds.setBounds(imageBounds.x, imageBounds.y, Renderer.SWITCH_BIT_SPACING * getBitWidth(), imageBounds.height);
		return imageBounds;
	}

	@Override
	public void changeBitWidth(int bitWidth) {
		Connection c = io.outputConnection(0);
		x += Renderer.SWITCH_BIT_SPACING * (c.getBitWidth() - bitWidth);
		c.changeBitWidth(bitWidth);
		Rectangle bounds = getBoundsRight();
		setClickAction(15, 15, bounds.width - 30, bounds.height - 30);
	}
}
