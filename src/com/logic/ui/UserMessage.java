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
	public static final Font MESSAGE_FONT = new Font("Arial", Font.PLAIN, 20);
	
	/**
	 * The background color of the message
	 */
	public static final Color MESSAGE_COLOR = new Color(255, 244, 179);
	
	/**
	 * The margin inside the label on each side
	 */
	public static final int X_MARGIN = 10;
	
	/**
	 * The margin inside the label on the top and bottom
	 */
	public static final int Y_MARGIN = 3;
	
	/**
	 * The offset of the label from the top of the CircuitPanel
	 */
	public static final int Y_OFFSET = 20;

	/**
	 * The text to display
	 */
	private String text;
	
	/**
	 * The timer that is used to clear the message, if this message is timed
	 */
	private Timer timer;
	
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
	}
	
	/**
	 * Starts the timer if this is a timed message
	 */
	public void start() {
		if(timer != null) timer.start();
	}

	/**
	 * Called when the timer finishes to clear this message from the CircuitPanel
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		cp.clearMessage();
		timer.stop();
	}

	/**
	 * Returns the message text
	 * @return The message text
	 */
	public String getText(){
		return text;
	}
}
