package com.logic.util;

import com.logic.components.Button;
import com.logic.components.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A class that holds static methods for performing various operations on lists of LComponents
 * @author Hank Stennes
 *
 */
public class CompUtils {

	public static final double[] RAD_ROTATION = new double[] {0, Math.PI / 2, Math.PI, -Math.PI / 2};

	/**
	 * Creates a component of the given type with the given coordinates (does not support custom components). Supports all components
	 * besides splitters since they require additional configuration (bit width split, in/out splitter).
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
			case "rom":
				lcomp = new ROM(x, y);
				break;
			case "ram":
				lcomp = new RAM(x, y);
				break;
			case "screen":
				lcomp = new Screen(x, y);
				break;
			case "label":
				lcomp = new UserLabel(x, y);
				break;
			case "splitter":
				throw new IllegalArgumentException("CompUtils.makeComponent does not support splitters");
			default:
				throw new IllegalArgumentException("Component name not recognized");
		}
		return lcomp;
	}

	/**
	 * Returns component data that is related to the state of the simulation. This varies based on the type of component,
	 * and is not meaningful for all components. The purpose of this method is to record component state before performing
	 * rendering that considers the component state multiple times, as the state could change during rendering as the simulation
	 * runs on its own thread. For example, we may want to record the signal that a Light is receiving ahead of time to make
	 * sure the value stays constant. The same data format returned from this method is considered by many methods in the
	 * Renderer, as well as LComponent.getActiveImageIndex.
	 * @param lcomp The lcomp to read state from
	 * @return The relevant component state represented as an integer
	 */
	public static int getSensitiveCompData(LComponent lcomp) {
		switch(lcomp.getType()) {
			case SWITCH:
				return ((Switch) lcomp).getState();
			case BUTTON:
				return ((Button) lcomp).getState();
			case LIGHT:
				IOManager io = lcomp.getIO();
				if(io.getNumInputs() == 0) return 0;
				return io.getInput(0);
			case CLOCK:
				return ((Clock) lcomp).isOn() ? 1 : 0;
			case DISPLAY:
				return ((Display) lcomp).getValue();
		}
		return 0;
	}

	/**
	 * Creates a deep clone of the given list of LComponents without adding any offset
	 * @param lcomps The LComponents to duplicate
	 * @return The list of duplicated components
	 */
	public static ArrayList<LComponent> duplicate(List<LComponent> lcomps){
		return duplicate(lcomps, new Point(0, 0), false);
	}
	
	/**
	 * Creates a deep clone of the given list of LComponents
	 * @param lcomps The LComponents to duplicate
	 * @param pos The (center) position of the copied components
	 * @param move True to use position, false to leave at current position
	 * @return The list of duplicated components
	 */
	public static ArrayList<LComponent> duplicate(List<LComponent> lcomps, Point pos, boolean move) {
		HashMap<LComponent, LComponent> oldToNew = new HashMap<>();
		ArrayList<Wire> oldWires = new ArrayList<>();

		int offX = 0, offY = 0;
		if(move) {
			Rectangle oldBounds = getBoundingRectangle(lcomps);
			offX = pos.x - (int) oldBounds.getCenterX();
			offY = pos.y - (int) oldBounds.getCenterY();
		}

		for(int l = 0; l < lcomps.size(); l++) {
			LComponent oldComp = lcomps.get(l);
			IOManager oldIO = oldComp.getIO();
			LComponent newComp = oldComp.makeCopy();
			newComp.setX(oldComp.getX() + offX);
			newComp.setY(oldComp.getY() + offY);
			oldToNew.put(oldComp, newComp);
			for(int c = 0; c < oldIO.getNumInputs(); c++) {
				Connection connection = oldIO.inputConnection(c);
				if(connection.numWires() == 1) {
					Wire oldWire = connection.getWire();
					Connection source = oldWire.getSourceConnection();
					if(source != null && lcomps.contains(source.getLcomp())) oldWires.add(oldWire);
				}
			}
		}

		for (Wire oldWire : oldWires) {
			OutputPin oldSourceConnection = oldWire.getSourceConnection();
			InputPin oldDestConnection = oldWire.getDestConnection();
			LComponent newSourceComp = oldToNew.get(oldSourceConnection.getLcomp());
			LComponent newDestComp = oldToNew.get(oldDestConnection.getLcomp());
			OutputPin newSourceConnection = newSourceComp.getIO().outputConnection(oldSourceConnection.getIndex());
			InputPin newDestConnection = newDestComp.getIO().inputConnection(oldDestConnection.getIndex());
			Wire newWire = new Wire();
			newSourceConnection.setSignal(oldSourceConnection.getSignal());
			newSourceConnection.addWire(newWire);
			newDestConnection.addWire(newWire);
		}

		return new ArrayList<>(oldToNew.values());
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
				if(Arrays.asList(content[Constants.LEFT]).contains(oldComp)) CompUtils.addInPlace(newComp, left, false);
				else if(Arrays.asList(content[Constants.UP]).contains(oldComp)) CompUtils.addInPlace(newComp, top, true);
				else if(Arrays.asList(content[Constants.RIGHT]).contains(oldComp)) CompUtils.addInPlace(newComp, right, false);
				else if(Arrays.asList(content[Constants.DOWN]).contains(oldComp)) CompUtils.addInPlace(newComp, bottom, true);
			}
		}

		LComponent[][] newContent = new LComponent[][] {right.toArray(new LComponent[0]),
				bottom.toArray(new LComponent[0]),
				left.toArray(new LComponent[0]),
				top.toArray(new LComponent[0])};
		Custom result = new Custom(custom.getX(), custom.getY(), custom.getLabel(), newContent, newInnerComps, custom.getTypeID());
		result.setName(custom.getName());
		result.setRotation(custom.getRotation());
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
		if(direction == Constants.CLOCKWISE) rotation = Constants.DOWN;
		else rotation = Constants.UP;
		for (LComponent lcomp : lcomps) {
			Point rotPoint = withRotation(lcomp.getX() - x, lcomp.getY() - y, width, height, rotation);
			if (direction == Constants.CLOCKWISE) {
				lcomp.setRotation(lcomp.getRotation() + 1);
				lcomp.setX(rotPoint.x - lcomp.getBounds().width + x + 1);
				lcomp.setY(rotPoint.y + y);
			} else {
				lcomp.setRotation(lcomp.getRotation() - 1);
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
				int above = xy ? lcomps.get(i).getX() : lcomps.get(i).getY();

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

	public static int[] promptROMProgram(){
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Hack programs", "hack");
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(filter);
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			ArrayList<Integer> data = new ArrayList<>();
			try {
				File file = fc.getSelectedFile();
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);

				String line;
				while ((line = br.readLine()) != null) data.add(Integer.parseInt(line, 2));
			} catch (IOException e) {
				e.printStackTrace();
			}

			int[] program = new int[data.size()];
			for (int i = 0; i < data.size(); i++) program[i] = data.get(i);
			return program;
		}
		else return null;
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
		if(rotation == Constants.DOWN) return new Point(height - y - 1, x);
		else if(rotation == Constants.UP) return new Point(y, width - x - 1);
		else if(rotation == Constants.LEFT) return new Point(width - x - 1, height - y - 1);
		else return new Point(x, y);
	}
}
