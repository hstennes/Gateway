package com.logic.ui;

import com.logic.components.LComponent;
import com.logic.main.LogicSimApp;
import org.apache.batik.gvt.GraphicsNode;

import java.io.Serializable;

/**
 * A class possessed by every LComponent that provides the code for drawing the component
 * @author Hank Stennes
 *
 */
public class CompDrawer implements Serializable {

	/**
	 * The indexes of the images that this component uses (in the IconLoader)
	 */
	private int[] images;
	
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

	public GraphicsNode getActiveImage(){
		return LogicSimApp.iconLoader.logicSVGs[images[lcomp.getActiveImageIndex()]];
	}

	public int getImageIndex(){
		return images[lcomp.getActiveImageIndex()];
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