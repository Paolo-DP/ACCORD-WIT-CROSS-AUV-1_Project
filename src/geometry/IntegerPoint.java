/**
 * Geometry package
 */
package geometry;

import java.awt.Point;

/**
 * Point with integer coordinates.
 * @author Ocampo
 */
public class IntegerPoint extends Point {

	/**
     * Constructs and initializes a point at the origin
     * (0,&nbsp;0) of the coordinate space.
     */
	public IntegerPoint() {
	}

	/**
     * Constructs and initializes a point with the same location as
     * the specified <code>Point</code> object.
     * @param       p a point
     */
	public IntegerPoint(Point p) {
		super(p);
	}

	/**
     * Constructs and initializes a point at the specified
     * {@code (x,y)} location in the coordinate space.
     * @param x the X coordinate of the newly constructed <code>Point</code>
     * @param y the Y coordinate of the newly constructed <code>Point</code>
     */
	public IntegerPoint(int x, int y) {
		super(x, y);
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
