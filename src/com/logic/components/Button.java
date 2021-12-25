package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.engine.LogicWorker;
import com.logic.util.Constants;

import java.awt.*;

/**
 * This class represents a button that outputs HIGH when pressed and LOW when not pressed, and requires the mouse to remain down in order to 
 * stay pressed
 * @author Hank Stennes
 *
 */
public class Button extends IComponent {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new Button
	 * @param x The x position of the Button
	 * @param y The y position of the button
	 */
	public Button(int x, int y) {
		super(x, y, CompType.BUTTON);
		setImages(new int[] {5, 6});
		io.addConnection(100, 40, Connection.OUTPUT, Constants.RIGHT);
		setClickAction(20, 20, 40, 40);
		setNotificationType(RELEASED);
	}
	
	@Override
	public void update(LogicEngine engine) {
		io.setOutputOld(0, getStateOld(), engine);
	}

	@Override
	public void clickAction(Point p) {
		setStateOld(true);
		LogicWorker.startLogic(this);
	}

	@Override
	public void notification(int type) {
		if(type == RELEASED) {
			if(getStateOld()) {
				setStateOld(false);
				LogicWorker.startLogic(this);
			}
		}
	}

	@Override
	public int getActiveImageIndex(){
		if(getStateOld()) return 1;
		return 0;
	}
	
	@Override
	public LComponent makeCopy() {
		Button result = new Button(x, y);
		result.setRotation(rotation);
		result.setName(getName());
		result.setShowLabel(isShowLabel());
		return result;
	}
}
