package bms.player.beatoraja.skin.lr2.commands;

/**
 * Standard destination command.
 */
public class StandardDestination {
	@CSVField(0)
	public int index;

	@CSVField(1)
	public int time;

	@CSVField(start = 2)
	public Region region;

	@CSVField(3)
	public int acc;

	@CSVField(start = 4)
	public Color color;

	@CSVField(5)
	public int blend;

	@CSVField(6)
	public int filter;

	@CSVField(7)
	public int angle;

	@CSVField(8)
	public int center;

	@CSVField(9)
	public int loop;

	@CSVField(10)
	public int timer;

	@CSVField(start = 11)
	public Options options = new Options();

	// Shortcuts for nested objects, don't edit!

	public int x() { return region.x; }
	public int y() { return region.y; }
	public int w() { return region.w; }
	public int h() { return region.h; }
	public int a() { return color.a; }
	public int r() { return color.r; }
	public int g() { return color.g; }
	public int b() { return color.b; }
	public int op1() { return options.op1; }
	public int op2() { return options.op2; }
	public int op3() { return options.op3; }
	public int op4() { return options.op4; }
}
