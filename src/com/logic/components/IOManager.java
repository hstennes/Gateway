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

	private ArrayList<Input> inputs;

	private ArrayList<Output> outputs;
	
	/**
	 * The LComponent using this ConnectionManager
	 */
	private LComponent lcomp;
	
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
	 * Returns the signal coming into the input at the specified index.
	 * @param index The index of the input to get
	 * @return The signal of the specified input
	 */
	public boolean getInput(int index) {
		if(index < inputs.size()) {
			Connection input = inputs.get(index);
			if(input.numWires() > 0) return input.getWire(0).getSignal();
		}
		return false;
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
	public void setOutput(int index, boolean signal, LogicEngine engine) {
		Output c = outputs.get(index);
		if(c.getSignal() != signal) {
			c.setSignal(signal);
			for(int i = 0; i < c.numWires(); i++) {
				Connection dest = c.getWire(i).getDestConnection();
				if(dest != null) engine.mark(dest.getLcomp());
			}
		}
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
			inputs.add(new Input(lcomp, x, y, inputs.size(), direction, 1));
			return inputs.size() - 1;
		}
		else {
			outputs.add(new Output(lcomp, x, y, outputs.size(), direction, 1));
			return outputs.size() - 1;
		}
	}
	
	/**
	 * Removes the given Connection from this IOManager and deletes it (which will delete its wires)
	 * @param c The Connection to remove
	 */
	public void removeConnection(Connection c) {
		if(inputs.contains(c)) inputs.remove(c);
		else if(outputs.contains(c)) outputs.remove(c);
		c.delete();
	}
	
	/**
	 * Moves all connections to the positions specified in the given ConnectionLayout. The order in which the positions are applied to the
	 * connections matches the order in which the connections were originally added to the manager
	 * @param layout
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

	public Input inputConnection(int index){
		return inputs.get(index);
	}

	public Output outputConnection(int index){
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
	 * Gets the full bounding area of the component, including connections. This is different from LComponent.getBounds,
	 * which includes only the space taken up by the main component image.
	 * TODO Improve this documentation
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
		for(int i = 0; i < inputs.size(); i++) {
			inputs.get(i).delete();
		}
		for(int i = 0; i < outputs.size(); i++) {
			outputs.get(i).delete();
		}
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
