package com.logic.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

import com.logic.components.*;
import com.logic.ui.CompRotator;

/**
 * A class that holds static methods for performing various operations on lists of LComponents
 * @author Hank Stennes
 *
 */
public class CompUtils {

	/**
	 * Creates a component of the given type with the given coordinates (does not support custom components)
	 * @param type The component type (case-insensitive CompType value as string)
	 * @param x The x position
	 * @param y The y position
	 * @return The component
	 */
	public static LComponent makeComponent(String type, int x, int y){
		LComponent lcomp;
		type = type.toLowerCase();
		switch (type) {
			case "buffer":
				lcomp = new SingleInputGate(x, y, CompType.BUFFER);
				break;
			case "not":
				lcomp = new SingleInputGate(x, y, CompType.NOT);
				break;
			case "and":
				lcomp = new BasicGate(x, y, CompType.AND);
				break;
			case "nand":
				lcomp = new BasicGate(x, y, CompType.NAND);
				break;
			case "or":
				lcomp = new BasicGate(x, y, CompType.OR);
				break;
			case "nor":
				lcomp = new BasicGate(x, y, CompType.NOR);
				break;
			case "xor":
				lcomp = new BasicGate(x, y, CompType.XOR);
				break;
			case "xnor":
				lcomp = new BasicGate(x, y, CompType.XNOR);
				break;
			case "clock":
				lcomp = new Clock(x, y);
				break;
			case "light":
				lcomp = new Light(x, y);
				break;
			case "switch":
				lcomp = new Switch(x, y);
				break;
			case "zero":
				lcomp = new Constant(x, y, CompType.ZERO);
				break;
			case "one":
				lcomp = new Constant(x, y, CompType.ONE);
				break;
			case "button":
				lcomp = new Button(x, y);
				break;
			case "display":
				lcomp = new Display(x, y);
				break;
			default:
				throw new IllegalArgumentException("Component name not recognized");
		}
		return lcomp;
	}

	/**
	 * Creates a deep clone of the given list of LComponents without adding any offset
	 * @param lcomps The LComponents to duplicate
	 * @return The list of duplicated components
	 */
	public static ArrayList<LComponent> duplicate(List<LComponent> lcomps){
		return duplicate(lcomps, new Point(0, 0));
	}
	
	/**
	 * Creates a deep clone of the given list of LComponents
	 * @param lcomps The LComponents to duplicate
	 * @param pos The (center) position of the copied components
	 * @return The list of duplicated components
	 */
	public static ArrayList<LComponent> duplicate(List<LComponent> lcomps, Point pos) {
		HashMap<LComponent, LComponent> oldToNew = new HashMap<LComponent, LComponent>();
		ArrayList<LComponent> newComps = new ArrayList<LComponent>();
		ArrayList<Wire> oldWires = new ArrayList<Wire>();

		Rectangle oldBounds = getBoundingRectangle(lcomps);
		
		for(int l = 0; l < lcomps.size(); l++) { 
			LComponent oldComp = lcomps.get(l);
			IOManager oldIO = oldComp.getIO();
			LComponent newComp = oldComp.makeCopy();
			newComp.setX(pos.x + oldComp.getX() - (int) oldBounds.getCenterX());
			newComp.setY(pos.y + oldComp.getY() - (int) oldBounds.getCenterY());
			oldToNew.put(oldComp, newComp);
			newComps.add(newComp);
			for(int c = 0; c < oldIO.getNumInputs(); c++) {
				Connection connection = oldIO.connectionAt(c, Connection.INPUT);
				if(connection.numWires() == 1) {
					Wire oldWire = connection.getWire();
					Connection source = oldWire.getSourceConnection();
					if(source != null && lcomps.contains(source.getLcomp())) oldWires.add(oldWire);
				}
			}
		}

		for(int w = 0; w < oldWires.size(); w++) {
			Wire oldWire = oldWires.get(w);
			Connection oldSourceConnection = oldWire.getSourceConnection();
			Connection oldDestConnection = oldWire.getDestConnection();
			LComponent newSourceComp = oldToNew.get(oldSourceConnection.getLcomp());
			LComponent newDestComp = oldToNew.get(oldDestConnection.getLcomp());
			Connection newSourceConnection = newSourceComp.getIO().connectionAt(oldSourceConnection.getIndex(), Connection.OUTPUT);
			Connection newDestConnection = newDestComp.getIO().connectionAt(oldDestConnection.getIndex(), Connection.INPUT);
			Wire newWire = new Wire();
			newWire.setSignal(oldWire.getSignal());
			newSourceConnection.addWire(newWire);
			newDestConnection.addWire(newWire);
		}
		
		return newComps;
	}
	
	/**
	 * Makes a copy of the given Custom component by duplicating its inner components
	 * @param custom The Custom component to copy
	 * @return A copy of the Custom component
	 */
	public static Custom duplicateCustom(Custom custom) {
		LComponent[][] content = custom.getContent();
		ArrayList<LComponent> innerComps = custom.getInnerComps();
		ArrayList<LComponent> newInnerComps = CompUtils.duplicate(innerComps);
		ArrayList<LComponent> top = new ArrayList<LComponent>(), bottom = new ArrayList<LComponent>(), left = new ArrayList<LComponent>(), 
				right = new ArrayList<LComponent>();
		for(int i = 0; i < newInnerComps.size(); i++) {
			LComponent oldComp = innerComps.get(i);
			LComponent newComp = newInnerComps.get(i);
			if(oldComp instanceof Light || oldComp instanceof Switch) {
				if(Arrays.asList(content[CompRotator.LEFT]).contains(oldComp)) CompUtils.addInPlace(newComp, left, false);
				else if(Arrays.asList(content[CompRotator.UP]).contains(oldComp)) CompUtils.addInPlace(newComp, top, true);
				else if(Arrays.asList(content[CompRotator.RIGHT]).contains(oldComp)) CompUtils.addInPlace(newComp, right, false);
				else if(Arrays.asList(content[CompRotator.DOWN]).contains(oldComp)) CompUtils.addInPlace(newComp, bottom, true);
			}
		}

		LComponent[][] newContent = new LComponent[][] {right.toArray(new LComponent[0]),
				bottom.toArray(new LComponent[0]),
				left.toArray(new LComponent[0]),
				top.toArray(new LComponent[0])};
		Custom result = new Custom(custom.getX(), custom.getY(), custom.getLabel(), newContent, newInnerComps, custom.getTypeID());
		result.setName(custom.getName());
		result.getRotator().setRotation(custom.getRotator().getRotation());
		return result;
	}
	
	/**
	 * Rotates the given components in the specified direction (CompRotator.CLOCKWISE or CompRotator.COUNTER_CLOCKWISE) so that all
	 * of the components are the same distance apart.  This method relies on the CompUtils.getBoundingRectangle(...) method along with
	 * the CompRotator.withRotation(...) method.
	 * @param lcomps The list of components to rotate
	 * @param direction The direction to rotate the components
	 */
	public static void rotateAll(ArrayList<LComponent> lcomps, boolean direction) {
		Rectangle bounds = getBoundingRectangle(lcomps);
		int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
		int rotation;
		if(direction == CompRotator.CLOCKWISE) rotation = CompRotator.DOWN;
		else rotation = CompRotator.UP;
		for(int i = 0; i < lcomps.size(); i++) {
			LComponent lcomp = lcomps.get(i);
			Point rotPoint = CompRotator.withRotation(lcomp.getX() - x, lcomp.getY() - y, width, height, rotation);
			CompRotator rotator = lcomp.getRotator();
			if(direction == CompRotator.CLOCKWISE) {
				rotator.setRotation(rotator.getRotation() + 1);
				lcomp.setX(rotPoint.x - lcomp.getBounds().width + x + 1);
				lcomp.setY(rotPoint.y + y);
			}
			else {
				rotator.setRotation(rotator.getRotation() - 1);
				lcomp.setX(rotPoint.x + x);
				lcomp.setY(rotPoint.y - lcomp.getBounds().height + y + 1);
			}
		}
	}
	
	/**
	 * Returns the smallest possible Rectangle that completely encloses all of the components 
	 * @return The bounding Rectangle
	 */
	public static Rectangle getBoundingRectangle(List<LComponent> lcomps) {
		int minX = 0, maxX = 0, minY = 0, maxY = 0;
		for(int i = 0; i < lcomps.size(); i++) {
			LComponent lcomp = lcomps.get(i);
			int compMinX = lcomp.getX();
			int compMaxX = (int) (lcomp.getX() + lcomp.getBounds().getWidth());
			int compMinY = lcomp.getY();
			int compMaxY = (int) (lcomp.getY() + lcomp.getBounds().getHeight());
			
			if(i == 0) {
				minX = compMinX;
				maxX = compMaxX;
				minY = compMinY;
				maxY = compMaxY;
			}
			else {
				if(compMinX < minX) minX = compMinX;
				if(compMaxX > maxX) maxX = compMaxX;
				if(compMinY < minY) minY = compMinY;
				if(compMaxY > maxY) maxY = compMaxY;
			}
		}
		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}
	
	/**
	 * Adds the given LComponent to the list based on its x or y value. The component is placed so that the list has the components in order
	 * of ascending x or y value
	 * @param lcomp The LComponent to add
	 * @param lcomps The list of LComponents
	 * @param xy True if the components are being sorted by x value, false if the components are being sorted by y value
	 */
	public static void addInPlace(LComponent lcomp, ArrayList<LComponent> lcomps, boolean xy) {
		if(lcomps.size() == 0) lcomps.add(lcomp);
		else {
			int val;
			if(xy) val = lcomp.getX();
			else val = lcomp.getY();
			boolean compAdded = false;
			
			for(int i = 0; i < lcomps.size(); i++) {
				int above;
				if(i >= lcomps.size()) above = Integer.MAX_VALUE;
				else {
					if(xy) above = lcomps.get(i).getX();
					else above = lcomps.get(i).getY();
				}
				int below;
				if(i - 1 < 0) below = Integer.MIN_VALUE;
				else {
					if(xy) below = lcomps.get(i - 1).getX();
					else below = lcomps.get(i - 1).getY();
				}
				if(below < val && val < above) {
					lcomps.add(i, lcomp);
					compAdded = true;
				}
			}
			if(!compAdded) lcomps.add(lcomp);
		}
	}
	
}
