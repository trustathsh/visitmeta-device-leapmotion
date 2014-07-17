/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of visitmeta-device-leapmotion, version 0.0.1,
 * a project that adds support for the LeapMotion device to VisITMeta GUI,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2014 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.hshannover.f4.trust.visitmeta.input.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;

import de.hshannover.f4.trust.visitmeta.input.gui.MotionController;
import de.hshannover.f4.trust.visitmeta.input.gui.MotionControllerHandler;

/**
 * A {@link Device} implementation that connects a LeapMotion device
 * to VisITMeta.
 * 
 * @author Bastian Hellmann
 * @author Oleg Wetzler
 *
 */
public class DeviceLeapMotion extends Listener implements Device {

	private static final String DEVICE_LEAPMOTION_VERSION = "${project.version}";
	private static final String DEVICE_LEAPMOTION_NAME = "${artifactId}";

	private static final String CONFIGURATION_FILENAME = "input_leapmotion.yaml";

	private static Logger logger = Logger.getLogger(DeviceLeapMotion.class);

	private Frame lastframe;
	private double zmid;
	private double zdead;
	private double xydead;
	private double zrecorded;
	private double xyrecorded;
	private double xyper;
	private double zper;
	private boolean target = false;
	private boolean joystick;

	@SuppressWarnings("unused")
	private boolean enabled;

	private Hand hand;
	private FingerList fingers;
	private double z;
	private double x;
	private double y;

	private Controller leapMotionController;
	private MotionControllerHandler motionControllerHandler;
	private Thread thread;
	private IdleRunnable runnable;

	@SuppressWarnings("unchecked")
	public DeviceLeapMotion() {
		String filenamePrefix = "devices" + File.separator + DEVICE_LEAPMOTION_NAME + "-" + DEVICE_LEAPMOTION_VERSION + File.separator + "config";
		String filename = filenamePrefix + File.separator + CONFIGURATION_FILENAME;

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(new File(filename)));
			Yaml yaml = new Yaml();
			Map<String, Object> map = (Map<String, Object>) yaml.load(br);

			this.zmid = (double) map.get("zmid");
			this.zdead = (double) map.get("zdead");
			this.xydead = (double) map.get("xydead");
			this.zrecorded = (double) map.get("zrecorded");
			this.xyrecorded = (double) map.get("xyrecorded");
			this.joystick = (boolean) map.get("joystick");
			this.enabled = (boolean) map.get("enable");
		} catch (FileNotFoundException e) {
			logger.warn("Could not load configuration file, using default configuration.");

			this.zmid = 150;
			this.zdead = 20;
			this.xydead = 20;
			this.zrecorded = 130;
			this.xyrecorded = 130;
			this.joystick = true;
			this.enabled = true;
		}

		this.xyper = xyrecorded - xydead;
		this.zper = zrecorded - zdead;
	}

	/* (non-Javadoc)
	 * @see com.leapmotion.leap.Listener#onInit(com.leapmotion.leap.Controller)
	 */
	@Override
	public void onInit(Controller controller) {
		logger.trace(getName() + " calls onInit().");
	}

	/* (non-Javadoc)
	 * @see com.leapmotion.leap.Listener#onConnect(com.leapmotion.leap.Controller)
	 */
	@Override
	public void onConnect(Controller controller) {
		logger.info(getName() + " connected.");
	}

	/* (non-Javadoc)
	 * @see com.leapmotion.leap.Listener#onFrame(com.leapmotion.leap.Controller)
	 */
	@Override
	public void onFrame(Controller controller) {
		super.onFrame(controller);

		Frame frame = controller.frame();

		MotionController api = motionControllerHandler.getCurrentMotionController();
		if (api != null) {
			if (!frame.hands().isEmpty()) {
				target = true;

				this.hand = frame.hands().get(0);
				if (this.hand != null) {
					this.fingers = hand.fingers();

					getCursor(api);

					if (joystick) {
						controlJoystick(frame, api);
					} else {
						controlMouse(frame, api);
					}
				}
			} else {
				if (target) {
					api.makeInvisible();
				}
				target = false;
			}

			this.lastframe = frame;
		}
	}

	/**
	 * Reads current palm position of hand and submits the position to the
	 * {@link MotionController}.
	 * 
	 * @param api {@link MotionController} instance
	 */
	private void getCursor(MotionController api) {
		this.z = hand.palmPosition().getY();
		z = z - zmid;
		this.x = hand.palmPosition().getX();
		this.y = hand.palmPosition().getZ();

		if (api != null) {
			api.setCursorPosition(x / xydead, y / xydead, z / zdead);
		}
	}

	/**
	 * Simulates a mouse drag operation.
	 * 
	 * @param frame current LeapMotion {@link Frame} including motion information
	 * @param api {@link MotionController} instance
	 */
	private void controlMouse(Frame frame, MotionController api) {
		if (this.lastframe != null) {
			if (fingers.count() <= 1) {

				Vector trans = frame.translation(this.lastframe);
				if (api != null) {
					api.moveCamera(trans.getX() / xyrecorded * 15, trans.getZ()
							/ xyrecorded * 15);
					api.zoom(trans.getY() / zrecorded * -8);
					api.repaint();
				}
			}
		}
	}

	/**
	 * Simulates joystick movement.
	 * 
	 * @param frame current LeapMotion {@link Frame} including motion information
	 * @param api {@link MotionController} instance
	 */
	private void controlJoystick(Frame frame, MotionController api) {
		x *= -1;
		y *= -1;

		if (fingers.count() >= 3) {

			if (z < zrecorded && z > -zrecorded && x < xyrecorded
					&& x > -xyrecorded && y < xyrecorded && y > -xyrecorded) {

				boolean zc = true;
				boolean xc = true;
				boolean yc = true;

				if (z > zdead) {
					z -= zdead;
				} else if (z < -zdead) {
					z += zdead;
				} else {
					z = 0;
					zc = false;
				}

				if (x > xydead) {
					x -= xydead;
				} else if (x < -xydead) {
					x += xydead;
				} else {
					x = 0;
					xc = false;
				}

				if (y > xydead) {
					y -= xydead;
				} else if (y < -xydead) {
					y += xydead;
				} else {
					y = 0;
					yc = false;
				}

				if (zc) {
					if (api != null) {
						api.zoom((z) / zper);
					}
				}
				if (xc || yc) {
					if (api != null) {
						api.moveCamera(x / xyper, y / xyper);
					}
				}

			}
		}

	}

	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.visitmeta.input.device.Device#init()
	 */
	@Override
	public boolean init() {
		logger.info(getName() + " v" + DEVICE_LEAPMOTION_VERSION + " initializes.");
		this.leapMotionController = new Controller();

		return leapMotionController.addListener(this);
	}

	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.visitmeta.input.device.Device#shutdown()
	 */
	@Override
	public boolean shutdown() {
		return leapMotionController.removeListener(this);
	}

	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.visitmeta.input.device.Device#start()
	 */
	@Override
	public void start() {
		this.runnable = new IdleRunnable();
		this.thread = new Thread(this.runnable);
		this.thread.start();
	}

	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.visitmeta.input.device.Device#stop()
	 */
	@Override
	public void stop() {
		this.runnable.running = false;
	}

	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.visitmeta.input.device.Device#setMotionControllerHandler(de.hshannover.f4.trust.visitmeta.input.gui.MotionControllerHandler)
	 */
	@Override
	public void setMotionControllerHandler(MotionControllerHandler handler) {
		this.motionControllerHandler = handler;
	}

	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.visitmeta.input.device.Device#getName()
	 */
	@Override
	public String getName() {
		return "LeapMotion";
	}

	/**
	 * Helper class that creates a Thread that does ... nothing;
	 * elsewise LeapMotion will stop after about 1 sec. FIXME find out why ...
	 * 
	 * @author Bastian Hellmann
	 *
	 */
	private class IdleRunnable implements Runnable {

		public boolean running = true;

		@Override
		public void run() {
			synchronized (this) {
				while (running) {	// FIXME won't work this way ...
					try {
						wait();
					} catch (InterruptedException e) {
						run();
					}
				}
			}
		}
	}
}
