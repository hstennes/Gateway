package com.logic.input;

import com.logic.ui.CircuitPanel;
import com.logic.ui.LToolBar;
import com.logic.ui.ZoomSlider;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Manages zooming and panning of the CircuitPanel
 * @author Hank Stennes
 *
 */
public class Camera extends MouseInputAdapter implements MouseWheelListener {

	/**
	 * The amount by which the zoom of the camera is changed when zoomIn or zoomOut is called 
	 */
	public final float increment = 0.2f;
	
	/**
	 * The wheel rotation required to make a change of 1 in the zoom value (higher values represent "slower" zoom)
	 */
	private final float zoomSpeed = 10;
	
	/**
	 * The minimum zoom value
	 */
	public final float minZoom = 0.1f;
	
	/**
	 * The maximum zoom value
	 */
	public final float maxZoom = 3;
	
	/**
	 * The zoom of the camera (the size of everything in the CircuitPanel is multiplied by this value
	 */
	private float zoom = 1;
	
	/**
	 * The x and y position of the camera
	 */
	private float x, y;
	
	/**
	 * The last recorded x and y position of the camera (used for panning)
	 */
	private int prevX, prevY;
	
	/**
	 * The previous tool bar mode ("Insert", "Select", or "Pan). This is used for reverting to a previous mode if the middle mouse button is
	 * released
	 */
	private String prevToolbarSelection = "Insert";
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * The LToolBar
	 */
	private LToolBar toolbar;
	
	/**
	 * The ZoomSlider
	 */
	private ZoomSlider slider;
	
	/**
	 * Constructs a new Camera
	 * @param cp The CircuitPanel 
	 * @param toolbar The LToolBar
	 */
	public Camera(CircuitPanel cp, LToolBar toolbar) {
		this.cp = cp;
		this.toolbar = toolbar; 
	}
	
	/**
	 * Zooms the camera by updating the zoom variable, repainting the CircuitPanel, and repositioning the zoom slider
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double rotation = e.getPreciseWheelRotation();
		double oldZoom = zoom;
		zoom -= (rotation / zoomSpeed);
		if(zoom < minZoom) zoom = minZoom;
		else if(zoom > maxZoom) zoom = maxZoom;
		double offsetCoefficient = (oldZoom - zoom) / (oldZoom * zoom);
		x += e.getX() * offsetCoefficient;
		y += e.getY() * offsetCoefficient;
		slider.updatePosition();
		cp.repaint();
	}
	
	/**
	 * Changes the program to pan mode if the middle mouse button was pressed
	 */
	@Override 
	public void mousePressed(MouseEvent e) {
		prevX = e.getX();
		prevY = e.getY();
		if(SwingUtilities.isMiddleMouseButton(e)) {
			prevToolbarSelection = toolbar.getToolMode();
			toolbar.changeToPan(LToolBar.INTERNAL);
		}
	}
	
	/**
	 * Changes the program out of pan mode if the middle mouse button was released
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if(SwingUtilities.isMiddleMouseButton(e)) {
			if(prevToolbarSelection.equals("Insert")) toolbar.changeToInsert(LToolBar.INTERNAL, true);
			else if(prevToolbarSelection.equals("Select")) toolbar.changeToSelect(LToolBar.INTERNAL);
		}
	}
	
	/**
	 * Pans the camera if the program is in pan mode
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if(toolbar.getToolMode().equals("Pan")) {
			int dx = e.getX() - prevX;
			int dy = e.getY() - prevY;
			x += dx * (1 / zoom);
			y += dy * (1 / zoom);
			prevX = e.getX();
			prevY = e.getY();
		}
		cp.repaint();
	}
	
	/**
	 * Zooms in by a small amount
	 */
	public void zoomIn() {
		setZoom(zoom + increment);
		slider.updatePosition();
	}
	
	/**
	 * Zooms out by a small amount
	 */
	public void zoomOut() {
		setZoom(zoom - increment);
		slider.updatePosition();
	}

	/**
	 * Returns the zoom of the camera
	 * @return The zoom of the camera
	 */
	public float getZoom() {
		return zoom;
	}
	
	/**
	 * Sets the zoom of the camera and repaints the CircuitPanel.  For some reason this method also changes the x and y position
	 * (something about keeping the camera centered?) so if you want a specific position set that after setting the zoom.
	 * @param zoom The new zoom value
	 */
	public void setZoom(float zoom) {
		if(zoom < minZoom) zoom = minZoom;
		else if(zoom > maxZoom) zoom = maxZoom;
		double offsetCoefficient = (this.zoom - zoom) / (this.zoom * zoom);
		x += (double) cp.getWidth() / 2 * offsetCoefficient;
		y += (double) cp.getHeight() / 2 * offsetCoefficient;
		this.zoom = zoom;
		cp.repaint();
	}

	/**
	 * Sets the ZoomSlider instance used by the Camera
	 * @param slider the ZoomSlider
	 */
	public void setZoomSlider(ZoomSlider slider) {
		this.slider = slider; 
	}
	
	/**
	 * Returns the x position of the camera
	 * @return The x position of the camera
	 */
	public float getX() {
		return x;
	}
	
	/**
	 * Sets the x position of the camera
	 * @param x The new x position
	 */
	public void setX(float x) {
		this.x = x;
	}
	
	/**
	 * Returns the y position of the camera
	 * @return The y position of the camera
	 */
	public float getY() {
		return y;
	}
	
	/**
	 * Sets the y position of the camera
	 * @param y The new y position
	 */
	public void setY(float y) {
		this.y = y;
	}
	
}
