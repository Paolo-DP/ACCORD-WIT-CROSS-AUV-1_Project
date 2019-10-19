
package vehicle;

import geometry.*;

/**
 * A class representing boundaries of the vehicle. It is a rectangle enclosing the 
 * vehicle, determining its bounds.
 * @author Ocampo
 */
public class VehicleBounds {
	
	private final GridLine[] bounds;
	private final GridPoint[] corners;

	/**
	 * Constructs the boundaries of the vehicle.
	 * @param bounds Lines representing the bounds of the vehicle.
	 * @param corners Corners of the bounds of the vehicle.
	 */
	public VehicleBounds(GridLine[] bounds, GridPoint[] corners) {
		this.bounds = bounds;
		this.corners = corners;
	}

	/**
	 * Returns the boundaries of the vehicle as an array of lines.
	 * @return Boundaries of the vehicle.
	 */
	public GridLine[] getBounds() {
		return bounds;
	}

	/**
	 * Returns the corners of the vehicle boundaries as an array of points.
	 * @return Corners of the vehicle boundaries.
	 */
	public GridPoint[] getCorners() {
		return corners;
	}
	
}
