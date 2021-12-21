package com.logic.ui;

import com.logic.components.*;
import com.logic.input.Camera;
import com.logic.input.CircuitEditor;
import com.logic.main.Window;
import com.logic.util.ActionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides both a graphical representation of the circuit and a framework for adding components and wires
 * @author Hank Stennes
 *
 */
public class CircuitPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Determines if a grid is drawn in the background of the CircuitPanel
	 */
	private boolean showGrid = true;

	/**
	 * Determines rendering quality. High quality uses SVGs and AA, low quality uses PNGs with no AA.
	 */
	private boolean highQuality = true;
	
	/**
	 * The list of all LComponents in the circuit
	 */
	public ArrayList<LComponent> lcomps = new ArrayList<LComponent>(); 
	
	/**
	 * The list of all wires in the circuit
	 */
	public ArrayList<Wire> wires = new ArrayList<Wire>();

	/**
	 * The camera, which is used for panning and zooming to see all of the circuit
	 */
	private Camera cam;
	
	/**
	 * The CompProperties panel
	 */
	private CompProperties properties;
	
	/**
	 * The CircuitEditor
	 */
	private CircuitEditor editor;
	
	/**
	 * The message that is being displayed by the CircuitPanel, if there is one (null otherwise)
	 */
	private UserMessage message;
	
	/**
	 * The Window that holds this CircuitPanel
	 */
	private Window window;

	private Renderer renderer;
	
	/**
	 * Constructs a new CircuitPanel
	 * @param compProperties The CompProperties panel
	 * @param toolbar The LToolBar
	 * @param insertPanel The InsertPanel
	 */
	public CircuitPanel(Window window, CompProperties compProperties, LToolBar toolbar, InsertPanel insertPanel) {
		this.window = window;
		cam = new Camera(this, toolbar);
		editor = new CircuitEditor(this, compProperties, toolbar, insertPanel);
		properties = compProperties;
		setFocusable(true);
		requestFocusInWindow();
		
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);  
		ActionMap actionMap = getActionMap();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
		actionMap.put("delete", ActionUtils.makeAbstractAction(editor, "deleteElements"));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
		actionMap.put("enter", ActionUtils.makeAbstractAction(editor, "enterActions"));
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
		actionMap.put("escape", ActionUtils.makeAbstractAction(editor, "escapeActions"));
		
		addMouseListener(editor);
		addMouseMotionListener(editor);
		addMouseWheelListener(cam);
		addMouseListener(cam);
		addMouseMotionListener(cam);

		renderer = new Renderer(this);
		//Debug.loadTestCircuit(this, true);
	}

	/**
	 * Paints this CircuitPanel by rendering all LComponents and Wires, drawing a grid if the grid is enabled, and displaying the current
	 * message if there is one
	 * @param g The Graphics object to use
	 */
	@Override
	public void paintComponent(Graphics g) {
		renderer.render(((Graphics2D) g), lcomps, wires, cam.getX(), cam.getY(), cam.getZoom());
	}
	
	/**
	 * Adds the given LComponent to the circuit
	 * @param lcomp The LComponent to add
	 */
	public void addLComp(LComponent lcomp) {
		lcomps.add(lcomp);
		if(lcomp instanceof SComponent) ((SComponent) lcomp).start(this);
		if(lcomp instanceof IComponent) addMouseListener((IComponent) lcomp);
	}

	/**
	 * Adds all of the given LComponents and all wires referenced by these LComponents to this CircuitPanel
	 * @param newComps The LComponents to add
	 */
	public void addLComps(List<LComponent> newComps) {
		for(int i = 0; i < newComps.size(); i++) {
			LComponent lcomp = newComps.get(i);
			for(int x = 0; x < lcomp.getIO().getNumInputs(); x++) {
				Connection connect = lcomp.getIO().connectionAt(x, Connection.INPUT);
				if(connect.numWires() > 0) addWire(connect.getWire(0));
			}
			addLComp(lcomp);
		}
	}

	/**
	 * Removes the given LComponent from the circuit. This method should only be called by a component when it is being deleted
	 * @param lcomp The LComponent to remove
	 */
	public void removeLComp(LComponent lcomp) {
		lcomps.remove(lcomp);
	}

	/**
	 * Adds the given wire to the circuit
	 * @param wire The wire to add
	 */
	public void addWire(Wire wire) {
		wires.add(wire);
	}

	/**
	 * Removes the given wire from the circuit. This method should only be called by a wire when it is being deleted
	 * @param wire The wire to remove
	 */
	public void removeWire(Wire wire) {
		wires.remove(wire);
	}

	/**
	 * Displays the given message in the CircuitPanel
	 * @param message The UserMessage to display
	 */
	public void dispMessage(UserMessage message) {
		this.message = message;
		message.start();
		repaint();
	}

	/**
	 * Clears the current message from the screen if there is one
	 */
	public void clearMessage() {
		message = null;
		repaint();
	}

	/**
	 * Removes all wires that are not connected to any connections. This method assumes that delete() has already been called on such wires
	 * in order for them to be in this state, so it simply removes them from the wires ArrayList. 
	 */
	public void cleanWires() {
		for(int i = wires.size() - 1; i >= 0; i--) {
			Wire w = wires.get(i);
			Connection s = w.getSourceConnection();
			if(s == null) {
				Connection d = w.getDestConnection();
				if(d == null) wires.remove(w);
			}
		}
	}
	
	/**
	 * Calculates the coordinates of the given mouse point (in the context of the JPanel) in the CircuitPanel coordinate system
	 * @param p The point to perform calculations on
	 * @return The transformed point
	 */
	public Point withTransform(Point p) {
		return new Point((int) (p.getX() / cam.getZoom() - cam.getX()), (int) (p.getY() / cam.getZoom() - cam.getY()));
	}

	/**
	 * Returns the window that holds this CircuitPanel
	 * @return The Window
	 */
	public Window getWindow() {
		return window;
	}
	
	/**
	 * Returns the Camera instance
	 * @return The Camera instance
	 */
	public Camera getCamera() {
		return cam;
	}
	
	/**
	 * Returns the CircuitEditor instance
	 * @return The CircuitEditor instance
	 */
	public CircuitEditor getEditor() {
		return editor;
	}
	
	/**
	 * Returns the CompProperties
	 * @return The CompProperties
	 */
	public CompProperties getProperties() {
		return properties;
	}

	public UserMessage getUserMessage(){
		return message;
	}

	/**
	 * Returns the bounds of the section of the CircuitPanel that is currently on the screen
	 * @return The view Rectangle
	 */
	public Rectangle getViewRect() {
		Rectangle view = new Rectangle(withTransform(new Point(0, 0)));
		view.add(withTransform(new Point(getWidth(), getHeight())));
		return view;
	}

	/**
	 * Returns the show grid setting
	 * @return The show grid setting
	 */
	public boolean isShowGrid() {
		return showGrid;
	}

	/**
	 * Changes the show grid setting and repaints this CircuitPanel
	 * @param showGrid The new show grid setting
	 */
	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
		repaint();
	}

	/**
	 * Returns the quality setting
	 * @return The quality setting
	 */
	public boolean isHighQuality(){
		return highQuality;
	}

	/**
	 * Changes the quality settings and repaints this CircuitPanel
	 * @param highQuality The new quality setting
	 */
	public void setHighQuality(boolean highQuality){
		this.highQuality = highQuality;
		repaint();
	}

	/**
	 * Sets the ZoomSlider instance (used because the ZoomSlider is not yet constructed when the CircuitPanel is)
	 * @param slider The ZoomSlider
	 */
	public void setZoomSlider(ZoomSlider slider) {
		cam.setZoomSlider(slider);
	}
	
}
