package com.logic.input;

import com.logic.components.Connection;
import com.logic.components.Wire;
import com.logic.engine.LogicWorker;
import com.logic.ui.CircuitPanel;

import java.awt.*;

/**
 * This class manages the selecting and deleting of wires. There is currently only support for selection one wire at a time
 * @author Hank Stennes
 *
 */
public class WireEditor {

	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * The wire that is currently selected
	 */
	private Wire selectedWire;

	private int selectedPoint;
	
	/**
	 * Constructs a new WireEditor
	 * @param cp The CircuitPanel
	 */
	public WireEditor(CircuitPanel cp) {
		this.cp = cp;
		selectedPoint = -1;
	}
	
	/**
	 * Selects the given wire
	 * @param wire The wire to select
	 */
	public void selectWire(Wire wire) {
		if(selectedWire != null) selectedWire.setSelected(false);
		wire.setSelected(true);
		selectedWire = wire;
		selectedPoint = -1;
	}

	public boolean isPointSelected(Wire wire, int index) {
		return selectedWire == wire && selectedPoint == index;
	}

	public void selectWirePoint(Wire wire, int index) {
		if(selectedWire != null) selectedWire.setSelected(false);
		selectedWire = wire;
		selectedPoint = index;
	}

	public void moveWirePoint(int x, int y){
		selectedWire.moveShapePoint(selectedPoint, new Point(x, y));
	}
	
	/**
	 * Deletes the selected wire, if there is one, and updates the logic of the circuit accordingly
	 */
	public void deleteWireOrPoint() {
		if(selectedPoint == -1) {
			Connection dest = selectedWire.getDestConnection();
			if (dest != null) LogicWorker.startLogic(dest.getLcomp());
			if (selectedWire != null) {
				selectedWire.delete();
				cp.removeWire(selectedWire);
				selectedWire = null;
			}
		}
		else {
			selectedWire.removeShapePoint(selectedPoint);
			selectedPoint = -1;
			selectedWire = null;
		}
		cp.repaint();
	}
	
	/**
	 * Deselects the selected wire, if there is one
	 */
	public void clear() {
		if(selectedWire != null) {
			selectedWire.setSelected(false);
			selectedWire = null;
		}
		selectedPoint = -1;
	}
	
	/**
	 * Tells whether there is a selected wire
	 * @return A boolean telling whether there is a selected wire
	 */
	public boolean hasSelectedWireOrPoint() {
		return selectedWire != null;
	}
	
}
