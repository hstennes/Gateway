package com.logic.components;

import com.logic.util.CompUtils;
import com.logic.util.Constants;
import com.logic.util.Deletable;

import java.awt.*;
import java.util.ArrayList;

/**
 * This class represents the inputs and outputs on an LComponent
 * @author Hank Stennes
 *
 */
public abstract class Connection implements Deletable, BitWidthEntity {

	private static final long serialVersionUID = 1L;
	
	/**
	 * A constant representing an input connection
	 */
	public static final int INPUT = 0;
	
	/**
	 * A constant representing an output connection
	 */
	public static final int OUTPUT = 1;
	
	/**
	 * The range from the center of the connection inside which a user click will be interpreted as clicking the connection
	 */
	public static final int DETECT_RANGE = 12;
	
	/**
	 * The LComponent that uses this connection
	 */
	private final LComponent lcomp;
	
	/**
	 * An ArrayList of all the wires that are connected to this connection (if this connection is an input, the maximum size of this list
	 * is 1)
	 */
	protected final ArrayList<Wire> wires;
	
	/**
	 * The index of this connection in the list of input connections or output connections
	 */
	private final int index;

	/**
	 * Holds rotated positions of the connection relative to the component in the order {x, y, down x, down y, left x, left y,
	 * up x, up y}
	 */
	private int[] pos;
	
	/**
	 * The direction that this connection is facing, which determines how wires appear when connected to it. Valid values are 
	 * CompRotator.RIGHT, CompRotator.DOWN, CompRotator.LEFT, and CompRotator.UP. This value does not change when the component is rotated, 
	 * so use getTrueDirection() method to get the absolute direction with component rotation taken into account.
	 */
	private int direction;

	/**
	 * Constructs a new Connection
	 * @param lcomp The LComponent that will use this connection
	 * @param x The x position of the connection (see Connection.x)
	 * @param y The y position of the connection (see Connection.y)
	 * @param index The index of the connection (see Connection.index)
	 * @param direction The directions of the connection (see Connection.direction)
	 */
	public Connection(LComponent lcomp, int x, int y, int index, int direction) {
		wires = new ArrayList<>();
		this.lcomp = lcomp;
		setXY(x, y);
		this.index = index;
		this.direction = direction;
	}

	public abstract boolean addWire(Wire wire);
	
	/**
	 * Removes the given wire from the connection
	 * @param wire The wire to remove
	 */
	public void removeWire(Wire wire) {
		wires.remove(wire);
	}
	
	/**
	 * Performs all necessary operations when adding a wire, which include adding it to the ArrayList of wires, updating the wire's references
	 * to its connections, and setting the wire's initial signal
	 * @param wire The wire to add
	 */
	protected void initWire(Wire wire) {
		wires.add(wire);
		wire.fillConnection(this);
	}

	/**
	 * Deletes all wires and removes them from the wires list.
	 */
	private void clearWires(){
		for(int i = wires.size() - 1; i >= 0; i--) {
			wires.get(i).delete();
		}
		wires.clear();
	}
	
	/**
	 * Returns the position of this Connection in the space of the CircuitPanel, accounting for component rotation
	 * @return This connection's position
	 */
	public Point getCoord() {
		int index = 2 * lcomp.getRotation();
		Point p = new Point(pos[index], pos[index + 1]);
		p.translate(lcomp.getX(), lcomp.getY());
		return p;
	}
	
	/**
	 * Returns the wire at the specified index
	 * @param index The index of the desired wire
	 * @return The wire at the specified index
	 */
	public Wire getWire(int index) {
		return wires.get(index);
	}
	
	/**
	 * Returns the first wire on the connection. This method is useful for input connections because they cannot have more than 1 wire
	 * @return The wire at index zero in the list of wires
	 */
	public Wire getWire() {
		return wires.get(0);
	}
	
	/**
	 * Returns the number of wires this connection has
	 * @return The number of wires on this connection
	 */
	public int numWires() {
		return wires.size();
	}
	
	/**
	 * Returns the LComponent that uses this connection
	 * @return The LComponent that uses this connection
	 */
	public LComponent getLcomp() {
		return lcomp;
	}

	/**
	 * Returns the index of this connection
	 * @return The index of this connection
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Returns the pixel x position of this connection (with a right rotation)
	 * @return The pixel x of this connection
	 */
	public int getX() {
		return pos[0];
	}
	
	/**
	 * Returns the pixel y position of this connection (with a right rotation)
	 * @return The pixel y of this connection
	 */
	public int getY() {
		return pos[1];
	}

	public int getType(){
		return this instanceof InputPin ? INPUT : OUTPUT;
	}

	/**
	 * Sets the pixel position of this connection and computes its possible position under all rotations. The given position is interpreted
	 * as being part of a right facing component. This method should be called even if the Connection is not to be moved if the size of the 
	 * LComponents image is changed as this changes rotation information.
	 * @param x The new pixel x position of this connection
	 * @param y The new pixel y position of this connection
	 */
	public void setXY(int x, int y) {
		Rectangle bounds = lcomp.getBoundsRight();
		Point down = CompUtils.withRotation(x, y, bounds.width, bounds.height, Constants.DOWN);
		Point left = CompUtils.withRotation(x, y, bounds.width, bounds.height, Constants.LEFT);
		Point up = CompUtils.withRotation(x, y, bounds.width, bounds.height, Constants.UP);
		pos = new int[] {x, y, down.x, down.y, left.x, left.y, up.x, up.y};
	}

	public int getDirection(){
		return direction;
	}

    /**
	 * Returns the direction of this connection when the rotation of the LComponent is accounted for (different than the direction field).
	 * The given direction can be used to determine how to render a wire without additional calculations
	 * @return The direction of this connection
	 */
	public int getAbsoluteDirection() {
		return (direction + lcomp.getRotation()) % 4;
	}
	
	/**
	 * Sets the direction that the connection is facing
	 * @param direction The direction of this connection
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	@Override
	public void delete() {
		clearWires();
	}
}
