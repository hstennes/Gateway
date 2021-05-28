package com.logic.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import com.logic.components.LComponent;
import com.logic.input.Selection;
import com.logic.main.LogicSimApp;
import com.logic.test.And;
import com.logic.test.AndOptimized;

/**
 * A class possessed by every LComponent that provides the code for drawing the component
 * @author Hank Stennes
 *
 */
public class CompDrawer implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The amount by which the LogicImages from the IconLoader are scaled up before they are drawn
	 */
	public static final float IMAGE_SCALE = 7;
	
	/**
	 * The indexes of the images that this component uses (in the IconLoader)
	 */
	private int[] images;
	
	/**
	 * The index of the image that is currently active, (in the images array) which means that it will be the image that is drawn when 
	 * draw(...) is called
	 */
	private int activeImageIndex;
	
	/**
	 * The LComponent that uses this CompDrawer
	 */
	private LComponent lcomp;
	
	/**
	 * Constructs a new CompDrawer
	 * @param lcomp The LComponent
	 */
	public CompDrawer(LComponent lcomp) {
		this.lcomp = lcomp;
	}
	
	/**
	 * Returns the active image
	 * @return The active image index
	 */
	public int getActiveImageIndex() {
		return activeImageIndex;  
	}
	
	/**
	 * Sets the active image
	 * @param activeImageIndex The new active image index
	 */
	public void setActiveImageIndex(int activeImageIndex) {
		this.activeImageIndex = activeImageIndex;
	}
	
	/**
	 * Returns the active image as specified by activeImageIndex
	 * @return The active image
	 */
	public LogicImage getActiveImage() {
		return LogicSimApp.iconLoader.logicImages[images[activeImageIndex]];
	}
	
	/**
	 * Draws the LComponent by displaying its image under the components current rotation and drawing a box around the component if it is
	 * selected
	 * @param g The Graphics object to use for painting
	 */
	public void draw(Graphics g) {
		BufferedImage currentImage = getActiveImage().getBufferedImage(lcomp.getRotator().getRotation());
		g.drawImage(currentImage, lcomp.getX(), lcomp.getY(),
				(int) IMAGE_SCALE * currentImage.getWidth(), (int) IMAGE_SCALE * currentImage.getHeight(), null);
		g.setColor(Selection.SELECT_COLOR);
		if(lcomp.isSelected()) ((Graphics2D) g).draw(lcomp.getBounds());
	}
	
	/**
	 * Sets the array of LogicImages that this component uses. This method is intended for use by subclasses of LComponent to define what
	 * images they are going to draw. The indexes of the images that have been placed in the given array will match the indexes given when 
	 * calling setActiveImage(...)
	 * @param images The array of LogicImages that the component will use
	 */
	public void setImages(int[] images) {
		this.images = images;
	}
	
}
