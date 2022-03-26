package com.logic.ui;

import com.logic.custom.OpCustom2;
import com.logic.files.FileManager;
import com.logic.main.LogicSimApp;
import com.logic.test.ChipTester;
import com.logic.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A tool bar for the program
 * @author Hank Stennes
 *
 */
public class LToolBar extends JToolBar implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The number of items before each tool bar divider (buttons are organized by function
	 */
	private final int dividerIndex1 = 3, dividerIndex2 = 6, dividerIndex3 = 11;
	
	/**
	 * The names of each button
	 */
	private String[] tooltips = new String[] {"New", "Open", "Save", "Select", "Pan", "Insert", "Undo", "Redo", "Cut", "Copy", "Paste", 
			"Delete", "Rotate Counter-Clockwise", "Rotate Clockwise", "Create Custom Component"};
	
	/**
	 * The button collection, used for the select, pan, and insert toggle buttons
	 */
	private AbstractBCollection buttonCollection;
	
	/**
	 * A constant that specifies that the state of a toggle button was changed by the user
	 */
	public static final int DIRECT = 0;
	
	/**
	 * A constant that specifies that the state of a toggle button was changed internally by the program
	 */
	public static final int INTERNAL = 1;
	
	/**
	 * A constant that specifies that the state of a toggle button was changed using the tools menu in the menu bar
	 */
	public static final int MENU = 2;
	
	/**
	 * The InsertPanel
	 */
	private InsertPanel insertPanel;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * The LMenuBar
	 */
	private LMenuBar menuBar;
	
	/**
	 * The file manager
	 */
	private FileManager fileManager;
		
	/**
	 * Constructs a new LToolBar
	 */
	public LToolBar() { 
		buttonCollection = new AbstractBCollection();
		createButtons();
	}
	
	/**
	 * Creates all of the buttons used by the tool bar according to the instance variables
	 */
	private void createButtons() {
		IconLoader icons = LogicSimApp.iconLoader;
		Insets padding = new Insets(0, 5, 0, 5);
		for(int i = 0; i < dividerIndex1; i++) {
			JButton button = new JButton(icons.toolBarIcons[i]);
			button.setToolTipText(tooltips[i]);
			button.setActionCommand(tooltips[i]);
			button.addActionListener(this);
			button.setMargin(padding);
			add(button);
		}
		
		addSeparator();
		
		for(int i = dividerIndex1; i < dividerIndex2; i++) {
			JToggleButton button = new JToggleButton(icons.toolBarIcons[i]);
			button.setToolTipText(tooltips[i]);
			button.setActionCommand(tooltips[i]);
			if(i == dividerIndex2 - 1) button.setSelected(true);
			button.addActionListener(this);
			buttonCollection.add(button, tooltips[i]);
			button.setMargin(padding);
			add(button);
		}
		
		addSeparator();
		
		for(int i = dividerIndex2; i < dividerIndex3; i++) {
			JButton button = new JButton(icons.toolBarIcons[i]);
			button.setToolTipText(tooltips[i]);
			button.setActionCommand(tooltips[i]);
			button.addActionListener(this);
			button.setMargin(padding);
			add(button);
		}
		
		addSeparator();
		
		for(int i = dividerIndex3; i < tooltips.length; i++) {
			JButton button = new JButton(icons.toolBarIcons[i]);
			button.setToolTipText(tooltips[i]);
			button.setActionCommand(tooltips[i]);
			button.addActionListener(this);
			button.setMargin(padding);
			add(button);
		}
	}
	
	/**
	 * Performs the necessary action to respond to a button press (not complete)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals("Pan")) changeToPan(DIRECT);
		else if(command.equals("Select")) changeToSelect(DIRECT);
		else if(command.equals("Insert")) changeToInsert(DIRECT, true);
		else if(command.equals("Delete")) cp.getEditor().deleteElements();
		else if(command.equals("Cut")) cp.getEditor().getSelection().cut();
		else if(command.equals("Copy")) cp.getEditor().getSelection().copy();
		else if(command.equals("Paste")) cp.getEditor().paste();
		else if(command.equals("Rotate Counter-Clockwise")) cp.getEditor().getSelection().rotate(Constants.COUNTER_CLOCKWISE);
		else if(command.equals("Rotate Clockwise")) cp.getEditor().getSelection().rotate(Constants.CLOCKWISE);
		else if(command.equals("Undo")) cp.getEditor().getRevision().undo();
		else if(command.equals("Redo")) cp.getEditor().getRevision().redo();
		else if(command.equals("New")) LogicSimApp.newWindow(null);
		else if(command.equals("Open")) fileManager.open();
		else if(command.equals("Save")) fileManager.save();
		else if(command.equals("Create Custom Component")) {
			//TODO this button is being used for testing
			//cp.getEditor().getCustomCreator().createCustom();
			new ChipTester(cp.getEditor().getSelection().get(0)).execute();
			/*ROM rom = new ROM(0, 0);
			rom.setProgram(new int[] {1, 1, 2, 3, 5, 8, 13});
			cp.addLComp(rom);*/
		}
	}
	
	/**
	 * Changes to pan mode
	 * @param type The type of change (INTERNAL, DIRECT, or MENU)
	 */
	public void changeToPan(int type) {
		if(type == INTERNAL || type == MENU) buttonCollection.select("Pan");
		if(type != MENU) menuBar.choosePan();
		cp.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	
	/**
	 * Changes to select mode 
	 * @param type The type of change (INTERNAL, DIRECT, or MENU)
	 */
	public void changeToSelect(int type) {
		if(type == INTERNAL || type == MENU) buttonCollection.select("Select");
		if(type != MENU) menuBar.chooseSelect();
		cp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		insertPanel.clear();
	}

	/**
	 * Changes to insert mode. If restoreSelection is true, then restoreSelection will be called on the InsertPanel's button collection. This
	 * value should only be set to false if this method is being called by InsertPanel
	 * @param type The type of change (INTERNAL, DIRECT, or MENU)
	 * @param 
	 */
	public void changeToInsert(int type, boolean restoreSelection) {
		if(type == INTERNAL || type == MENU) buttonCollection.select("Insert");
		if(type != MENU) menuBar.chooseInsert();
		cp.getEditor().getSelection().clear();
		cp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		if(restoreSelection) insertPanel.restore();
	}
	
	/**
	 * Returns the name of the tool button that is currently selected ("Insert", "Pan", or "Select")
	 * @return The selected button name
	 */
	public String getToolMode() {
		return buttonCollection.getSelectedButtonName();
	}
	
	/**
	 * Sets the InsertPanel
	 * @param insertPanel The InsertPanel
	 */
	public void setInsertPanel(InsertPanel insertPanel) {
		this.insertPanel = insertPanel;
	}
	
	/**
	 * Sets the CircuitPanel
	 * @param cp The CircuitPanel
	 */
	public void setCircuitPanel(CircuitPanel cp) {
		this.cp = cp;
	}
	
	/**
	 * Sets the LMenuBar
	 * @param menuBar The LMenuBar
	 */
	public void setLMenuBar(LMenuBar menuBar) {
		this.menuBar = menuBar;
	}
	
	/**
	 * Sets the FileManager 
	 * @param fileManager The FileManager
	 */
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
}
