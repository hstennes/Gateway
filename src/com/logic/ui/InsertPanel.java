package com.logic.ui;

import com.logic.main.LogicSimApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The panel of buttons to insert LComponents
 * @author Hank Stennes
 *
 */
public class InsertPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	/**
	 * The size of the component buttons
	 */
	public static final int BUTTON_SIZE = 70;

	/**
	 * The amount of padding around the body of each logic icon, used to determine how the SVGs should be rendered
	 */
	public static final int[] paddingValues = new int[] {20, 20, 20, 20, 20, 20, 20, 20, 16, 18, 15, 15, 15, 18, 17, 15, 15};

	/**
	 * The names of the components in the InsertPanel, which are used for showing tool tips and by the Inserter
	 */
	private String[] logicNames = new String[] {"Buffer", "Not", "And", "Nand", "Or", "Nor", "Xor", "Xnor", "Clock", "Light", "Switch", 
			"Zero", "One", "Button", "Display", "Splitter", "ROM", "RAM"};
	
	/**
	 * The collection of buttons
	 */
	private AbstractBCollection buttonCollection;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * The tool bar
	 */
	private LToolBar toolbar;

	/**
	 * Constructs a new InsertPanel
	 * @param toolbar The tool bar
	 */
	public InsertPanel(LToolBar toolbar) {
		this.toolbar = toolbar;
		buttonCollection = new AbstractBCollection();
		setLayout(new FlowLayout(FlowLayout.LEFT));
		createButtons();
	}

	/**
	 * Creates the buttons
	 */
	private void createButtons() {
		for(int i = 0; i < logicNames.length; i++) {
			JToggleButton button = new JToggleButton(LogicSimApp.iconLoader.logicIcons[i]);
			button.setToolTipText(logicNames[i]);
			button.setActionCommand(logicNames[i]);
			if(i == 0) button.setSelected(true);
			button.addActionListener(this);
			button.setPreferredSize(new Dimension(70, 70));
			buttonCollection.add(button, logicNames[i]);
			add(button);
		}
	}
	
	/**
	 * Prepares for insertion; 
	 * Sets the tool bar mode to insert, clears the selection, sets the cursor to the default cursor, and repaints the CircuitPanel
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		toolbar.changeToInsert(LToolBar.INTERNAL, false);
		cp.repaint();
	}
	
	/**
	 * Returns the name of the selected component button
	 * @return The selected component name
	 */
	public String getSelectedComponent() {
		return buttonCollection.getSelectedButtonName();
	}
	
	/**
	 * Restores the last recorded selected button from before this InsertPanel was cleared using the AbstractBCollection.restoreSelection()
	 * method
	 */
	public void restore() {
		buttonCollection.restoreSelection();
	}
	
	/**
	 * De-selects all buttons by calling AbstractBCollection.clearSelection()
	 */
	public void clear() {
		buttonCollection.clearSelection();
	}
	
	/**
	 * Sets the CircuitPanel
	 * @param cp The CircuitPanel
	 */
	public void setCircuitPanel(CircuitPanel cp) {
		this.cp = cp;
	}
}
