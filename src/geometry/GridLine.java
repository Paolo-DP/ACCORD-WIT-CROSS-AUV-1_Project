/**
 * Geometry package
 */
package geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * Line segment on a grid. Consists of point coordinates of type double.
 * @author Ocampo
 */
public class GridLine extends Line2D.Double {

	/**
	 * Constructs a grid line.
	 * @param x1 x-coordinate of point 1
	 * @param y1 y-coordinate of point 1
	 * @param x2 x-coordinate of point 2
	 * @param y2 y-coordinate of point 2
	 */
	public GridLine(double x1, double y1, double x2, double y2) {
		super(x1, y1, x2, y2);
	}

	/**
	 * Construct a grid line from two points
	 * @param p1 Point 1
	 * @param p2 Point 2
	 */
	public GridLine(Point2D p1, Point2D p2) {
		super(p1, p2);
	}
	
	/**
	 * Returns the slope of the line.
	 * @return Slope of the line.
	 */
	public double getSlope() {
		return (y2 - y1) / (x2 - x1);
	}
	
	/**
	 * CHecks if the line has an undefined slope. This is a convenience method to check 
	 * if the line has an undefined slope to avoid exceptions that might be thrown by 
	 * dividing by zero.
	 * @return True if the line has an undefined slope.
	 */
	public boolean isUndefinedSlope() {
		return x2 == x1;
	}
	
	/**
	 * Checks if the line is a point. That is, it checks if the points are coincident. 
	 * It returns false when at least one of the points is null.
	 * @return True if the endpoints of the line are coincident.
	 */
	public boolean isPoint() {
		if (getP1() == null || getP2() == null) return false;
		return getP1().equals(getP2());
	}
	
}
