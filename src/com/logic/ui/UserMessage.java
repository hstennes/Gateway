package com.logic.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A message that can be displayed in the CircuitPanel
 * @author Hank Stennes
 *
 */
public class UserMessage implements ActionListener {
	
	/**
	 * The font of the message
	 */
	private final Font labelFont = new Font("Arial", Font.PLAIN, 20);
	
	/**
	 * The background color of the message
	 */
	private final Color labelColor = new Color(255, 244, 179);
	
	/**
	 * The margin inside the label on each side
	 */
	private final int xMargin = 10;
	
	/**
	 * The margin inside the label on the top and bottom
	 */
	private final int yMargin = 3;
	
	/**
	 * The offset of the label from the top of the CircuitPanel
	 */
	private final int offset = 20;

	/**
	 * The text to display
	 */
	private String text;
	
	/**
	 * The timer that is used to clear the message, if this message is timed
	 */
	private Timer timer;

	/**
	 * The LabelDrawer instance
	 */
	private LabelDrawer drawer;
	
	/**
	 * The CircuitPanel
	 */
	private CircuitPanel cp;
	
	/**
	 * Constructs a new UserMessage that stays on the screen until CircuitPanel.clearMessage() is called by another class
	 * @param cp The CircuitPanel
	 * @param text The text
	 */
	public UserMessage(CircuitPanel cp, String text) {
		this.cp = cp;
		this.text = text;
		drawer = new LabelDrawer(labelFont, labelColor, xMargin, yMargin);
	}
	
	/**
	 * Constructs a new UserMessage that clears itself from the screen after the given time has passed
	 * @param cp The CircuitPanel
	 * @param text The text
	 * @param duration The duration of the message in milliseconds
	 */
	public UserMessage(CircuitPanel cp, String text, int duration) {
		this.cp = cp;
		this.text = text;
		timer = new Timer(duration, this);
		drawer = new LabelDrawer(labelFont, labelColor, xMargin, yMargin);
	}
	
	/**
	 * Starts the timer if this is a timed message
	 */
	public void start() {
		if(timer != null) timer.start();
	}
	
	/**
	 * Renders the message at the top center of the CircuitPanel
	 * @param g The Graphics object to use
	 */
	public void render(Graphics g) {
		drawer.render(((Graphics2D) g), cp.getWidth() / 2, offset, LabelDrawer.CENTER, LabelDrawer.START, text);
	}

	/**
	 * Called when the timer finishes to clear this message from the CircuitPanel
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		cp.clearMessage();
		timer.stop();
	}
	
}
