package bms.tool.util;

public class ColorUtils {
	public static String rgba2hex(int pixel) {
		int r = (pixel >>> 24) & 0xFF;
		int g = (pixel >>> 16) & 0xFF;
		int b = (pixel >>> 8) & 0xFF;
		int a = pixel & 0xFF;
		return String.format("%02X%02X%02X%02X", r, g, b, a);
	}
}
