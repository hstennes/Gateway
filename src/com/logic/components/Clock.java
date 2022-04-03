package com.logic.components;

import com.logic.engine.LogicEngine;
import com.logic.ui.CircuitPanel;
import com.logic.util.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class represents the clock component, which that toggles its output at a specified interval through the use of swing timers
 * @author Hank Stennes
 *
 */
public class Clock extends SComponent {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The interval at which to toggle the output
	 */
	public static final int DEFAULT_DELAY = 500;
	
	/**
	 * The current delay value
	 */
	private int delay;
	
	/**
	 * The state of the clock. When on is true, the component outputs a high signal, and when on is false it outputs a low signal
	 */
	private boolean on = false;
	
	/**
	 * The swing timer instance used to operate the clock
	 */
	private Timer timer;
	
	/**
	 * Constructs a new Clock
	 * @param x The x position of the clock
	 * @param y The y position of the clock
	 */
	public Clock(int x, int y) {
		super(x, y, CompType.CLOCK);
		setImages(new int[] {11, 12});
		io.addConnection(100, 40, Connection.OUTPUT, Constants.RIGHT);
		delay = DEFAULT_DELAY;
	}
	
	@Override
	public void update(LogicEngine engine) {
		io.setOutputOld(0, on, engine);
	}
	
	@Override
	public LComponent makeCopy() {
		Clock result = new Clock(x, y);
		result.setRotation(rotation);
		result.setName(getName());
		result.setDelay(delay);
		return result;
	}
	
	@Override
	public void delete() {
		stop();
		super.delete();
	}
	
	/**
	 * Creates and starts the timer in this clock. If the clock already has a timer, the reference to this timer is overwritten. The 
	 * CircuitPanel parameter is necessary for repainting when the state of the clock changes.
	 * @param cp The CircuitPanel that should be repainted when this Clock changes state 
	 */
	@Override
	public void start(CircuitPanel cp) {
		if(timer != null) timer.stop();
		/*timer = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				on = !on;
				innerChange();
				cp.repaint();
			}
		});
		timer.start();*/
	}

	@Override
	public int getActiveImageIndex(){
		if(on) return 1;
		return 0;
	}
	
	/**
	 * Stops the timer in this clock
	 */
	public void stop() {
		timer.stop();
	}
	
	/**
	 * Returns the current delay value on this clock's timer (measured in milliseconds)
	 * @return The current delay value
	 */
	public int getDelay() {
		return delay;
	}
	
	/**
	 * Sets the delay value on this clock's timer (measured in milliseconds)
	 * @param delay The new delay value
	 */
	public void setDelay(int delay) {
		this.delay = delay;
		if(timer != null) timer.setDelay(delay);
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on){
		this.on = on;
	}
}
