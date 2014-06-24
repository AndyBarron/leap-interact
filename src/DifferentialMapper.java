import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;

import com.google.common.collect.EvictingQueue;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Controller.PolicyFlag;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;

public class DifferentialMapper extends Listener {
	
	private float interactionZ = 0;
	private float scaleRangeZ = 250;

	private final Robot robot;
	private final EvictingQueue<Vector> pointerHistory;
	
	static final int KEY_CODE_CLICK = 157; // TODO don't hard code this
	private boolean pressingClick = false;
	private boolean clicking = false;
	
	public DifferentialMapper(Robot r, int pointerHistorySize) {
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
		
		if (f.hands().count() != 1 ) {
			this.pointerHistory.clear();
			return;
		}
		
		Finger indexFinger = f.hands().get(0).fingers().fingerType(Finger.Type.TYPE_INDEX).get(0);
		Vector pointer = indexFinger.tipPosition();
		
		if (pointer.getZ() > interactionZ) {
			this.pointerHistory.clear();
			return;
		}
		
		boolean added = false;
		if (!this.pointerHistory.isEmpty()) {
			// do stuff
			// Vector delta = pointer.minus(lastPointer);
			Vector lastPointer = this.getMeanPointer();
			this.pointerHistory.add(new Vector(pointer));
			added = true;
			Vector curAvg = this.getMeanPointer();
			Vector delta = curAvg.minus(lastPointer);
			float baseMult = 3.5f;
			float mult = Util.mapRange(pointer.getZ(), interactionZ, interactionZ-scaleRangeZ, baseMult, 0, true);
			int dx = Math.round(delta.getX()*mult);
			int dy = -Math.round(delta.getY()*mult);
			this.mouseTranslate(dx,dy);
		}
		
		if(!added){
			this.pointerHistory.add(new Vector(pointer));
		}
		
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
	
	private void mouseClick() {
		
		if(this.clicking) return;
		
		System.out.println("click");
		this.clicking = true;
		this.robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	private void mouseRelease() {
		
		if(!this.clicking) return;
		
		System.out.println("release");
		this.clicking = false;
		this.robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}


}

class DMTest {

	public static void main(String[] args) {

		Robot rob = null;
		try {
			rob = new Robot();
		} catch (AWTException e) {
			System.err.println("Could not instantiate AWT Robot");
			return;
		}

		// Create a sample listener and controller
		DifferentialMapper listener = new DifferentialMapper(rob, 60 / 10);
		Controller controller = new Controller();
		
		// enable background updates
		controller.setPolicyFlags(PolicyFlag.POLICY_BACKGROUND_FRAMES);

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