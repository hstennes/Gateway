package com.logic.components;

import com.logic.ui.CircuitPanel;
import com.logic.util.Constants;
import com.logic.util.Deletable;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.io.Serializable;

/**
 * A CircuitElement that holds a boolean signal and connects an output connection to an input connection
 * @author Hank Stennes
 *
 */
public class Wire extends CircuitElement implements Deletable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * The path that the wire follows in the CircuitPanel
	 */
	private CubicCurve2D curve;


	/**
	 * The output connection the wire is attached to
	 */
	private Output source;

	/**
	 * The input connection the wire is attached to
	 */
	private Input dest;
	
	/**
	 * Returns the wire's signal (the least significant bit if there are multiple)
	 * @return The wire's signal
	 */
	public synchronized boolean getSignal() {
		if(source == null) return false;
		return source.getSignal();
	}
	
	/**
	 * Returns the Connection that can set this wire's signal
	 * @return The source connection
	 */
	public Output getSourceConnection() {
		return source;
	}
	
	/**
	 * Returns the Connection that uses this wire's signal as input
	 * @return The destination connection
	 */
	public Input getDestConnection() {
		return dest;
	}
	
	/**
	 * Sets a null connection to the given connection, if one or both connections are null
	 * @param connect The connection to add
	 */
	public void fillConnection(Connection connect) {
		if(source == null && connect instanceof Output) source = (Output) connect;
		else if(dest == null && connect instanceof Input) dest = (Input) connect;
	}

	public CubicCurve2D getCurve(){
		return curve;
	}
	
	/**
	 * Returns the curve last drawn by this wire
	 * @return The wire's curve
	 */
	public CubicCurve2D getCurveUpdate(CircuitPanel cp) {
		Connection s = getSourceConnection(), d = getDestConnection();
		Connection c1, c2;
		if(d != null && s == null) {
			c1 = d;
			c2 = s;
		}
		else {
			c1 = s;
			c2 = d;
		}
		if(c1 == null) return null;

		Point p1 = c1.getCoord();
		Point p3, p4;
		int offset;
		if(c2 == null){
			p4 = cp.getEditor().getWireBuilder().getMousePoint();
			p3 = p4;
			offset = wireOffset(p1, p4);
		}
		else{
			p4 = c2.getCoord();
			offset = wireOffset(p1, p4);
			p3 = offsetInDirection(p4, offset, c2.getAbsoluteDirection());
		}
		Point p2 = offsetInDirection(p1, offset, c1.getAbsoluteDirection());
		curve = new CubicCurve2D.Double(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
		return curve;
	}

	private int wireOffset(Point p1, Point p2){
		int dx = p1.x - p2.x, dy = p1.y - p2.y;
		return (int) (0.2 * (Math.abs(dx) + Math.abs(dy)));
	}

	/**
	 * Returns the point that is the specified distance away from the given point in the given CompRotator direction
	 * @param p The original point
	 * @param offset The offset to apply
	 * @param direction The direction of the offset
	 * @return
	 */
	private Point offsetInDirection(Point p, int offset, int direction) {
		if(direction == Constants.UP) return new Point(p.x, p.y - offset);
		else if(direction == Constants.RIGHT) return new Point(p.x + offset, p.y);
		else if(direction == Constants.DOWN) return new Point(p.x, p.y + offset);
		else if(direction == Constants.LEFT) return new Point(p.x - offset, p.y);
		return new Point(p);
	}
	
	/**
	 * Tells whether the wire is complete (when it has two references to Connections)
	 * @return The completion state of the wire
	 */
	public boolean isComplete() {
		return source != null && dest != null;
	}
	
	@Override
	public void delete() {
		if(source != null) {
			source.removeWire(this);
			source = null;
		}
		if(dest != null) {
			dest.removeWire(this);
			dest = null;
		}
	}
}
