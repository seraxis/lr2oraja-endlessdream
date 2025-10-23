package bms.player.beatoraja;

/**
 * Customizable resolution, because original Resolution enum cannot be instantiated at runtime
 *
 * @see Resolution
 */
public class CustomizableResolution {
	public final int width;
	public final int height;

	public CustomizableResolution(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
