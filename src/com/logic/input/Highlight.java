package com.logic.input;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.logic.components.LComponent;
import com.logic.ui.CircuitPanel;

/**
 * This class manages the creation of a highlight box by the user for the purpose of selecting LComponents
 * @author Hank Stennes
 *
 */
public class Highlight {
	
	/**
	 * The current highlight box, within the context of the CircuitPanel coordinate system
	 */
	private Rectangle box;
	
	/**
	 * A boolean that tells whether the user is currently dragging a highlight box
	 */
	private boolean active = false; 
	
	/**
	 * The anchor point of the highlight box
	 */
	private int startX, startY;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * The CircuitEditor
	 */
	private CircuitEditor editor;
	
	/**
	 * Constructs a new Highlight
	 * @param cp The CircuitPanel
	 * @param editor The CircuitEditor
	 */
	public Highlight(CircuitPanel cp, CircuitEditor editor) {
		this.cp = cp;
		this.editor = editor;
		box = new Rectangle();
	}
	
	/**
	 * This method should be called when the mouse is dragged in the CircuitPanel and the selection size is 0
	 * @param x The x location of the mouse when the mouseDragged event occurred
	 * @param y The y location of the mouse when the mouseDragged event occurred
	 */
	public void drag(int x, int y) {
		if(!active) {
			active = true;
			startX = x;
			startY = y;
			box.setBounds(startX, startY, 0, 0);
		}
		box = new Rectangle(new Point(startX, startY));
		box.add(new Point(x, y));
	}
	
	/**
	 * Adds any LComponents that intersect the highlight box to the selection and clears the highlight box
	 */
	public void release() {
		ArrayList<LComponent> lcomps = new ArrayList<LComponent>();
		for(int i = 0; i < cp.lcomps.size(); i++) {
			LComponent lcomp = cp.lcomps.get(i);
			if(lcomp.getBounds().intersects(box)) lcomps.add(lcomp);
		}
		editor.getSelection().select(lcomps);
		box.setBounds(0, 0, 0, 0);
		active = false;
	}

	public Rectangle getBounds(){
		return box;
	}
	
}
