package com.logic.ui;

import com.logic.components.BasicGate;
import com.logic.components.LComponent;
import com.logic.engine.LogicWorker;
import com.logic.input.CircuitState;
import com.logic.input.Selection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A spinner for changing the number of inputs of a component
 * @author Hank Stennes
 *
 */
public class InputSpinner extends LabeledSpinner implements ChangeListener {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The Selection
	 */
	private Selection selection;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * Constructs a new InputSpinner
	 */
	public InputSpinner() {
		super("Inputs: ");
		spinner.addChangeListener(this);
	}

	/**
	 * Changes the number of inputs of the selected LComponent
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if(selection.size() != 1) return;
		LComponent lcomp = selection.get(0);
		if(!(lcomp instanceof BasicGate)) return;

		BasicGate gate = (BasicGate) lcomp;
		int value = (int) spinner.getValue();
		if(value > BasicGate.MAX_INPUTS) value = BasicGate.MAX_INPUTS;
		else if(value < BasicGate.MIN_INPUTS) value = BasicGate.MIN_INPUTS;
		spinner.setValue(value);

		int currentInputs = gate.getIO().getNumInputs();
		if(value != currentInputs) {
			gate.setNumInputs(value);
			if(value < currentInputs) cp.cleanWires();
			cp.getEditor().getRevision().saveState(new CircuitState(cp));
			LogicWorker.startLogic(gate);
			cp.repaint();
		}
	}

	/**
	 * Sets the CircuitPanel
	 * @param cp The CircuitPanel
	 */
	public void setCircuitPanel(CircuitPanel cp) {
		this.cp = cp;
		selection = cp.getEditor().getSelection();
	}
}
