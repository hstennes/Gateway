package com.logic.components;

import com.logic.ui.CircuitPanel;
import com.logic.util.Constants;
import com.logic.util.Deletable;

import java.awt.*;
import java.awt.geom.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A CircuitElement that holds a boolean signal and connects an output connection to an input connection
 * @author Hank Stennes
 *
 */
public class Wire extends CircuitElement implements Deletable, Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final float CURVINESS = 0.18f;
	
	/**
	 * The path that the wire follows in the CircuitPanel
	 */
	private Path2D path;

	/**
	 * The output connection the wire is attached to
	 */
	private OutputPin source;

	/**
	 * The input connection the wire is attached to
	 */
	private InputPin dest;

	/**
	 * The list of shaping points on the wire
	 */
	private ArrayList<Point> shapePoints;

	public Wire() {
		shapePoints = new ArrayList<>();
	}
	
	/**
	 * Returns the wire's signal (the least significant bit if there are multiple)
	 * @return The wire's signal
	 */
	@Deprecated
	public synchronized boolean getSignalOld() {
		if(source == null) return false;
		return source.getSignalOld();
	}

	public synchronized int getSignal(){
		if(source == null) return 0;
		return source.getSignal();
	}
	
	/**
	 * Returns the Connection that can set this wire's signal
	 * @return The source connection
	 */
	public OutputPin getSourceConnection() {
		return source;
	}
	
	/**
	 * Returns the Connection that uses this wire's signal as input
	 * @return The destination connection
	 */
	public InputPin getDestConnection() {
		return dest;
	}
	
	/**
	 * Sets a null connection to the given connection, if one or both connections are null
	 * @param connect The connection to add
	 */
	public void fillConnection(Connection connect) {
		if(source == null && connect instanceof OutputPin) source = (OutputPin) connect;
		else if(dest == null && connect instanceof InputPin) dest = (InputPin) connect;
	}



	public void addShapePoint(Point p){
		shapePoints.add(p);
	}

	public void removeShapePoint(int index) {
		shapePoints.remove(index);
	}

	public void moveShapePoint(int index, Point moveTo) {
		if(index >= shapePoints.size()) return;
		shapePoints.get(index).setLocation(moveTo);
	}

	public ArrayList<Point> getShapePoints() {
		return shapePoints;
	}

	public Path2D getCurve(){
		return path;
	}
	
	/**
	 * Returns the curve last drawn by this wire
	 * @return The wire's curve
	 */
	public Path2D getCurveUpdate(CircuitPanel cp) {
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
		int startOffset;
		int endOffset;
		if(c2 == null){
			p4 = cp.getEditor().getWireBuilder().getMousePoint();
			startOffset = wireOffset(p1, p4);
			endOffset = 0;
		}
		else{
			p4 = c2.getCoord();
			startOffset = wireOffset(p1, shapePoints.size() > 0 ? shapePoints.get(0) : p4);
			endOffset = wireOffset(shapePoints.size() > 0 ? shapePoints.get(shapePoints.size() - 1) : p1, p4);
		}
		Point p2 = offsetInDirection(p1, startOffset, c1.getAbsoluteDirection());
		p3 = offsetInDirection(p4, endOffset, c2 == null ? Constants.RIGHT : c2.getAbsoluteDirection());
		path = buildPath(p1, p2, p3, p4);
		return path;
	}

	private Path2D buildPath(Point p1, Point p2, Point p3, Point p4) {
		path = new Path2D.Double();
		if(shapePoints.size() == 0) {
			CubicCurve2D curve = new CubicCurve2D.Double(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
			path.append(curve, false);
			return path;
		}
		else {
			Point sp1 = shapePoints.get(0);
			CubicCurve2D startCurve = new CubicCurve2D.Double(p1.x, p1.y, p2.x, p2.y, sp1.x, sp1.y, sp1.x, sp1.y);
			path.append(startCurve, false);
			for(int i = 1; i < shapePoints.size(); i++){
				sp1 = shapePoints.get(i);
				Point sp2 = shapePoints.get(i - 1);
				path.append(new Line2D.Double(sp2.x, sp2.y, sp1.x, sp1.y), false);
			}
			CubicCurve2D endCurve = new CubicCurve2D.Double(sp1.x, sp1.y, sp1.x, sp1.y, p3.x, p3.y, p4.x, p4.y);
			path.append(endCurve, false);
		}
		return path;
	}

	private int wireOffset(Point p1, Point p2){
		int dx = p1.x - p2.x, dy = p1.y - p2.y;
		return (int) (CURVINESS * (Math.abs(dx) + Math.abs(dy)));
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
