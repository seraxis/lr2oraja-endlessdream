package bms.player.beatoraja.skin.lr2.commands;

/**
 * Region represents a rectangle area, shared in many classes
 */
public class Region{
	public @CSVField(0) int x;
	public @CSVField(1) int y;
	public @CSVField(2) int w;
	public @CSVField(3) int h;

	public void flipNegativeLength() {
		if (w < 0) {
			x += w;
			w *= -1;
		}
		if (h < 0) {
			y += h;
			h *= -1;
		}
	}
}
