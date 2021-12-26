package com.logic.ui;

import com.logic.components.BitWidthEntity;
import com.logic.components.LComponent;
import com.logic.engine.LogicWorker;
import com.logic.input.CircuitState;
import com.logic.input.Selection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BitWidthSpinner extends LabeledSpinner implements ChangeListener {

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
    public BitWidthSpinner() {
        super("BitWidth: ");
        spinner.addChangeListener(this);
    }

    /**
     * Changes the bit width of the selected LComponent
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if(selection.size() != 1) return;
        LComponent lcomp = selection.get(0);
        if(!(lcomp instanceof BitWidthEntity)) return;

        BitWidthEntity gate = (BitWidthEntity) lcomp;
        int value = (int) spinner.getValue();
        if(value > BitWidthEntity.MAX_BITS) value = BitWidthEntity.MAX_BITS;
        else if(value < BitWidthEntity.MIN_BITS) value = BitWidthEntity.MIN_BITS;
        spinner.setValue(value);

        if(value != gate.getBitWidth()) {
            gate.changeBitWidth(value);
            cp.getEditor().getRevision().saveState(new CircuitState(cp));
            LogicWorker.startLogic(lcomp);
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
