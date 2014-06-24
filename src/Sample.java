/******************************************************************************\
 * Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.               *
 * Leap Motion proprietary and confidential. Not for distribution.              *
 * Use subject to the terms of the Leap Motion SDK Agreement available at       *
 * https://developer.leapmotion.com/sdk_agreement, or another agreement         *
 * between Leap Motion and you, your company or other organization.             *
\******************************************************************************/

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.io.IOException;

import com.google.common.collect.EvictingQueue;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;

class SampleListener extends Listener {

	public float clickZ = -99999;
	public float precisionZ = 0;

	private final Robot robot;
	private final EvictingQueue<Vector> pointerHistory;
	private final Point2D.Double mousePos = new Point2D.Double();
	private final Point2D.Double targetPos = new Point2D.Double();

	public SampleListener(Robot r, int pointerHistorySize) {
		super();
		this.robot = r;
		this.pointerHistory = EvictingQueue.create(pointerHistorySize);
	}

	public void onInit(Controller controller) {
		System.out.println("Initialized");
	}

	public void onConnect(Controller controller) {
		System.out.println("Connected");
		// controller.enableGesture(Gesture.Type.TYPE_SWIPE);
		// controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
		// controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		// controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
	}

	public void onDisconnect(Controller controller) {
		// Note: not dispatched when running in a debugger.
		System.out.println("Disconnected");
	}

	public void onExit(Controller controller) {
		System.out.println("Exited");
	}

	public void onFrame(Controller controller) {
		// Get the most recent frame and report some basic information
		Frame f = controller.frame();
		Robot r = this.robot;
		if (f.pointables().count() == 0)
			return;
		Pointable p = f.pointables().frontmost();
		Vector position = p.tipPosition();

		if (!this.pointerHistory.isEmpty()) {
			Vector lastPoint = this.pointerHistory.peek();
			int buttons = InputEvent.BUTTON1_DOWN_MASK;
			if (position.getZ() < clickZ && lastPoint.getZ() > clickZ) {
				// moving INTO click zone
				r.mousePress(buttons);
			} else if (position.getZ() > clickZ && lastPoint.getZ() < clickZ) {
				// moving OUT of click zone
				r.mouseRelease(buttons);
			}
		}
		this.pointerHistory.add(new Vector(position));

		Vector avg = this.getMeanPointer();
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		double x = Util.mapRange(avg.getX(), -200, 200, 0, width, true);
		double y = Util.mapRange(avg.getY(), 100, 300, height, 0, true);
		targetPos.setLocation(x, y);

		if (position.getZ() > this.precisionZ) {
			mousePos.setLocation(targetPos);
		} else {
			mousePos.x += (targetPos.x - mousePos.x) * 0.05;
			mousePos.y += (targetPos.y - mousePos.y) * 0.05;
		}

		r.mouseMove((int) Math.round(mousePos.getX()),
				(int) Math.round(mousePos.getY()));
	}

	private Vector getMeanPointer() {
		if (this.pointerHistory.isEmpty()) {
			return null;
		}

		Vector total = new Vector();
		for (Vector v : this.pointerHistory) {
			total = total.plus(v);
		}

		return total.divide(this.pointerHistory.size());
	}

}

class Sample {

	public static void main(String[] args) {

		Robot rob = null;
		try {
			rob = new Robot();
		} catch (AWTException e) {
			System.err.println("Could not instantiate AWT Robot");
			return;
		}

		// Create a sample listener and controller
		SampleListener listener = new SampleListener(rob, 60 / 10);
		Controller controller = new Controller();

		// Have the sample listener receive events from the controller
		controller.addListener(listener);

		// Keep this process running until Enter is pressed
		System.out.println("Press Enter to quit...");
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Remove the sample listener when done
		controller.removeListener(listener);
	}
}
