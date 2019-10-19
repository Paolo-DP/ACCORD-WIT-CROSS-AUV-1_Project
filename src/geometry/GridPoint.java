/**
 * Geometry package
 */
package geometry;

import java.awt.geom.Point2D;

/**
 * A point on the grid. It has double coordinates.
 * @author Ocampo
 */
public class GridPoint extends Point2D.Double {

	/**
	 * Constructs a point at (0,0).
	 */
	public GridPoint() {
	}

	/**
	 * Constructs a point at (x,y).
	 * @param x x-coordinate of the point.
	 * @param y y-coordinate of the point;
	 */
	public GridPoint(double x, double y) {
		super(x, y);
	}
	
	/**
	 * Translates this point, at location (x,y), by dx along the x axis and dy 
	 * along the y axis so that it now represents the point (x+dx,y+dy).
	 * @param dx Movement along the x-axis.
	 * @param dy Movement along the y-axis.
	 */
	public void translate(double dx, double dy) {
		x += dx;
		y += dy;
	}
	
	/**
	 * Rotates the coordinate plane by a specified angle and returns the new 
	 * coordinates of the point with respect to the unrotated plane.
	 * @param angle Angle to rotate the plane, in radians.
	 */
	public void rotate(double angle) {
		x = x * Math.cos(angle) - y * Math.sin(angle);
		y = x * Math.sin(angle) + y * Math.cos(angle);
	}

	/**
	 * Returns a string representation of the point, in the form (x,y).
	 * @return String representation of the point, in the form (x,y).
	 */
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
	
	
}
