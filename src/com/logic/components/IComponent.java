package com.logic.components;

import com.logic.util.CompUtils;
import com.logic.util.Constants;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * 
 * A subclass of LComponent that provides the additional functionality of detecting mouse presses and releases and the user clicking
 * on a specified area within the component. Input components should extends this class if they need to use user input to determine their
 * state
 * @author Hank Stennes
 *
 */
public abstract class IComponent extends LabeledComponent implements MouseListener {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constants that specify the types of events that this IComponent should respond to through the notification(...) method
	 */
	public static final int NO_NOTIFICATION = 0, PRESSED = 1, RELEASED = 2, BOTH = 3;
	
	/**
	 * The type of event that this IComponent is listening for, 
	 */
	private int notificationType = NO_NOTIFICATION;
	
	/**
	 * The boundary in which a user click will cause a click action, relative to the position of the component, under a CompRotator.RIGHT 
	 * (defualt) rotation.
	 */
	private Rectangle clickBounds;
	
	/**
	 * The boundary in which a user click will cause a click action, relative to the position of the component, under a CompRotator.DOWN 
	 * rotation.
	 */
	private Rectangle downClickBounds;
	
	/**
	 * The boundary in which a user click will cause a click action, relative to the position of the component, under a CompRotator.LEFT 
	 * rotation.
	 */
	private Rectangle leftClickBounds;
	
	/**
	 * The boundary in which a user click will cause a click action, relative to the position of the component, under a CompRotator.UP 
	 * rotation.
	 */
	private Rectangle upClickBounds;

	/**
	 * The state of this IComponent, which should only be accessed through synchronized getter and setter methods. Valid state values
	 * are determined by the subclass.
	 */
	private int state;
	
	/**
	 * Constructs a new IComponent
	 * @param x The x position of the component
	 * @param y The y position of the component
	 * @param type The type of component (valid values are the types used by any class that is a subclass of IComponent)
	 */
	public IComponent(int x, int y, CompType type) {
		super(x, y, type);
		clickBounds = new Rectangle();
	}
	
	/**
	 * Called when the user clicks in the boundary that has been set using the setClickAction method
	 * @param boundsClick The click point relative to the click area on the component
	 */
	protected abstract void clickAction(Point boundsClick);

	/**
	 * Called when the user clicks in the boundary that has been set using the setClickAction method.
	 * @param cpClick The click point in CircuitPanel space
	 */
	public void componentClicked(Point cpClick){
		Rectangle actionBounds = getClickActionBounds();
		Point actionClick;
		if(rotation == Constants.RIGHT) actionClick = new Point(cpClick.x - actionBounds.x, cpClick.y - actionBounds.y);
		else if(rotation == Constants.UP) actionClick = new Point(actionBounds.y + actionBounds.height - cpClick.y, cpClick.x - actionBounds.x);
		else if(rotation == Constants.LEFT) actionClick = new Point(actionBounds.x + actionBounds.width - cpClick.x, actionBounds.y + actionBounds.height - cpClick.y);
		else actionClick = new Point(cpClick.y - actionBounds.y, actionBounds.x + actionBounds.width - cpClick.x);
		clickAction(actionClick);
	}
	
	/**
	 * Called when the action specified through setNotification has occurred
	 * @param type The type of notification (MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED)
	 */
	public abstract void notification(int type);
	
	/**
	 * Sends a mouse pressed notification if necessary
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if(notificationType == PRESSED || notificationType == BOTH) notification(PRESSED);
	}
	
	/**
	 * Sends a mouse released notification if necessary
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if(notificationType == RELEASED || notificationType == BOTH) notification(RELEASED);
	}
	
	/**
	 * Sets the click action for this IComponent
	 * @param x The pixel x position of the top left corner of the bounding box
	 * @param y The pixel y position of the top left corner of the bounding box
	 * @param width The pixel width of the bounding box
	 * @param height The pixel height of the bounding box
	 */
	protected void setClickAction(int x, int y, int width, int height) {
		clickBounds = new Rectangle(x, y, width, height);
		downClickBounds = rotate(clickBounds, Constants.DOWN);
		leftClickBounds = rotate(clickBounds, Constants.LEFT);
		upClickBounds = rotate(clickBounds, Constants.UP);
	}
	
	/**
	 * Sets this component to listen for the specified event. This means that notification(...) will be called on this IComponent if the 
	 * event occurs.
	 * @param type The type of notification (MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED)
	 */
	protected void setNotificationType(int type) {
		notificationType = type;
	}
	
	/**
	 * Finds the new state of the given Rectangle if this component is rotated
	 * @param r The rectangle to rotate
	 * @param rotation The rotation of the resulting Rectangle
	 * @return The rotated Rectangle
	 */
	private Rectangle rotate(Rectangle r, int rotation) {
		Rectangle bounds = getBoundsRight();
		Point p1 = CompUtils.withRotation(r.x, r.y, bounds.width, bounds.height, rotation);
		Point p2 = CompUtils.withRotation(r.x + r.width, r.y + r.height, bounds.width,
				bounds.height, rotation);
		Rectangle result = new Rectangle(p1);
		result.add(p2);
		return result;
	}
	
	/**
	 * Returns the boundary inside which a user click should call clickAction(), accounting for component rotation
	 * @return The click action boundary
	 */
	public Rectangle getClickActionBounds() {
		if(rotation == Constants.RIGHT) return new Rectangle(clickBounds.x + x, clickBounds.y + y, clickBounds.width, clickBounds.height);
		else if(rotation == Constants.DOWN) return new Rectangle(downClickBounds.x + x, downClickBounds.y + y, downClickBounds.width, downClickBounds.height);
		else if(rotation == Constants.LEFT) return new Rectangle(leftClickBounds.x + x, leftClickBounds.y + y, leftClickBounds.width, leftClickBounds.height);
		else if(rotation == Constants.UP) return new Rectangle(upClickBounds.x + x, upClickBounds.y + y, upClickBounds.width, upClickBounds.height);
		return null;
	}
	
	/**
	 * Returns the state of this IComponent
	 * @return The state
	 */
	@Deprecated
	public synchronized boolean getStateOld() {
		return (state & 1) == 1;
	}

	/**
	 * Sets the state of this IComponent
	 * @param state The new state
	 */
	@Deprecated
	public synchronized void setStateOld(boolean state) {
		if(state) this.state |= 1;
		else this.state &= ~1;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	@Override
	public void mouseEntered(MouseEvent e) {};
	@Override
	public void mouseClicked(MouseEvent e) {};
	@Override
	public void mouseExited(MouseEvent e) {};
	
}
