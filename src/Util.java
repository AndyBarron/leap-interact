import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;

public class Util {
	public static double mapRange(double val, double min1, double max1,
			double min2, double max2, boolean clamp) {

		double percent = (val - min1) / (max1 - min1);
		if (clamp) {
			percent = clamp(percent, 0, 1);
		}
		return (percent * (max2 - min2)) + min2;
	}
	
	public static float mapRange(float val, float min1, float max1,
			float min2, float max2, boolean clamp) {

		float percent = (val - min1) / (max1 - min1);
		if (clamp) {
			percent = clamp(percent, 0, 1);
		}
		return (percent * (max2 - min2)) + min2;
	}

	public static double clamp(double val, double min, double max) {
		if (val < min)
			return min;
		else if (val > max)
			return max;
		else
			return val;
	}
	
	public static float clamp(float val, float min, float max) {
		if (val < min)
			return min;
		else if (val > max)
			return max;
		else
			return val;
	}

	public static int clamp(int val, int min, int max) {
		if (val < min)
			return min;
		else if (val > max)
			return max;
		else
			return val;
	}

	public static Point getMousePosition() {
		return MouseInfo.getPointerInfo().getLocation();
	}
	
	public static Dimension getScreenResolution() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		return new Dimension(width,height);
	}

}
