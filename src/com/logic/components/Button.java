package com.logic.components;

import java.awt.*;

import com.logic.engine.LogicEngine;
import com.logic.engine.LogicWorker;
import com.logic.ui.CircuitPanel;
import com.logic.ui.CompRotator;

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
		drawer.setImages(new int[] {5, 6});
		io.addConnection(100, 40, Connection.OUTPUT, CompRotator.RIGHT);
		setClickAction(20, 20, 40, 40);
		setNotificationType(RELEASED);
	}
	
	@Override
	public void update(LogicEngine engine) {
		io.setOutput(0, getState(), engine);
	}

	@Override
	public void clickAction(Point p) {
		setState(true);
		LogicWorker.startLogic(this);
	}

	@Override
	public void notification(int type) {
		if(type == RELEASED) {
			if(getState()) {
				setState(false);
				LogicWorker.startLogic(this);
			}
		}
	}

	@Override
	public int getActiveImageIndex(){
		if(getState()) return 1;
		return 0;
	}
	
	@Override
	public LComponent makeCopy() {
		Button result = new Button(x, y);
		result.getRotator().setRotation(rotator.getRotation());
		result.setName(getName());
		result.setShowLabel(isShowLabel());
		return result;
	}
}
