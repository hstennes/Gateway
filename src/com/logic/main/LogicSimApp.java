package com.logic.main;

import com.logic.engine.LogicFunctions;
import com.logic.ui.FontLoader;
import com.logic.ui.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * A digital logic simulator meant as an alternative to applications like Logicly and Boolr.
 * @author GoopyLotus5844
 */
public class LogicSimApp {

	/**
	 * The release version
	 */
	public static final String VERSION = "v1.2";

	/**
	 * The default DPI value, used to calculate resolution scaling
	 */
	private static final int DEFAULT_DPI = 96;

	/**
	 * The display scaling
	 */
	public static float DISP_SCALE;

	/**
	 * The inverse of DISP_SCALE for faster calculations
	 */
	public static float INV_DISP_SCALE;

	/**
	 * OS type, used to determine if the look and feel should be switched from default java to windows
	 */
	public static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * The IconLoader for the program, which is responsible for loading and holding all of the images that the program uses
	 */
	public static IconLoader iconLoader;

	/**
	 * Loads and stores fonts (there is currently only 1 for the seven segment display)
	 */
	public static FontLoader fontLoader;
	
	/**
	 * The list of all currently open windows, used for closing the application when there are none left
	 */
	public static ArrayList<Window> windows;
	
	/**
	 * Starts the program 
	 */
	public LogicSimApp() {
		fontLoader = new FontLoader();
		iconLoader = new IconLoader();
		iconLoader.makeImageIcons();
		windows = new ArrayList<Window>();
		LogicFunctions.setFunctions();
		newWindow(null);
	}
	
	/**
	 * Opens a new window that contains the given file. If path is equal to null, then a blank window is opened
	 * @param path The file path, null if the window is to be a new file
	 */
	public static void newWindow(String path) {
		Window window;
		if(path != null) window = new Window(path);
		else window = new Window();
		
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				windows.remove(window);
				if(windows.size() == 0) System.exit(0);
			}
		});
		windows.add(window);
	}
	
	/**
	 * The main method of the program, which creates a new LogicSimApp on the event dispatch thread through the use of SwingUtilities.
	 * invokeLater(...)
	 * @param args The arguments to the main method, which have no effect whatsoever on anything
	 */
	public static void main(String[] args) {
		DISP_SCALE = (float) Toolkit.getDefaultToolkit().getScreenResolution() / DEFAULT_DPI;
		INV_DISP_SCALE = 1 / DISP_SCALE;
		if(OS.contains("win")) {
			try {
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			} catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
		}
		SwingUtilities.invokeLater(LogicSimApp::new);
		/**
		 * To do list:
		 * Add built in latches, flip flops, and random generator
		 * Add copy / paste between windows
		 * ADD MULTI BIT WIRES
		 * Text labels (implemented through CompProperties)
		 * Keyboard shortcuts (such as press A for and, O for or)
		 * Add circuit timing diagrams for ICs
		 * Add ability to print circuits to pdf
		 * Add ability to move components with arrow keys
		 * Add ability to reshape wires
		 */
		
		/**
		 * Issues
		 * The location field in the properties can malfunction
		 * The insert and select options are redundant; there is no reason to ever use them
		 */
	}
}
