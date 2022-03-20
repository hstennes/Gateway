package com.logic.ui;

import com.logic.components.Clock;
import com.logic.components.LComponent;
import com.logic.custom.CustomType;
import com.logic.custom.OpCustom2;
import com.logic.input.Camera;
import com.logic.util.CompUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that displays the components that make up custom components
 * @author Hank Stennes
 *
 */
public class CustomViewer {

	/**
	 * A boolean that tells whether a custom component is currently being viewed
	 */
	private boolean active;
	
	/**
	 * The list of LComponents that were in the CircuitPanel before they were replaced with those making up the custom component. This list
	 * is used for restoring the CircuitPanel to its previous state when exit() is called
	 */
	private ArrayList<LComponent> oldComps;
	
	/**
	 * The zoom of the camera before this CustomViewer became active
	 */
	private float oldCamZoom;
	
	/**
	 * The position of the camera before this CustomViewer became active
	 */
	private float oldCamX, oldCamY;

	private CustomType viewingType;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * Constructs a new CustomViewer
	 * @param cp The CircuitPanel
	 */
	public CustomViewer(CircuitPanel cp) {
		this.cp = cp;
		oldComps = new ArrayList<>();
	}
	
	/**
	 * Displays the inner components in the given custom component using its dispComps list. This method stores the current CircuitPanel
	 * state, clears the CircuitPanel and selection, disables the CircuitEditor, adds the inner components to the the CircuitPanel, 
	 * repositions the camera, and displays the custom component message.
	 * @param custom The Custom component
	 */
	public void view(OpCustom2 custom) {
		viewingType = custom.getCustomType();
		ArrayList<LComponent> dispComps = custom.projectInnerStateToType();

		oldComps.addAll(cp.lcomps);
		cp.lcomps.clear();
		cp.wires.clear();
		cp.getEditor().getSelection().clear();
		//TODO prevent user from modifying lights and switches
		cp.addLComps(custom.getCustomType().lcomps);
		
		Camera cam = cp.getCamera();
		oldCamZoom = cam.getZoom();
		oldCamX = cam.getX();
		oldCamY = cam.getY();
		cam.setZoom(1);
		Rectangle boundingRect = CompUtils.getBoundingRectangle(dispComps);
		cam.setX((int) -(boundingRect.getX() - (cp.getWidth() - boundingRect.getWidth()) / 2));
		cam.setY((int) -(boundingRect.getY() - (cp.getHeight() - boundingRect.getHeight()) / 2));
		
		cp.getProperties().refresh();
		cp.dispMessage(new UserMessage(cp, "Viewing custom component, press ESC to exit"));
		active = true;
	}
	
	/**
	 * This method exits the custom component view by clearing the CircuitPanel, adding the oldComps list to the CircuitPanel, enabling
	 * the CircuitEditor, and repositioning the camera.
	 */
	public void exit() {
		ArrayList<LComponent> newComps = new ArrayList<>(cp.lcomps);
		viewingType.modify(newComps);

		for(LComponent lcomp : cp.lcomps) {
			if(lcomp instanceof OpCustom2) ((OpCustom2) lcomp).stop();
			else if(lcomp instanceof Clock) ((Clock) lcomp).stop();
		}
		cp.lcomps.clear();
		cp.wires.clear();
		cp.addLComps(oldComps);

		rebuildDependentComponents();

		Camera cam = cp.getCamera();
		cam.setZoom(oldCamZoom);
		cam.setX(oldCamX);
		cam.setY(oldCamY);
		
		cp.getProperties().refresh();
		cp.clearMessage();
		cp.repaint();
		active = false;
		oldComps.clear();
	}

	private void rebuildDependentComponents(){
		ArrayList<CustomType> customTypes = cp.getEditor().getCustomCreator().getCustomTypes();
		for(int i = viewingType.typeID + 1; i < customTypes.size(); i++){
			customTypes.get(i).invalidate();
		}
		for(LComponent lcomp : cp.lcomps){
			if(lcomp instanceof OpCustom2) ((OpCustom2) lcomp).invalidate();
		}
		for(CustomType type : customTypes) type.rebuildingComplete();
	}
	
	/**
	 * Tells whether this CustomViewer is currently active
	 * @return The active state
	 */
	public boolean isActive() {
		return active;
	}
}
