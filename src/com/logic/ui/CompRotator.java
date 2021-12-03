package com.logic.ui;

import java.awt.Point;
import java.io.Serializable;

/**
 * This class is responsible for performing operations relating to rotating LComponents by 90 degrees
 * @author Hank Stennes
 *
 */
public class CompRotator implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final double[] RAD_ROTATION = new double[] {0, Math.PI / 2, Math.PI, -Math.PI / 2};
	
	/**
	 * A constant representing the right (default) direction
	 */
	public static final int RIGHT = 0;
	
	/**
	 * A constant representing the down direction
	 */
	public static final int DOWN = 1;
	
	/**
	 * A constant representing the left direction
	 */
	public static final int LEFT = 2;
	
	/**
	 * A constant representing the up direction
	 */
	public static final int UP = 3;
	
	/**
	 * A constant representing the clockwise rotation modification
	 */
	public static final boolean CLOCKWISE = true;
	
	/**
	 * A constant representing the counter clickwise rotation modification
	 */
	public static final boolean COUNTER_CLOCKWISE = false;
	
	/**
	 * The current rotation
	 */
	private int rotation;
	
	/**
	 * Constructs a new CompRotator
     */
	public CompRotator() {
		rotation = RIGHT; 
	}
	
	/**
	 * Calculates the location of the given point in a rectangle of the given dimensions if the Rectangle (which starts with a RIGHT rotation)
	 * is rotated to the specified rotation
	 * @param x The x position of the point
	 * @param y The y position of the point
	 * @param width The width of the rectangle that contains the point
	 * @param height The height of the rectangle that contains the point
	 * @param rotation The new rotation of the rectangle
	 * @return The rotated point
	 */
	public static Point withRotation(int x, int y, int width, int height, int rotation) {
		if(rotation == DOWN) return new Point(height - y - 1, x);
		else if(rotation == UP) return new Point(y, width - x - 1);
		else if(rotation == LEFT) return new Point(width - x - 1, height - y - 1);
		else return new Point(x, y);
	}
	
	/**
	 * Returns the current rotation of the component
	 * @return The current rotation, expressed as one of the four rotation constants
	 */
	public int getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the rotation of the component
	 * @param rotation The new rotation 
	 */
	public void setRotation(int rotation) {
		//java is inane
		this.rotation = ((rotation % 4) + 4) % 4;
	}
	
}
