package com.logic.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import com.logic.components.Custom;
import com.logic.components.LComponent;
import com.logic.components.Light;
import com.logic.components.Switch;
import com.logic.input.CircuitState;
import com.logic.input.Selection;
import com.logic.util.CompUtils;

/**
 * This class handles the UI for creating custom components
 * @author Hank Stennes
 *
 */
public class CustomCreator {
	
	/**
	 * The padding inside of the centerRectangle
	 */
	private final int centerRectExpand = 25;
	
	/**
	 * The x and y length of the divider lines drawn around the center rectangle
	 */
	private final int dividerLineExtension = 1000;
	
	/**
	 * The placement location of a new custom component relative to the bottom right corner of the center rectangle
	 */
	private final int customPlacementOffset = 100;
	
	/**
	 * The help message that will be displayed in the CircuitPanel
	 */
	private final String messageText = "Drag lights and switches outside the box to specify the layout of the component";
	
	/**
	 * The LComponents that are currently being made into a custom component
	 */
	private ArrayList<LComponent> lcomps;
	
	/**
	 * The center rectangle that is drawn around the components 
	 */
	public Rectangle centerRect;
	
	/**
	 * A boolean that tells whether a custom component is currently being created
	 */
	private boolean active = false;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;

	/**
	 * A list containing copies of all custom components that have been created. A future feature could be to actually display these in a list rather than having the user just
	 * copy / paste, but for now this list is only useful for saving the circuit.
	 */
	private ArrayList<Custom> customs;
	
	/**
	 * Constructs a new CustomCreator
	 * @param cp The CircuitPanel
	 */
	public CustomCreator(CircuitPanel cp) {
		this.cp = cp;
		lcomps = new ArrayList<>();
		customs = new ArrayList<>();
	}
	
	/**
	 * Renders the center bounding box and the diagonal lines that divide each input/output section if this CustomCreator is currently active
	 * @param g The Graphics object to use
	 */
	public void render(Graphics g) {
		if(active) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.BLUE);
			g2d.setStroke(new BasicStroke(5));
			g2d.draw(centerRect);
			
			int x = centerRect.x;
			int y = centerRect.y;
			int x2 = centerRect.x + centerRect.width;
			int y2 = centerRect.y + centerRect.height;
			g2d.drawLine(x, y, x - dividerLineExtension, y - dividerLineExtension);
			g2d.drawLine(x2, y, x2 + dividerLineExtension, y - dividerLineExtension);
			g2d.drawLine(x2, y2, x2 + dividerLineExtension, y2 + dividerLineExtension);
			g2d.drawLine(x, y2, x - dividerLineExtension, y2 + dividerLineExtension);
			g2d.setStroke(new BasicStroke(1));
		}
	}

	/**
	 * Activates this CustomCreator. This method displays the custom component message, calculates the bounding rectangle that will be drawn
	 * around the selection, records the current selection, and clears the selection. If this CustomCreator is already active, than this
	 * method returns without doing anything. The CustomCreator will become deactivated when completeCustom() is executed successfully.
	 */
	public void createCustom() {
		Selection selection = cp.getEditor().getSelection();
		if(active || selection.size() == 0) return;
		active = true;
		cp.dispMessage(new UserMessage(cp, messageText));
		centerRect = CompUtils.getBoundingRectangle(selection);
		centerRect.setBounds(centerRect.x - centerRectExpand, 
				centerRect.y - centerRectExpand, 
				centerRect.width + 2 * centerRectExpand, 
				centerRect.height + 2 * centerRectExpand);
		lcomps.addAll(selection);
		selection.clear();
	}
	
	/**
	 * Calculates the layout of inputs and outputs for the custom component based on the positions given by the user and adds the custom 
	 * component to the CircuitPanel. If the user has left any of the lights or switches inside of the bounding rectangle, then this method
	 * will do nothing and return a value of false.
	 */
	public void completeCustom() {
		ArrayList<LComponent> lcomps = CompUtils.duplicate(this.lcomps);
		LComponent[][] content = getCustomContent(lcomps, centerRect);
		if(content == null)
			JOptionPane.showMessageDialog(null, "Please drag all lights and switches outside of the rectangle");
		String label = JOptionPane.showInputDialog(null, "New component label?");
		if(label != null){
			int x = centerRect.x + centerRect.width + customPlacementOffset;
			int y = centerRect.y + centerRect.height + customPlacementOffset;
			Custom custom = new Custom(x, y, label, content, lcomps, customs.size());
			storeCopy(custom);
			cp.addLComp(custom);
			cp.getEditor().getRevision().saveState(new CircuitState(cp));
		}
		reset();
	}

	/**
	 * Constructs the content parameter for a Custom component from the bounding rectangle and list of components
	 * @param lcomps The components selected by the user
	 * @param centerBounds The bounding box
	 * @return The content parameter
	 */
	public static LComponent[][] getCustomContent(ArrayList<LComponent> lcomps, Rectangle centerBounds){
		int a = centerBounds.x;
		int b = centerBounds.y;
		int a2 = centerBounds.x + centerBounds.width;
		int b2 = centerBounds.y + centerBounds.height;
		ArrayList<LComponent> top = new ArrayList<LComponent>();
		ArrayList<LComponent> bottom = new ArrayList<LComponent>();
		ArrayList<LComponent> left = new ArrayList<LComponent>();
		ArrayList<LComponent> right = new ArrayList<LComponent>();
		for(int i = 0; i < lcomps.size(); i++) {
			LComponent lcomp = lcomps.get(i);
			if(lcomp instanceof Light || lcomp instanceof Switch) {
				if(centerBounds.contains(lcomp.getX(), lcomp.getY())) return null;
				int x = lcomp.getX();
				int y = lcomp.getY();
				if(y - b + a <= x && x <= -y + b + a2 && y <= b) CompUtils.addInPlace(lcomp, top, true);
				else if(-y + b2 + a <= x && x <= y - b2 + a2 && y >= b2) CompUtils.addInPlace(lcomp, bottom, true);
				else if(x <= a && x - a + b <= y && y <= a - x + b2) CompUtils.addInPlace(lcomp, left, false);
				else if(x >= a && a2 - x + b <= y && y <= x - a2 + b2) CompUtils.addInPlace(lcomp, right, false);
			}
		}

		return new LComponent[][] {right.toArray(new LComponent[0]),
				bottom.toArray(new LComponent[0]),
				left.toArray(new LComponent[0]),
				top.toArray(new LComponent[0])};
	}

	/**
	 * Saves a copy of a newly created custom component to the list of distinct customs
	 * @param custom The component to add
	 */
	private void storeCopy(Custom custom){
		Custom copy = CompUtils.duplicateCustom(custom);
		copy.setX(0);
		copy.setY(0);
		customs.add(copy);
	}
	
	/**
	 * Resets this CustomCreator by setting its active state to false, clearing the list of involved components, clearing the center 
	 * rectangle, and clearing the custom component message from the CircuitPanel
	 */
	public void reset() {
		lcomps.clear();
		active = false;
		centerRect.setBounds(0, 0, 0, 0);
		cp.clearMessage();
	}
	
	/**
	 * Tells whether this CustomCreator is active
	 * @return The active state
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Returns the list of distinct types of custom components
	 * @return the list of customs
	 */
	public ArrayList<Custom> getCustoms(){
		return customs;
	}

	/**
	 * Sets the list of distinct custom components (for loading from file)
	 * @param customs The list of customs
	 */
	public void setCustoms(ArrayList<Custom> customs){
		this.customs = customs;
	}
	
}
