import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.io.IOException;

import com.google.common.collect.EvictingQueue;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;

public class DifferentialMapperBackup extends Listener {
	
	private float interactionZ = 0;
	private float scaleRangeZ = 150;

	private final Robot robot;
	private final EvictingQueue<Vector> pointerHistory;
	
	private Vector lastPointer = null;

	public DifferentialMapperBackup(Robot r, int pointerHistorySize) {
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
		// Robot r = this.robot;
		
		if (f.pointables().count() == 0) {
			this.lastPointer = null;
			return;
		}
		
		Pointable p = f.pointables().frontmost();
		Vector pointer = p.tipPosition();
		
		if (pointer.getZ() > interactionZ) {
			this.lastPointer = null;
			return;
		}
		
		
		if (this.lastPointer != null) {
			// do stuff
			Vector delta = pointer.minus(lastPointer);
			float baseMult = 4.5f;
			float mult = Util.mapRange(pointer.getZ(), interactionZ, interactionZ-scaleRangeZ, baseMult, 0, true);
			int dx = Math.round(delta.getX()*mult);
			int dy = -Math.round(delta.getY()*mult);
			this.mouseTranslate(dx,dy);
		}
		
		this.lastPointer = new Vector(pointer);
		
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
	
	private void mouseTranslate(int x, int y) {
		Point current = Util.getMousePosition();
		this.mouseMove(current.x + x, current.y + y);
	}
	
	private void mouseMove(int x, int y) {
		Dimension res = Util.getScreenResolution();
		this.robot.mouseMove(Util.clamp(x,0,res.width-1), Util.clamp(y,0,res.height-1));
	}

}

class DMTestBackup {

	public static void main(String[] args) {

		Robot rob = null;
		try {
			rob = new Robot();
		} catch (AWTException e) {
			System.err.println("Could not instantiate AWT Robot");
			return;
		}

		// Create a sample listener and controller
		Listener listener = new DifferentialMapperBackup(rob, 60 / 10);
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