package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.main.LogicSimApp;
import com.logic.ui.CompProperties;
import com.logic.util.CompUtils;
import com.logic.util.Constants;
import com.logic.util.Deletable;
import com.logic.util.NameConverter;
import org.apache.batik.gvt.GraphicsNode;

import java.awt.*;
import java.io.Serializable;

/**
 * The superclass for all logic components (CircuitElements that have inputs, outputs, and perform logic)
 * @author Hank Stennes
 *
 */
public abstract class LComponent extends CircuitElement implements Deletable, Serializable {

	/**
	 * The position of the component in the CircuitPanel
	 */
	protected int x, y;

	/**
	 * The current rotation
	 */
	protected int rotation;
	
	/**
	 * The type of component (ex CompType.AND, CompType.SWITCH)
	 */
	protected CompType type;

	/**
	 * The indexes of the images that this component uses in the IconLoader
	 */
	protected int[] images;
	
	/**
	 * The IOManager, which holds all of the connections
	 */
	protected IOManager io;
	
	/**
	 * The component's name, which is displayed in the CompProperties panel
	 */
	protected String name;
	
	/**
	 * Comments about the component, which is display in the CompProperties panel
	 */
	private String comments;
	
	/**
	 * Constructs a new LComponent
	 * @param x The x position
	 * @param y The y position
	 * @param type The type of component
	 */
	public LComponent(int x, int y, CompType type) {
		this.x = x;
		this.y = y;
		rotation = Constants.RIGHT;
		this.type = type;
		io = new IOManager(this);
		name = CompProperties.defaultName;
		comments = CompProperties.defaultComments;
	}
	
	/**
	 * Updates the components outputs based on its inputs and internal state. Only call from LogicWorker.doInBackground()
	 * @param logicEngine The LogicEngine instance calling update
	 */
	public abstract void update(LogicEngine logicEngine);
	
	/**
	 * Makes a deep copy of the component
	 * @return A copy of the component
	 */
	public abstract LComponent makeCopy();

	/**
	 * Returns a bounding box for the component based on its (x, y) position, the drawer's active image, and the current rotation
	 * @return A bounding box for the component
	 */
	public Rectangle getBounds() {
		Rectangle boundsRight = getBoundsRight();
		if(rotation == Constants.UP ||rotation == Constants.DOWN)
			return new Rectangle(x, y, boundsRight.height, boundsRight.width);
		return new Rectangle(x, y, boundsRight.width, boundsRight.height);
	}
	
	/**
	 * Returns a bounding box for the component when it is facing in a rightward direction (the default direction)
	 * @return A bounding box for the component
	 */
	public Rectangle getBoundsRight() {
		int index = images[getActiveImageIndex(CompUtils.getSensitiveCompData(this))];
		return new Rectangle(x, y, LogicSimApp.iconLoader.imageWidth[index], LogicSimApp.iconLoader.imageHeight[index]);
	}

	/**
	 * Returns the index of the current image in the CompDrawer.images array. Subclasses should override if they use more than one image.
	 * Rather than reading from the components own internal state to determine the image, this method takes the parameter compData in the
	 * format returned by CompUtils.getSentitiveCompData. See CompUtils.getSensitiveCompData for more information.
	 * @return The active image index
	 */
	protected int getActiveImageIndex(int compData){
		return 0;
	}

	/**
	 * Returns the SVG that should be used for the component based on its current state
	 * @return The active image
	 */
	public GraphicsNode getActiveImage(int compData){
		if(images == null) return null;
		return LogicSimApp.iconLoader.logicSVGs[images[getActiveImageIndex(compData)]];
	}
	
	/**
	 * Returns the x position of the component
	 * @return the x position of the component
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Sets the x position of the component
	 * @param x the new x position
	 */
	public void setX(int x) {
		this.x = x;
	}
	
	/**
	 * Returns the y position of the component
	 * @return the y position of the component
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Sets the y position of the component
	 * @param y the new y position
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Returns the current rotation of the component
	 * @return The current rotation, expressed as one of the four rotation constants
	 */
	public int getRotation() {
		return rotation;
	}

	/**
	 * Sets the rotation of the component
	 * @param rotation The new rotation
	 */
	public void setRotation(int rotation) {
		//java is inane
		this.rotation = ((rotation % 4) + 4) % 4;
	}
	
	/**
	 * Returns the type of component (not necessarily the subclass, some subclasses use multiple types)
	 * @return The type of component
	 */
	public CompType getType() {
		return type;
	}

	/**
	 * Sets the images that the component can use
	 * @param images The image indexes in IconLoader
	 */
	protected void setImages(int[] images){
		this.images = images;
	}
	
	/**
	 * Returns the IOManager
	 * @return the IOManager for this component
	 */
	public IOManager getIO() {
		return io;
	}
	
	/**
	 * Returns this component's name
	 * @return The name of this component (it will be "untitled component" if not set)
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this component
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the comments for this component
	 * @return This component's comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * Sets the component's comments
	 * @param comments The new comment
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	@Override
	public void delete() {
		io.delete();
	}
	
	@Override
	public String toString() {
		return (this instanceof Custom ? "\"" + ((Custom) this).getLabel() + "\"" :
			NameConverter.nameFromType(type) + ", (" + x + ", " + y + ")");
	}
}
