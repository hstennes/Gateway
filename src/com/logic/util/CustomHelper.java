package com.logic.util;

import com.logic.components.Connection;
import com.logic.components.LComponent;
import com.logic.components.Light;
import com.logic.components.Switch;
import com.logic.ui.CompProperties;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

/**
 * A class that provides helper methods for the setup and functioning of a Custom component
 * @author Hank Stennes
 *
 */
public class CustomHelper {
	
	/**
	 * The space between each connection
	 */
	private final int connectionSpacing = 50;

	/**
	 * How far connections extend away from the component
	 */
	private final int connectionLength = 25;

	/**
	 * Value to be added to absolute minimum width required to fit left connection, right connection, main labels
	 */
	private final int labelWidthPadding = 50;

	/**
	 * Extra value to be added to necessary height of component
	 */
	private final int verticalSpacing = 10;
	
	/**
	 * The minimum size for the width or height of a component.
	 */
	private final int minSize = 50;
	
	/**
	 * The map of lights and switches from the CustomCreator
	 */
	private final LComponent[][] content;

	private String[][] connectionLabels;

	private int[][] inIndexToSideNum;

	private int[][] outIndexToSideNum;

	private final int[] sideRange = {Constants.RIGHT, Constants.DOWN, Constants.LEFT, Constants.UP};
	
	/**
	 * Constructs a new CustomHelper
	 * @param content The content map given to the constructor of the custom component
	 */
	public CustomHelper(LComponent[][] content) {
		this.content = content;
		connectionLabels = generateConnectionLabels();
		generateIndexToSideNum();
	}
	
	/**
	 * Chooses an appropriate width for the custom component. This is done by taking the maximum between the width required
	 * in order to fit the label and the width required to fit the number of connections.
	 * @param label The label of the custom component
	 * @param labelFont The font to be used for the label
	 * @return The width of the Custom component
	 */
	public int chooseWidth(String label, Font labelFont) {
		int widthFromConnect = findDimension(content[Constants.UP], content[Constants.DOWN]);
		FontRenderContext frc = new FontRenderContext(labelFont.getTransform(), true, true);
		Rectangle2D mainLabelBounds = labelFont.getStringBounds(label, frc);

		int maxLeftWidth = 0;
		for(int i = 0; i < connectionLabels[Constants.LEFT].length; i++) {
			int width = (int) labelFont.getStringBounds(connectionLabels[Constants.LEFT][i], frc).getWidth();
			if(width > maxLeftWidth) maxLeftWidth = width;
		}
		int maxRightWidth = 0;
		for(int i = 0; i < connectionLabels[Constants.RIGHT].length; i++){
			int width = (int) labelFont.getStringBounds(connectionLabels[Constants.RIGHT][i], frc).getWidth();
			if(width > maxRightWidth) maxRightWidth = width;
		}

		int widthFromLabels = (int) (mainLabelBounds.getWidth() + Math.max(maxLeftWidth, maxRightWidth) * 2 + labelWidthPadding);
		return Math.max(widthFromConnect, widthFromLabels);
	}
	
	/**
	 * Chooses an appropriate height for the custom component in large pixels. This is done by choosing the smallest value that allows for 
	 * the connections to be evenly spaced and centered according to the connectionSpacing constant.
	 * @return
	 */
	public int chooseHeight() {
		return findDimension(content[Constants.LEFT], content[Constants.RIGHT]) + verticalSpacing;
	}
	
	/**
	 * Returns the list of points that each connection should be placed at for the given side. The points will be spaced apart by the 
	 * connectionSpacing constant and the group of points will be centered within their side of the component.
	 * @param sideNum The CompRotator constant that represents the side 
	 * @param width The width of the custom component, as returned by chooseWidth
	 * @param height The height of the custom component, as returned by chooseHeight
	 * @return A list of points to place the connections at
	 */
	public Point[] getConnectionPoints(int sideNum, int width, int height) {
		int num = countConnections(content[sideNum]);
		Point[] points = new Point[num];
		int connectionSpace = num + (num - 1) * connectionSpacing;	
		int pos;
		if(sideNum == Constants.LEFT || sideNum == Constants.RIGHT) pos = ((height) - connectionSpace) / 2;
		else pos = ((width) - connectionSpace) / 2;
		
		for(int i = 0; i < points.length; i++) {
			if(sideNum == Constants.LEFT) points[i] = new Point(-connectionLength, pos);
			else if(sideNum == Constants.UP) points[i] = new Point(pos, -connectionLength);
			else if(sideNum == Constants.DOWN) points[i] = new Point(pos, height + connectionLength);
			else if(sideNum == Constants.RIGHT) points[i] = new Point(width + connectionLength, pos);
			pos += connectionSpacing + 1;
		}
		return points;
	}

	/**
	 * Returns a 2D array of connection labels in the same format as the content array based on the names of the input / output
	 * components.
	 * @return The connection labels
	 */
	public String[][] getConnectionLabels(boolean refresh) {
		if(refresh) connectionLabels = generateConnectionLabels();
		return connectionLabels;
	}

	/**
	 * Returns the side number and position on the side of the connection with the given index in IOManager. Resulting array is
	 * of the form {side number, index on side}
	 * @param index The index of the connection in IOManager
	 * @param connectionType The type of connection (input or output)
	 * @return The side number and position
	 */
	public int[] getSideAndNum(int index, int connectionType) {
		if(connectionType == Connection.INPUT) return inIndexToSideNum[index];
		return outIndexToSideNum[index];
	}

	/**
	 * Generates a 2D array of connection labels in the same format as the content array based on the names of the input / output
	 * components.
	 * @return The connection labels
	 */
	private String[][] generateConnectionLabels(){
		String[][] labels = new String[4][];
		for(int i : sideRange){
			labels[i] = new String[content[i].length];
			for(int j = 0; j < content[i].length; j++) {
				String compName = content[i][j].getName();
				labels[i][j] = compName.equals(CompProperties.defaultName) ? "" : compName;
			}
		}
		return labels;
	}

	/**
	 * Fills inIndexToSideNum and outIndexToSideNum. See getSideAndNum.
	 */
	private void generateIndexToSideNum() {
		int numInputs = 0;
		int numOutputs = 0;
		for(int i : sideRange) {
			for(int j = 0; j < content[i].length; j++) {
				LComponent lcomp = content[i][j];
				if (lcomp instanceof Switch) numInputs++;
				else if (lcomp instanceof Light) numOutputs++;
			}
		}
		inIndexToSideNum = new int[numInputs][];
		outIndexToSideNum = new int[numOutputs][];
		int inIndex = 0;
		int outIndex = 0;
		for(int i : sideRange) {
			for (int j = 0; j < content[i].length; j++) {
				LComponent lcomp = content[i][j];
				if (lcomp instanceof Switch) {
					inIndexToSideNum[inIndex] = new int[]{i, j};
					inIndex++;
				} else if (lcomp instanceof Light) {
					outIndexToSideNum[outIndex] = new int[]{i, j};
					outIndex++;
				}
			}
		}
	}
	
	/**
	 * Finds the large pixel dimension required in order to fit the given lists of components on the top/bottom or the left/right
	 * @param one The first list of lights and switches (left or top)
	 * @param two The second list of lights and switches (right or bottom)
	 * @return The minimum dimension that fits the given inputs and outputs
	 */
	private int findDimension(LComponent[] one, LComponent[] two) {
		int firstDimension = Math.max(minSize, connectionSpacing * countConnections(one) + 20);
		int secondDimension = Math.max(minSize, connectionSpacing * countConnections(two) + 20);
		return Math.max(firstDimension, secondDimension);
	}
	
	/**
	 * Counts the number of connections that are represented by the given array of Lights and Switches, without causing a null pointer if
	 * the array is null
	 * @param side The array
	 * @return The number of connections
	 */
	private int countConnections(LComponent[] side) {
		if(side != null) return side.length;
		return 0;
	}
}
