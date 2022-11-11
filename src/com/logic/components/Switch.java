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

	private static final int SINGLE_BIT_CLICK_PADDING = 15;

	private static final int MULTI_BIT_CLICK_PADDING = 5;
	
	/**
	 * Constructs a new switch
	 * @param x The x position
	 * @param y The y position
	 */
	public Switch(int x, int y) {
		super(x, y, CompType.SWITCH);
		setImages(new int[] {3, 4});
		io.addConnection(80, 40, Connection.OUTPUT, Constants.RIGHT);
		updateClickBounds(getBoundsRight());
	}
	
	@Override
	public void update(LogicEngine engine) {
		io.setOutputStrict(0, getState(), engine);
	}

	@Override
	public void clickAction(Point p) {
		int bits = getBitWidth();
		if(bits == 1) setState(1 & ~getState());
		else{
			int section = bits - (p.x + MULTI_BIT_CLICK_PADDING) / Renderer.SWITCH_BIT_SPACING;
			setState(getState() ^ (1 << (section - 1)));
		}
		LogicWorker.startLogic(this);
	}

	@Override
	public void notification(int type) { }

	@Override
	public int getActiveImageIndex(int compData){
		if((compData & 1) == 1) return 1;
		return 0;
	}
	
	@Override
	public LComponent makeCopy() {
		Switch result = new Switch(x, y);
		result.setRotation(rotation);
		result.setName(getName());
		result.setState(getState());
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
		if(io.getNumOutputs() == 1) return io.outputConnection(0).getBitWidth();
		return 1;
	}

	@Override
	public void changeBitWidth(int bitWidth) {
		if(getBitWidth() == bitWidth) return;
		int oldWidth = getBoundsRight().width;
		io.outputConnection(0).changeBitWidth(bitWidth);
		bitWidthUpdate(oldWidth, true);
	}

	@Override
	public void validateBitWidth(){
		bitWidthUpdate(-1, false);
	}

	private void bitWidthUpdate(int oldWidth, boolean move){
		Rectangle bounds = getBoundsRight();
		if(move) {
			if(rotation == Constants.RIGHT) x += oldWidth - bounds.width;
			else if(rotation == Constants.DOWN) y += oldWidth - bounds.width;
		}

		updateClickBounds(bounds);
		io.outputConnection(0).setXY(bounds.width + 20, bounds.height / 2);
	}

	private void updateClickBounds(Rectangle compBounds){
		int padding = getBitWidth() == 1 ? SINGLE_BIT_CLICK_PADDING : MULTI_BIT_CLICK_PADDING;
		setClickAction(padding, padding,
				compBounds.width - 2 * padding,
				compBounds.height - 2 * padding);
	}
}
