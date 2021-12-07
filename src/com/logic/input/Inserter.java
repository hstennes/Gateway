package com.logic.input;

import com.logic.components.LComponent;
import com.logic.ui.CircuitPanel;
import com.logic.ui.InsertPanel;
import com.logic.util.CompUtils;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * This class holds the code for adding components to the CircuitPanel
 * @author Hank Stennes
 *
 */
public class Inserter {

	/**
	 * The position of a scheduled insert
	 */
	private int scheduleX, scheduleY;
	
	/**
	 * A boolean telling whether an insert is scheduled
	 */
	private boolean insertScheduled;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * The CircuitEditor
	 */
	private CircuitEditor editor;
	
	/**
	 * The InsertPanel
	 */
	private InsertPanel insertPanel;
	
	/**
	 * The RevisionManager
	 */
	private RevisionManager revision;
	
	/**
	 * Constructs a new Inserter
	 * @param cp The CircuitPanel
	 * @param editor The CircuitEditor
	 * @param insertPanel The InsertPanel
	 */
	public Inserter(CircuitPanel cp, CircuitEditor editor, InsertPanel insertPanel, RevisionManager revision) {
		this.cp = cp;
		this.editor = editor;
		this.insertPanel = insertPanel;
		this.revision = revision;
	}
	
	/**
	 * Schedules an insert so that it can be performed when attemptInsert() is called. This method is usually called within a mouse pressed
	 * method
	 * @param scheduleX The x value of the CircuitPanel coordinate
	 * @param scheduleY The y value of the CircuitPanel coordinate
	 */
	public void scheduleInsert(int scheduleX, int scheduleY) {
		this.scheduleX = scheduleX;
		this.scheduleY = scheduleY;
		insertScheduled = true;
	}
	
	/**
	 * Attempts an insert. The insert will occur as long as an insert has been scheduled and the mouse has not moved more than CircuitEditor
	 * .DRAG_THRESH in the x or y direction
	 * @param e The MouseEvent that is causing this method to be called
	 */
	public void attemptInsert(MouseEvent e) {
		if(insertScheduled) {
			insertScheduled = false;
			Point location = cp.withTransform(e.getPoint());
			if(Math.abs(location.x - scheduleX) < CircuitEditor.DRAG_THRESH && Math.abs(location.y - scheduleY) < CircuitEditor.DRAG_THRESH) {
				String name = insertPanel.getSelectedComponent();
				if(name != null) doInsert(name, location);
			}
		}
	}
	
	/**
	 * Inserts a new LComponent of the type represented by the given name at the specified location. If editor.isSnap() returns true, then
	 * the coordinates that the component is placed at are certain to be multiples of CircuitEditor.SNAP_DIST. Otherwise, the component
	 * is placed so that is center appears at the mouse's location based on the offsets HashMap. This method also saves a CircuitState
	 * so that the insertion can be undone.
	 * @param name The name of the LComponent (the CompType with only the first letter capitalized)
	 * @param location The location of the mouse
	 */
	private void doInsert(String name, Point location) {
		LComponent lcomp = CompUtils.makeComponent(name, location.x, location.y);
		Rectangle b = lcomp.getBounds();
		if(editor.isSnap()){
			lcomp.setX((b.x - b.width / 2) / CircuitEditor.SNAP_DIST * CircuitEditor.SNAP_DIST);
			lcomp.setY((b.y - b.height / 2) / CircuitEditor.SNAP_DIST * CircuitEditor.SNAP_DIST);
		}
		else {
			lcomp.setX(b.x - b.width / 2);
			lcomp.setY(b.y - b.height / 2);
		}
		cp.addLComp(lcomp);
		revision.saveState(new CircuitState(cp));
	}
}
