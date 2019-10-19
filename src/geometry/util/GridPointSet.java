/**
 * Geometry utility package.
 */
package geometry.util;

import geometry.GridLine;
import geometry.GridPoint;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Set of grid points.
 * @author Ocampo
 */
public class GridPointSet extends LinkedHashSet<GridPoint> {

	/**
	 * Constructs a grid point set.
	 * @param initialCapacity Initial capacity.
	 * @param loadFactor Load factor.
	 */
	public GridPointSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Constructs a grid point set.
	 * @param initialCapacity Initial capacity.
	 */
	public GridPointSet(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs an empty grid point set.
	 */
	public GridPointSet() {
	}

	/**
	 * Constructs a grid point set from a collection.
	 * @param c Collection
	 */
	public GridPointSet(Collection<? extends GridPoint> c) {
		super(c);
	}
	
	/**
	 * Constructs a grid point set from a collection from a line segment. The 
	 * line segment will be sampled into points spaced by the interval with 
	 * respect to the starting point. The endpoints of the grid line are 
	 * included.
	 * @param line Line segment.
	 * @param interval Sampling interval.
	 */
	public GridPointSet(GridLine line, double interval) {
		addFromLine(line, interval);
	}
	
	private void addFromLine(GridLine line, double interval) {
		if (line.x1 == line.x2) {
			// THe line has an undefined slope. Intervals divide unto the y.
			double yStart = Math.min(line.y1, line.y2);
			double yEnd = Math.max(line.y1, line.y2);
			for (double y = yStart; true; y = Math.min(y + interval, yEnd)) {
				add(new GridPoint(line.x1, y));
				if (y >= yEnd) break;
			}
		} else {
			// Slope is not undefined
			double xStart = Math.min(line.x1, line.x2);
			double xEnd = Math.max(line.x1, line.x2);
			double m = line.getSlope();
			double xInterval = interval / Math.sqrt(Math.pow(m, 2) + 1);
			for (double x = xStart; true; x = Math.min(x + xInterval, xEnd)) {
				double y = line.y1 + m * (x - line.x1);
				add(new GridPoint(x, y));
				if (x >= xEnd) break;
			}
		}
	}
	
}
