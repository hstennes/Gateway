package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.ui.Renderer;
import com.logic.util.ConnectionLayout;
import com.logic.util.Deletable;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class organizes the connections used by a component and provides methods for accessing and editing them
 * @author Hank Stennes
 *
 */
public class IOManager implements Deletable, Serializable {

	private static final long serialVersionUID = 1L;

	private final ArrayList<InputPin> inputs;

	private final ArrayList<OutputPin> outputs;
	
	/**
	 * The LComponent using this ConnectionManager
	 */
	private final LComponent lcomp;
	
	/**
	 * Constructs a new IOManager
	 * @param lcomp The LComponent creating this ConnectionManager
	 */
	public IOManager(LComponent lcomp) {
		this.lcomp = lcomp;
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();
	}
	
	/**
	 * Returns the first bit of the signal being received at the specified index. Exists for compatibility with one-bit
	 * parts of the application.
	 * @param index The index of the input
	 * @return The signal of the specified input
	 */
	@Deprecated
	public boolean getInputOld(int index) {
		if(index < inputs.size()) {
			Connection input = inputs.get(index);
			if(input.numWires() > 0) return input.getWire(0).getSignalOld();
		}
		return false;
	}

	/**
	 * Gets the signal being received at the specified index, or 0 if no wire is connected. The signal is correct up to
	 * the bit width of the previous output connection, but may contain random bits after that point.
	 * @param index The index of the input
	 * @return The signal of the specified input
	 */
	public int getInput(int index){
		Connection input = inputs.get(index);
		if(input.numWires() > 0) return input.getWire(0).getSignal();
		return 0;
	}

	/**
	 * Same as getInput, but overwrites all extraneous bits in the integer with 0s to guarantee that the bit width is accurate.
	 * @param index The index of the input
	 * @return The signal of the specified input
	 */
	public int getInputStrict(int index){
		return getInput(index) & (1 << inputs.get(index).getBitWidth()) - 1;
	}
	
	/**
	 * Sets the output of the connection at the given index to the specified boolean signal by changing the states of all connected wires
	 * to the signal. If the state of the wires is the same as the new signal, this method does nothing. For this method to function properly,
	 * it is important that all wires connected to the same connection be kept at the same state. ONLY call this method from a logic thread, 
	 * not the EDT.
	 * @param index The index of the connection
	 * @param signal The signal to set the connected wires to
	 * @param engine The LogicEngine instance that was passed to the update method that called this method
	 */
	@Deprecated
	public void setOutputOld(int index, boolean signal, LogicEngine engine) {
		OutputPin c = outputs.get(index);
		if(c.getSignalOld() != signal) {
			c.setSignalOld(signal);
			for(int i = 0; i < c.numWires(); i++) {
				Connection dest = c.getWire(i).getDestConnection();
				if(dest != null) engine.mark(dest.getLcomp());
			}
		}
	}

	public void setOutput(int index, int signal, LogicEngine engine){
		OutputPin c = outputs.get(index);
		if(c.getSignal() != signal){
			c.setSignal(signal);
			for(int i = 0; i < c.numWires(); i++){
				Connection dest = c.getWire(i).getDestConnection();
				if(dest != null) engine.mark(dest.getLcomp());
			}
		}
	}

	public void setOutputStrict(int index, int signal, LogicEngine engine){
		setOutput(index, signal & (1 << outputs.get(index).getBitWidth()) - 1, engine);
	}
	
	/**
	 * Adds a new Connection to this IOManager
	 * @param x See Connection constructor
	 * @param y See Connection constructor
	 * @param type See Connection constructor
	 * @param direction See Connection constructor
	 * @return The index that the connection was placed at in the input or output list
	 */
	public int addConnection(int x, int y, int type, int direction) {
		if(type == Connection.INPUT) {
			inputs.add(new InputPin(lcomp, x, y, inputs.size(), direction, 1));
			return inputs.size() - 1;
		}
		else {
			outputs.add(new OutputPin(lcomp, x, y, outputs.size(), direction, 1));
			return outputs.size() - 1;
		}
	}

	/**
	 * Removes the given Connection and deleted it (which deletes its wires)
	 * @param index The index of the connection in its respective list
	 * @param type INPUT or OUTPUT
	 */
	public void removeConnection(int index, int type){
		Connection c;
		if(type == Connection.INPUT) c = inputs.remove(index);
		else c = outputs.remove(index);
		c.delete();
	}
	
	/**
	 * Moves all connections to the positions specified in the given ConnectionLayout. The order in which the positions are applied to the
	 * connections matches the order in which the connections were originally added to the manager
	 * @param layout The connection layout
	 */
	public void setConnectionLayout(ConnectionLayout layout) {
		for(int i = 0; i < layout.getNumConnections(); i++) {
			Connection c;
			if(layout.getType() == Connection.INPUT) c = inputs.get(i);
			else c = outputs.get(i);
			Point p = layout.getPoint(i);
			int direction = layout.getDirection(i);
			c.setXY(p.x, p.y);
			c.setDirection(direction);
		}
	}

	/**
	 * Returns the input connection at the specified index
	 * @param index The index
	 * @return The input connection
	 */
	public InputPin inputConnection(int index){
		return inputs.get(index);
	}

	/**
	 * Returns the output connection at the specified index
	 * @param index The index
	 * @return The output connection
	 */
	public OutputPin outputConnection(int index){
		return outputs.get(index);
	}

	/**
	 * Returns the connection at or near the given point (how near a connection can be in order to be considered "at" a point is specified by
	 * Connection.DETECT_RANGE
	 * @param p The point to find a connection for
	 * @return The connection at the specified point (in the CircuitPanel space), or null if there is none
	 */
	public Connection connectionAt(Point p) {
		for(int i = 0; i < inputs.size(); i++) {
			Connection c = inputs.get(i);
			Point p1 = c.getCoord();
			int range = Connection.DETECT_RANGE;
			Rectangle bounds = new Rectangle(p1.x - range, p1.y - range, 2 * range, 2 * range);
			if(bounds.contains(p)) return c;
		}
		for(int i = 0; i < outputs.size(); i++) {
			Connection c = outputs.get(i);
			Point p1 = c.getCoord();
			int range = Connection.DETECT_RANGE;
			Rectangle bounds = new Rectangle(p1.x - range, p1.y - range, 2 * range, 2 * range);
			if(bounds.contains(p)) return c;
		}
		return null;
	}

	/**
	 * Returns the rectangle that fits around all connections, relative to the origin of the component. The width and height are always positive,
	 * but the x position is often negative because input connections are present on the left side of the component.
	 * @return The connection bounds
	 */
	public Rectangle getConnectionBounds(){
		Rectangle b = lcomp.getBoundsRight();
		int xMin = 0, xMax = b.width, yMin = 0, yMax = b.height;
		int r = Renderer.CONNECT_RAD;
		for(Connection c : inputs){
			int cx = c.getX(), cy = c.getY();
			if(cx - r < xMin) xMin = cx - r;
			else if(cx + r > xMax) xMax = cx + r;
			if(cy - r < yMin) yMin = cy - r;
			else if(cy + r > yMax) yMax = cy + r;
		}
		for(Connection c : outputs){
			int cx = c.getX(), cy = c.getY();
			if(cx - r < xMin) xMin = cx - r;
			else if(cx + r > xMax) xMax = cx + r;
			if(cy - r < yMin) yMin = cy - r;
			else if(cy + r > yMax) yMax = cy + r;
		}
		Rectangle bounds = new Rectangle(new Point(xMin, yMin));
		bounds.add(new Point(xMax, yMax));
		return bounds;
	}

	@Override
	public void delete() {
		for (InputPin input : inputs) input.delete();
		for (OutputPin output : outputs) output.delete();
	}

	/**
	 * Returns the number of input connections
	 * @return The number of input connections
	 */
	public int getNumInputs() {
		return inputs.size();
	}

	/**
	 * Returns the number of output connections
	 * @return The number of output connections
	 */
	public int getNumOutputs() {
		return outputs.size();
	}
}
